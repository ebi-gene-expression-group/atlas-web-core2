package uk.ac.ebi.atlas.trader;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.commons.readers.TsvStreamer;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.experimentimport.sdrf.SdrfParser;
import uk.ac.ebi.atlas.model.OntologyTerm;
import uk.ac.ebi.atlas.model.experiment.sdrf.SampleCharacteristic;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.resource.AtlasResource;
import uk.ac.ebi.atlas.resource.DataFileHub;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

@Component
public class ExperimentDesignParser {
    private static final String ONTOLOGY_TERM_DELIMITER = " ";

    static final Pattern SAMPLE_COLUMN_HEADER_PATTERN = Pattern.compile("\\s*Sample Characteristic\\[(.*?)]\\s*");
    private static final Pattern SAMPLE_ONTOLOGY_TERM_COLUMN_HEADER_PATTERN =
            Pattern.compile("\\s*Sample Characteristic Ontology Term\\[(.*?)]\\s*");

    private static final Pattern FACTOR_COLUMN_HEADER_PATTERN = Pattern.compile("\\s*Factor Value\\[(.*?)]\\s*");
    private static final Pattern FACTOR_VALUE_ONTOLOGY_TERM_COLUMN_HEADER_PATTERN =
            Pattern.compile("\\s*Factor Value Ontology Term\\[(.*?)]\\s*");

    private final DataFileHub dataFileHub;
    private final SdrfParser sdrfParser;

    ExperimentDesignParser(DataFileHub dataFileHub, SdrfParser sdrfParser) {
        this.dataFileHub = dataFileHub;
        this.sdrfParser = sdrfParser;
    }

    public ExperimentDesign parse(String experimentAccession) {

        AtlasResource<TsvStreamer> r = dataFileHub.getExperimentFiles(experimentAccession).experimentDesign;

        if (!r.exists()) {
            throw new ResourceNotFoundException(String.format("%s does not exist", r));
        }

        try (TsvStreamer tsvStreamer = r.get()) {
            Iterator<String[]> lineIterator = tsvStreamer.get().iterator();

            var experimentDesign = new ExperimentDesign();

            if (dataFileHub.getExperimentFiles(experimentAccession).sdrf.exists()) {
                var headers = sdrfParser.parseHeader(experimentAccession);
                experimentDesign.setOrderedSampleCharacteristicHeaders(headers.get("characteristics"));
                experimentDesign.setOrderedFactorHeaders(headers.get("factorvalue"));
            }

            if (lineIterator.hasNext()) {
                String[] headerLine = lineIterator.next();

                Map<String, Integer> sampleHeaderIndexes =
                        extractHeaderIndexes(headerLine, SAMPLE_COLUMN_HEADER_PATTERN);
                Map<String, Integer> sampleValueOntologyTermHeaderIndexes =
                        extractHeaderIndexes(headerLine, SAMPLE_ONTOLOGY_TERM_COLUMN_HEADER_PATTERN);

                Map<String, Integer> factorHeaderIndexes =
                        extractHeaderIndexes(headerLine, FACTOR_COLUMN_HEADER_PATTERN);
                Map<String, Integer> factorValueOntologyTermHeaderIndexes =
                        extractHeaderIndexes(headerLine, FACTOR_VALUE_ONTOLOGY_TERM_COLUMN_HEADER_PATTERN);

                int headersStartIndex =
                        headerLine.length - (sampleHeaderIndexes.size() + sampleValueOntologyTermHeaderIndexes.size() +
                                factorHeaderIndexes.size() + factorValueOntologyTermHeaderIndexes.size());

                for (var assayHeaderField : Arrays.copyOf(headerLine, headersStartIndex)) {
                    experimentDesign.addAssayHeader(assayHeaderField);
                }

                while (lineIterator.hasNext()) {
                    String[] line = lineIterator.next();

                    var runOrAssay = line[0];
                    if (headersStartIndex > 1) {
                        experimentDesign.putArrayDesign(runOrAssay, line[1]);
                    }

                    for (var sampleHeader : sampleHeaderIndexes.keySet()) {
                        var sampleValue = line[sampleHeaderIndexes.get(sampleHeader)];

                        var sampleValueOntologyTermIndex =
                                sampleValueOntologyTermHeaderIndexes.get(sampleHeader);
                        var sampleValueOntologyTerms =
                                createOntologyTerms(line, sampleValueOntologyTermIndex);
                        var sampleCharacteristic =
                                SampleCharacteristic.create(sampleHeader, sampleValue, sampleValueOntologyTerms);

                        experimentDesign.putSampleCharacteristic(runOrAssay, sampleHeader, sampleCharacteristic);
                    }

                    for (var factorHeader : factorHeaderIndexes.keySet()) {
                        var factorValue = line[factorHeaderIndexes.get(factorHeader)];

                        var factorValueOntologyTermIndex =
                                factorValueOntologyTermHeaderIndexes.get(factorHeader);
                        var factorValueOntologyTerms =
                                createOntologyTerms(line, factorValueOntologyTermIndex);

                        experimentDesign.putFactor(runOrAssay, factorHeader, factorValue, factorValueOntologyTerms);
                    }
                }
            }

            return experimentDesign;
        }
    }

    private OntologyTerm[] createOntologyTerms(String[] line, Integer ontologyTermIndex) {
        if (ontologyTermIndex == null || line[ontologyTermIndex].isEmpty()) {
            return new OntologyTerm[0];
        }

        ImmutableList.Builder<OntologyTerm> ontologyTermBuilder = new ImmutableList.Builder<>();
        var uriField = line[ontologyTermIndex];
        for (var uri : uriField.split(ONTOLOGY_TERM_DELIMITER)) {
            ontologyTermBuilder.add(OntologyTerm.createFromURI(uri));
        }
        var ontologyTermList = ontologyTermBuilder.build();

        return ontologyTermList.toArray(new OntologyTerm[0]);
    }

    private Map<String, Integer> extractHeaderIndexes(String[] columnHeaders, Pattern columnHeaderPattern) {
        Map<String, Integer> map = new TreeMap<>();
        for (var i = 0; i < columnHeaders.length; i++) {
            var matchingHeaderContent = extractMatchingContent(columnHeaders[i], columnHeaderPattern);
            if (matchingHeaderContent != null) {
                map.put(matchingHeaderContent, i);
            }
        }
        return map;
    }

    @Nullable
    static String extractMatchingContent(String string, Pattern pattern) {
        var matcher = pattern.matcher(string);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

}
