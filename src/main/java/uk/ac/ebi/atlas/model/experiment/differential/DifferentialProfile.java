package uk.ac.ebi.atlas.model.experiment.differential;

import org.apache.commons.collections.CollectionUtils;
import uk.ac.ebi.atlas.model.Profile;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class DifferentialProfile<X extends DifferentialExpression, P extends Profile<Contrast, X, P>>
        extends Profile<Contrast, X, P> {

    private static final double MIN_P_VALUE = 1;

    public DifferentialProfile(String geneId, String geneName) {
        super(geneId, geneName);
    }

    public long getSpecificity(final Regulation regulation) {
        return expressionsByCondition.values().stream()
                .filter(expr -> expr.getLevel() != 0 && expr.isRegulatedLike(regulation))
                .count();
    }

    public double getAveragePValueOn(Collection<Contrast> contrasts) {
        checkArgument(!CollectionUtils.isEmpty(contrasts),
                "This method must be invoked with all conditions when the set of selected conditions is empty");

        double pValueTotal = 0D;

        for (Contrast contrast : contrasts) {
            X expression = getExpression(contrast);
            if (expression != null) {
                pValueTotal += expression.getPValue();
            } else {
                pValueTotal += MIN_P_VALUE;
            }
        }

        return pValueTotal / contrasts.size();
    }
}
