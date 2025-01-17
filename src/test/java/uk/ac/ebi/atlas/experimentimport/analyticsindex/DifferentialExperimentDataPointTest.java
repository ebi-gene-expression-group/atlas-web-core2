package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.DifferentialExperimentDataPoint;
import uk.ac.ebi.atlas.experimentimport.analytics.differential.rnaseq.RnaSeqDifferentialAnalytics;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.DifferentialExperimentBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomEnsemblGeneId;

class DifferentialExperimentDataPointTest {
    private final static ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Test
    void testGetRelevantBioentityPropertyNames() {
        var subject =
                new DifferentialExperimentDataPoint(
                        new DifferentialExperimentBuilder().build(),
                        new RnaSeqDifferentialAnalytics("", "", 0.03, 1.23), "", 5);

        assertThat(subject.getProperties())
                .containsKeys("factors", "regulation", "contrast_id", "num_replicates", "fold_change", "p_value")
                .doesNotContainKeys(
                        "expression_level",
                        "expression_level_fpkm",
                        "expression_levels",
                        "expression_levels_fpkm",
                        "assay_group_id");
    }

    @Test
    void isPrivateFieldIsSet() {
        var experiment = new DifferentialExperimentBuilder().build();

        var subject =
                new DifferentialExperimentDataPoint(
                        experiment,
                        new RnaSeqDifferentialAnalytics(
                                generateRandomEnsemblGeneId(),
                                experiment.getDataColumnDescriptors().get(0).getId(),
                                RNG.nextDouble(),
                                RNG.nextDouble(0, 20)),
                        "",
                        5);

        assertThat(subject.getProperties())
                .containsEntry("is_private", experiment.isPrivate());
    }
}
