package uk.ac.ebi.atlas.experimentimport.idf;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.commons.readers.TsvStreamer;
import uk.ac.ebi.atlas.model.Publication;
import uk.ac.ebi.atlas.resource.DataFileHub;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.trimAllWhitespace;

@Component
public class IdfParser {
    private static final String INVESTIGATION_TITLE_ID = "Investigation Title";
    private static final String EXPERIMENT_DESCRIPTION_ID = "Experiment Description";
    private static final String PUBMED_ID = "PubMed ID";
    private static final String PUBLICATION_TITLE_ID = "Publication Title";
    private static final String PUBLICATION_DOI_ID = "Publication DOI";
    private static final String AE_EXPERIMENT_DISPLAY_NAME_ID = "Comment[AEExperimentDisplayName]";
    private static final String EXPECTED_CLUSTERS_ID = "Comment[EAExpectedClusters]";
    private static final String ADDITIONAL_ATTRIBUTES_ID = "Comment[EAAdditionalAttributes]";
    private static final String SECONDARY_ACCESSION_ID = "Comment[SecondaryAccession]";

    private static final Set<String> LINE_IDS = Stream.of(INVESTIGATION_TITLE_ID, EXPERIMENT_DESCRIPTION_ID, PUBMED_ID,
            PUBLICATION_TITLE_ID, PUBLICATION_DOI_ID, AE_EXPERIMENT_DISPLAY_NAME_ID, EXPECTED_CLUSTERS_ID,
            ADDITIONAL_ATTRIBUTES_ID, SECONDARY_ACCESSION_ID)
            .map(IdfParser::convertIdfFieldNameToKey)
            .collect(Collectors.toSet());

    private DataFileHub dataFileHub;

    private Map<String, List<String>> parsedIdf;

    public IdfParser(DataFileHub dataFileHub) {
        this.dataFileHub = dataFileHub;
    }

    public IdfParserOutput parse(String experimentAccession) {
        try (TsvStreamer idfStreamer = dataFileHub.getExperimentFiles(experimentAccession).idf.get()) {
            parsedIdf = idfStreamer.get()
                    .filter(line -> line.length > 1)
                    .filter(line -> LINE_IDS.contains(convertIdfFieldNameToKey(line[0])))
                    .collect(Collectors.toMap(
                            line -> convertIdfFieldNameToKey(line[0]),
                            line -> Arrays.stream(line)
                                    .skip(1)
                                    .filter(item -> !item.isEmpty())
                                    .collect(toList()),
                            (accumulatedValues, newValue) -> accumulatedValues));

            String title =
                    getParsedOutputByKey(
                            AE_EXPERIMENT_DISPLAY_NAME_ID,
                            getParsedOutputByKey(INVESTIGATION_TITLE_ID, emptyList()))
                    .stream()
                    .findFirst()
                    .orElse("");

            ImmutableSet<String> secondaryAccession = ImmutableSet.copyOf(getParsedOutputByKey(SECONDARY_ACCESSION_ID, emptyList()));

            String experimentDescription = getParsedOutputByKey(EXPERIMENT_DESCRIPTION_ID, emptyList())
                    .stream()
                    .findFirst()
                    .orElse("");

            List<Publication> publications = createListOfPublications(
                    getParsedOutputByKey(PUBMED_ID, emptyList()),
                    getParsedOutputByKey(PUBLICATION_TITLE_ID, emptyList()),
                    getParsedOutputByKey(PUBLICATION_DOI_ID, emptyList()));

            int expectedClusters =
                    NumberUtils.toInt(
                            getParsedOutputByKey(EXPECTED_CLUSTERS_ID, emptyList()).stream()
                                    .findFirst()
                                    .orElse(""),
                            0);

            List<String> metadataFieldsOfInterest =
                    getParsedOutputByKey(ADDITIONAL_ATTRIBUTES_ID, emptyList());

            return new IdfParserOutput(
                    title,
                    secondaryAccession,
                    experimentDescription,
                    publications,
                    expectedClusters,
                    metadataFieldsOfInterest
            );

        }
    }

    // For each publication title, retrieves corresponding PubMed ID and DOI (if they exist) and creates a list of
    // Publication objects
    private List<Publication> createListOfPublications(List<String> pubmedIds,
                                                       List<String> publicationTitles,
                                                       List<String> publicationDois) {
        return IntStream.range(0, publicationTitles.size()).boxed()
                .map(i ->
                        new Publication(
                                getPublicationInformation(i, pubmedIds),
                                getPublicationInformation(i, publicationDois),
                                publicationTitles.get(i)))
                .collect(toList());
    }

    private List<String> getParsedOutputByKey(String key, List<String> outputIfEmpty) {
        List<String> values = parsedIdf.getOrDefault(convertIdfFieldNameToKey(key), emptyList());

        if (values.isEmpty() || values.stream().allMatch(StringUtils::isBlank)) {
            return outputIfEmpty;
        }

        return values;
    }

    private String getPublicationInformation(int index, List<String> list) {
        return index < list.size() ? list.get(index) : null;
    }

    private static String convertIdfFieldNameToKey(String idfField) {
        return trimAllWhitespace(idfField).toUpperCase();
    }
}
