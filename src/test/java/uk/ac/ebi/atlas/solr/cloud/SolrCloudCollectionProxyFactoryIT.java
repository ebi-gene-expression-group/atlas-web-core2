package uk.ac.ebi.atlas.solr.cloud;

import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class SolrCloudCollectionProxyFactoryIT {
    @Autowired
    private SolrCloudCollectionProxyFactory subject;

    @Test
    public void createAnalyticsCollectionProxy() {
        assertThat(subject.create(BulkAnalyticsCollectionProxy.class).rawQuery(new SolrQuery("*:*")).getResults().size())
                .isGreaterThan(0);
    }
}
