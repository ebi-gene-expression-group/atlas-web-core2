package uk.ac.ebi.atlas.profiles.differential;

import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;

import java.util.List;

public interface ProfileStreamOptions<R extends ReportsGeneExpression> {
    Integer getHeatmapMatrixSize();
    boolean isSpecific();
    List<R> getDataColumnsToReturn();
    List<R> getAllDataColumns();
}
