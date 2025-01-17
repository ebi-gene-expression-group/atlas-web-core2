package uk.ac.ebi.atlas.home.species;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

class SpeciesSummaryTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private static final int MAX_NUMBER_OF_BASELINE_EXPERIMENTS = 2000;
    private static final int MAX_NUMBER_OF_DIFFERENTIAL_EXPERIMENTS = 2000;

    @Test
    void hasRightParameters() {
        var numberOfBaselineExperiments = RNG.nextLong(MAX_NUMBER_OF_BASELINE_EXPERIMENTS);
        var numberOfDifferentialExperiments = RNG.nextLong(MAX_NUMBER_OF_DIFFERENTIAL_EXPERIMENTS);
        var species = generateRandomSpecies();

        assertThat(SpeciesSummary.create(
                species.getReferenceName(),
                species.getKingdom(),
                numberOfBaselineExperiments,
                numberOfDifferentialExperiments))
                .extracting(
                        "species",
                        "kingdom",
                        "totalExperiments",
                        "baselineExperiments",
                        "differentialExperiments")
                .containsExactly(
                        species.getReferenceName(),
                        species.getKingdom(),
                        numberOfBaselineExperiments + numberOfDifferentialExperiments,
                        numberOfBaselineExperiments,
                        numberOfDifferentialExperiments);

        assertThat(SpeciesSummary.create(
                species.getReferenceName(),
                species.getKingdom(),
                numberOfBaselineExperiments + numberOfDifferentialExperiments))
                .extracting(
                        "species",
                        "kingdom",
                        "totalExperiments",
                        "baselineExperiments",
                        "differentialExperiments")
                .containsExactly(
                        species.getReferenceName(),
                        species.getKingdom(),
                        numberOfBaselineExperiments + numberOfDifferentialExperiments,
                        0L,
                        0L);
    }

    @Test
    void comparatorWorks() {
        var poolOfSummaries =
                IntStream.range(0, RNG.nextInt(2, 1000)).boxed()
                        .map(__ -> SpeciesSummary.create(
                                generateRandomSpecies().getReferenceName(),
                                generateRandomSpecies().getKingdom(),
                                RNG.nextInt(MAX_NUMBER_OF_BASELINE_EXPERIMENTS),
                                RNG.nextInt(MAX_NUMBER_OF_DIFFERENTIAL_EXPERIMENTS)))
                        .sorted(SpeciesSummary.BY_SIZE_DESCENDING)
                        .collect(toImmutableList());

        assertThat(poolOfSummaries.reverse())
                .extracting("totalExperiments")
                .isSorted();
    }
}