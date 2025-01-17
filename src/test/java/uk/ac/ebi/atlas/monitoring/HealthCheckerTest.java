package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckerTest {
    private final static Random RNG = ThreadLocalRandom.current();
    private final static ImmutableSet<String> collectionNames = ImmutableSet.of("collectionName1");
    private final static ImmutableSet<String> collectionAliases = ImmutableSet.of("collectionAlias1");

    @Mock
    private HealthCheckService healthCheckServiceMock;

    private HealthChecker subject;

    @BeforeEach
    void setUp() {
        subject = new HealthChecker(healthCheckServiceMock, collectionNames, collectionAliases);
    }

    @Test
    void resultReflectsServicesStatus() {
        var isSolrUp = RNG.nextBoolean();
        when(healthCheckServiceMock.isSolrUp(any(), any())).thenReturn(isSolrUp);

        var isDbUp = RNG.nextBoolean();
        when(healthCheckServiceMock.isDatabaseUp()).thenReturn(isDbUp);

        assertThat(subject.getHealthStatus())
                .containsOnly(
                        createMapEntry("solr", isSolrUp ? "UP" : "DOWN"),
                        createMapEntry("db", isDbUp ? "UP" : "DOWN"));
    }

    private static <K, V> Map.Entry<K, V> createMapEntry(K key, V value) {
        return ImmutableMap.of(key, value).entrySet().iterator().next();
    }
}