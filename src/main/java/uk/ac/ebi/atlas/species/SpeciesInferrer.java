package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.solr.bioentities.query.SolrQueryService;

import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class SpeciesInferrer {
    private final SolrQueryService bioentitiesSearchService;
    private final SpeciesFactory speciesFactory;
    private final SpeciesFinder speciesFinder;

    public SpeciesInferrer(SolrQueryService bioentitiesSearchService,
                           SpeciesFactory speciesFactory,
                           SpeciesFinder speciesFinder) {
        this.bioentitiesSearchService = bioentitiesSearchService;
        this.speciesFactory = speciesFactory;
        this.speciesFinder = speciesFinder;
    }

    public Species inferSpecies(@NotNull SemanticQuery geneQuery,
                                @NotNull SemanticQuery conditionQuery,
                                @NotNull String speciesString) {
        if (isBlank(speciesString)) {
            return inferSpecies(geneQuery, conditionQuery);
        }

        return speciesFactory.create(speciesString);
    }

    public Species inferSpeciesForGeneQuery(@NotNull SemanticQuery geneQuery) {
        return inferSpecies(geneQuery, SemanticQuery.create());
    }

    public Species inferSpeciesForGeneQuery(@NotNull SemanticQuery geneQuery, @NotNull String speciesString) {
        return inferSpecies(geneQuery, SemanticQuery.create(), speciesString);
    }

    private Species inferSpecies(SemanticQuery geneQuery, SemanticQuery conditionQuery) {
        if (geneQuery == null || geneQuery.isEmpty() && conditionQuery.isEmpty()) {
            return speciesFactory.createUnknownSpecies();
        }

        // First try to guess the species from the query and the list of experiments we’ve got in Atlas
        var speciesCandidatesBuilder = ImmutableSet.<String>builder();
        speciesCandidatesBuilder.addAll(speciesFinder.findSpecies(geneQuery, conditionQuery));

        // If no candidate species have been found, try to guess from our bioentities collection
        if (conditionQuery.size() == 0 && speciesCandidatesBuilder.build().size() == 0) {
            speciesCandidatesBuilder.addAll(
                    geneQuery.terms().stream()
                            .map(bioentitiesSearchService::fetchSpecies)
                            .flatMap(Collection::stream)
                            .map(speciesFactory::create)
                            .map(Species::getReferenceName)
                            .collect(ImmutableSet.toImmutableSet()));
        }

        var speciesCandidates = speciesCandidatesBuilder.build();

        return speciesCandidates.size() == 1 ?
                speciesFactory.create(speciesCandidates.iterator().next()) :
                speciesFactory.createUnknownSpecies();
    }
}
