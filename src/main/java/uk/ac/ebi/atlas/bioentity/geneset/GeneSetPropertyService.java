package uk.ac.ebi.atlas.bioentity.geneset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.bioentity.go.GoPoTrader;
import uk.ac.ebi.atlas.bioentity.interpro.InterProTrader;
import uk.ac.ebi.atlas.model.OntologyTerm;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;
import uk.ac.ebi.atlas.utils.ReactomeClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.DESCRIPTION;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.GO;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.INTERPRO;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.PATHWAYID;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.PO;

@Profile("!cli")
@Component
public class GeneSetPropertyService {
    private final GoPoTrader goPoTermTrader;
    private final InterProTrader interProTermTrader;
    private final ReactomeClient reactomeClient;

    public GeneSetPropertyService(GoPoTrader goPoTermTrader,
                                  InterProTrader interProTermTrader,
                                  ReactomeClient reactomeClient) {
        this.goPoTermTrader = goPoTermTrader;
        this.interProTermTrader = interProTermTrader;
        this.reactomeClient = reactomeClient;
    }

    public static final List<BioentityPropertyName> ALL =
            ImmutableList.of(PATHWAYID, GO, PO, INTERPRO);

    public Map<BioentityPropertyName, Set<String>> propertyValuesByType(String identifier, boolean isPlant) {
        identifier = identifier.toUpperCase();

        if (GeneSetUtil.matchesReactomeID(identifier)) {
            if (isPlant) {
                return propertyValuesByType(
                        PATHWAYID, identifier, reactomeClient.getPlantPathwayName(identifier).orElse(""));
            } else {
                return propertyValuesByType(
                        PATHWAYID, identifier, reactomeClient.getPathwayName(identifier).orElse(""));
            }
        } else if (GeneSetUtil.matchesGeneOntologyAccession(identifier)) {
            return propertyValuesByType(
                    GO, identifier, goPoTermTrader.get(identifier).map(OntologyTerm::name).orElse(""));
        } else if (GeneSetUtil.matchesPlantOntologyAccession(identifier)) {
            return propertyValuesByType(
                    PO, identifier, goPoTermTrader.get(identifier).map(OntologyTerm::name).orElse(""));
        } else if (GeneSetUtil.matchesInterProAccession(identifier)) {
            return propertyValuesByType(
                    INTERPRO, identifier, interProTermTrader.get(identifier).map(OntologyTerm::name).orElse(""));
        } else {
            return ImmutableMap.of();
        }
    }

    private Map<BioentityPropertyName, Set<String>> propertyValuesByType(BioentityPropertyName which,
                                                                         String identifier, String value) {
        return ImmutableMap.of(which, ImmutableSet.of(identifier), DESCRIPTION, ImmutableSet.of(value));
    }
}
