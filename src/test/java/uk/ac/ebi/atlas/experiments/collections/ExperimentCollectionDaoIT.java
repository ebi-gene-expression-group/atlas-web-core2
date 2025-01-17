package uk.ac.ebi.atlas.experiments.collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class ExperimentCollectionDaoIT {
    @Inject
    private DataSource dataSource;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private JdbcUtils jdbcUtils;

    @Inject
    private ExperimentCollectionDao subject;

    @BeforeAll
    void populateDatabaseTables() {
        var populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("fixtures/scxa/collections.sql"));
        populator.execute(dataSource);
    }

    @Test
    @Order(1)
    void ifNoCollectionCanBeFoundReturnEmpty() {
        assertThat(subject.findCollection("foo")).isEmpty();
    }

    // TODO Remove this test once collections table is in the shared schema, but remember to clean with @AfterAll
    @ParameterizedTest
    @MethodSource("experimentCollectionProvider")
    @Order(2)
    void ifTableNotFoundReturnEmpty(String collectionId) {
        assertThat(subject.findCollection(collectionId)).isNotEmpty();
        deleteFromTables(jdbcTemplate, "collections");
        assertThat(subject.findCollection(collectionId)).isEmpty();
    }

    private Stream<String> experimentCollectionProvider() {
        return Stream.of(jdbcUtils.fetchRandomExperimentCollectionId());
    }
}