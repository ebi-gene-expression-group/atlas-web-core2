package uk.ac.ebi.atlas.utils;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.atlas.configuration.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomDoi;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomPubmedId;

@ContextConfiguration(classes = TestConfig.class)
class EuropePmcClientIT {
    private MockRestServiceServer mockServer;

    private EuropePmcClient subject;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        subject = new EuropePmcClient(restTemplate);
    }

    @Test
    void publicationForValidDoi() throws Exception {
        var doiResponse =
                IOUtils.toString(this.getClass().getResourceAsStream("europepmc-doi-response.json"), "UTF-8");

        mockServer.expect(once(), requestTo(startsWith(EuropePmcClient.URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("format", UriUtils.encode("json", "UTF-8")))
                .andExpect(queryParam("query", startsWith(UriUtils.encode("DOI:", "UTF-8"))))
                .andRespond(withSuccess(doiResponse, MediaType.APPLICATION_JSON));


        var result = subject.getPublicationByDoi(generateRandomDoi());

        assertThat(result)
                .get()
                .extracting("doi", "authors", "title")
                .isNotEmpty();

        mockServer.verify();
    }

    @Test
    void publicationForValidPubmedId() throws Exception {
        var pubmedIdResponse =
                IOUtils.toString(this.getClass().getResourceAsStream("europepmc-pubmed-id-response.json"), "UTF-8");

        mockServer.expect(once(), requestTo(startsWith(EuropePmcClient.URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("format", UriUtils.encode("json", "UTF-8")))
                .andExpect(queryParam("query", startsWith(UriUtils.encode("SRC:MED AND EXT_ID:", "UTF-8"))))
                .andRespond(withSuccess(pubmedIdResponse, MediaType.APPLICATION_JSON));

        var result = subject.getPublicationByPubmedId(generateRandomPubmedId());

        assertThat(result)
                .get()
                .extracting("pubmedId", "authors", "title")
                .isNotEmpty();

        mockServer.verify();
    }

    @Test
    void noResultForEmptyDoi() throws Exception {
        var noResultsResponse =
                IOUtils.toString(this.getClass().getResourceAsStream("europepmc-no-results-response.json"), "UTF-8");

        mockServer.expect(once(), requestTo(startsWith(EuropePmcClient.URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("format", UriUtils.encode("json", "UTF-8")))
                .andExpect(queryParam("query", UriUtils.encode("DOI:\"\"", "UTF-8")))
                .andRespond(withSuccess(noResultsResponse, MediaType.APPLICATION_JSON));

        assertThat(subject.getPublicationByDoi("")).isEmpty();

        mockServer.verify();
    }

    @Test
    void noResultForBlankPubmedId() {
        // Because of a bug in EuropePMC, our client doesn’t make a request to EuropePMC if the pubmed ID is blank.
        // Uncomment the following block and mockServer.verify() at the end if the bug is fixed:
        // var noResultsResponse =
        //         IOUtils.toString(this.getClass().getResourceAsStream("europepmc-no-results-response.json"), "UTF-8");
        // mockServer.expect(once(), requestTo(startsWith(EuropePmcClient.URL)))
        //         .andExpect(method(HttpMethod.GET))
        //         .andExpect(queryParam("format", URLEncoder.encode("json")))
        //         .andExpect(queryParam("query", URLEncoder.encode("SRC:MED AND EXT_ID:")))
        //         .andRespond(withSuccess(noResultsResponse, MediaType.APPLICATION_JSON));

        assertThat(subject.getPublicationByPubmedId("")).isEmpty();
        // mockServer.verify();
    }

}

