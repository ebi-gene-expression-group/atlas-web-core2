package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.EmbeddedSolrCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.SolrCloudCollectionProxyFactory;
import uk.ac.ebi.atlas.solr.cloud.collections.BulkAnalyticsCollectionProxy;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnalyticsIndexerServiceIT {
    @Inject
    DataSource dataSource;

    @Inject
    JdbcUtils jdbcUtils;

    @Inject
    private EmbeddedSolrCollectionProxyFactory embeddedSolrCollectionProxyFactory;

    @Inject
    private ExperimentDataPointStreamFactory experimentDataPointStreamFactory;

    @Mock
    private SolrCloudCollectionProxyFactory solrCloudCollectionProxyFactoryMock;

    private AnalyticsIndexerService subject;

    @BeforeAll
    void populateDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("/fixtures/gxa/experiment.sql"));
        populator.execute(dataSource);
    }

    @AfterAll
    void cleanDatabaseTables() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("/fixtures/gxa/experiment-delete.sql"));
        populator.execute(dataSource);
    }

    @BeforeEach
    void setUp() {
        when(solrCloudCollectionProxyFactoryMock.create(BulkAnalyticsCollectionProxy.class))
                .thenReturn(embeddedSolrCollectionProxyFactory.createAnalyticsCollectionProxy());

        subject = new AnalyticsIndexerService(solrCloudCollectionProxyFactoryMock, experimentDataPointStreamFactory);
    }

    private Stream<String> experimentAccessionProvider() {
        return Stream.of(jdbcUtils.fetchRandomPublicExperimentAccession());
    }


}