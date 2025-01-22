package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import org.apache.solr.common.SolrInputDocument;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.SolrInputDocumentInputStream;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.profiles.IterableObjectInputStream;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@Component
public class AnalyticsIndexerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsIndexerService.class);
    private static final long COMMIT_SIZE = 5_000_000;

    private final BulkAnalyticsCollectionProxy bulkAnalyticsCollectionProxy;
    private final ExperimentDataPointStreamFactory experimentDataPointStreamFactory;

    public AnalyticsIndexerService(SolrCloudCollectionProxyFactory collectionProxyFactory,
                                   ExperimentDataPointStreamFactory experimentDataPointStreamFactory) {
        this.bulkAnalyticsCollectionProxy = collectionProxyFactory.create(BulkAnalyticsCollectionProxy.class);
        this.experimentDataPointStreamFactory = experimentDataPointStreamFactory;
    }

    public int index(@NotNull Experiment experiment,
                     @NotNull Map<@NotNull String,
                                  @NotNull Map<@NotNull BioentityPropertyName,
                                               @NotNull Set<@NotNull String>>> bioentityIdToProperties,
                     int batchSize) {

        var toLoad = new ArrayList<SolrInputDocument>(batchSize);
        var addedIntoThisBatch = 0;
        var addedSinceLastCommit = 0;
        var addedInTotal = 0;

        try (var solrInputDocumentInputStream =
                new SolrInputDocumentInputStream(
                        experimentDataPointStreamFactory.stream(experiment),
                        bioentityIdToProperties)) {

            var it = new IterableObjectInputStream<>(solrInputDocumentInputStream).iterator();
            while (it.hasNext()) {
                // Create a batch of documents to send to Solr
                while (addedIntoThisBatch < batchSize && it.hasNext()) {
                    var analyticsInputDocument = it.next();
                    toLoad.add(analyticsInputDocument);
                    addedIntoThisBatch++;
                }

                // Send docs to Solr
                if (addedIntoThisBatch > 0) {
                    var updateResponse = bulkAnalyticsCollectionProxy.add(toLoad);
                    LOGGER.info(
                            "Sent {} documents for {}, qTime:{}",
                            addedIntoThisBatch, experiment.getAccession(), updateResponse.getQTime());
                    addedSinceLastCommit += addedIntoThisBatch;
                    addedInTotal += addedIntoThisBatch;
                    addedIntoThisBatch = 0;
                    toLoad.clear();
                }

                // If we don’t commit every now and then Solr’s performance slowly degrades until we have a timeout
                if (addedSinceLastCommit >= COMMIT_SIZE) {
                    LOGGER.info("Committing {} documents", addedSinceLastCommit);
                    bulkAnalyticsCollectionProxy.commit();
                    addedSinceLastCommit = 0;
                }
            }
            bulkAnalyticsCollectionProxy.commit();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        LOGGER.info("Finished: " + experiment.getAccession());
        return addedInTotal;
    }

    public void deleteExperimentFromIndex(String accession) {
        LOGGER.info("Deleting documents for {}", accession);
        var solrQueryBuilder = new SolrQueryBuilder<BulkAnalyticsCollectionProxy>();
        solrQueryBuilder.addQueryFieldByTerm(BulkAnalyticsCollectionProxy.EXPERIMENT_ACCESSION, accession);
        bulkAnalyticsCollectionProxy.deleteByQuery(solrQueryBuilder);
        bulkAnalyticsCollectionProxy.commit();
        LOGGER.info("Done deleting documents for {}", accession);
    }

    public void deleteAll() {
        LOGGER.info("Deleting all documents");
        bulkAnalyticsCollectionProxy.deleteAll();
        bulkAnalyticsCollectionProxy.commit();
        LOGGER.info("Done deleting all documents");
    }
}
