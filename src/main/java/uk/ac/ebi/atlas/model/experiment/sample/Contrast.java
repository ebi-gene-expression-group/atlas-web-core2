package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Contrast extends ReportsGeneExpression implements Comparable<Contrast> {
    private final String displayName;
    private final AssayGroup referenceAssayGroup;
    private final AssayGroup testAssayGroup;
    private final String arrayDesignAccession;  // Used only in microarray experiments

    public Contrast(@NotNull String id,
                    @NotNull String displayName,
                    @NotNull AssayGroup referenceAssayGroup,
                    @NotNull AssayGroup testAssayGroup,
                    @Nullable String arrayDesignAccession) {
        super(
                id,
                ImmutableList.<BiologicalReplicate>builder()
                        .addAll(referenceAssayGroup.getAssays())
                        .addAll(testAssayGroup.getAssays())
                        .build());

        checkArgument(
                isNotBlank(displayName),
                "Contrast display name cannot be null or empty");

        checkArgument(
                arrayDesignAccession == null || isNotBlank(arrayDesignAccession),
                "Array design accession cannot be empty");

        checkArgument(
                !referenceAssayGroup.equals(testAssayGroup),    // Equality of assay groups is ID-based
                "Reference assay group ID cannot be the same as test assay group ID");

        this.referenceAssayGroup = referenceAssayGroup;
        this.testAssayGroup = testAssayGroup;
        this.displayName = displayName;
        this.arrayDesignAccession = arrayDesignAccession;
    }

    @Nullable
    public String getArrayDesignAccession() {
        return arrayDesignAccession;
    }

    @NotNull
    public AssayGroup getReferenceAssayGroup() {
        return referenceAssayGroup;
    }

    @NotNull
    public AssayGroup getTestAssayGroup() {
        return testAssayGroup;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int compareTo(@NotNull Contrast that) {
        return this.displayName.compareTo(that.getDisplayName());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Contrast) {
            Contrast that = (Contrast) o;
            return (this.displayName.equals(that.getDisplayName()));
        }
        return false;
    }

    @NotNull
    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("id", id);
        o.addProperty("displayName", displayName);
        o.add("referenceAssayGroup", referenceAssayGroup.toJson());
        o.add("testAssayGroup", testAssayGroup.toJson());
        o.addProperty("arrayDesignAccession", arrayDesignAccession);
        return o;
    }
}
