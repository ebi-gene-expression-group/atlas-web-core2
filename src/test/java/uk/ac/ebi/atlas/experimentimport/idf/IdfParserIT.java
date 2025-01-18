package uk.ac.ebi.atlas.experimentimport.idf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.resource.DataFileHub;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.sql.DataSource;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class IdfParserIT {
    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = TestConfig.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Bulk {
        @Autowired
        private DataSource dataSource;

        @Autowired
        private Path experimentsDirPath;

        @Autowired
        private Path experimentDesignDirPath;

        @Autowired
        private JdbcUtils jdbcUtils;

        @BeforeAll
        void populateDatabaseTables() {
            ResourceDatabasePopulator bulkPopulator = new ResourceDatabasePopulator();
            bulkPopulator.addScripts(new ClassPathResource("fixtures/gxa/experiment.sql"));
            bulkPopulator.execute(dataSource);
        }

        @AfterAll
        void cleanDatabaseTables() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScripts(new ClassPathResource("fixtures/gxa/experiment-delete.sql"));
            populator.execute(dataSource);
        }

        @ParameterizedTest
        @MethodSource("bulkExperimentsProvider")
        void testParserForExpressionAtlas(String experimentAccession) {
            IdfParser idfParser = new IdfParser(new DataFileHub(experimentsDirPath, experimentDesignDirPath));

            IdfParserOutput result = idfParser.parse(experimentAccession);

            assertThat(result.getExpectedClusters()).isEqualTo(0);
            assertThat(result.getTitle()).isNotEmpty();
            assertThat(result.getExperimentDescription()).isNotEmpty();
            assertThat(result.getPublications()).isNotNull();
        }

        private Iterable<String> bulkExperimentsProvider() {
            return jdbcUtils.fetchAllExperimentAccessions();
        }
    }
}
