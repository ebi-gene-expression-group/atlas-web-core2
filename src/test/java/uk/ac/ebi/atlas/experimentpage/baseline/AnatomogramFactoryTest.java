package uk.ac.ebi.atlas.experimentpage.baseline;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.testutils.MockExperiment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayGroup;

class AnatomogramFactoryTest {
    @Test
    void anatomogramShowsUpWhenThereAreOrganismParts() {
        ExperimentDesign experimentDesign = new ExperimentDesign();

        List<AssayGroup> assayGroups =
                ImmutableList.of(
                        generateRandomAssayGroup(),
                        generateRandomAssayGroup(),
                        generateRandomAssayGroup(),
                        generateRandomAssayGroup());

        experimentDesign.putFactor(
                assayGroups.get(0).getFirstAssayId(), AnatomogramFactory.FACTOR_TYPE_WITH_ANATOMOGRAM, "liver");
        experimentDesign.putFactor(
                assayGroups.get(1).getFirstAssayId(), AnatomogramFactory.FACTOR_TYPE_WITH_ANATOMOGRAM, "liver");
        experimentDesign.putFactor(
                assayGroups.get(2).getFirstAssayId(), AnatomogramFactory.FACTOR_TYPE_WITH_ANATOMOGRAM, "heart");

        experimentDesign.putFactor(
                assayGroups.get(0).getFirstAssayId(), "other_type", "a");
        experimentDesign.putFactor(
                assayGroups.get(1).getFirstAssayId(), "other_type", "b");
        experimentDesign.putFactor(
                assayGroups.get(2).getFirstAssayId(), "other_type", "c");
        experimentDesign.putFactor(
                assayGroups.get(3).getFirstAssayId(), "other_type", "d");

        BaselineExperiment experiment = MockExperiment.createBaselineExperiment(experimentDesign, assayGroups);

        assertThat(new AnatomogramFactory().get(assayGroups.subList(0, 3), experiment))
                .isPresent();
        assertThat(new AnatomogramFactory().get(assayGroups.subList(0, 2), experiment))
                .isPresent();
        assertThat(new AnatomogramFactory().get(assayGroups.subList(1, 3), experiment))
                .isPresent();
        assertThat(new AnatomogramFactory().get(assayGroups.subList(1, 2), experiment))
                .isPresent();
        assertThat(new AnatomogramFactory().get(assayGroups.subList(2, 3), experiment))
                .isPresent();
        assertThat(new AnatomogramFactory().get(assayGroups.subList(3, 4), experiment))
                .isNotPresent();
        assertThat(new AnatomogramFactory().get(ImmutableList.of(), experiment))
                .isNotPresent();
    }
}
