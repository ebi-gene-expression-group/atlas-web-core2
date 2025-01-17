package uk.ac.ebi.atlas.model.experiment;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ebi.atlas.commons.readers.XmlReader;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sample.BiologicalReplicate;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class ExperimentConfiguration {
    private static final String EXPERIMENT_TYPE = "experimentType";

    private XmlReader xmlReader;
    private XPath xpath = XPathFactory.newInstance().newXPath();

    public ExperimentConfiguration(XmlReader xmlReader) {
        this.xmlReader = xmlReader;
    }

    public List<Pair<Contrast, Boolean>> getContrastAndAnnotationPairs() {
        List<Pair<Contrast, Boolean>> contrasts = new ArrayList<>();

        NodeList arrayDesigns = xmlReader.getDocument().getElementsByTagName("array_design");
        for (int i = 0; i < arrayDesigns.getLength(); i++) {
            Node currentArrayDesign = arrayDesigns.item(i);
            String arrayDesignAccession = currentArrayDesign.getFirstChild().getTextContent().trim();
            parseContrastConfiguration(
                    "analytics[" + (i + 1) + "]/contrasts/contrast/@id", arrayDesignAccession, contrasts);
        }

        // in case no array designs (case of RNA-seq)
        if (arrayDesigns.getLength() == 0) {
            parseContrastConfiguration("analytics/contrasts/contrast/@id", null, contrasts);
        }

        return contrasts;
    }

    public List<Contrast> getContrasts() {
        return getContrastAndAnnotationPairs().stream().map(Pair::getLeft).collect(Collectors.toList());
    }

    private void parseContrastConfiguration(
            @NotNull String query,
            @Nullable String arrayDesignAccession,
            @NotNull List<@NotNull Pair<@NotNull Contrast, @NotNull Boolean>> contrasts) {
        String[] ids = xmlReader.getStringArray(query);
        for (String id : ids) {
            contrasts.add(getContrastAndCttvPrimaryPair(id, arrayDesignAccession));
        }
    }

    private Pair<Contrast, Boolean> getContrastAndCttvPrimaryPair(@NotNull String id,
                                                                  @Nullable String arrayDesignAccession) {
        Configuration configuration = xmlReader.configurationAt("analytics/contrasts/contrast[@id=\'" + id + "\']");
        String name = configuration.getString("name");
        String reference = configuration.getString("reference_assay_group");
        String test = configuration.getString("test_assay_group");
        Validate.noNullElements(
                new String[]{name, reference, test},
                MessageFormat.format("Contrast id {0} requires: name, reference assay group, test assay group", id));

        return Pair.of(
                new Contrast(id, name, getAssayGroup(reference), getAssayGroup(test), arrayDesignAccession),
                Integer.valueOf(1).equals(configuration.getInt("@cttv_primary", -1)));
    }

    private AssayGroup getAssayGroup(String id) {
        try {
            XPathExpression expr =
                    xpath.compile("/configuration/analytics/assay_groups/assay_group[@id='" + id + "']/assay");

            NodeList nl = (NodeList) expr.evaluate(xmlReader.getDocument(), XPathConstants.NODESET);

            ImmutableSet.Builder<BiologicalReplicate> biologicalReplicates = ImmutableSet.builder();

            Multimap<String, String> technicalReplicatesPerId = LinkedListMultimap.create();

            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                String technicalReplicateId = getAttribute(node, "technical_replicate_id");
                if (technicalReplicateId != null) {
                    technicalReplicatesPerId.put(technicalReplicateId, node.getTextContent());
                } else {
                    biologicalReplicates.add(BiologicalReplicate.create(node.getTextContent()));
                }
            }

            technicalReplicatesPerId.asMap().forEach((biologicalReplicateId, assays) ->
               biologicalReplicates.add(assays.size() > 1 ?
                       BiologicalReplicate.create(biologicalReplicateId, ImmutableSet.copyOf(assays)) :
                       BiologicalReplicate.create(assays.iterator().next())));

            return new AssayGroup(id, biologicalReplicates.build());

        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Problem parsing configuration file.", e);
        }
    }

    @Nullable
    private String getAttribute(Node node, String name) {
        Node attribute = node.getAttributes().getNamedItem(name);
        return (attribute == null) ? null : attribute.getNodeValue();
    }

    public List<AssayGroup> getAssayGroups() {
        ImmutableList.Builder<AssayGroup> b = ImmutableList.builder();

        for (String assayGroupId : xmlReader.getStringArray("/analytics/assay_groups/assay_group/@id")) {
            b.add(getAssayGroup(assayGroupId));
        }

        return b.build();
    }

    public ExperimentType getExperimentType() {
        Element configuration = xmlReader.getDocument().getDocumentElement();
        String type = configuration.getAttribute(EXPERIMENT_TYPE);

        if (StringUtils.isEmpty(type)) {
            throw new IllegalArgumentException(String.format("Missing %s attribute on root element", EXPERIMENT_TYPE));
        }

        ExperimentType experimentType = ExperimentType.get(type);
        if (experimentType == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "Unknown %s attribute: \"%s\". Must be one of: [%s]",
                            EXPERIMENT_TYPE, type, Joiner.on(", ").join(EnumSet.allOf(ExperimentType.class))));
        }

        return experimentType;
    }

    @NotNull
    public ImmutableSortedSet<String> getArrayDesignAccessions() {
        return ImmutableSortedSet.copyOf(xmlReader.getList("analytics/array_design"));
    }
}
