package uk.ac.ebi.atlas.model.arraydesign;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.NotNull;

@AutoValue
public abstract class ArrayDesign {
    public abstract String getAccession();
    public abstract String getName();

    @NotNull
    public static ArrayDesign create(@NotNull String accession, @NotNull String name) {
        return new AutoValue_ArrayDesign(accession, name);
    }

    @NotNull
    public static ArrayDesign create(@NotNull String accession) {
        return create(accession, accession);
    }
}
