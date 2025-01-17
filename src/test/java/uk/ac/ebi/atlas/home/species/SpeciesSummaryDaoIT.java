package uk.ac.ebi.atlas.home.species;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.configuration.TestConfig;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@WebAppConfiguration
@Transactional
class SpeciesSummaryDaoIT {
    @Inject
    private SpeciesSummaryDao subject;

    @Test
    void returnsEmptyWhenThereAreNoExperiments() {
        assertThat(subject.getExperimentCountBySpeciesAndExperimentType())
                .isEmpty();
    }

    @Test
    @Sql("/fixtures/gxa/experiment.sql")
    void saneResultsForBulk() {
        assertThat(subject.getExperimentCountBySpeciesAndExperimentType())
                .allSatisfy(triplet -> assertThat(triplet.getRight()).isGreaterThan(0));
    }

    @Test
    @Sql("/fixtures/scxa/experiment.sql")
    void saneResultsForSingleCell() {
        assertThat(subject.getExperimentCountBySpeciesAndExperimentType())
                .allSatisfy(triplet -> assertThat(triplet.getRight()).isGreaterThan(0));
    }
}
