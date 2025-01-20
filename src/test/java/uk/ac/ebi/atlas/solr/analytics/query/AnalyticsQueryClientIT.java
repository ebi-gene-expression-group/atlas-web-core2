package uk.ac.ebi.atlas.solr.analytics.query;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomEnsemblGeneId;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomGeneOntologyAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class AnalyticsQueryClientIT {
    private final static ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Autowired
    private AnalyticsQueryClient subject;

    @Test
    void baselineFacetsOnlyReturnsBaselineExperiments() {
        var queryResponse = subject.queryBuilder()
                .baselineFacets()
                .queryIdentifierOrConditionsSearch(SemanticQuery.create("lung"))
                .fetch();

        var experimentTypes =
                JsonPath.using(Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS))
                        .parse(queryResponse)
                        .<List<String>>read("$.facets.experimentType.buckets[*].val");

        // If we want to go to an extra level of correctness we can get the experiment type from the DB, but if the
        // type isn’t right in Solr our problems are going to be bigger than this test
        assertThat(experimentTypes)
                .isNotEmpty()
                .allMatch(type -> ExperimentType.get(type).isBaseline());
    }

    @Test
    void returnsAllResultsFound() {
        var queryResponse =
                subject.queryBuilder()
                        .baselineFacets()
                        .queryIdentifierSearch(SemanticQuery.create(SemanticQueryTerm.create("MUC1", "symbol")))
                        .ofSpecies("homo sapiens")
                        .fetch();

        var numberOfResults =
                JsonPath.using(Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS))
                        .parse(queryResponse)
                        .<Integer>read("response.numFound");
        var results =
                JsonPath.using(Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS))
                        .parse(queryResponse)
                        .<List<?>>read("$..assayGroupId.buckets[*]");

        assertThat(results)
                .isNotEmpty()
                .hasSize(numberOfResults);
    }

    @Nested
    @ContextConfiguration(classes = TestConfig.class)
    class BuilderIT {
        @Value("classpath:/solr-queries/baseline.heatmap.pivot.query.json")
        private Resource baselineFacetsQueryJson;

        @Value("classpath:/solr-queries/differential.facets.query.json")
        private Resource differentialFacetsQueryJson;

        @Value("classpath:/solr-queries/experimentType.query.json")
        private Resource experimentTypesQueryJson;

        @Value("classpath:/solr-queries/bioentityIdentifier.query.json")
        private Resource bioentityIdentifiersQueryJson;

        private AnalyticsQueryClient subject;

        @BeforeEach
        void setUp() {
            subject = new TestableAnalyticsQueryClient();
        }

        @Test
        void queryWithCategory() {
            var goAccession = generateRandomGeneOntologyAccession();
            var experimentAccession = generateRandomExperimentAccession();

            var queryMade = subject.queryBuilder()
                    .bioentityIdentifierFacets(-1)
                    .queryIdentifierSearch(
                            SemanticQuery.create(SemanticQueryTerm.create(goAccession, "go")))
                    .inExperiment(experimentAccession)
                    .fetch();

            assertThat(queryMade).contains("keyword_go");
            assertThat(queryMade).contains("\"" + goAccession + "\"");
            assertThat(queryMade).contains(experimentAccession);
        }

        @Test
        void queryWithNoCategoryButObviouslyAnEnsemblIdDoesABioentityIdentifierQuery() {
            var ensemblId = generateRandomEnsemblGeneId();

            var queryMade = subject.queryBuilder()
                    .bioentityIdentifierFacets(-1)
                    .queryIdentifierSearch(SemanticQuery.create(SemanticQueryTerm.create(ensemblId)))
                    .fetch();

            assertThat(queryMade).contains(ensemblId);
            assertThat(queryMade).doesNotContain("keyword_");
            assertThat(queryMade).contains("bioentity_identifier");
        }

        @Test
        void speciesComeInQuoted() {
            var speciesName = generateRandomSpecies().getName();
            var experimentAccession = generateRandomExperimentAccession();

            var queryMade = subject.queryBuilder()
                    .bioentityIdentifierFacets(-1)
                    .ofSpecies(speciesName)
                    .inExperiment(experimentAccession)
                    .fetch();

            assertThat(queryMade).contains("\"" + speciesName + "\"");
        }

        @Test
        void weGuessThatZincFingerCanNotBeAKeyword() {
            var experimentAccession = generateRandomExperimentAccession();
            var geneFeature = randomAlphabetic(5, 20);

            var queryMade = subject.queryBuilder()
                    .bioentityIdentifierFacets(-1)
                    .queryIdentifierSearch(SemanticQuery.create(geneFeature))
                    .inExperiment(experimentAccession)
                    .fetch();

            assertThat(queryMade).doesNotContain("keyword_");
            assertThat(queryMade).contains("identifier_search");
            assertThat(queryMade).contains(geneFeature);
            assertThat(queryMade).contains(experimentAccession);
        }

        @Test
        void defaultQueryIsTheSolrDefault() {
            var queryMade = subject.queryBuilder().fetch();
            assertThat(queryMade).contains("q=*:*");
        }

        @Test
        void omitEmptyConditionQuery() {
            var experimentAccession = generateRandomExperimentAccession();
            var geneFeature = randomAlphabetic(5, 20);

            var queryMade = subject.queryBuilder()
                    .bioentityIdentifierFacets(-1)
                    .queryIdentifierSearch(SemanticQuery.create(geneFeature))
                    .queryConditionsSearch(SemanticQuery.create())
                    .inExperiment(experimentAccession)
                    .fetch();

            assertThat(queryMade).doesNotContain("keyword_");
            assertThat(queryMade).contains("identifier_search");
            assertThat(queryMade).contains(geneFeature);
            assertThat(queryMade).contains(experimentAccession);

            assertThat(queryMade).doesNotContain("conditionsSearch");
        }

        @Test
        void bothConditionQueryAndIdentifierSearchMakeItIntoTheQueryString() {
            var geneFeature = randomAlphabetic(5, 20);
            var organismPart = randomAlphabetic(5, 20);

            var queryMade = subject.queryBuilder()
                    .bioentityIdentifierFacets(-1)
                    .queryIdentifierSearch(SemanticQuery.create(geneFeature))
                    .queryConditionsSearch(SemanticQuery.create(organismPart))
                    .fetch();

            assertThat(queryMade).doesNotContain("keyword_");
            assertThat(queryMade).contains("identifier_search");
            assertThat(queryMade).contains(geneFeature);

            assertThat(queryMade).contains("conditions_search");
            assertThat(queryMade).contains(organismPart);
        }

        @Test
        void queryConditionSearchOrIdentifierSearchIncludesTheQueryStringTwiceForQueriesWithNoCategory() {
            var multiWordSearchTerm = randomAlphabetic(5, 20) + " " + randomAlphabetic(5, 20);

            var queryMade = subject.queryBuilder()
                    .bioentityIdentifierFacets(-1)
                    .queryIdentifierOrConditionsSearch(SemanticQuery.create(multiWordSearchTerm))
                    .fetch();

            assertThat(queryMade).doesNotContain("keyword_"); //two words so this is not a keyword
            assertThat(queryMade).contains("identifier_search");
            assertThat(queryMade).contains("conditions_search");
            assertThat(queryMade.split(multiWordSearchTerm).length).isGreaterThan(2);
        }

        class TestableAnalyticsQueryClient extends AnalyticsQueryClient {
            public TestableAnalyticsQueryClient() {
                super(new RestTemplate(),
                  new HttpHeaders(),
                  new String[]{""},
                  baselineFacetsQueryJson,
                  differentialFacetsQueryJson,
                  experimentTypesQueryJson,
                  bioentityIdentifiersQueryJson);
            }

            @Override
            protected String fetchResponseAsString(String url, SolrQuery q) {
                return URLDecoder.decode(q.toString(), StandardCharsets.UTF_8);
            }

            // This ensures that the response corresponds to the first query
            @Override
            protected boolean responseNonEmpty(String jsonFromSolr) {
                return true;
            }
        }
    }
}

