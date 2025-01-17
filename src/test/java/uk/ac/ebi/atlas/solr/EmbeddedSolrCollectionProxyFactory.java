package uk.ac.ebi.atlas.solr;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class EmbeddedSolrCollectionProxyFactory {
    private final EmbeddedSolrServerFactory embeddedSolrServerFactory;

    public EmbeddedSolrCollectionProxyFactory(EmbeddedSolrServerFactory embeddedSolrServerFactory) {
        this.embeddedSolrServerFactory = embeddedSolrServerFactory;
    }

    public BulkAnalyticsCollectionProxy createAnalyticsCollectionProxy() {
        return new BulkAnalyticsCollectionProxy(
                new EmbeddedSolrServer(
                        embeddedSolrServerFactory.getCoreContainer(), BulkAnalyticsCollectionProxy.COLLECTION_NAME));
    }
}
