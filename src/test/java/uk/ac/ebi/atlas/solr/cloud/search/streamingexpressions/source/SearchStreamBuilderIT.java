package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.source;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.io.UncheckedIOException;

import static java.util.stream.Collectors.toList;
import static org.apache.solr.client.solrj.SolrQuery.ORDER.asc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_NAME;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.PROPERTY_VALUE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
// Some of the tests in this class are executed against the scxa-analytics collection, and have been moved to
// atlas-web-single-cell/app
class SearchStreamBuilderIT {
    @Autowired
    private SolrCloudCollectionProxyFactory collectionProxyFactory;

    private BioentitiesCollectionProxy bioentitiesCollectionProxy;

    @BeforeEach
    void setUp() {
        bioentitiesCollectionProxy = collectionProxyFactory.create(BioentitiesCollectionProxy.class);
    }

    @Test
    void testMinimalQuery() {
        var solrQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .setFieldList(ImmutableSet.of(BIOENTITY_IDENTIFIER, SPECIES, PROPERTY_NAME, PROPERTY_VALUE))
                        .sortBy(BIOENTITY_IDENTIFIER, asc);

        try (var tupleStreamer =
                     TupleStreamer.of(
                             new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build())) {
            assertThat(tupleStreamer.get().collect(toList())).isNotEmpty();
        }
    }

    @Test
    void testQuery() {
        var solrQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .addQueryFieldByTerm(SPECIES, "Mus_musculus")
                        .setFieldList(ImmutableSet.of(BIOENTITY_IDENTIFIER, SPECIES, PROPERTY_NAME, PROPERTY_VALUE))
                        .sortBy(BIOENTITY_IDENTIFIER, asc);

        try (var tupleStreamer =
                     TupleStreamer.of(
                             new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build())) {
            assertThat(tupleStreamer.get().collect(toList()))
                    .allSatisfy(
                            tuple -> assertThat(tuple.getString(BIOENTITY_IDENTIFIER.name())).startsWith("ENSMUSG"));
        }
    }

    @Test
    void noResults() {
        var solrQueryBuilder =
                new SolrQueryBuilder<BioentitiesCollectionProxy>()
                        .addQueryFieldByTerm(PROPERTY_VALUE, "Foobar")
                        .setFieldList(ImmutableSet.of(BIOENTITY_IDENTIFIER, SPECIES, PROPERTY_NAME, PROPERTY_VALUE))
                        .sortBy(BIOENTITY_IDENTIFIER, asc);

        try (var tupleStreamer =
                     TupleStreamer.of(
                             new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build())) {
            assertThat(tupleStreamer.get().collect(toList()))
                    .isEmpty();
        }
    }

    @Test
    void requiresSortFieldAndToBePresentInFieldList() {
        var solrQueryBuilder = new SolrQueryBuilder<BioentitiesCollectionProxy>();

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build());

        solrQueryBuilder.sortBy(BIOENTITY_IDENTIFIER, asc);
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build());

        solrQueryBuilder.setFieldList(BIOENTITY_IDENTIFIER);
        try (var tupleStreamer =
                     TupleStreamer.of(
                             new SearchStreamBuilder<>(bioentitiesCollectionProxy, solrQueryBuilder).build())) {
            assertThat(tupleStreamer.get().collect(toList()))
                    .isNotEmpty();
        }
    }
}
