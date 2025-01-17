package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AssayGroup extends ReportsGeneExpression {
    public AssayGroup(@NotNull String id,
                      @NotNull Collection<@NotNull BiologicalReplicate> assays) {
        super(id, assays);
    }

    // Because the first assay ID is required to be known, all the clients that build instances of AssayGroup should
    // use a Set implementation that preserves insertion order. E.g. in AssayGroupFactory it’s ImmutableSet and in
    // ExperimentConfiguration it’s the value set from a LinkedListMultimap
    @NotNull
    public String getFirstAssayId() {
        return getAssayIds().iterator().next();
    }

    // TODO We should change assayAccessions to assayIds, to use ID exclusively instead of a max  of ID and accession
    @NotNull
    public JsonObject toJson() {
        JsonObject o = new JsonObject();

        o.addProperty("id", id);

        JsonArray a = new JsonArray();
        getAssayIds().forEach(a::add);
        o.add("assayAccessions", a);

        o.addProperty("replicates", getAssays().size());

        return o;
    }
}
