package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);
    private SolrCloudHealthService solrCloudHealthService;
    private PostgreSqlHealthService postgreSqlHealthService;

    public HealthCheckService(SolrCloudHealthService solrCloudHealthService,
                              PostgreSqlHealthService postgreSqlHealthService) {
        this.solrCloudHealthService = solrCloudHealthService;
        this.postgreSqlHealthService = postgreSqlHealthService;
    }

    public boolean isSolrUp(ImmutableCollection<String> collectionNames,
                            ImmutableCollection<String> collectionAliases) {
        try {
            return solrCloudHealthService.areCollectionsUp(collectionNames, collectionAliases);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    public boolean isDatabaseUp() {
        try {
            return postgreSqlHealthService.isDatabaseUp();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }
}
