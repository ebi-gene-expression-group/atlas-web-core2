package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

class SpeciesTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private static final int SPECIES_MAX_WORD_COUNT = 4;
    private static final int SPECIES_WORD_MIN_LENGTH = 3;
    private static final int SPECIES_WORD_MAX_LENGTH = 20;
    private static final ImmutableList<String> FACTOR_TYPES =
            ImmutableList.of("ORGANISM_PART", "DISEASE", "DEVELOPMENTAL_STAGE", "SEX", "AGE");
    private static final ImmutableList<String> KINGDOMS =
            ImmutableList.of(
                    "North", "Mountain and the Vale", "Isles and Rivers", "Rock", "Stormlands", "Reach", "Dorne");
    private static final int SPECIES_RESOURCES_MAX_LENGTH = 5;

    @Test
    void equalityIsDefinedBySpeciesNameAndProperties() {
        var speciesProperties = generateRandomSpeciesProperties();
        var species = new Species(generateRandomSpeciesName(), speciesProperties);

        assertThat(species)
                .isEqualTo(species)
                .isEqualTo(new Species(species.getName(), speciesProperties))
                .isNotEqualTo(new Species(generateRandomSpeciesName(), speciesProperties))
                .isNotEqualTo(new Species(species.getName(), generateRandomSpeciesProperties()));
    }

    @Test
    void speciesNameIsCaseInsensitive() {
        var speciesName = generateRandomSpeciesName();
        var speciesProperties = generateRandomSpeciesProperties();

        assertThat(new Species(speciesName.toLowerCase(), speciesProperties))
                .isEqualTo(new Species(speciesName.toUpperCase(), speciesProperties));
    }

    @Test
    void hashCodeIsBasedOnSpeciesProperties() {
        var speciesProperties = generateRandomSpeciesProperties();
        var speciesName = generateRandomSpeciesName();

        assertThat(new Species(speciesName, speciesProperties))
                .hasSameHashCodeAs(new Species(generateRandomSpeciesName(), speciesProperties));
        assertThat(new Species(speciesName, speciesProperties).hashCode())
                .isNotEqualTo(new Species(speciesName, generateRandomSpeciesProperties()).hashCode());
    }

    private static SpeciesProperties generateRandomSpeciesProperties() {
        return SpeciesProperties.create(
                generateRandomSpeciesName().replace(" ", "_"),
                FACTOR_TYPES.get(RNG.nextInt(FACTOR_TYPES.size())),
                KINGDOMS.get(RNG.nextInt(KINGDOMS.size())),
                IntStream.range(0, RNG.nextInt(SPECIES_RESOURCES_MAX_LENGTH)).boxed()
                        .map(__ -> ImmutableMap.of(generateRandomSpeciesWord(), generateRandomSpeciesWord()))
                        .collect(toImmutableList()));
    }

    private static String generateRandomSpeciesWord() {
        return randomAlphabetic(SPECIES_WORD_MIN_LENGTH, SPECIES_WORD_MAX_LENGTH);
    }

    private static String generateRandomSpeciesName() {
        return IntStream.range(0, RNG.nextInt(1, SPECIES_MAX_WORD_COUNT)).boxed()
                        .map(__ -> generateRandomSpeciesWord())
                        .collect(joining(" "));
    }
}
