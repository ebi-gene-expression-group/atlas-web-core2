package uk.ac.ebi.atlas.model.card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.home.species.SpeciesSummary;
import uk.ac.ebi.atlas.species.Species;

import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

// This test needs to be an IT with @WebAppConfiguration for UrlHelpers to have access to ServletRequestAttributes
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfig.class)
class CardModelFactoryIT {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    private CardModelFactory subject;

    @BeforeEach
    void setUp() {
        subject = new CardModelFactory();
    }

    @Test
    void createPopularSpeciesCard() {
        Species species = generateRandomSpecies();

        SpeciesSummary someWeirdSpeciesInfo =
                SpeciesSummary.create(
                        species.getName(),
                        "Dorne",
                        RNG.nextInt(1, 1000),
                        RNG.nextInt(1, 1000));

        assertThat(subject.create(someWeirdSpeciesInfo))
                .extracting("iconType", "iconSrc")
                .containsOnly(CardIconType.SPECIES, species.getName());

        assertThat(subject.create(someWeirdSpeciesInfo).content())
                .hasSize(3);
    }

    @Test
    void experimentWordIsPluralisedInSpeciesCards() {
        assertThat(
                subject.create(
                        SpeciesSummary.create(
                                randomAlphabetic(10),
                                randomAlphabetic(10),
                                0,
                                1))
                        .content().get(0).getLeft())
                .endsWith("experiment");

        assertThat(
                subject.create(
                        SpeciesSummary.create(
                                randomAlphabetic(10),
                                randomAlphabetic(10),
                                1))
                        .content().get(0).getLeft())
                .endsWith("experiment");


        assertThat(
                subject.create(
                        SpeciesSummary.create(
                                randomAlphabetic(10),
                                randomAlphabetic(10),
                                1,
                                1)).content().get(0).getLeft())
                .endsWith("experiments");

        assertThat(
                subject.create(
                        SpeciesSummary.create(
                                randomAlphabetic(10),
                                randomAlphabetic(10),
                                RNG.nextInt(2, Integer.MAX_VALUE)))
                        .content().get(0).getLeft())
                .endsWith("experiments");

    }
}
