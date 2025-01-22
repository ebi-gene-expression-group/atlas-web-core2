package uk.ac.ebi.atlas.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EuropePmcClientTest {
    @Mock
    private RestTemplate restTemplateMock;

    private EuropePmcClient subject;

    @Before
    public void setUp() throws Exception {
        subject = new EuropePmcClient(restTemplateMock);
    }

    @Test
    public void returnsNoResultsIfEuropePmcIsUnavailable() {
        when(restTemplateMock.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(RestClientException.class);

        assertThat(subject.getPublicationByDoi("foo")).isEmpty();
        assertThat(subject.getPublicationByPubmedId("foo")).isEmpty();
    }
}
