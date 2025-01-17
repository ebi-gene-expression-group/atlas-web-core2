package uk.ac.ebi.atlas.monitoring;

import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {
    @Mock
    private SolrCloudHealthService solrCloudHealthServiceMock;

    @Mock
    private PostgreSqlHealthService postgreSqlHealthServiceMock;

    private HealthCheckService subject;

    private static final ImmutableSet<String> MOCK_SOLR_COLLECTIONS =
            ImmutableSet.of("mockCollection1", "mockCollection2");
    private static final ImmutableSet<String> MOCK_SOLR_COLLECTION_ALIAS = ImmutableSet.of("mockCollectionAlias");

    @BeforeEach
    void setUp() {
        subject = new HealthCheckService(solrCloudHealthServiceMock, postgreSqlHealthServiceMock);
    }

    @Test
    void solrCollectionsAreUp() throws IOException, SolrServerException {
        when(solrCloudHealthServiceMock.areCollectionsUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS))
                .thenReturn(true);
        assertThat(subject.isSolrUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS)).isTrue();
    }

    @Test
    void solrCollectionsAreDown() throws IOException, SolrServerException {
        when(solrCloudHealthServiceMock.areCollectionsUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS))
                .thenReturn(false);
        assertThat(subject.isSolrUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS)).isFalse();
    }

    @Test
    void solrThrowsException() throws IOException, SolrServerException {
        when(solrCloudHealthServiceMock.areCollectionsUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS))
                .thenThrow(RuntimeException.class);
        assertThat(subject.isSolrUp(MOCK_SOLR_COLLECTIONS, MOCK_SOLR_COLLECTION_ALIAS)).isFalse();
    }

    @Test
    void experimentDaoThrowsException() {
        when(postgreSqlHealthServiceMock.isDatabaseUp()).thenThrow(RuntimeException.class);
        assertThat(subject.isDatabaseUp()).isFalse();
    }
}
