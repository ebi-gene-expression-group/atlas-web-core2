package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolrCloudHealthServiceTest {
    // Yes, we know that you shouldn’t mock classes you don’t own, but setting a mock server for this feels overkill
    @Mock
    private CloudSolrClient cloudSolrClientMock;

    private SolrCloudHealthService subject;

    @BeforeEach
    void setUp() {

        subject = new SolrCloudHealthService(cloudSolrClientMock);
    }

    @Test
    void solrCollection_shardHasAtLeastOneActiveReplica_collectionIsUp() throws SolrServerException, IOException {
        var collectionName = randomAlphanumeric(10);

        var shardWithOneReplicaActive = new LinkedHashMap<>(ImmutableMap.of(
                randomAlphanumeric(10),
                new LinkedHashMap<>(ImmutableMap.of(
                        "replicas",
                        new LinkedHashMap<>(ImmutableMap.of(
                                randomAlphanumeric(10),
                                new LinkedHashMap(ImmutableMap.of("state", "down")),
                                randomAlphanumeric(10),
                                new LinkedHashMap(ImmutableMap.of("state", "active"))))))));

        var collection = new SimpleOrderedMap<>();
        collection.add(collectionName, new LinkedHashMap<>(ImmutableMap.of("shards", shardWithOneReplicaActive)));

        var collections = new SimpleOrderedMap<>();
        collections.add("collections", collection);

        var response = new SimpleOrderedMap<>();
        response.add("cluster", collections);

        when(cloudSolrClientMock.request(any(CollectionAdminRequest.ClusterStatus.class)))
                .thenReturn(response);

        assertThat(subject.areCollectionsUp(ImmutableSet.of(collectionName), ImmutableSet.of()))
                .isTrue();
    }

    @Test
    void solrCollection_shardHasAllReplicasDown_collectionIsNotUp() throws SolrServerException, IOException {
        var collectionName = randomAlphanumeric(10);

        var shardWithNoActiveReplicas = new LinkedHashMap<>(ImmutableMap.of(
                randomAlphanumeric(10),
                new LinkedHashMap<>(ImmutableMap.of(
                        "replicas",
                        new LinkedHashMap<>(ImmutableMap.of(
                                randomAlphanumeric(10),
                                new LinkedHashMap(ImmutableMap.of("state", "down")),
                                randomAlphanumeric(10),
                                new LinkedHashMap(ImmutableMap.of("state", "down"))))))));

        var collection = new SimpleOrderedMap<>();
        collection.add(collectionName, new LinkedHashMap<>(ImmutableMap.of("shards", shardWithNoActiveReplicas)));

        var collections = new SimpleOrderedMap<>();
        collections.add("collections", collection);

        var response = new SimpleOrderedMap<>();
        response.add("cluster", collections);

        when(cloudSolrClientMock.request(any(CollectionAdminRequest.ClusterStatus.class)))
                .thenReturn(response);

        assertThat(subject.areCollectionsUp(ImmutableSet.of(collectionName), ImmutableSet.of()))
                .isFalse();
    }
}