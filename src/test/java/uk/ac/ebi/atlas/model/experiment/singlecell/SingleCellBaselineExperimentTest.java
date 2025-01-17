package uk.ac.ebi.atlas.model.experiment.singlecell;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.SingleCellBaselineExperimentBuilder;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

class SingleCellBaselineExperimentTest {
    // The best test name
    @Test
    void analysedAssaysAreAnalysed() {
        SingleCellBaselineExperiment subject = new SingleCellBaselineExperimentBuilder().build();

        assertThat(subject.getAnalysedAssays())
                .allMatch(assayId -> subject.propertiesForAssay(assayId).get(0).get("analysed").getAsBoolean());
    }

    @Test
    void nonExistingAssaysAreNotAnalysed() {
        SingleCellBaselineExperiment subject = new SingleCellBaselineExperimentBuilder().build();

        assertThat(subject.propertiesForAssay(randomAlphanumeric(1, 10)).get(0).get("analysed").getAsBoolean())
                .isFalse();
    }
}