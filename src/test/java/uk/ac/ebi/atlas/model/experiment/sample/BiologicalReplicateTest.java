package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayIds;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomRnaSeqRunId;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomTechnicalReplicateGroupId;

class BiologicalReplicateTest {
    @Test
    @DisplayName("Biological replicates built with a single ID represent a single assay")
    void noTechnicalReplicates() {
        String assayId = generateRandomRnaSeqRunId();
        assertThat(
                BiologicalReplicate.create(assayId).getAssayIds())
                .containsExactly(assayId);
    }

    @Test
    @DisplayName("Biological replicates can also be technical replicate groups with multiple assay IDs " +
                "(technical replicates)")
    void multipleTechnicalReplicates() {
        String technicalReplicateGroupId = generateRandomTechnicalReplicateGroupId();
        Set<String> assayIds = generateRandomAssayIds();

        assertThat(
                BiologicalReplicate.create(technicalReplicateGroupId, assayIds).getAssayIds())
                .containsExactlyElementsOf(assayIds);
    }

    @Test
    @DisplayName("Technical replicate groups must contain more than one assay")
    void throwIfOnlyOneTehnicalReplicate() {
        String technicalReplicateGroupId = generateRandomTechnicalReplicateGroupId();
        Set<String> assayIds = ImmutableSet.of(generateRandomRnaSeqRunId());

        assertThatIllegalArgumentException().isThrownBy(
                () -> BiologicalReplicate.create(technicalReplicateGroupId, assayIds));
    }

    @Test
    @DisplayName("Technical replicate groups cannot contain duplicate replicate IDs")
    void throwIfDuplicateTechnicalReplicateIds() {
        String technicalReplicateGroupId = generateRandomTechnicalReplicateGroupId();
        String assayId = generateRandomRnaSeqRunId();
        ImmutableList<String> assayIds = ImmutableList.of(assayId, assayId);

        assertThatIllegalArgumentException().isThrownBy(
                () -> BiologicalReplicate.create(technicalReplicateGroupId, assayIds));
    }

    @Test
    @DisplayName("Technical replicate group ID cannot be a technical replicate ID")
    void throwIfTechnicalReplicateIdsContainGroupId() {
        String technicalReplicateGroupId = generateRandomTechnicalReplicateGroupId();
        Set<String> assayIds =
                ImmutableSet.<String>builder()
                        .add(technicalReplicateGroupId.toUpperCase())
                        .addAll(generateRandomAssayIds())
                        .build();

        assertThatIllegalArgumentException().isThrownBy(
                () -> BiologicalReplicate.create(technicalReplicateGroupId, assayIds));
    }

    @Test
    @DisplayName("Assay ID cannot be empty")
    void throwIfBlankAssayId() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> BiologicalReplicate.create(generateBlankString()));
    }

    @Test
    @DisplayName("Technical replicate group ID cannot be empty")
    void throwIfBlankTechnicalReplicateGroupId() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> BiologicalReplicate.create(
                        generateBlankString(),
                        generateRandomAssayIds()));
    }

    @Test
    @DisplayName("Technical replicate IDs cannot be empty")
    void throwIfBlankTechnicalReplicateId() {
        String technicalReplicateGroupId = generateRandomTechnicalReplicateGroupId();
        Set<String> assayIdsWithEmptyId =
                ImmutableSet.<String>builder()
                        .addAll(generateRandomAssayIds())
                        .add(generateBlankString())
                        .build();

        assertThatIllegalArgumentException().isThrownBy(
                () -> BiologicalReplicate.create(technicalReplicateGroupId, assayIdsWithEmptyId));

        Set<String> assayIdsWithNullId = Sets.newHashSet(generateRandomAssayIds());
        assayIdsWithNullId.add(null);

        assertThatIllegalArgumentException().isThrownBy(
                () -> BiologicalReplicate.create(technicalReplicateGroupId, assayIdsWithNullId));
    }
}
