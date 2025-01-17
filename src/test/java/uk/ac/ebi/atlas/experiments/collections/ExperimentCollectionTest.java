package uk.ac.ebi.atlas.experiments.collections;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

class ExperimentCollectionTest {
    private final static Random RNG = ThreadLocalRandom.current();

    @Test
    void nullImageBytesBecomesAnEmptyImage() {
        assertThat(ExperimentCollection.create(
                randomAlphabetic(1, 4).toUpperCase(),
                randomAlphabetic(5, 10).toUpperCase(),
                randomAlphanumeric(10, 30),
                null).icon())
                .isEmpty();
    }

    @Test
    void emptyImageBytesBecomesAnEmptyImage() {
        assertThat(ExperimentCollection.create(
                randomAlphabetic(1, 4).toUpperCase(),
                randomAlphabetic(5, 10).toUpperCase(),
                randomAlphanumeric(10, 30),
                new byte[0]).icon())
                .isEmpty();
    }

    @Test
    void brokenBytesBecomesAnEmptyImage() {
        var randombBytes = new byte[RNG.nextInt(4096)];
        RNG.nextBytes(randombBytes);
        assertThat(ExperimentCollection.create(
                randomAlphabetic(1, 4).toUpperCase(),
                randomAlphabetic(5, 10).toUpperCase(),
                randomAlphanumeric(10, 30),
                randombBytes).icon())
                .isEmpty();
    }

}