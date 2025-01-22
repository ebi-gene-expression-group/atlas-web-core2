package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.source;

// The correct way of doing these tests is by creating an EmbeddedSolrServer with a set of documents to have an
// analytics collection fixture but, alas!, streaming expressions only work in SolrCloud so we use the instance in lime

import org.apache.solr.client.solrj.io.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.ASSAY_GROUP_ID;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.EXPERIMENT_ACCESSION;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.EXPRESSION_LEVEL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class FacetStreamBuilderIT {
    private static final String E_MTAB_5214 = "E-MTAB-5214";

    @Autowired
    private SolrCloudCollectionProxyFactory collectionProxyFactory;

    private BulkAnalyticsCollectionProxy bulkAnalyticsCollectionProxy;

    @BeforeEach
    void setUp() {
        bulkAnalyticsCollectionProxy = collectionProxyFactory.create(BulkAnalyticsCollectionProxy.class);
    }

    // TODO Maybe check correctness against AnalyticsCollectionProxy by querying the collection

    @ParameterizedTest
    @MethodSource("solrQueryBuildersProvider")
    @DisplayName("Narrower query results is a subset of the broader query results")
    void filtersAndQueriesRestrictResults(SolrQueryBuilder<BulkAnalyticsCollectionProxy> broadSolrQueryBuilder,
                                          SolrQueryBuilder<BulkAnalyticsCollectionProxy> narrowSolrQueryBuilder) {
        try (var broadQueryStreamer =
                     TupleStreamer.of(
                             new FacetStreamBuilder<>(bulkAnalyticsCollectionProxy, BIOENTITY_IDENTIFIER)
                                     .withQuery(broadSolrQueryBuilder.build())
                                     .sortByCountsAscending()
                                     .build());
             var narrowQueryStreamer =
                     TupleStreamer.of(
                             new FacetStreamBuilder<>(bulkAnalyticsCollectionProxy, BIOENTITY_IDENTIFIER)
                                     .withQuery(narrowSolrQueryBuilder.build())
                                     .sortByCountsAscending()
                                     .build())) {

            var broadQueryResults = broadQueryStreamer.get().map(Tuple::getFields).collect(toList());
            var narrowQueryResults = narrowQueryStreamer.get().map(Tuple::getFields).collect(toList());

            assertThat(broadQueryResults.size()).isGreaterThan(narrowQueryResults.size());

            assertThat(broadQueryResults).extracting(BIOENTITY_IDENTIFIER.name())
                    .contains(
                            narrowQueryResults.stream()
                                    .map(tupleMap -> tupleMap.get(BIOENTITY_IDENTIFIER.name())).toArray());
        }
    }

    @ParameterizedTest
    @MethodSource("solrQueryBuildersProvider")
    void queryAndQueryBuilderAreEquivalent(SolrQueryBuilder<BulkAnalyticsCollectionProxy> solrQueryBuilder) {
        try (var filteredStreamer1 =
                     TupleStreamer.of(
                             new FacetStreamBuilder<>(bulkAnalyticsCollectionProxy, BIOENTITY_IDENTIFIER)
                                     .withQuery(solrQueryBuilder.build())
                                     .sortByCountsAscending()
                                     .build());
             var filteredStreamer2 =
                     TupleStreamer.of(
                             new FacetStreamBuilder<>(bulkAnalyticsCollectionProxy, BIOENTITY_IDENTIFIER)
                                     .withQuery(solrQueryBuilder.build())
                                     .sortByCountsAscending()
                                     .build())) {
            assertThat(filteredStreamer1.get().map(Tuple::getFields))
                    .extracting(BIOENTITY_IDENTIFIER.name())
                    .containsExactlyInAnyOrder(
                            filteredStreamer2.get()
                                    .map(tuple -> tuple.getString(BIOENTITY_IDENTIFIER.name()))
                                    .toArray());
        }
    }

    @Test
    void includeAverage() {
        // We need to specify a baseline experiment, otherwise avg(abs(expression_level)) will be 0
        var solrQueryBuilder = new SolrQueryBuilder<BulkAnalyticsCollectionProxy>();
        solrQueryBuilder.addFilterFieldByTerm(EXPERIMENT_ACCESSION, E_MTAB_5214);
        try (var filteredByExperimentStreamer =
                     TupleStreamer.of(new FacetStreamBuilder<>(bulkAnalyticsCollectionProxy, BIOENTITY_IDENTIFIER)
                            .withQuery(solrQueryBuilder.build())
                            .withAbsoluteAverageOf(EXPRESSION_LEVEL)
                            .sortByCountsAscending()
                            .build())) {
            filteredByExperimentStreamer.get().forEach(
                    tuple -> assertThat(tuple.getDouble("avg(abs(expression_level))")).isNotNull().isGreaterThan(0));
        }
    }

    @Test
    void sortByCountsAscending() {
        try (var streamer =
                     TupleStreamer.of(new FacetStreamBuilder<>(bulkAnalyticsCollectionProxy, BIOENTITY_IDENTIFIER)
                            .sortByCountsAscending()
                            .build())) {

            var results = streamer.get().collect(toList());

            for (var i = 0; i < results.size() - 1; i++) {
                assertThat(results.get(i).getLong("count(*)"))
                        .isLessThanOrEqualTo(results.get(i + 1).getLong("count(*)"));
            }

        }
    }

    @Test
    void sortByCountsDescending() {
        try (var streamer =
                     TupleStreamer.of(new FacetStreamBuilder<>(bulkAnalyticsCollectionProxy, BIOENTITY_IDENTIFIER)
                             .sortByCountsDescending()
                             .build())) {

            var results = streamer.get().collect(toList());

            for (var i = 0; i < results.size() - 1; i++) {
                assertThat(results.get(i).getLong("count(*)"))
                        .isGreaterThanOrEqualTo(results.get(i + 1).getLong("count(*)"));
            }

        }
    }

    @Test
    void sortByAverage() {
        try (var streamer =
                     TupleStreamer.of(new FacetStreamBuilder<>(bulkAnalyticsCollectionProxy, BIOENTITY_IDENTIFIER)
                            .sortByAbsoluteAverageDescending(EXPRESSION_LEVEL)
                            .build())) {

            var results = streamer.get().collect(toList());

            for (var i = 0; i < results.size() - 1; i++) {
                assertThat(results.get(i).getDouble("avg(abs(" + EXPRESSION_LEVEL.name() + "))"))
                        .isGreaterThanOrEqualTo(
                                results.get(i + 1).getDouble(
                                        "avg(abs(" + EXPRESSION_LEVEL.name() + "))"));
            }

        }
    }

    private static Stream<Arguments> solrQueryBuildersProvider() {
        var assayGroups = IntStream.range(1, 16).boxed().map(i -> "g" + i).collect(toSet());

        var hugeSolrQueryBuilder =
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                        .addFilterFieldByTerm(ASSAY_GROUP_ID, assayGroups)
                        .addFilterFieldByRangeMin(EXPRESSION_LEVEL, 10.0);

        var bigSolrQueryBuilder =
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                        .addFilterFieldByTerm(ASSAY_GROUP_ID, assayGroups)
                        .addFilterFieldByRangeMin(EXPRESSION_LEVEL, 10.0)
                        .addFilterFieldByRangeMax(EXPRESSION_LEVEL, 10000.0);

        var smallSolrQueryBuilder =
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                        .addFilterFieldByTerm(ASSAY_GROUP_ID, assayGroups)
                        .addFilterFieldByRangeMin(EXPRESSION_LEVEL, 10.0)
                        .addFilterFieldByRangeMinMax(EXPRESSION_LEVEL, 300.0, 600.0);

        var tinySolrQueryBuilder =
                new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                        .addFilterFieldByTerm(ASSAY_GROUP_ID, assayGroups)
                        .addFilterFieldByRangeMin(EXPRESSION_LEVEL, 10.0)
                        .addFilterFieldByRangeMinMax(EXPRESSION_LEVEL, 300.0, 600.0)
                        .addQueryFieldByTerm(EXPERIMENT_ACCESSION, E_MTAB_5214);

        return Stream.of(
                Arguments.of(hugeSolrQueryBuilder, bigSolrQueryBuilder),
                Arguments.of(bigSolrQueryBuilder, smallSolrQueryBuilder),
                Arguments.of(smallSolrQueryBuilder, tinySolrQueryBuilder));
    }
}
