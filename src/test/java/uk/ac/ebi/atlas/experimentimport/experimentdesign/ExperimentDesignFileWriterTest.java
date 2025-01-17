package uk.ac.ebi.atlas.experimentimport.experimentdesign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.commons.writers.TsvWriter;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.OntologyTerm;
import uk.ac.ebi.atlas.model.experiment.sdrf.SampleCharacteristic;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.PROTEOMICS_DIFFERENTIAL;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.RNASEQ_MRNA_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE;

@ExtendWith(MockitoExtension.class)
class ExperimentDesignFileWriterTest {
    private static final String ASSAY_1 = "ASSAY_1";
    private static final String ASSAY_2 = "ASSAY_2";
    private static final String ASSAY_3 = "ASSAY_3";
    private static final String CHARACTERISTIC_1_HEADER = "CHARACTERISTIC_1";
    private static final String CHARACTERISTIC_2_HEADER = "CHARACTERISTIC_2";
    private static final String CHARACTERISTIC_3_HEADER = "CHARACTERISTIC_3";
    private static final String FACTOR_HEADER = "FACTOR_1";
    private static final String FACTOR2_HEADER = "FACTOR_2";

    private static final String UBERON_1 = "UBERON:0000001";
    private static final String UBERON_2 = "UBERON:0000002";
    private static final String HTTP_OBO = "http://purl.obolibrary.org/obo/";
    private static final String HTTP_OBO_UBERON_1 = HTTP_OBO + UBERON_1;
    private static final String CHAR_1 = "CHAR_1";
    private static final String CHAR_2 = "CHAR_2";
    private static final String CHAR_3 = "CHAR_3";
    private static final String ASSAY_1_FACTOR_VALUE = "ASSAY1_FACTOR";
    private static final String ASSAY_2_FACTOR_VALUE = "ASSAY2_FACTOR";
    private static final String ASSAY_3_FACTOR_VALUE = "ASSAY3_FACTOR";
    private static final String ASSAY_1_FACTOR2_VALUE = "ASSAY1_FACTOR2";
    private static final String ASSAY_2_FACTOR2_VALUE = "ASSAY2_FACTOR2";
    private static final String ASSAY_3_FACTOR2_VALUE = "ASSAY3_FACTOR2";
    private static final String F_1 = "F:1";
    private static final String F_2 = "F:2";

    private static final OntologyTerm[] ABSENT_ONTOLOGY_TERMS = {};

    @Mock
    private TsvWriter tsvWriter;

    private ExperimentDesign experimentDesign;

    private ExperimentDesignFileWriter subject;

    @BeforeEach
    public void buildExperimentDesign() {
        subject = new ExperimentDesignFileWriter(tsvWriter, RNASEQ_MRNA_BASELINE);
        experimentDesign = new ExperimentDesign();

        var sampleCharacteristic1 =
                SampleCharacteristic.create("C1", CHAR_1, OntologyTerm.createFromURI(HTTP_OBO_UBERON_1));
        var sampleCharacteristic2 =
                SampleCharacteristic.create("C2", CHAR_2, OntologyTerm.createFromURI(UBERON_2));
        var sampleCharacteristic3 =
                SampleCharacteristic.create("C3", CHAR_3);

        experimentDesign.putFactor(
                ASSAY_1, FACTOR_HEADER, ASSAY_1_FACTOR_VALUE, OntologyTerm.create(F_1, "", HTTP_OBO));
        experimentDesign.putFactor(
                ASSAY_1, FACTOR2_HEADER, ASSAY_1_FACTOR2_VALUE, ABSENT_ONTOLOGY_TERMS);
        experimentDesign.putSampleCharacteristic(ASSAY_1, CHARACTERISTIC_1_HEADER, sampleCharacteristic1);
        experimentDesign.putSampleCharacteristic(ASSAY_1, CHARACTERISTIC_2_HEADER, sampleCharacteristic2);
        experimentDesign.putSampleCharacteristic(ASSAY_1, CHARACTERISTIC_3_HEADER, sampleCharacteristic3);

        experimentDesign.putFactor(
                ASSAY_2, FACTOR_HEADER, ASSAY_2_FACTOR_VALUE, OntologyTerm.create(F_2, "", ""));
        experimentDesign.putFactor(
                ASSAY_2, FACTOR2_HEADER, ASSAY_2_FACTOR2_VALUE, ABSENT_ONTOLOGY_TERMS);
        experimentDesign.putSampleCharacteristic(ASSAY_2, CHARACTERISTIC_1_HEADER, sampleCharacteristic1);
        experimentDesign.putSampleCharacteristic(ASSAY_2, CHARACTERISTIC_2_HEADER, sampleCharacteristic2);
        experimentDesign.putSampleCharacteristic(ASSAY_2, CHARACTERISTIC_3_HEADER, sampleCharacteristic3);

        experimentDesign.putFactor(
                ASSAY_3, FACTOR_HEADER, ASSAY_3_FACTOR_VALUE, ABSENT_ONTOLOGY_TERMS);
        experimentDesign.putFactor(
                ASSAY_3, FACTOR2_HEADER, ASSAY_3_FACTOR2_VALUE, ABSENT_ONTOLOGY_TERMS);
        experimentDesign.putSampleCharacteristic(ASSAY_3, CHARACTERISTIC_1_HEADER, sampleCharacteristic1);
        experimentDesign.putSampleCharacteristic(ASSAY_3, CHARACTERISTIC_2_HEADER, sampleCharacteristic2);
        experimentDesign.putSampleCharacteristic(ASSAY_3, CHARACTERISTIC_3_HEADER, sampleCharacteristic3);
    }

    @Test
    public void testHeaders() {
        var headers = subject.buildColumnHeaders(RNASEQ_MRNA_BASELINE, experimentDesign);

        assertThat(headers)
                .isEqualTo(
                        new String[] {
                                "Run",
                                "Sample Characteristic[CHARACTERISTIC_1]",
                                "Sample Characteristic Ontology Term[CHARACTERISTIC_1]",
                                "Sample Characteristic[CHARACTERISTIC_2]",
                                "Sample Characteristic Ontology Term[CHARACTERISTIC_2]",
                                "Sample Characteristic[CHARACTERISTIC_3]",
                                "Sample Characteristic Ontology Term[CHARACTERISTIC_3]",
                                "Factor Value[FACTOR_1]",
                                "Factor Value Ontology Term[FACTOR_1]",
                                "Factor Value[FACTOR_2]",
                                "Factor Value Ontology Term[FACTOR_2]"});
    }

    @Test
    public void testHeadersForProteomicsDifferential() {
        var headers = subject.buildColumnHeaders(PROTEOMICS_DIFFERENTIAL, experimentDesign);

        assertThat(headers)
                .isEqualTo(
                        new String[] {
                                "Run",
                                "Sample Characteristic[CHARACTERISTIC_1]",
                                "Sample Characteristic Ontology Term[CHARACTERISTIC_1]",
                                "Sample Characteristic[CHARACTERISTIC_2]",
                                "Sample Characteristic Ontology Term[CHARACTERISTIC_2]",
                                "Sample Characteristic[CHARACTERISTIC_3]",
                                "Sample Characteristic Ontology Term[CHARACTERISTIC_3]",
                                "Factor Value[FACTOR_1]",
                                "Factor Value Ontology Term[FACTOR_1]",
                                "Factor Value[FACTOR_2]",
                                "Factor Value Ontology Term[FACTOR_2]"});
    }

    @Test
    public void test() {
        var rows = subject.asTableOntologyTermsData(experimentDesign);

        assertThat(rows.get(0))
                .isEqualTo(
                        new String[] {
                                "ASSAY_1",
                                "CHAR_1",
                                "http://purl.obolibrary.org/obo/UBERON:0000001",
                                "CHAR_2", "UBERON:0000002",
                                "CHAR_3",
                                null,
                                "ASSAY1_FACTOR",
                                "http://purl.obolibrary.org/obo/F:1",
                                "ASSAY1_FACTOR2",
                                null});
        assertThat(rows.get(1))
                .isEqualTo(
                        new String[]{
                                "ASSAY_2",
                                "CHAR_1",
                                "http://purl.obolibrary.org/obo/UBERON:0000001",
                                "CHAR_2",
                                "UBERON:0000002",
                                "CHAR_3",
                                null,
                                "ASSAY2_FACTOR",
                                "F:2",
                                "ASSAY2_FACTOR2",
                                null});
        assertThat(rows.get(2))
                .isEqualTo(
                        new String[] {
                                "ASSAY_3",
                                "CHAR_1",
                                "http://purl.obolibrary.org/obo/UBERON:0000001",
                                "CHAR_2",
                                "UBERON:0000002",
                                "CHAR_3",
                                null,
                                "ASSAY3_FACTOR",
                                null,
                                "ASSAY3_FACTOR2",
                                null});
    }

    @Test
    public void testHeadersForSingleNucleusRnaseqMRNABaseline() {
        var headers = subject.buildColumnHeaders(SINGLE_NUCLEUS_RNASEQ_MRNA_BASELINE, experimentDesign);

        assertThat(headers)
          .isEqualTo(
            new String[] {
              "Assay",
              "Sample Characteristic[CHARACTERISTIC_1]",
              "Sample Characteristic Ontology Term[CHARACTERISTIC_1]",
              "Sample Characteristic[CHARACTERISTIC_2]",
              "Sample Characteristic Ontology Term[CHARACTERISTIC_2]",
              "Sample Characteristic[CHARACTERISTIC_3]",
              "Sample Characteristic Ontology Term[CHARACTERISTIC_3]",
              "Factor Value[FACTOR_1]",
              "Factor Value Ontology Term[FACTOR_1]",
              "Factor Value[FACTOR_2]",
              "Factor Value Ontology Term[FACTOR_2]"});
    }
}
