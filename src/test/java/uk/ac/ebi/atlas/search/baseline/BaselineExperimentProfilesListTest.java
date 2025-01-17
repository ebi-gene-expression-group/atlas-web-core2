package uk.ac.ebi.atlas.search.baseline;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExpression;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;
import uk.ac.ebi.atlas.model.experiment.sdrf.FactorSet;
import uk.ac.ebi.atlas.testutils.MockExperiment;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;

class BaselineExperimentProfilesListTest {
    private String factorHeader = "type1";

    @Test
    void testGetFactorsAcrossExperiments() {
        var result = new BaselineExperimentProfilesList();

        var p1 = new BaselineExperimentProfile(MockExperiment.createBaselineExperiment(), new FactorSet());
        p1.add(new FactorAcrossExperiments(new Factor(factorHeader, "v1")), new BaselineExpression(1.0));
        p1.add(new FactorAcrossExperiments(new Factor(factorHeader, "v2")), new BaselineExpression(2.0));
        result.add(p1);
        assertThat(result.getFactorsAcrossExperiments()).hasSize(2);

        var p2 = new BaselineExperimentProfile(MockExperiment.createBaselineExperiment(), new FactorSet());
        p2.add(new FactorAcrossExperiments(new Factor(factorHeader, "v1")), new BaselineExpression(1.0));
        p2.add(new FactorAcrossExperiments(new Factor(factorHeader, "v3")), new BaselineExpression(3.0));
        result.add(p2);
        assertThat(result.getFactorsAcrossExperiments()).hasSize(3);
    }

    @Test
    void zerosDoMakeItToTheList() {
        var result = new BaselineExperimentProfilesList();

        var p1 = new BaselineExperimentProfile(MockExperiment.createBaselineExperiment(), new FactorSet());
        p1.add(new FactorAcrossExperiments(new Factor(factorHeader, "v1")), new BaselineExpression(1.0));
        p1.add(new FactorAcrossExperiments(new Factor(factorHeader, "v2")), new BaselineExpression(2.0));
        p1.add(new FactorAcrossExperiments(new Factor(factorHeader, "v3")), new BaselineExpression(0.0));
        result.add(p1);
        assertThat(result.getFactorsAcrossExperiments()).hasSize(3);
    }

    @Test
    void assaysComposedOfSamplesWithEmptyAnnotationsAreRemovedFromTheProfiles() {
        var result = new BaselineExperimentProfilesList();

        var p1 = new BaselineExperimentProfile(MockExperiment.createBaselineExperiment(), new FactorSet());
        p1.add(
                new FactorAcrossExperiments(
                        new Factor(factorHeader, generateBlankString())), new BaselineExpression(1.0));
        p1.add(new FactorAcrossExperiments(new Factor(factorHeader, "v2")), new BaselineExpression(2.0));
        p1.add(new FactorAcrossExperiments(new Factor(factorHeader, "v3")), new BaselineExpression(0.0));
        result.add(p1);
        assertThat(result.getFactorsAcrossExperiments()).hasSize(2);
    }
}
