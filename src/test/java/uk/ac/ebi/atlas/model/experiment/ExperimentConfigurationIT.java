package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExperimentConfigurationIT {
    @Inject
    private DataSource dataSource;

    @Inject
    private JdbcUtils jdbcUtils;

    @Inject
    private Path experimentsDirPath;

    @Inject
    private Path experimentDesignDirPath;

    private ExperimentConfiguration subject;

    @BeforeAll
    void populateDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("fixtures/gxa/experiment.sql"));
        populator.execute(dataSource);
    }

    @AfterAll
    void cleanDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("fixtures/gxa/experiment-delete.sql"));
        populator.execute(dataSource);
    }

    @ParameterizedTest
    @MethodSource("microArrayExperimentAccessionProvider")
    void testGetArrayDesignNames(String experimentAccession) {
        subject =
                new ConfigurationTrader(new DataFileHub(experimentsDirPath, experimentDesignDirPath))
                        .getExperimentConfiguration(experimentAccession);

        assertThat(subject.getArrayDesignAccessions())
                .isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("microArrayExperimentAccessionProvider")
    void testGetContrasts(String experimentAccession) {
//        subject =
//                new ConfigurationTrader(new DataFileHub(dataFilesPath.resolve("gxa")))
//                        .getExperimentConfiguration(experimentAccession);
//
//        assertThat(subject.getContrasts())
//                .isNotEmpty()
//                .allSatisfy(contrast -> assertThat(contrast.getReferenceAssayGroup().getAssayIds()).isNotEmpty())
//                .allSatisfy(contrast -> assertThat(contrast.getTestAssayGroup().getAssayIds()).isNotEmpty())
//                .extracting("id", "displayName")
//                .doesNotContainNull();
    }

    private Stream<String> microArrayExperimentAccessionProvider() {
        ArrayList<ExperimentType> microarrayExperimentTypes =
                Lists.newArrayList(
                        MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL,
                        MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL,
                        MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL);
        Collections.shuffle(microarrayExperimentTypes);

        return Stream.of(jdbcUtils.fetchRandomExperimentAccession(microarrayExperimentTypes.get(0)));
    }
}
