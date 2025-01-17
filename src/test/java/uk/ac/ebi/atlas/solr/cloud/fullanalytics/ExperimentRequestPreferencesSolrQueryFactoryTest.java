package uk.ac.ebi.atlas.solr.cloud.fullanalytics;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;
import uk.ac.ebi.atlas.web.RnaSeqBaselineRequestPreferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars;
import static org.assertj.core.api.Assertions.assertThat;

class ExperimentRequestPreferencesSolrQueryFactoryTest {
    private static final String E_MTAB_513 = "E-MTAB-513";
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Test
    void testDefaultQuery() {
        var requestPreferences = new RnaSeqBaselineRequestPreferences();
        var solrQuery =
                ExperimentRequestPreferencesSolrQueryFactory.createSolrQuery(E_MTAB_513, requestPreferences);

        assertThat(solrQuery.toMap(new HashMap<>()))
                .containsEntry(
                        "fq",
                        new String[]{
                                "experiment_accession:(\"" + escapeQueryChars(E_MTAB_513) + "\")",
                                "expression_level:[" + requestPreferences.getDefaultCutoff() + " TO *]"})
                .containsEntry(
                        "q",
                        "(keyword_gene_biotype:(\"protein_coding\"))")
                .containsEntry(
                        "fl",
                        "*")
                .containsEntry(
                        "rows",
                        String.valueOf(SolrQueryBuilder.DEFAULT_ROWS));
    }

    @Test
    void testEmptyGeneQuery() {
        var requestPreferences = new RnaSeqBaselineRequestPreferences();
        requestPreferences.setGeneQuery(SemanticQuery.create());
        var solrQuery = ExperimentRequestPreferencesSolrQueryFactory.createSolrQuery(E_MTAB_513, requestPreferences);

        assertThat(solrQuery.toMap(new HashMap<>()))
                .containsEntry(
                        "fq",
                        new String[]{
                                "experiment_accession:(\"" + escapeQueryChars(E_MTAB_513) + "\")",
                                "expression_level:[" + requestPreferences.getDefaultCutoff() + " TO *]"})
                .containsEntry(
                        "q",
                        "*:*")
                .containsEntry(
                        "fl",
                        "*")
                .containsEntry(
                        "rows",
                        String.valueOf(SolrQueryBuilder.DEFAULT_ROWS));
    }

    @Test
    void testQueriesAreJoinedWithAnd() {
        var numAssayGroups = RNG.nextInt(1, 100);
        var assayGroups = new HashSet<String>(numAssayGroups);
        while (assayGroups.size() < numAssayGroups) {
            assayGroups.add("g" + RNG.nextInt(1, 1000));
        }

        var requestPreferences = new RnaSeqBaselineRequestPreferences();
        requestPreferences.setSelectedColumnIds(assayGroups);
        var solrQuery = ExperimentRequestPreferencesSolrQueryFactory.createSolrQuery(E_MTAB_513, requestPreferences);

        var explodedQuery = Arrays.asList(solrQuery.getQuery().split(" AND "));

        assertThat(explodedQuery)
                .hasSize(2)
                .allMatch(query -> query.matches("\\(\\w+:.+\\)"));

        assertThat(
                explodedQuery.stream()
                        .filter(query -> query.startsWith("(assay_group_id"))
                        .findFirst()
                        .orElse("")
                        .split(":")[1].split(" OR "))
                .hasSize(numAssayGroups);
    }

    // TODO test gene query OR’s the fields
}
