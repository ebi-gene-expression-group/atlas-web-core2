package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

public class Cell extends ReportsGeneExpression {
    @NotNull
    public Cell(@NotNull String id) {
        super(id, ImmutableList.of(BiologicalReplicate.create(id)));
    }
}
