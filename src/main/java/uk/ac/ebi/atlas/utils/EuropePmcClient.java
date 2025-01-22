package uk.ac.ebi.atlas.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.atlas.model.Publication;

import java.io.IOException;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class EuropePmcClient {
    static final String URL = "https://www.ebi.ac.uk/europepmc/webservices/rest/search";
    private UriComponentsBuilder uriComponentsBuilder =
            UriComponentsBuilder.fromUriString(URL)
                    .queryParam("format", "json")
                    .queryParam("query", "{queryValue}");

    private RestTemplate restTemplate;
    private ObjectMapper mapper;

    @Autowired
    public EuropePmcClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.mapper = new ObjectMapper();
    }

    public Optional<Publication> getPublicationByDoi(String doi) {
        // Enclose query in quotes as EuropePmc only searches up to the slash for DOIs not enclosed in quotes
        return parseResponseWithOneResult("DOI:" + "\"" + doi + "\"");
    }

    public Optional<Publication> getPublicationByPubmedId(String pubmedId) {
        // Currently a query for an empty PubMed ID incorrectly returns a result; you can try it yourself:
        // https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=SRC:MED%20AND%20EXT_ID:&format=json
        // "resultList":{"result":[{"id":"35811804","source":"MED","pmid":"35811804","pmcid":"PMC9218589"...
        // You can remove this check and this comment if EuropePmc fix this bug
        if (isEmpty(pubmedId)) {
            return Optional.empty();
        }

        return parseResponseWithOneResult("SRC:MED AND EXT_ID:" + pubmedId);
    }

    private Optional<Publication> parseResponseWithOneResult(String queryValue) {
        try {
            var response = restTemplate.getForEntity(uriComponentsBuilder.build(queryValue), String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                try {
                    var responseAsJson = mapper.readTree(response.getBody());

                    if (responseAsJson.has("resultList")) {
                        var publicationResultList = responseAsJson.get("resultList").get("result");

                        if (publicationResultList.has(0)) {
                            return Optional.of(mapper.readValue(publicationResultList.get(0).toString(), Publication.class));
                        } else {
                            return Optional.empty();
                        }
                    }

                } catch (IOException e) {
                    return Optional.empty();
                }
            }
        } catch (RestClientException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }
}
