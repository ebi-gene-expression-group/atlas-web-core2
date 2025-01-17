package uk.ac.ebi.atlas.experimentimport.sdrf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.resource.DataFileHub;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SdrfParserIT {
    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = TestConfig.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Bulk {
        @Inject
        private DataSource dataSource;

        @Inject
        private Path dataFilesPath;

        @Inject
        private JdbcUtils jdbcUtils;

        @BeforeAll
        void populateDatabaseTables() {
            var populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/gxa/experiment.sql"));
            populator.execute(dataSource);
        }

        @AfterAll
        void cleanDatabaseTables() {
            var populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/gxa/experiment-delete.sql"));
            populator.execute(dataSource);
        }

        @ParameterizedTest
        @MethodSource("expressionAtlasExperimentsProvider")
        void testParserForExpressionAtlas(String experimentAccession) {
            var sdrfParser = new SdrfParser(new DataFileHub(dataFilesPath.resolve("gxa"), dataFilesPath.resolve("expdesign")));
            assertThat(sdrfParser.parseSingleCellTechnologyType(experimentAccession)).isEmpty();
        }

        private Iterable<String> expressionAtlasExperimentsProvider() {
            return jdbcUtils.fetchAllExperimentAccessions();
        }
    }
}
