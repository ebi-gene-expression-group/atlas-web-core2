package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

// A biological replicate can be formed by a single assay or multiple assays that form a technical replicate group
@AutoValue
public abstract class BiologicalReplicate {
    public abstract String getId();
    public abstract ImmutableSet<String> getAssayIds();

    @NotNull
    public static BiologicalReplicate create(@NotNull String assayId) {
        checkArgument(
                isNotBlank(assayId),
                "Assay ID cannot be blank");
        return new AutoValue_BiologicalReplicate(assayId, ImmutableSet.of(assayId));
    }

    @NotNull
    public static BiologicalReplicate create(@NotNull String technicalReplicateGroupId,
                                             @NotNull Collection<@NotNull String> assayIds) {
        checkArgument(
                isNotBlank(technicalReplicateGroupId),
                "Technical replicate group ID cannot be blank");
        checkArgument(
                assayIds.stream().allMatch(StringUtils::isNotBlank),
                "Technical replicate IDs cannot be blank");
        checkArgument(
                assayIds.stream().noneMatch(id -> id.equalsIgnoreCase(technicalReplicateGroupId)),
                "Technical replicate group ID must be different from technical replicate IDs");
        checkArgument(
                assayIds.size() > 1,
                "Technical replicate group must have more than one technical replicate");
        checkArgument(
                ImmutableSet.copyOf(assayIds).size() == assayIds.size(),
                "Technical replicate group cannot have duplicate replicate IDs");

        return new AutoValue_BiologicalReplicate(technicalReplicateGroupId, ImmutableSet.copyOf(assayIds));
    }
}
