package uk.ac.ebi.atlas.model.experiment.baseline;

import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.BaselineExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayGroups;

class BaselineExperimentTest {
    private final static Random RNG = ThreadLocalRandom.current();
    
    @Test
    void factorsOfAnAssayGroupAreRetrievedFromFirstAssayOnly() {
        List<AssayGroup> assayGroups = generateRandomAssayGroups(10);

        // Find the first assay group that contains more than one assay
        AssayGroup multiAssayGroup = assayGroups.stream()
                .filter(assayGroup -> assayGroup.getAssayIds().size() > 1)
                .findFirst()
                .orElseThrow(RuntimeException::new);

        // Generate factors for each assay within that assay group
        // In a real experiment this would be a very bad idea, and we expect all assay IDs in an assay group to have
        // the same factors (which, in a way, is the definition of assay group!)
        List<Pair<String, Factor>> assayIdFactorPairs = multiAssayGroup.getAssayIds().stream()
                .map(assayId -> Pair.of(assayId, new Factor(randomAlphabetic(10), randomAlphabetic(10))))
                .collect(toImmutableList());

        ExperimentDesign experimentDesign = new ExperimentDesign();
        assayIdFactorPairs.forEach(pair ->
                experimentDesign.putFactor(pair.getLeft(), pair.getRight().getHeader(), pair.getRight().getValue()));

        Factor expected =
                assayIdFactorPairs.stream()
                        .filter(pair -> pair.getLeft().equals(multiAssayGroup.getFirstAssayId()))
                        .findFirst()
                        .orElseThrow(RuntimeException::new)
                        .getRight();

        BaselineExperiment subject =
                new BaselineExperimentBuilder()
                        .withSamples(assayGroups)
                        .withExperimentDesign(experimentDesign)
                        .withAssayId2Factor()
                        .build();

        assertThat(subject.getFactors(multiAssayGroup).iterator().next())
                .isEqualTo(expected);
    }

    @Test
    void propertiesForAssay() {
        ReadContext ctx;
        BaselineExperiment subject = new BaselineExperimentBuilder().build();

        ctx = JsonPath.parse(subject.propertiesForAssay(randomAlphabetic(10)).toString());
        assertThat(ctx.<List<Boolean>>read("$[*].analysed")).containsOnly(false);

        ImmutableList<String> experimentAssayIds =
                subject.getDataColumnDescriptors().stream()
                        .flatMap(assayGroup -> assayGroup.getAssayIds().stream())
                        .collect(toImmutableList());
        String anyExperimentAssayId = experimentAssayIds.get(RNG.nextInt(experimentAssayIds.size()));

        ctx = JsonPath.parse(subject.propertiesForAssay(anyExperimentAssayId).toString());
        assertThat(ctx.<List<Boolean>>read("$[*].analysed")).containsOnly(true);
    }
}
