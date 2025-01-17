package uk.ac.ebi.atlas.experimentimport.analyticsindex.stream;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.experimentimport.analytics.differential.microarray.MicroarrayDifferentialAnalytics;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomArrayDesignAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomEnsemblGeneId;

class BaselineExperimentDataPointTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Test
    void isPrivateFieldIsSet() {
        var experiment = new ExperimentBuilder.MicroarrayExperimentBuilder().build();

        var subject =
                new MicroarrayExperimentDataPoint(
                        experiment,
                        new MicroarrayDifferentialAnalytics(
                                generateRandomEnsemblGeneId(),
                                generateRandomArrayDesignAccession(),
                                experiment.getDataColumnDescriptors().get(0).getId(),
                                RNG.nextDouble(),
                                RNG.nextDouble(0, 20),
                                RNG.nextDouble()),
                        "",
                        5);

        assertThat(subject.getProperties())
                .containsEntry("is_private", experiment.isPrivate());
    }}