package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayIds;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomBiologicalReplicates;

// Remember that because we pass biological replicates in a set there can’t be any duplicates
class ReportsGeneExpressionTest {
    private static final int MAX_REPLICATE_COUNT = 10;

    // Minimal, behaviourless implementation
    private class TestSample extends ReportsGeneExpression {
        TestSample(@NotNull String id,
                   @NotNull Collection<@NotNull BiologicalReplicate> assays) {
            super(id, assays);
        }
    }

    @Test
    void hashCodeComesFromId() {
        String id = randomAlphanumeric(10);
        ReportsGeneExpression subject = new TestSample(id, generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT));

        assertThat(subject).hasSameHashCodeAs(id);
    }

    @Test
    void equalityComesFromId() {
        String id = randomAlphanumeric(10);
        ReportsGeneExpression subject = new TestSample(id, generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT));

        assertThat(subject)
                .isEqualTo(new TestSample(id, generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT)));

        // Implicitly asserted above, but we get 100% coverage with just this, so why not
        assertThat(subject).isEqualTo(subject);
    }

    @Test
    void equalsNeverThrows() {
        ReportsGeneExpression subject =
                new TestSample(
                        randomAlphanumeric(10),
                        generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT));

        // equals() should never throw, it must always return true or false
        assertThat(subject).isNotEqualTo(null);
        assertThat(subject).isNotEqualTo(1);
    }

    @Test
    void validIfEmptyId() {
        var assays = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        var blankString = generateBlankString();
        var subject = new TestSample(blankString, assays);

        assertThat(subject)
                .hasFieldOrPropertyWithValue("id", blankString)
                .hasFieldOrPropertyWithValue("assays", assays);
    }

//    @Test
//    void throwIfAssaysIsEmpty() {
//        assertThatIllegalArgumentException().isThrownBy(
//                () -> new TestSample(randomAlphanumeric(10), ImmutableSet.of()));
//    }

    @Test
    void throwIfDuplicateTechnicalReplicateId() {
        // For this test we need, at least, one technical replicate group in the set of biological replicates
        ImmutableSet<BiologicalReplicate> replicates = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        while (replicates.stream().allMatch(replicate -> replicate.getAssayIds().size() == 1)) {
            replicates = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        }

        // Get assay IDs of any technical replicate group
        List<String> technicalReplicateGroupAssayIds =
                new ArrayList<>(
                        replicates.stream()
                                .filter(replicate -> replicate.getAssayIds().size() > 1)
                                .findAny()
                                .orElseThrow(RuntimeException::new)
                                .getAssayIds());
        // We shuffle so that we pick any assay ID randomly when we do get(0) below
        Collections.shuffle(technicalReplicateGroupAssayIds);

        ImmutableSet<BiologicalReplicate> replicatesWithDuplicateTechnicalReplicate =
                ImmutableSet.<BiologicalReplicate>builder()
                        .addAll(replicates)
                        .add(BiologicalReplicate.create(
                                randomAlphanumeric(10),
                                ImmutableSet.of(
                                        randomAlphanumeric(10),
                                        // Replacing this second ID with any other string will make the test fail
                                        technicalReplicateGroupAssayIds.get(0))))
                        .build();

        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestSample(randomAlphanumeric(10), replicatesWithDuplicateTechnicalReplicate));
    }

    @Test
    void throwIfAssayIdEqualToTechnicalReplicateId() {
        // For this test we need, at least, one technical replicate group in the set of biological replicates
        ImmutableSet<BiologicalReplicate> replicates = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        while (replicates.stream().allMatch(replicate -> replicate.getAssayIds().size() == 1)) {
            replicates = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        }

        // Get the assay IDs of any technical replicate group
        List<String> technicalReplicateGroupAssayIds =
                new ArrayList<>(
                        replicates.stream()
                                .filter(replicate -> replicate.getAssayIds().size() > 1)
                                .findAny()
                                .orElseThrow(RuntimeException::new)
                                .getAssayIds());
        // We shuffle so that we pick any assay ID randomly when we do get(0) below
        Collections.shuffle(technicalReplicateGroupAssayIds);

        ImmutableSet<BiologicalReplicate> finalReplicates = ImmutableSet.copyOf(replicates);
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestSample(
                        randomAlphanumeric(10),
                        ImmutableSet.<BiologicalReplicate>builder()
                                .addAll(finalReplicates)
                                // Not adding the replicate below will make the test fail
                                .add(BiologicalReplicate.create(technicalReplicateGroupAssayIds.get(0)))
                                .build()));
    }

    @Test
    void throwIfDuplicateTechnicalReplicateGroupId() {
        // For this test we need, at least, one technical replicate group in the set of biological replicates
        ImmutableSet<BiologicalReplicate> replicates = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        while (replicates.stream().allMatch(replicate -> replicate.getAssayIds().size() == 1)) {
            replicates = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        }

        // Get the assay IDs of any technical replicate group
        BiologicalReplicate technicalReplicateGroup =
                replicates.stream()
                        .filter(replicate -> replicate.getAssayIds().size() > 1)
                        .findAny()
                        .orElseThrow(RuntimeException::new);

        ImmutableSet<BiologicalReplicate> finalReplicates = ImmutableSet.copyOf(replicates);
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestSample(
                        randomAlphanumeric(10),
                        ImmutableSet.<BiologicalReplicate>builder()
                                .addAll(finalReplicates)
                                // Not adding the replicate below will make the test fail
                                .add(BiologicalReplicate.create(
                                        technicalReplicateGroup.getId(),
                                        generateRandomAssayIds()))
                                .build()));
    }

    @Test
    void gettersForProperties() {
        String id = randomAlphanumeric(10);
        ImmutableSet<BiologicalReplicate> assays = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        ReportsGeneExpression subject = new TestSample(id, assays);

        assertThat(subject)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("assays", assays)
                .hasFieldOrPropertyWithValue(
                        "assayIds",
                        assays.stream().flatMap(assay -> assay.getAssayIds().stream()).collect(toImmutableSet()));
    }
}