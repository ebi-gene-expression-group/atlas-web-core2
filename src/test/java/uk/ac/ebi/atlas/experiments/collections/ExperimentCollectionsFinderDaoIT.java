package uk.ac.ebi.atlas.experiments.collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.jdbc.JdbcTestUtils.deleteFromTables;
import static org.springframework.test.jdbc.JdbcTestUtils.dropTables;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExperimentCollectionsFinderDaoIT {
    @Inject
    private DataSource dataSource;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private JdbcUtils jdbcUtils;

    @Inject
    private ExperimentCollectionsFinderDao subject;

    @BeforeAll
    void populateDatabaseTables() {
        var populator = new ResourceDatabasePopulator();
        populator.addScripts(
                new ClassPathResource("fixtures/scxa/experiment.sql"),
                new ClassPathResource("fixtures/scxa/collections.sql"),
                new ClassPathResource("fixtures/scxa/experiment2collection.sql"));
        populator.execute(dataSource);
    }

    @AfterAll
    void cleanDatabaseTables() {
        var populator = new ResourceDatabasePopulator();
        populator.addScripts(
                new ClassPathResource("fixtures/scxa/experiment-delete.sql"),
                new ClassPathResource("fixtures/scxa/collections-delete.sql"));
        populator.execute(dataSource);
    }

    @ParameterizedTest
    @MethodSource("experimentAccessionProvider")
    @Order(1)
    void collectionIdsAreRetrievedForValidExperimentAccession(String experimentAccession) {
        assertThat(subject.findExperimentCollectionIds(experimentAccession))
                .isNotEmpty();
    }

    @Test
    @Order(2)
    void returnEmptyListIfExperimentHasNoCollections() {
        assertThat(subject.findExperimentCollectionIds(generateRandomExperimentAccession()))
                .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("experimentAccessionProvider")
    @Order(3)
    void returnsAnEmptyListIfTableCannotBeFound(String experimentAccession) {
        assertThat(subject.findExperimentCollectionIds(experimentAccession))
                .isNotEmpty();
        deleteFromTables(jdbcTemplate, "experiment2collection");
        assertThat(subject.findExperimentCollectionIds(experimentAccession))
                .isEmpty();
    }

    private Stream<String> experimentAccessionProvider() {
        return Stream.of(jdbcUtils.fetchRandomExperimentAccessionWithCollections());
    }
}
