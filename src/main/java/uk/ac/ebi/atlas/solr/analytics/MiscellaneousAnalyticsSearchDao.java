package uk.ac.ebi.atlas.solr.analytics;

import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.solr.analytics.query.AnalyticsQueryClient;

@Component
public class MiscellaneousAnalyticsSearchDao {
    private final AnalyticsQueryClient analyticsQueryClient;

    public MiscellaneousAnalyticsSearchDao(AnalyticsQueryClient analyticsQueryClient) {
        this.analyticsQueryClient = analyticsQueryClient;
    }

    String fetchExperimentTypesInAnyField(SemanticQuery query) {
        return analyticsQueryClient.queryBuilder()
                        .experimentTypeFacets()
                        .queryIdentifierOrConditionsSearch(query)
                        .fetch();
    }

    String fetchExperimentTypes(SemanticQuery geneQuery, SemanticQuery conditionQuery, String species) {
        return analyticsQueryClient.queryBuilder()
                        .experimentTypeFacets()
                        .queryIdentifierSearch(geneQuery)
                        .queryConditionsSearch(conditionQuery)
                        .filterBaselineOrDifferentialExperiments()
                        .ofSpecies(species)
                        .fetch();
    }

    String searchBioentityIdentifiers(SemanticQuery geneQuery,
                                      SemanticQuery conditionQuery,
                                      String species,
                                      int facetLimit) {
        return analyticsQueryClient.queryBuilder()
                        .bioentityIdentifierFacets(facetLimit)
                        .queryIdentifierSearch(geneQuery)
                        .queryConditionsSearch(conditionQuery)
                        .ofSpecies(species)
                        .fetch();
    }

    String searchBioentityIdentifiersForTissuesInBaselineExperiments(SemanticQuery geneQuery) {
        return analyticsQueryClient.queryBuilder()
                        .bioentityIdentifierFacets(1)
                        .filterBaselineExperiments()
                        .queryIdentifierSearch(geneQuery)
                        .fetch();
    }

    String getSpecies(SemanticQuery geneQuery, SemanticQuery conditionQuery) {
        return analyticsQueryClient.queryBuilder()
                .speciesFacets()
                .queryIdentifierSearch(geneQuery)
                .queryConditionsSearch(conditionQuery)
                .fetch();
    }
}
