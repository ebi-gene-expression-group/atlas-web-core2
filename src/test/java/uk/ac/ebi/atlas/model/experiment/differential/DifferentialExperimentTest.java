package uk.ac.ebi.atlas.model.experiment.differential;

import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.DifferentialExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomContrasts;

class DifferentialExperimentTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Test
    void openTargetsCttvPrimaryAnnotations() {
        List<Contrast> contrasts = generateRandomContrasts(RNG.nextInt(1, 10), false);
        ImmutableList<Boolean> cttvPrimaryAnnotations =
                contrasts.stream()
                        .map(__ -> RNG.nextBoolean())
                        .collect(toImmutableList());

        DifferentialExperiment subject =
                new DifferentialExperimentBuilder()
                        .withSamples(contrasts)
                        .withCttvAnnotations(cttvPrimaryAnnotations)
                        .build();

        int contrastIndex = RNG.nextInt(contrasts.size());
        assertThat(subject.doesContrastHaveCttvPrimaryAnnotation(contrasts.get(contrastIndex)))
                .isEqualTo(cttvPrimaryAnnotations.get(contrastIndex));
    }

    @Test
    void propertiesForAssay() {
        ImmutableList<Contrast> contrasts = generateRandomContrasts(RNG.nextInt(1, 10), false);
        DifferentialExperiment subject = new DifferentialExperimentBuilder().withSamples(contrasts).build();

        ImmutableList<String> referenceAssayIds = contrasts.stream()
                .flatMap(contrast -> contrast.getReferenceAssayGroup().getAssayIds().stream())
                .collect(toImmutableList());
        String referenceAssayId = referenceAssayIds.get(RNG.nextInt(referenceAssayIds.size()));

        ImmutableList<String> testAssayIds = contrasts.stream()
                .flatMap(contrast -> contrast.getTestAssayGroup().getAssayIds().stream())
                .collect(toImmutableList());
        String testAssayId = testAssayIds.get(RNG.nextInt(testAssayIds.size()));

        ReadContext referenceCtx = JsonPath.parse(subject.propertiesForAssay(referenceAssayId).toString());
        assertThat(contrasts)
                .anyMatch(contrast ->
                        referenceCtx.<List<String>>read("$[*].contrastName").contains(contrast.getDisplayName()) &&
                        referenceCtx.<List<String>>read("$[*].referenceOrTest").contains("reference"));


        ReadContext testCtx = JsonPath.parse(subject.propertiesForAssay(testAssayId).toString());
        assertThat(contrasts)
                .anyMatch(contrast ->
                        testCtx.<List<String>>read("$[*].contrastName").contains(contrast.getDisplayName()) &&
                        testCtx.<List<String>>read("$[*].referenceOrTest").contains("test"));

        ReadContext fooCtx = JsonPath.parse(subject.propertiesForAssay(randomAlphabetic(10)).toString());
        assertThat(fooCtx.<List<String>>read("$[*].contrastName"))
                .containsOnly("none");
        assertThat(fooCtx.<List<String>>read("$[*].referenceOrTest"))
                .containsOnly("");
    }

    @Test
    void throwIfContrastsContainTheSameComparisonWithReferenceAndTestReversed() {
        List<Contrast> contrasts = generateRandomContrasts(RNG.nextInt(1, 10), false);
        Contrast anyContrast = contrasts.get(RNG.nextInt(contrasts.size()));

        List<Contrast> invalidContrasts =
                ImmutableList.<Contrast>builder()
                        .addAll(contrasts)
                        .add(new Contrast(
                                anyContrast.getTestAssayGroup() + "_" + anyContrast.getReferenceAssayGroup(),
                                "‘" + randomAlphanumeric(5,15) + "’ vs ‘" + randomAlphanumeric(5, 15) + "’",
                                anyContrast.getTestAssayGroup(),
                                anyContrast.getReferenceAssayGroup(),
                                anyContrast.getArrayDesignAccession()))
                        .build();

        assertThatIllegalArgumentException().isThrownBy(
                () -> new DifferentialExperimentBuilder().withSamples(invalidContrasts).build());
    }

//    @Test
//    void cttvAnnotations() {
//        List<Contrast> contrasts = generateRandomContrasts(RNG.nextInt(1, 10), RNG.nextBoolean());
//        DifferentialExperiment subject = new DifferentialExperimentBuilder().withSamples(contrasts).build();
//
//        assertThat(contrasts)
//                .allMatch(contrast -> subject.doesContrastHaveCttvPrimaryAnnotation(contrast));
//    }
}
