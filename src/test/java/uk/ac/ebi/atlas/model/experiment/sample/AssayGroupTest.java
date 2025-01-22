package uk.ac.ebi.atlas.model.experiment.sample;

import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayGroupId;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomBiologicalReplicates;

class AssayGroupTest {
    private static final int MAX_REPLICATE_COUNT = 10;

    @Test
    void firstAssayAccession() {
        ImmutableSet<BiologicalReplicate> biologicalReplicates = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        AssayGroup assayGroup = new AssayGroup(generateRandomAssayGroupId(), biologicalReplicates);

        assertThat(assayGroup)
                .hasFieldOrPropertyWithValue(
                        "firstAssayId",
                        biologicalReplicates.iterator().next().getAssayIds().iterator().next());
    }

    @Test
    void toJson() {
        ImmutableSet<BiologicalReplicate> biologicalReplicates = generateRandomBiologicalReplicates(1, MAX_REPLICATE_COUNT);
        AssayGroup assayGroup = new AssayGroup(generateRandomAssayGroupId(), biologicalReplicates);

        ReadContext ctx = JsonPath.parse(assayGroup.toJson().toString());

        assertThat(ctx.<String>read("$.id"))
                .isEqualTo(assayGroup.getId());

        assertThat(ctx.<List<String>>read("$.assayAccessions"))
                .containsAll(assayGroup.getAssayIds());

        assertThat(ctx.<Integer>read("$.replicates"))
                .isEqualTo(assayGroup.getAssays().size());
    }
}