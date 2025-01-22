package uk.ac.ebi.atlas.home.species;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesProperties;
import uk.ac.ebi.atlas.testutils.RandomDataTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpeciesSummaryServiceTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private static final int MAX_DIFFERENT_SPECIES = 100;
    private static final int MAX_EXPERIMENTS_PER_TYPE = 500;
    private static final int MAX_DIFFERENT_SUBSPECIES = 10;

    @Mock
    private SpeciesSummaryDao speciesSummaryDaoMock;

    private SpeciesSummaryService subject;

    @BeforeEach
    void setUp() {
        subject = new SpeciesSummaryService(speciesSummaryDaoMock);
    }

    @Test
    void returnsEmptyWhenThereAreNoExperiments() {
        when(speciesSummaryDaoMock.getExperimentCountBySpeciesAndExperimentType()).thenReturn(ImmutableList.of());

        assertThat(subject.getReferenceSpeciesSummariesGroupedByKingdom())
                .isEmpty();
    }

    @Test
    void producesTheRightSummaries() {
        var species = generateRandomSpecies();

        when(speciesSummaryDaoMock.getExperimentCountBySpeciesAndExperimentType())
                .thenReturn(generateRandomExperimentCountBySpeciesAndExperimentType(species).asList());

        assertThat(subject.getReferenceSpeciesSummariesGroupedByKingdom().keySet())
                .containsAnyElementsOf(
                        species.stream().map(Species::getKingdom).collect(toImmutableSet()));

        var kingdom = species.asList().get(RNG.nextInt(species.size())).getKingdom();
        assertThat(subject.getReferenceSpeciesSummariesGroupedByKingdom().get(kingdom).size())
                .isLessThanOrEqualTo(
                        species.stream()
                                .filter(_species -> _species.getKingdom().equalsIgnoreCase(kingdom))
                                .map(Species::getReferenceName)
                                .collect(toImmutableSet())
                                .size());
    }

    @Test
    void getReferenceSpeciesIsEmptyWhenThereAreNoExperiments() {
        when(speciesSummaryDaoMock.getExperimentCountBySpeciesAndExperimentType()).thenReturn(ImmutableList.of());

        assertThat(subject.getSpecies()).isEmpty();
    }

    @Test
    void getReferenceSpeciesAggregatesSubspecies() {
        var randomSpecies = generateRandomSpecies();

        // Create some subspecies from the randomSpecies pool
        var subspecies =
                IntStream.range(1, RNG.nextInt(1, MAX_DIFFERENT_SUBSPECIES)).boxed()
                        .map(__ -> randomSpecies.asList().get(RNG.nextInt(0, randomSpecies.size())))
                        .map(_species ->
                                new Species(
                                        _species.getName() + " " + randomAlphabetic(3, 10).toLowerCase(),
                                        SpeciesProperties.create(
                                                _species.getEnsemblName(),
                                                _species.getDefaultQueryFactorType(),
                                                _species.getKingdom(),
                                                _species.getGenomeBrowsers())))
                        .collect(toImmutableSet());

        var experiments =
                generateRandomExperimentCountBySpeciesAndExperimentType(
                        Sets.union(randomSpecies, subspecies).immutableCopy());

        when(speciesSummaryDaoMock.getExperimentCountBySpeciesAndExperimentType())
                .thenReturn(experiments.asList());

        var actualSpecies = subject.getSpecies().size();

        var missingSpecies = Math.abs(actualSpecies - randomSpecies.size());

        var missingSpeciesProb = Math.ceil((missingSpecies * 100d) / (actualSpecies + missingSpecies));

        assertThat(actualSpecies).isCloseTo(randomSpecies.size(),
                within(Double.valueOf(missingSpeciesProb).intValue()));
    }

    private static ImmutableSet<Species> generateRandomSpecies() {
        return IntStream.range(0, RNG.nextInt(1, MAX_DIFFERENT_SPECIES)).boxed()
                .map(__ -> RandomDataTestUtils.generateRandomSpecies())
                .collect(toImmutableSet());
    }

    private static ImmutableSet<Triple<Species, ExperimentType, Long>>
    generateRandomExperimentCountBySpeciesAndExperimentType(Collection<Species> species) {
        // For each species in the pool create a random amount of experiments of each type...
        return species.stream()
                .flatMap(_species ->
                        Arrays.stream(ExperimentType.values()).map(experimentType ->
                                Triple.of(
                                        _species,
                                        ExperimentType.values()
                                                [RNG.nextInt(ExperimentType.values().length)],
                                        RNG.nextLong(1, MAX_EXPERIMENTS_PER_TYPE))))
                // ... and then flip a coin to keep or discard that specific triplet
                .filter(__ -> RNG.nextDouble() < 0.5)
                .collect(toImmutableSet());
    }

}
