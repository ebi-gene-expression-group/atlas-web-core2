package uk.ac.ebi.atlas.experimentimport.coexpression;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.NotNull;

@AutoValue
public abstract class BaselineCoexpression implements Comparable<BaselineCoexpression> {
    public abstract double ceStatistic();
    public abstract String ceGeneID();

    static BaselineCoexpression create(double ceStatistic, @NotNull String ceGeneID) {
        return new AutoValue_BaselineCoexpression(ceStatistic, ceGeneID);
    }

    @Override
    public int compareTo(@NotNull BaselineCoexpression other) {
        int c = Double.compare(this.ceStatistic(), other.ceStatistic());
        return c == 0 ? this.ceGeneID().compareTo(other.ceGeneID()) : c;
    }
}
