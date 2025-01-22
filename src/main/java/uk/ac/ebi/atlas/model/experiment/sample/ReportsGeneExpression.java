package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

// In principle it could be argued that for the case of differential experiments we’re abusing the name of the class,
// (since what’s properly reporting gene expression are the reference and test assay groups), but remember that
// contrasts report *differential* expression
public abstract class ReportsGeneExpression {
    protected final String id;
    protected final ImmutableSet<BiologicalReplicate> assays;

    public ReportsGeneExpression(@NotNull String id,
                                 @NotNull Collection<@NotNull BiologicalReplicate> assays) {

        // TODO This is a small defeat in a grand reengineering of the class previously known as DescribesDataColumn.
        //      Because this class is used by the multiexperiment heatmap we need to relax this condition, but it’s
        //      left commented out here because it will be fixed, along with the multiexperiment heatmap (we don’t know
        //      how yet, but rest assured it will happen!).
        // checkArgument(
        //         !assays.isEmpty(),
        //         this.getClass().getSimpleName() + " must contain at least one assay");

        // Find duplicate assay IDs among biological replicates/technical replicate groups
        var allAssayIds = new HashSet<>();
        var duplicates = assays.stream()
                .flatMap(sample -> sample.getAssayIds().size() > 1 ?
                         Stream.concat(Stream.of(sample.getId()), sample.getAssayIds().stream()) :
                         Stream.of(sample.getId()))
                .filter(n -> !allAssayIds.add(n))   // Set::add returns false if the item was already in the set
                .collect(toImmutableSet());
        checkArgument(
                duplicates.isEmpty(),
                this.getClass().getSimpleName() + " cannot contain duplicate assays: " + String.join(",", duplicates));

        this.id = id;
        this.assays = ImmutableSet.copyOf(assays);
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public ImmutableSet<String> getAssayIds() {
        return assays.stream()
                .flatMap(biologicalReplicate -> biologicalReplicate.getAssayIds().stream())
                .collect(toImmutableSet());
    }

    @NotNull
    public ImmutableSet<BiologicalReplicate> getAssays() {
        return assays;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ReportsGeneExpression) {
            var that = (ReportsGeneExpression) o;
            return (this.id.equals(that.id));
        }
        return false;
    }
}
