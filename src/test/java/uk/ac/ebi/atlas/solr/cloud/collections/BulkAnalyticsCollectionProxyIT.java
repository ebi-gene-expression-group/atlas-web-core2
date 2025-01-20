package uk.ac.ebi.atlas.solr.cloud.collections;

import com.google.common.collect.ImmutableSet;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.differential.Regulation;
import uk.ac.ebi.atlas.solr.EmbeddedSolrCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.ENSGENE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.EXPERIMENT_TYPE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.asAnalyticsSchemaField;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class BulkAnalyticsCollectionProxyIT {
    // These two documents span a reasonable field set in the schema
    private static final SolrInputDocument RNASEQ_BASELINE_INPUT_DOCUMENT =
            createRnaSeqBaselineAnalyticsSolrInputDocument(
                    "ENSG00000150991",
                    "E-MTAB-3358",
                    "g52",
                    15.0,
                    "ORGANISM_PART",
                    "homo sapiens",
                    "animals",
                    ImmutableSet.of("ENSG00000150991"),
                    ImmutableSet.of("UBC"),
                    ImmutableSet.of("protein_coding"),
                    ImmutableSet.of(),
                    ImmutableSet.of("GO:1902036", "GO:0006283", "GO:0036297", "GO:0007179"),
                    ImmutableSet.of("IPR000626", "IPR029071", "IPR019956"),
                    ImmutableSet.of("R-HSA-400253", "R-HSA-110314", "R-HSA-110313", "R-HSA-110312"),
                    ImmutableSet.of("F5GYU3", "Q5PY61", "Q96C32", "F5H6Q2", "F5GXK7"),
                    "ubiquitin C",
                    "lungs",
                    false);

    private static final SolrInputDocument MICROARRAY_DIFFERENTIAL_INPUT_DOCUMENT =
            createMicroarray1ColourDifferentialAnalyticsSolrInputDocument(
                    "ENSG00000196968",
                    "E-TABM-234",
                    "g5_g1",
                    -1.3,
                    0.00199494739061544,
                    -4.33210240798583,
                    "ORGANISM_PART",
                    "homo sapiens",
                    "animals",
                    ImmutableSet.of("disease", "histology"),
                    Regulation.DOWN,
                    8,
                    ImmutableSet.of("79094_at", "62111_at", "A_33_P3354713", "Hs.268231.0.S2_3p_at", "3251920"),
                    ImmutableSet.of("ENSG00000196968"),
                    ImmutableSet.of("FUT11"),
                    ImmutableSet.of("protein_coding"),
                    ImmutableSet.of("MGC33202"),
                    ImmutableSet.of("GO:0016757", "GO:0036065", "GO:0032580", "GO:0006486"),
                    ImmutableSet.of("IPR017176", "IPR031481", "IPR001503"),
                    ImmutableSet.of("R-HSA-400253", "R-HSA-110314", "R-HSA-110313", "R-HSA-110312"),
                    ImmutableSet.of("F5GYU3", "Q5PY61", "Q96C32", "F5H6Q2", "F5GXK7"),
                    "fucosyltransferase 11",
                    "vasectomy adult male Homo sapiens testis ejaculatory azoospermia premortem idiopathi " +
                            "c infertility",
                    false);

    @Autowired
    private EmbeddedSolrCollectionProxyFactory embeddedSolrCollectionProxyFactory;

    private BulkAnalyticsCollectionProxy subject;

    @BeforeEach
    void setUp() {
        subject = embeddedSolrCollectionProxyFactory.createAnalyticsCollectionProxy();
    }

    @Test
    void addAndRetrieveDocuments() {
        var updateResponse =
                subject.add(ImmutableSet.of(RNASEQ_BASELINE_INPUT_DOCUMENT, MICROARRAY_DIFFERENTIAL_INPUT_DOCUMENT));
        subject.commit();

        assertThat(updateResponse.getStatus()).isEqualTo(0);

        assertThat(subject.query(new SolrQueryBuilder<>()).getResults()).hasSize(2);
        assertThat(
                subject.query(
                        new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                                .addQueryFieldByTerm(BIOENTITY_IDENTIFIER, "ENSG00000150991"))
                        .getResults())
                .hasSize(1);

        assertThat(
                subject.query(
                        new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                                .addQueryFieldByTerm(asAnalyticsSchemaField(ENSGENE), "ENSG00000150991"))
                        .getResults())
                .hasSize(1);

        assertThat(
                subject.query(
                        new SolrQueryBuilder<BulkAnalyticsCollectionProxy>()
                                .addQueryFieldByTerm(
                                        EXPERIMENT_TYPE, ExperimentType.MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL.name()))
                        .getResults())
                .hasSize(1);
    }

    @Test
    void deleteAll() {
        subject.add(ImmutableSet.of(RNASEQ_BASELINE_INPUT_DOCUMENT, MICROARRAY_DIFFERENTIAL_INPUT_DOCUMENT));
        subject.commit();
        assertThat(subject.query(new SolrQueryBuilder<>()).getResults()).hasSize(2);

        var updateResponse = subject.deleteAll();
        subject.commit();
        assertThat(updateResponse.getStatus()).isEqualTo(0);
        assertThat(subject.query(new SolrQueryBuilder<>()).getResults()).hasSize(0);
    }

    @Test
    void dedupesSilently() {
        subject.add(ImmutableSet.of(RNASEQ_BASELINE_INPUT_DOCUMENT));
        var updateResponse = subject.add(ImmutableSet.of(RNASEQ_BASELINE_INPUT_DOCUMENT));
        subject.commit();

        assertThat(updateResponse.getStatus()).isEqualTo(0);
        assertThat(subject.query(new SolrQueryBuilder<>()).getResults()).hasSize(1);
    }

    @Test
    void baselineSignature() {
        assertOnlySignatureFieldsIncreaseCollectionSize(
                RNASEQ_BASELINE_INPUT_DOCUMENT,
                ImmutableSet.of("bioentity_identifier", "experiment_accession", "assay_group_id"));
    }

    @Test
    void differentialSignature() {
        assertOnlySignatureFieldsIncreaseCollectionSize(
                MICROARRAY_DIFFERENTIAL_INPUT_DOCUMENT,
                ImmutableSet.of("bioentity_identifier", "experiment_accession", "contrast_id"));
    }

    private void assertOnlySignatureFieldsIncreaseCollectionSize(SolrInputDocument inputDocument,
                                                                 Collection<String> signatureFieldNames) {
        for (var fieldName: inputDocument.getFieldNames()) {
            subject.deleteAll();
            subject.add(ImmutableSet.of(inputDocument));

            var modifiedDocument = inputDocument.deepCopy();

            if (modifiedDocument.getFieldValue(fieldName) != null) {
                if (modifiedDocument.getFieldValue(fieldName).getClass().equals(String.class)) {
                    modifiedDocument.removeField(fieldName);
                    modifiedDocument.addField(fieldName, "some random string");
                } else {
                    // We can do this because fields are either strings or numbers
                    modifiedDocument.removeField(fieldName);
                    modifiedDocument.addField(fieldName, 4);    // chosen by fair dice roll, guaranteed to be random
                }

                subject.add(ImmutableSet.of(modifiedDocument));
                subject.commit();

                if (signatureFieldNames.contains(fieldName)) {
                    assertThat(subject.query(new SolrQueryBuilder<>()).getResults()).hasSize(2);
                } else {
                    assertThat(subject.query(new SolrQueryBuilder<>()).getResults()).hasSize(1);
                }
            }
        }
    }

    private static SolrInputDocument createAnalyticsInputDocument(
            String bioentityIdentifier,
            String experimentAccession,
            String defaultQueryFactorType,
            String species,
            String kingdom,
            Collection<String> ensgene,
            Collection<String> symbol,
            Collection<String> geneBiotype,
            Collection<String> synonym,
            Collection<String> go,
            Collection<String> interpro,
            Collection<String> pathwayId,
            Collection<String> uniprot,
            String identifierSearch,
            String conditionsSearch,
            boolean isPrivate) {

        var document = new SolrInputDocument();

        document.addField("bioentity_identifier", bioentityIdentifier);
        document.addField("experiment_accession", experimentAccession);
        document.addField("default_query_factor_type", defaultQueryFactorType);
        document.addField("species", species);
        document.addField("kingdom", kingdom);

        document.addField("keyword_ensgene", ensgene);
        document.addField("keyword_symbol", symbol);
        document.addField("keyword_gene_biotype", geneBiotype);
        document.addField("keyword_synonym", synonym);
        document.addField("keyword_go", go);
        document.addField("keyword_interpro", interpro);
        document.addField("keyword_pathwayid", pathwayId);
        document.addField("keyword_uniprot", uniprot);

        document.addField("identifier_search", identifierSearch);
        document.addField("conditions_search", conditionsSearch);

        document.addField("is_private", isPrivate);

        return document;
    }

    private static SolrInputDocument createRnaSeqBaselineAnalyticsSolrInputDocument(
            String bioentityIdentifier,
            String experimentAccession,
            String assayGroupId,
            Double expressionLevel,
            String defaultQueryFactorType,
            String species,
            String kingdom,
            Collection<String> ensgene,
            Collection<String> symbol,
            Collection<String> geneBiotype,
            Collection<String> synonym,
            Collection<String> go,
            Collection<String> interpro,
            Collection<String> pathwayId,
            Collection<String> uniprot,
            String identifierSearch,
            String conditionsSearch,
            boolean isPrivate) {

        var document =
                createAnalyticsInputDocument(
                        bioentityIdentifier,
                        experimentAccession,
                        defaultQueryFactorType,
                        species,
                        kingdom,
                        ensgene,
                        symbol,
                        geneBiotype,
                        synonym,
                        go,
                        interpro,
                        pathwayId,
                        uniprot,
                        identifierSearch,
                        conditionsSearch,
                        isPrivate);

        document.addField("assay_group_id", assayGroupId);
        document.addField("expression_level", expressionLevel);
        document.addField("experiment_type", ExperimentType.RNASEQ_MRNA_BASELINE.name());

        return document;
    }

    private static SolrInputDocument createMicroarray1ColourDifferentialAnalyticsSolrInputDocument(
            String bioentityIdentifier,
            String experimentAccession,
            String contrastId,
            Double log2FoldChange,
            Double adjustedPValue,
            Double tStatistic,
            String defaultQueryFactorType,
            String species, String kingdom,
            Collection<String> factors,
            Regulation regulation,
            int numReplicates,
            Collection<String> designElements,
            Collection<String> ensgene,
            Collection<String> symbol,
            Collection<String> geneBiotype,
            Collection<String> synonym,
            Collection<String> go,
            Collection<String> interpro,
            Collection<String> pathwayId,
            Collection<String> uniprot,
            String identifierSearch,
            String conditionsSearch,
            boolean isPrivate) {

        SolrInputDocument document =
                createAnalyticsInputDocument(
                        bioentityIdentifier,
                        experimentAccession,
                        defaultQueryFactorType,
                        species,
                        kingdom,
                        ensgene,
                        symbol,
                        geneBiotype,
                        synonym,
                        go,
                        interpro,
                        pathwayId,
                        uniprot,
                        identifierSearch,
                        conditionsSearch,
                        isPrivate);

        document.addField("contrast_id", contrastId);
        document.addField("fold_change", log2FoldChange);
        document.addField("p_value", adjustedPValue);
        document.addField("experiment_type", ExperimentType.MICROARRAY_1COLOUR_MRNA_DIFFERENTIAL.name());
        document.addField("t_statistic", tStatistic);
        document.addField("factors", factors);
        document.addField("regulation", regulation);
        document.addField("num_replicates", numReplicates);
        document.addField("keyword_design_element", designElements);

        return document;
    }
}

