package uk.ac.ebi.atlas.solr.analytics;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum AnalyticsPropertyName {

    METADATA("metadata", "Metadata");

    private static final ImmutableMap<String, AnalyticsPropertyName> PROPERTIES_BY_NAME =
            ImmutableMap.copyOf(Arrays.stream(values()).collect(Collectors.toMap(v -> v.name, v -> v)));

    public final String name;
    public final String label;

    AnalyticsPropertyName(String name, String label) {
        this.name = name.toLowerCase();
        this.label = label;
    }
}
