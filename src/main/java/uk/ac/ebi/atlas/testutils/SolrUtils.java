package uk.ac.ebi.atlas.testutils;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;

import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.AnalyticsSchemaField;
import static uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy.IS_PRIVATE;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.SPECIES;

import uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class SolrUtils {
    private static final int MAX_ROWS = 10000;
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    private BulkAnalyticsCollectionProxy bulkAnalyticsCollectionProxy;
    private BioentitiesCollectionProxy bioentitiesCollectionProxy;

    public SolrUtils(SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactory) {
        bulkAnalyticsCollectionProxy = solrCloudCollectionProxyFactory.create(BulkAnalyticsCollectionProxy.class);
        bioentitiesCollectionProxy = solrCloudCollectionProxyFactory.create(BioentitiesCollectionProxy.class);
    }

    public String fetchRandomGeneIdFromAnalytics() {
        SolrQueryBuilder<BulkAnalyticsCollectionProxy> queryBuilder = new SolrQueryBuilder<>();
        queryBuilder
                .addFilterFieldByTerm(IS_PRIVATE, "false")
                .setFieldList(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER)
                .setRows(MAX_ROWS);

        return getRandomSolrDocument(bulkAnalyticsCollectionProxy.query(queryBuilder).getResults())
                .getFieldValue("bioentity_identifier")
                .toString();
    }

    public String fetchRandomGeneIdFromAnalytics(AnalyticsSchemaField field, String term) {
        SolrQueryBuilder<BulkAnalyticsCollectionProxy> queryBuilder = new SolrQueryBuilder<>();
        queryBuilder
                .addFilterFieldByTerm(IS_PRIVATE, "false")
                .addQueryFieldByTerm(field, term)
                .setFieldList(BulkAnalyticsCollectionProxy.BIOENTITY_IDENTIFIER)
                .setRows(MAX_ROWS);

        return getRandomSolrDocument(bulkAnalyticsCollectionProxy.query(queryBuilder).getResults())
                .getFieldValue("bioentity_identifier")
                .toString();
    }

    public String fetchRandomGeneWithoutSymbolFromAnalytics() {
        SolrQuery solrQuery = new SolrQuery("-keyword_symbol:*");
        solrQuery.setRows(MAX_ROWS);
        return getRandomSolrDocument(bulkAnalyticsCollectionProxy.rawQuery(solrQuery).getResults())
                .getFieldValue("bioentity_identifier")
                .toString();
    }

    public String fetchRandomGeneOfSpecies(String species) {
        SolrQueryBuilder<BioentitiesCollectionProxy> queryBuilder = new SolrQueryBuilder<>();
        queryBuilder.addFilterFieldByTerm(SPECIES, species);
        queryBuilder.setFieldList(BioentitiesCollectionProxy.BIOENTITY_IDENTIFIER);
        queryBuilder.setRows(MAX_ROWS);
        return getRandomSolrDocument(bioentitiesCollectionProxy.query(queryBuilder).getResults())
                .getFieldValue("bioentity_identifier")
                .toString();
    }

    private SolrDocument getRandomSolrDocument(SolrDocumentList solrDocumentList) {
        return solrDocumentList.get(RNG.nextInt(solrDocumentList.size()));
    }
}
