package uk.ac.ebi.atlas.resource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.ExpressionUnit;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.resource.AtlasResource;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional(transactionManager = "txManager")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataFileHubIT {
    @Inject
    private DataSource dataSource;

    @Inject
    private JdbcUtils jdbcUtils;

    @Inject
    private DataFileHub subject;

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

    @Test
    void testGetExperimentFiles() {
        var experimentAccession = jdbcUtils.fetchRandomExperimentAccession();

        assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).analysisMethods);
        assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).condensedSdrf);
        assertAtlasResourceExists(subject.getExperimentFiles(experimentAccession).experimentDesign);
    }

    @Test
    void testGetBaselineFiles() {
        var experimentAccession = jdbcUtils.fetchRandomExperimentAccession(ExperimentType.RNASEQ_MRNA_BASELINE);

        assertAtlasResourceExists(
                subject.getRnaSeqBaselineExperimentFiles(experimentAccession)
                        .dataFile(ExpressionUnit.Absolute.Rna.TPM));
        assertAtlasResourceExists(
                subject.getRnaSeqBaselineExperimentFiles(experimentAccession)
                        .dataFile(ExpressionUnit.Absolute.Rna.FPKM));
    }

    @Test
    void testGetProteomicsBaselineFiles() {
        var experimentAccession = jdbcUtils.fetchRandomExperimentAccession(ExperimentType.PROTEOMICS_BASELINE);

        assertAtlasResourceExists(subject.getProteomicsBaselineExperimentFiles(experimentAccession).main);
    }

    @Test
    void testGetDifferentialExperimentFiles() {
        var experimentAccession = jdbcUtils.fetchRandomExperimentAccession(ExperimentType.RNASEQ_MRNA_DIFFERENTIAL);

        assertAtlasResourceExists(subject.getBulkDifferentialExperimentFiles(experimentAccession).analytics);
        assertAtlasResourceExists(subject.getBulkDifferentialExperimentFiles(experimentAccession).rawCounts);
    }

    private static void assertAtlasResourceExists(AtlasResource<?> resource) {
        assertThat(resource.exists()).isTrue();
    }
}
