package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;

public class HealthChecker {
    private final HealthCheckService healthCheckService;
    private final ImmutableSet<String> collectionNames;
    private final ImmutableSet<String> collectionAliases;

    public HealthChecker(HealthCheckService healthCheckService,
                         Collection<String> collectionNames,
                         Collection<String> collectionAliases) {
        this.healthCheckService = healthCheckService;
        this.collectionNames = ImmutableSet.copyOf(collectionNames);
        this.collectionAliases = ImmutableSet.copyOf(collectionAliases);
    }

    protected ImmutableMap<String, String> getHealthStatus() {
        return ImmutableMap.of(
                "solr",
                healthCheckService.isSolrUp(collectionNames, collectionAliases) ? "UP" : "DOWN",
                "db",
                healthCheckService.isDatabaseUp() ? "UP" : "DOWN");
    }
}
