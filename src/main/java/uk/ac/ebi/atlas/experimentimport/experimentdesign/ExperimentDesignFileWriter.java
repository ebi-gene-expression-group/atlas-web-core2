package uk.ac.ebi.atlas.experimentimport.experimentdesign;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import uk.ac.ebi.atlas.commons.writers.TsvWriter;
import uk.ac.ebi.atlas.model.OntologyTerm;
import uk.ac.ebi.atlas.model.experiment.sdrf.SampleCharacteristic;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.removeEnd;

public class ExperimentDesignFileWriter {
    private static final String ONTOLOGY_TERM_DELIMITER = " ";
    private static final String SAMPLE_CHARACTERISTICS_NAME_HEADER_TEMPLATE =
            "Sample Characteristic[{0}]";
    private static final String SAMPLE_CHARACTERISTICS_ONTOLOGY_TERM_HEADER_TEMPLATE =
            "Sample Characteristic Ontology Term[{0}]";
    private static final String FACTOR_NAME_HEADER_TEMPLATE = "Factor Value[{0}]";
    private static final String FACTOR_VALUE_ONTOLOGY_TERM_TEMPLATE = "Factor Value Ontology Term[{0}]";

    private final TsvWriter tsvWriter;
    private final ExperimentType experimentType;

    public ExperimentDesignFileWriter(TsvWriter tsvWriter, ExperimentType experimentType) {
        this.tsvWriter = tsvWriter;
        this.experimentType = experimentType;
    }

    public void write(ExperimentDesign experimentDesign) throws IOException {
        var columnHeaders = buildColumnHeaders(experimentType, experimentDesign);
        tsvWriter.writeNext(columnHeaders);
        tsvWriter.writeAll(asTableOntologyTermsData(experimentDesign));
        tsvWriter.close();
    }

    String[] buildColumnHeaders(ExperimentType type, ExperimentDesign experimentDesign) {
        var headers = Lists.newArrayList(getCommonColumnHeaders(type));
        headers.addAll(
                toHeaders(
                        experimentDesign.getSampleCharacteristicHeaders(),
                        SAMPLE_CHARACTERISTICS_NAME_HEADER_TEMPLATE,
                        SAMPLE_CHARACTERISTICS_ONTOLOGY_TERM_HEADER_TEMPLATE));
        headers.addAll(toHeaders(
                experimentDesign.getFactorHeaders(),
                FACTOR_NAME_HEADER_TEMPLATE, FACTOR_VALUE_ONTOLOGY_TERM_TEMPLATE));

        return headers.toArray(new String[0]);
    }

    private List<String> toHeaders(Set<String> propertyNames,
                                   final String headerTemplate1,
                                   final String headerTemplate2) {
        var headers = new ArrayList<String>();
        for (String propertyName: propertyNames) {
            headers.add(MessageFormat.format(headerTemplate1, propertyName));
            headers.add(MessageFormat.format(headerTemplate2, propertyName));
        }
        return headers;
    }

    private List<String> getCommonColumnHeaders(ExperimentType type) {
        switch (type) {
            case MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_2COLOUR_MRNA_DIFFERENTIAL:
            case MICROARRAY_1COLOUR_MICRORNA_DIFFERENTIAL:
                return Lists.newArrayList("Assay", "Array");
            case RNASEQ_MRNA_BASELINE:
            case RNASEQ_MRNA_DIFFERENTIAL:
            case PROTEOMICS_BASELINE:
            case PROTEOMICS_DIFFERENTIAL:
            case PROTEOMICS_BASELINE_DIA:
                return Lists.newArrayList("Run");
            case SINGLE_CELL_RNASEQ_MRNA_BASELINE:
            case SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE:
                return Lists.newArrayList("Assay");
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

    List<String[]> asTableOntologyTermsData(ExperimentDesign experimentDesign) {
        var tableData = Lists.<String[]>newArrayList();
        tableData.addAll(
                experimentDesign.getAllRunOrAssay().stream()
                        .map(runOrAssay -> composeTableRowWithOntologyTerms(experimentDesign, runOrAssay))
                        .collect(Collectors.toList()));
        return tableData;
    }

    private String[] composeTableRowWithOntologyTerms(ExperimentDesign experimentDesign, String runOrAssay) {
        var row = Lists.newArrayList(runOrAssay);

        var arrayDesign = experimentDesign.getArrayDesign(runOrAssay);
        if (!Strings.isNullOrEmpty(arrayDesign)) {
            row.add(arrayDesign);
        }

        for (var sampleHeader : experimentDesign.getSampleCharacteristicHeaders()) {
            var sampleCharacteristic = experimentDesign.getSampleCharacteristic(runOrAssay, sampleHeader);
            addSampleCharacteristicValue(row, sampleCharacteristic);
            addSampleCharacteristicOntologyTerm(row, sampleCharacteristic);
        }

        for (var factorHeader : experimentDesign.getFactorHeaders()) {
            var factor = experimentDesign.getFactor(runOrAssay, factorHeader);
            addFactorValue(row, factor);
            addFactorValueOntologyTerm(row, factor);
        }

        return row.toArray(new String[0]);
    }

    private void addFactorValue(List<String> row, Factor factor) {
        var factorValue = (factor == null) ? null : factor.getValue();
        row.add(factorValue);
    }

    private void addFactorValueOntologyTerm(List<String> row, Factor factor) {
        var factorValueOntologyTermId = (factor == null || factor.getValueOntologyTerms().isEmpty()) ?
                null :
                joinURIs(factor.getValueOntologyTerms());
        row.add(factorValueOntologyTermId);
    }

    private void addSampleCharacteristicValue(List<String> row, SampleCharacteristic sampleCharacteristic) {
        var value = (sampleCharacteristic == null) ? null : sampleCharacteristic.getValue();
        row.add(value);
    }

    private void addSampleCharacteristicOntologyTerm(List<String> row, SampleCharacteristic sampleCharacteristic) {
        var ontologyTermId =
                (sampleCharacteristic == null || sampleCharacteristic.getValueOntologyTerms().isEmpty()) ?
                        null :
                        joinURIs(sampleCharacteristic.getValueOntologyTerms());
        row.add(ontologyTermId);
    }

    private static String joinURIs(Set<OntologyTerm> ontologyTerms) {
        var sb = new StringBuilder();
        for (var ontologyTerm : ontologyTerms) {
            sb.append(ontologyTerm.uri()).append(ONTOLOGY_TERM_DELIMITER);
        }

        return removeEnd(sb.toString(), ONTOLOGY_TERM_DELIMITER);
    }
}
