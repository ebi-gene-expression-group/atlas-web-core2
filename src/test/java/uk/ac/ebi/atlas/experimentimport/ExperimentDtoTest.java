package uk.ac.ebi.atlas.experimentimport;

import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomDoi;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomPubmedId;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

class ExperimentDtoTest {
    private static final Random RNG = ThreadLocalRandom.current();
    private static final int MAXIMUM_PUBLICATION_COUNT = 10;

    static ExperimentDto generateRandomExperimentDto() {
        return new ExperimentDto(
                generateRandomExperimentAccession(),
                ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)],
                generateRandomSpecies().getName(),
                IntStream.range(0, MAXIMUM_PUBLICATION_COUNT)
                        .boxed()
                        .map(__ -> generateRandomPubmedId())
                        .collect(toImmutableSet()),
                IntStream.range(0, MAXIMUM_PUBLICATION_COUNT)
                        .boxed()
                        .map(__ -> generateRandomDoi())
                        .collect(toImmutableSet()),
                new Timestamp(new Date().getTime()),
                new Timestamp(new Date().getTime()),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());
    }

    @Test
    void toJson() {
        var subject = generateRandomExperimentDto();
        var subjectAsJson = subject.toJson();

        assertThat(subjectAsJson.get("accession").getAsString())
                .isEqualTo(subject.getExperimentAccession());
        assertThat(subjectAsJson.get("type").getAsString())
                .isEqualTo(subject.getExperimentType().name());
        assertThat(subjectAsJson.get("species").getAsString())
                .isEqualTo(subject.getSpecies());
        assertThat(subjectAsJson.get("pubmedIds").getAsJsonArray())
                .containsExactlyInAnyOrderElementsOf(
                        subject.getPubmedIds().stream().map(JsonPrimitive::new).collect(toImmutableSet()));
        assertThat(subjectAsJson.get("dois").getAsJsonArray())
                .containsExactlyInAnyOrderElementsOf(
                        subject.getDois().stream().map(JsonPrimitive::new).collect(toImmutableSet()));
        assertThat(subjectAsJson.get("isPrivate").getAsBoolean())
                .isEqualTo(subject.isPrivate());
        assertThat(subjectAsJson.get("accessKey").getAsString())
                .isEqualTo(subject.getAccessKey());
        assertThat(subjectAsJson.get("loadDate").getAsString())
                .isEqualTo(subject.getLoadDate().toString());
        assertThat(subjectAsJson.get("lastUpdate").getAsString())
                .isEqualTo(subject.getLastUpdate().toString());
    }

    @Test
    void datesAreNotNeededToBuildValidInstances() {
        var subject = new ExperimentDto(
                generateRandomExperimentAccession(),
                ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)],
                generateRandomSpecies().getName(),
                IntStream.range(0, MAXIMUM_PUBLICATION_COUNT)
                        .boxed()
                        .map(__ -> generateRandomPubmedId())
                        .collect(toImmutableSet()),
                IntStream.range(0, MAXIMUM_PUBLICATION_COUNT)
                        .boxed()
                        .map(__ -> generateRandomDoi())
                        .collect(toImmutableSet()),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());

        assertThat(subject.getLoadDate()).isNull();
        assertThat(subject.getLastUpdate()).isNull();
    }

    @Test
    void equalityIsBasedOnExperimentAccession() {
        var experimentAccession = generateRandomExperimentAccession();

        var one = new ExperimentDto(
                experimentAccession,
                ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)],
                generateRandomSpecies().getName(),
                IntStream.range(0, MAXIMUM_PUBLICATION_COUNT)
                        .boxed()
                        .map(__ -> generateRandomPubmedId())
                        .collect(toImmutableSet()),
                IntStream.range(0, MAXIMUM_PUBLICATION_COUNT)
                        .boxed()
                        .map(__ -> generateRandomDoi())
                        .collect(toImmutableSet()),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());

        var another =
                new ExperimentDto(
                        experimentAccession,
                        ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)],
                        generateRandomSpecies().getName(),
                        IntStream.range(0, MAXIMUM_PUBLICATION_COUNT)
                                .boxed()
                                .map(__ -> generateRandomPubmedId())
                                .collect(toImmutableSet()),
                        IntStream.range(0, MAXIMUM_PUBLICATION_COUNT)
                                .boxed()
                                .map(__ -> generateRandomDoi())
                                .collect(toImmutableSet()),
                        new Timestamp(new Date().getTime()),
                        new Timestamp(new Date().getTime()),
                        RNG.nextBoolean(),
                        UUID.randomUUID().toString());

        assertThat(one).isEqualTo(another);
        assertThat(one).hasSameHashCodeAs(another);
        assertThat(one).isNotEqualTo(null);
    }
}