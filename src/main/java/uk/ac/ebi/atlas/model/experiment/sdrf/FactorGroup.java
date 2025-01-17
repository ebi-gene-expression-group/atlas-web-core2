package uk.ac.ebi.atlas.model.experiment.sdrf;

import java.util.Collection;
import java.util.List;

// A group of factors. Will be associated with an assay group.
// Backed by a map of type -> factor, not meant to be mutated after creation.

public interface FactorGroup extends Iterable<Factor>, Comparable<FactorGroup> {
    Factor factorOfType(String type);
    List<Factor> without(Collection<Factor> factors);
    FactorGroup withoutTypes(Collection<String> factorTypes);
    int size();
    boolean isEmpty();
    boolean containsOnlyOrganism();
}
