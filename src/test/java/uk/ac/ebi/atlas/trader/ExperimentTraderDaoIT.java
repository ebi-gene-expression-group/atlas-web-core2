package uk.ac.ebi.atlas.trader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.PROTEOMICS_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.SINGLE_CELL_RNASEQ_MRNA_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class ExperimentTraderDaoIT {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcUtils jdbcTestUtils;

    @Autowired
    private ExperimentTraderDao subject;

    @BeforeEach
    void setUp() {
        subject = new ExperimentTraderDao(namedParameterJdbcTemplate);
    }

    @Sql("/fixtures/gxa/experiment.sql")
    @Test
    void ifNoTypeIsProvidedReturnAllExperiment() {
        assertThat(subject.fetchPublicExperimentAccessions())
                .isNotEmpty()
                .size().isEqualTo(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "experiment", "private=FALSE"));
    }

    @Sql("/fixtures/scxa/experiment.sql")
    @Test
    void emptyIfNoExperimentsCanBeFound() {
        assertThat(subject.fetchPublicExperimentAccessions(SINGLE_CELL_RNASEQ_MRNA_BASELINE, SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE))
                .isNotEmpty()
                .size().isEqualTo(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "experiment", "private=FALSE"));
        assertThat(subject.fetchPublicExperimentAccessions(PROTEOMICS_BASELINE))
                .isEmpty();
    }

    @Sql({"/fixtures/gxa/experiment.sql", "/fixtures/scxa/experiment.sql"})
    @Test
    void getSpecificTypeOfExperiments() {
        assertThat(ExperimentType.values())
                .allSatisfy(experimentType ->
                    assertThat(subject.fetchPublicExperimentAccessions(experimentType))
                            .isNotEmpty()
                            .size().isLessThan(
                                    JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "experiment", "private=FALSE")));
    }

    @Sql({"/fixtures/gxa/experiment.sql", "/fixtures/scxa/experiment.sql"})
    @Test
    void shouldGetPrivateExperimentAccessionsIfAvailable() {
        var accession = jdbcTestUtils.fetchRandomExperimentAccession();
        jdbcTestUtils.updatePublicExperimentAccessionToPrivate(accession);
        assertThat(subject.fetchPrivateExperimentAccessions()).isNotEmpty();
        jdbcTestUtils.updatePrivateExperimentAccessionToPublic(accession);
    }

    @Test
    void shouldGetEmptyIfNoPrivateExperimentsCanBeFound() {
        assertThat(subject.fetchPrivateExperimentAccessions()).isEmpty();
    }
}
