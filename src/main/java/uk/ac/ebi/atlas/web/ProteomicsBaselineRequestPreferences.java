package uk.ac.ebi.atlas.web;

import uk.ac.ebi.atlas.model.ExpressionUnit;
import uk.ac.ebi.atlas.search.SemanticQuery;

public class ProteomicsBaselineRequestPreferences extends BaselineRequestPreferences<ExpressionUnit.Absolute.Protein> {
    private static final double DEFAULT_CUTOFF = 0.0d;
    private ExpressionUnit.Absolute.Protein unit = ExpressionUnit.Absolute.Protein.RA;

    @Override
    public double getDefaultCutoff() {
        return DEFAULT_CUTOFF;
    }

    public static ProteomicsBaselineRequestPreferences requestAllData() {
        ProteomicsBaselineRequestPreferences preferences = new ProteomicsBaselineRequestPreferences();
        preferences.setCutoff(VERY_SMALL_NON_ZERO_VALUE);
        preferences.setGeneQuery(SemanticQuery.create());
        return preferences;
    }

    @Override
    public ExpressionUnit.Absolute.Protein getUnit() {
        return unit;
    }

    public void setUnit(ExpressionUnit.Absolute.Protein unit) {
        this.unit = unit;
    }
}
