package uk.ac.ebi.atlas.bioentity.properties;

import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ebi.atlas.bioentity.go.GoPoTrader;
import uk.ac.ebi.atlas.bioentity.interpro.InterProTrader;
import uk.ac.ebi.atlas.model.OntologyTerm;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;
import uk.ac.ebi.atlas.species.SpeciesInferrer;
import uk.ac.ebi.atlas.utils.ReactomeClient;

import java.util.Collection;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.text.WordUtils.capitalize;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.GO;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.PO;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.SYMBOL;

@Profile("!cli")
@Service
public class BioEntityPropertyService {
    private final SpeciesInferrer speciesInferrer;
    private final BioEntityPropertyDao bioEntityPropertyDao;
    private final ReactomeClient reactomeClient;
    private final GoPoTrader goPoTermTrader;
    private final InterProTrader interProTermTrader;

    public BioEntityPropertyService(SpeciesInferrer speciesInferrer,
                                    BioEntityPropertyDao bioEntityPropertyDao,
                                    ReactomeClient reactomeClient,
                                    GoPoTrader goPoTermTrader,
                                    InterProTrader interProTermTrader) {

        this.speciesInferrer = speciesInferrer;
        this.bioEntityPropertyDao = bioEntityPropertyDao;
        this.reactomeClient = reactomeClient;
        this.goPoTermTrader = goPoTermTrader;
        this.interProTermTrader = interProTermTrader;
    }

    Map<String, String> mapToLinkText(BioentityPropertyName propertyName,
                                      Collection<String> propertyValues,
                                      boolean isPlantSpecies) {
        switch (propertyName) {
            case ORTHOLOG:
                return propertyValues.stream()
                        .collect(toMap(identity(), this::fetchSymbolAndSpeciesForOrtholog));
            case PATHWAYID:
                return isPlantSpecies ?
                        reactomeClient.getPlantPathwayNames(propertyValues) :
                        reactomeClient.getPathwayNames(propertyValues);
            case GO: case PO:
                return propertyValues.stream()
                        .collect(toMap(identity(),
                                       p -> goPoTermTrader.get(p).map(OntologyTerm::name).orElse(p)));
            case INTERPRO:
                return propertyValues.stream()
                        .collect(toMap(identity(),
                                       p -> interProTermTrader.get(p).map(OntologyTerm::name).orElse(p)));
            default:
                return propertyValues.stream().collect(toMap(identity(), identity()));
        }
    }

    int assessRelevance(BioentityPropertyName bioentityPropertyName, String propertyValue) {
        if (ImmutableList.of(GO, PO).contains(bioentityPropertyName)) {
            return goPoTermTrader.get(propertyValue).map(OntologyTerm::depth).orElse(0);
        } else {
            return 0;
        }
    }

    private String fetchSymbolAndSpeciesForOrtholog(String identifier) {
        var species = speciesInferrer.inferSpeciesForGeneQuery(SemanticQuery.create(identifier));

        if (species.isUnknown()) {
            return identifier;
        }

        var identifierSymbols = bioEntityPropertyDao.fetchPropertyValuesForGeneId(identifier, SYMBOL);

        var speciesToken = " (" + capitalize(species.getName(), new char[0]) + ")";
        if (!identifierSymbols.isEmpty()) {
            return identifierSymbols.iterator().next() + speciesToken;
        }

        return identifier + speciesToken;
    }
}
