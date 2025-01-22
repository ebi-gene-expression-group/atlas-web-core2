package uk.ac.ebi.atlas.testutils;

import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sample.BiologicalReplicate;

import java.util.Arrays;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class AssayGroupFactory {
    public AssayGroupFactory() {
        throw new UnsupportedOperationException();
    }

    public static AssayGroup create(String id, String... assayAccessions) {
        return new AssayGroup(
                id,
                Arrays.stream(assayAccessions)
                        .map(BiologicalReplicate::create)
                        .collect(toImmutableSet()));
    }
}
