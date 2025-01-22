package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.MapUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toMap;

@Component
public class SolrCloudHealthService {
    private final CloudSolrClient cloudSolrClient;

    public SolrCloudHealthService(CloudSolrClient cloudSolrClient) {
        this.cloudSolrClient = cloudSolrClient;
    }

    public boolean areCollectionsUp(ImmutableCollection<String> collectionNames,
                                    ImmutableCollection<String> collectionAliases)
            throws IOException, SolrServerException {
        var response = cloudSolrClient.request(new CollectionAdminRequest.ClusterStatus());

        var allCollectionNames =
                ImmutableSet.<String>builder()
                        .addAll(collectionNames)
                        // Get real collection names for each alias
                        .addAll(
                                collectionAliases.stream()
                                        .map(alias ->
                                                getCollectionNameForAlias(response, alias)).collect(toImmutableSet()))
                        .build();

        // Check that all collections have no inactive shards
        return allCollectionNames
                .stream()
                .allMatch(collectionName -> getInactiveShards(response, collectionName).isEmpty());
    }

    // Retrieves the collection name associated with an alias, e.g. the scxa-analytics alias returns scxa-analytics-v2
    private String getCollectionNameForAlias(NamedList<Object> response, String alias) {
        var aliases = (LinkedHashMap) response.findRecursive("cluster", "aliases");
        var collectionName = aliases.get(alias);

        if (collectionName != null) {
            return collectionName.toString();
        } else {
            throw new RuntimeException("Alias " + alias + " does not exist or does not match any collection");
        }
    }

    // Returns a set of shards that are not "active" for a given collection; inactive shards are those for which there
    // is no active replica
    private ImmutableSet<String> getInactiveShards(NamedList<Object> response, String collectionName) {
        var collectionStatus = (LinkedHashMap) response.findRecursive("cluster", "collections", collectionName);

        if (MapUtils.isEmpty(collectionStatus)) {
            throw new RuntimeException("Collection " + collectionName + " does not exist");
        } else {
            var shards = (LinkedHashMap) collectionStatus.get("shards");

            // Return an immutable set of shard names that are not active
            return mapOfLinkedHashMap(shards).entrySet().stream()
                    .filter(entry -> !isShardActive(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(toImmutableSet());
        }
    }

    private static Map<String, LinkedHashMap> mapOfLinkedHashMap(LinkedHashMap<?, ?> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String && entry.getValue() instanceof LinkedHashMap)
                .collect(toMap(
                        entry -> (String) entry.getKey(),
                        entry -> (LinkedHashMap) entry.getValue()));
    }

    // Given a shard status return if there is an active replica
    private boolean isShardActive(LinkedHashMap shard) {
        var replicas = (LinkedHashMap) shard.get("replicas");

        return mapOfLinkedHashMap(replicas).values().stream()
                .anyMatch(node -> node.get("state").toString().equalsIgnoreCase("active"));
    }
}
