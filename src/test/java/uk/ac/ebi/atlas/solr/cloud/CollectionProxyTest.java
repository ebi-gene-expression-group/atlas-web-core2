package uk.ac.ebi.atlas.solr.cloud;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.params.SolrParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionProxyTest {
    @Mock
    private SolrClient solrClientMock;

    private CollectionProxy subject;

    @BeforeEach
    void setUp() {
        subject = new CollectionProxy(solrClientMock, "mocked_collection") {};
    }

    @Test
    void queryIOExceptionIsWrapped() throws Exception {
        when(solrClientMock.query(anyString(), any(SolrParams.class), eq(SolrRequest.METHOD.POST)))
                .thenThrow(new IOException());

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> subject.rawQuery(new SolrQuery("*:*")));
    }

    @Test
    void querySolrServerExceptionIsWrapped() throws Exception {
        when(solrClientMock.query(anyString(), any(SolrParams.class), eq(SolrRequest.METHOD.POST)))
                .thenThrow(new SolrServerException(""));

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> subject.rawQuery(new SolrQuery("*:*")));
    }

    @Test
    void addIOExceptionTriggersRollback() throws Exception {
        when(solrClientMock.request(any(UpdateRequest.class), eq(subject.nameOrAlias)))
                .thenThrow(new IOException());

        subject.deleteAll();
        verify(solrClientMock).rollback();
    }

    @Test
    void addSolrServerExceptionTriggersRollback() throws Exception {
        when(solrClientMock.request(any(UpdateRequest.class), eq(subject.nameOrAlias)))
                .thenThrow(new SolrServerException(""));

        subject.deleteAll();
        verify(solrClientMock).rollback();
    }

    @Test
    void deleteIOExceptionTriggersRollback() throws Exception {
        when(solrClientMock.request(any(SolrRequest.class), eq(subject.nameOrAlias)))
                .thenThrow(new IOException());

        subject.deleteAll();
        verify(solrClientMock).rollback();
    }

    @Test
    void deleteSolrServerExceptionTriggersRollback() throws Exception {
        when(solrClientMock.request(any(UpdateRequest.class), eq(subject.nameOrAlias)))
                .thenThrow(new SolrServerException(""));

        subject.deleteAll();
        verify(solrClientMock).rollback();
    }

    @Test
    void ifAllFailsAndRollbackThrowsSolrServerExceptionItIsWrapped() throws Exception {
        when(solrClientMock.request(any(UpdateRequest.class), eq(subject.nameOrAlias)))
                .thenThrow(new SolrServerException(""));
        when(solrClientMock.rollback()).thenThrow(new SolrServerException(""));

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> subject.deleteAll());
    }

    @Test
    void ifAllFailsAndRollbackThrowsIOExceptionItIsWrapped() throws Exception {
        when(solrClientMock.request(any(UpdateRequest.class), eq(subject.nameOrAlias)))
                .thenThrow(new SolrServerException(""));
        when(solrClientMock.rollback()).thenThrow(new IOException(""));

        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> subject.deleteAll());
    }
}
