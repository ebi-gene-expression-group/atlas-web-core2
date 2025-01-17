package uk.ac.ebi.atlas.solr.cloud;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

// CollectionProxy acts as an interface between the client application and each Solr collection that abstracts any
// low-level details such as request processors, specific schema field names, etc. There should be a subclass for each
// collection that we want to interact with. See package uk.ac.ebi.atlas.solr.cloud.collections.

// Unfortunately Java has no nice way of referring to a class’s own type to use as a generic parameter in a general
// way, let alone a safe one! If you feel curious about it:
// https://stackoverflow.com/questions/7354740/is-there-a-way-to-refer-to-the-current-type-with-a-type-variable
// And if you finally want something to make your head spin:
// https://en.wikipedia.org/wiki/Curiously_recurring_template_pattern
// Some frameworks provide solutions for this: http://manifold.systems/docs.html#the-self-type
// We leave it to the judicious Atlas developers to extend this class as intended.
public abstract class CollectionProxy<SELF extends CollectionProxy<?>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionProxy.class);
    private static final int MAX_RETRIES = 10;
    private static final long WAITING_TIME_BETWEEN_RETRIES = 10000;

    public final SolrClient solrClient;
    public final String nameOrAlias;

    protected CollectionProxy(SolrClient solrClient, String nameOrAlias) {
        this.solrClient = solrClient;
        this.nameOrAlias = nameOrAlias;
    }

    public QueryResponse query(SolrQueryBuilder<SELF> solrQueryBuilder) {
        LOGGER.debug("Querying {}", solrQueryBuilder.build().getQuery());
        return rawQuery(solrQueryBuilder.build());
    }

    public UpdateResponse deleteByQuery(SolrQueryBuilder<SELF> solrQueryBuilder) {
        LOGGER.debug("Deleting {}", solrQueryBuilder.build().getQuery());
        return deleteByRawQuery(solrQueryBuilder.build());
    }

    public final QueryResponse rawQuery(SolrQuery solrQuery) {
        return logQuery(solrQuery);
    }

    protected final FieldStatsInfo fieldStats(String fieldName, SolrQuery solrQuery) {
        LOGGER.debug("Field stats {}", solrQuery.getQuery());
        solrQuery.setRows(0);
        solrQuery.setGetFieldStatistics(true);
        solrQuery.setGetFieldStatistics(fieldName);
        solrQuery.addStatsFieldCalcDistinct(fieldName, true);
        return logQuery(solrQuery).getFieldStatsInfo().get(fieldName);
    }

    // Each subclass should add its own requestProcessor, or pass an empty string if none is used
    protected final UpdateResponse add(Collection<SolrInputDocument> docs, String requestProcessor) {
        LOGGER.debug("Adding {} documents...", docs.size());

        var retries = 0;
        var waitingTime = 0L;
        while (retries < MAX_RETRIES) {
            retries++;
            waitingTime += retries * WAITING_TIME_BETWEEN_RETRIES;
            try {
                var updateRequest = new UpdateRequest();
                updateRequest.setParam("processor", requestProcessor);
                var updateResponse = process(updateRequest.add(docs));
                LOGGER.info("Finished on {} attempt", ordinal(retries));
                return updateResponse;
            } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException e) {
                LOGGER.warn(
                        "{} retry: {}, waiting {} seconds and trying again...",
                        ordinal(retries), e.getMessage(), waitingTime/1000);
                try {
                    Thread.sleep(waitingTime);
                } catch (InterruptedException lastException) {
                    throw new RuntimeException(lastException);
                }
            }
        }
        LOGGER.error("Retried {} times, giving up :(", retries);
        return rollback();
    }

    public final UpdateResponse deleteAll() {
        LOGGER.debug("Deleting all documents");
        return deleteByRawQuery(new SolrQuery("*:*"));
    }

    public final UpdateResponse deleteByRawQuery(SolrQuery solrQuery) {
        try {
            return process(new UpdateRequest().deleteByQuery(solrQuery.getQuery()));
        } catch (IOException | SolrServerException e) {
            logException(e);
            return rollback();
        }
    }

    private UpdateResponse process(UpdateRequest updateRequest) throws IOException, SolrServerException {
        LOGGER.debug("Processing transaction");
        return updateRequest.process(solrClient, nameOrAlias);
    }

    public final synchronized UpdateResponse commit() {
        try {
            LOGGER.debug("Committing update");
            return new UpdateRequest().commit(solrClient, nameOrAlias);
        } catch (IOException | SolrServerException e) {
            logException(e);
            return rollback();
        }
    }


    private synchronized UpdateResponse rollback() {
        try {
            return solrClient.rollback();
        } catch (IOException e) {
            logException(e);
            throw new UncheckedIOException(e);
        } catch (SolrServerException e) {
            logException(e);
            throw new UncheckedIOException(new IOException(e));
        }
    }

    private QueryResponse logQuery(SolrQuery solrQuery) {
        try {
            return solrClient.query(nameOrAlias, solrQuery, SolrRequest.METHOD.POST);
        } catch (IOException e) {
            logException(e);
            throw new UncheckedIOException(e);
        } catch (SolrServerException e) {
            logException(e);
            throw new UncheckedIOException(new IOException(e));
        }
    }

    private void logException(Exception e) {
        LOGGER.error(
                "Problem connecting to SolrCloud {} with collection {}, full stack trace follows:\n\t{}",
                solrClient.getClass().getSimpleName(),
                nameOrAlias,
                Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n\t")));
    }

    private static String ordinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];

        }
    }
}
