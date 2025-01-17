package uk.ac.ebi.atlas.profiles;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ebi.atlas.experimentpage.context.BulkDifferentialRequestContext;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.DifferentialExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExpression;
import uk.ac.ebi.atlas.model.experiment.differential.rnaseq.BulkDifferentialProfile;
import uk.ac.ebi.atlas.testutils.AssayGroupFactory;
import uk.ac.ebi.atlas.web.DifferentialRequestPreferences;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProfileStreamFilterTest {
    private static final AssayGroup G1 = AssayGroupFactory.create("g1", "assay_1");
    private static final AssayGroup G2 = AssayGroupFactory.create("g2", "assay_2");
    private static final AssayGroup G3 = AssayGroupFactory.create("g3", "assay_3");

    private static final Contrast G1_G2 = new Contrast("g1_g2", "contrast 1", G1, G2, null);
    private static final Contrast G1_G3 = new Contrast("g1_g3", "contrast 2", G1, G3, null);

    private static final DifferentialExperiment EXPERIMENT =
            new DifferentialExperimentBuilder()
                    .withSamples(ImmutableList.of(G1_G2, G1_G3))
                    .build();

    private BulkDifferentialProfile profile(DifferentialExpression expressionForG1G2,
                                  DifferentialExpression expressionForG1G3) {
        BulkDifferentialProfile profile = new BulkDifferentialProfile("id", "name");
        Optional.ofNullable(expressionForG1G2).ifPresent(e -> profile.add(G1_G2, e));
        Optional.ofNullable(expressionForG1G3).ifPresent(e -> profile.add(G1_G3, e));
        return profile;
    }

    private void test(BulkDifferentialProfile profile,
                      DifferentialRequestPreferences differentialRequestPreferences,
                      boolean expected) {
        Predicate<BulkDifferentialProfile> f =
                ProfileStreamFilter.create(new BulkDifferentialRequestContext(differentialRequestPreferences, EXPERIMENT));
        assertThat(
                profile.toString(),
                f.test(profile),
                is(expected)
        );
    }

    private void test(BulkDifferentialProfile profile, boolean expected) {
        test(profile, new DifferentialRequestPreferences(), expected);
    }

    private void test(BulkDifferentialProfile profile, Collection<Contrast> keepContrasts, boolean expected) {
        DifferentialRequestPreferences differentialRequestPreferences = new DifferentialRequestPreferences();
        differentialRequestPreferences.setSelectedColumnIds(keepContrasts.stream()
                .map(ReportsGeneExpression::getId)
                .collect(Collectors.toSet()));
        test(profile, differentialRequestPreferences, expected);
    }

    @Test
    public void filterOutEmptyProfiles() {
        test(profile(null, null), false);
        test(profile(new DifferentialExpression(0.01, 0.0), null), false);
        test(profile(new DifferentialExpression(0.01, 0.0), new DifferentialExpression(0.03, 0.0)), false);

    }

    @Test
    public void keepProfileIfExpressedSomewhere() {
        test(profile(new DifferentialExpression(0.01, 2.0), null), true);
        test(profile(new DifferentialExpression(0.01, -2.0), null), true);
        test(profile(new DifferentialExpression(0.01, 2.0), new DifferentialExpression(0.03, 4.0)), true);
    }

    @Test
    public void keepProfileIfExpressedOnSpecifiedColumns() {
        test(profile(
                new DifferentialExpression(0.01, 2.0), new DifferentialExpression(0.03, 4.0)),
                ImmutableList.of(G1_G2, G1_G3), true);
        test(profile(
                new DifferentialExpression(0.01, 2.0), new DifferentialExpression(0.03, 4.0)),
                ImmutableList.of(G1_G2), true);
        test(profile(
                new DifferentialExpression(0.01, 2.0), new DifferentialExpression(0.03, 4.0)),
                ImmutableList.of(G1_G3), true);

        test(profile(new DifferentialExpression(0.01, 2.0), null), ImmutableList.of(G1_G2, G1_G3), true);
        test(profile(new DifferentialExpression(0.01, 2.0), null), ImmutableList.of(G1_G2), true);
        test(profile(new DifferentialExpression(0.01, 2.0), null), ImmutableList.of(G1_G3), false);
    }
}
