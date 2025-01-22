package uk.ac.ebi.atlas.model.experiment.differential.microarray;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonObject;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExpression;

public class MicroarrayExpression extends DifferentialExpression {
    private double tStatistic;

    public MicroarrayExpression(double pValue, double foldChange, double tStatistic) {
        super(pValue, foldChange);
        this.tStatistic = tStatistic;
    }

    public double getTstatistic() {
        return tStatistic;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("p-value", getPValue())
                .add("foldChange", getFoldChange())
                .add("t stat", getTstatistic())
                .toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject result = super.toJson();
        result.addProperty("tStat", tStatistic);
        return result;
    }
}
