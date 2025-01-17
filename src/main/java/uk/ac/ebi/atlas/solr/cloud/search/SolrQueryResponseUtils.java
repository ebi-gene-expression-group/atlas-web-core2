package uk.ac.ebi.atlas.solr.cloud.search;

import com.google.common.collect.ImmutableList;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class SolrQueryResponseUtils {

    protected SolrQueryResponseUtils() {
        throw new UnsupportedOperationException();
    }

    public static List<String> getValuesForFacetField(SimpleOrderedMap map, String facetField) {
        List<SimpleOrderedMap> results = extractSimpleOrderedMaps(map.findRecursive(facetField, "buckets"));

        return results
                .stream()
                .map(x -> x.get("val").toString())
                .collect(Collectors.toList());
    }

    public static List<SimpleOrderedMap> extractSimpleOrderedMaps(Object o) {
        if (o instanceof ArrayList) {
            return ((ArrayList<?>) o)
                    .stream()
                    .filter(element -> element instanceof SimpleOrderedMap)
                    .map(element -> (SimpleOrderedMap) element)
                    .collect(toImmutableList());
        }
        else {
            return ImmutableList.of();
        }
    }
}
