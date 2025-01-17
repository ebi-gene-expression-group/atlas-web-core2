package uk.ac.ebi.atlas.bioentity.properties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesignDao;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;
import uk.ac.ebi.atlas.species.Species;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.DESCRIPTION;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.DESIGN_ELEMENT;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.GO;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.PO;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.SYMBOL;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

@Profile("!cli")
@Component
public class BioEntityCardModelFactory {
    // These are displayed in the header, so we don’t show them in the card table
    private static final ImmutableList<BioentityPropertyName> SKIP_PROPERTIES = ImmutableList.of(DESCRIPTION, SYMBOL);

    private final ArrayDesignDao arrayDesignDao;
    private final BioEntityPropertyService bioEntityPropertyService;

    public BioEntityCardModelFactory(BioEntityPropertyService bioEntityPropertyService,
                                     ArrayDesignDao arrayDesignDao) {
        this.arrayDesignDao = arrayDesignDao;
        this.bioEntityPropertyService = bioEntityPropertyService;
    }

    public Map<String, Object> modelAttributes(String identifier,
                                               Species species,
                                               List<BioentityPropertyName> orderedPropertyNames,
                                               String entityName,
                                               Map<BioentityPropertyName,Set<String>> propertyValuesByType) {

        addDesignElements(identifier, propertyValuesByType);
        var result = new HashMap<String, Object>();

        result.put("entityBriefName",
                StringUtils.isEmpty(entityName) ?
                        identifier :
                        entityName);

        result.put("entityFullName",
                StringUtils.isEmpty(entityName) ?
                        identifier :
                        MessageFormat.format("{0} ({1})", identifier, entityName));

        result.put("bioEntityDescription", getBioEntityDescription(propertyValuesByType));

        result.put("propertyNames", propertiesWeWillDisplay(orderedPropertyNames, propertyValuesByType));

        result.put("bioentityProperties",
                GSON.toJson(bioentityProperties(identifier, species, orderedPropertyNames, propertyValuesByType)));

        return result;
    }

    private List<BioentityPropertyName> propertiesWeWillDisplay(
            List<BioentityPropertyName> desiredOrderOfPropertyNames,
            final Map<BioentityPropertyName, Set<String>> propertyValuesByType) {

        return desiredOrderOfPropertyNames.stream()
                .filter(propertyName ->
                        !SKIP_PROPERTIES.contains(propertyName) &&
                        propertyValuesByType.containsKey(propertyName))
                .collect(toList());
    }

    JsonArray bioentityProperties(String identifier,
                                  Species species,
                                  List<BioentityPropertyName> desiredOrderOfPropertyNames,
                                  Map<BioentityPropertyName, Set<String>> propertyValuesByType) {
        var result = new JsonArray();

        for (var bioentityPropertyName : propertiesWeWillDisplay(desiredOrderOfPropertyNames, propertyValuesByType)) {
            var values = new JsonArray();

            for (var propertyLink :
                    createLinks(
                            identifier,
                            bioentityPropertyName,
                            propertyValuesByType.get(bioentityPropertyName),
                            species)) {
                values.add(propertyLink.toJson());
            }

            if (values.size() > 0) {
                var jsonValues = new JsonObject();
                jsonValues.addProperty("type", bioentityPropertyName.name);
                jsonValues.addProperty("name", bioentityPropertyName.label);
                jsonValues.add("values", values);
                result.add(jsonValues);
            }
        }

        return result;
    }

    private void addDesignElements(String identifier, Map<BioentityPropertyName, Set<String>> propertyValuesByType) {
        var designElements = ImmutableSet.copyOf(arrayDesignDao.getDesignElements(identifier));

        if (!designElements.isEmpty()) {
            propertyValuesByType.put(DESIGN_ELEMENT, designElements);
        }
    }

    private String getBioEntityDescription(Map<BioentityPropertyName, Set<String>> propertyValuesByType) {
        var firstValueOfDescription =
                propertyValuesByType.getOrDefault(DESCRIPTION, ImmutableSet.of("")).iterator().next();
        return StringUtils.substringBefore(firstValueOfDescription, "[Source").trim();
    }

    private List<PropertyLink> createLinks(String identifier,
                                           BioentityPropertyName propertyName,
                                           Collection<String> propertyValues,
                                           Species species) {

        return bioEntityPropertyService
                .mapToLinkText(propertyName, propertyValues, species.isPlant()).entrySet().stream().map(
                        linkWithText ->
                                createLink(
                                        linkWithText.getValue(),
                                        identifier,
                                        propertyName,
                                        linkWithText.getKey(),
                                        species,
                                        bioEntityPropertyService.assessRelevance(propertyName, linkWithText.getKey())))
                .sorted(comparing(PropertyLink::getRelevance).reversed())
                .collect(toList());
    }

    private PropertyLink createLink(String text,
                                    String identifier,
                                    BioentityPropertyName propertyName,
                                    String propertyValue,
                                    Species species,
                                    int relevance) {
        return new PropertyLink(
                text,
                // identifier is only used in ENSFAMILY_DESCRIPTION as parameter {1}
                MessageFormat.format(
                        BioEntityCardProperties.getUrlTemplate(propertyName, species),
                        getEncodedString(propertyName, propertyValue),
                        identifier),
                relevance);
    }

    private String getEncodedString(BioentityPropertyName propertyName, String propertyValue) {
        return
                URLEncoder.encode(
                        propertyName == GO || propertyName == PO ?
                                propertyValue.replaceAll(":", "_") :
                                propertyValue,
                        StandardCharsets.UTF_8);
    }
}
