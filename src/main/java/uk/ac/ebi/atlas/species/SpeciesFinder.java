package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import uk.ac.ebi.atlas.search.SemanticQuery;

@Service
public interface SpeciesFinder {
    default ImmutableList<String> findSpecies(SemanticQuery geneQuery, SemanticQuery conditionQuery) {
        return ImmutableList.of();
    }
}
