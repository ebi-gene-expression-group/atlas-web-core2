package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
public class ResourceLinkGenerator {
    private static final WebClient webClient = WebClient.create();

    public ImmutableList<ExternallyAvailableContent> getLinks(Experiment<?> experiment,
                                                                     Map<String, String> resourceTypeMapping,
                                                                     UriBuilder uriBuilder,
                                                                     Function<String, ExternallyAvailableContent.Description> createIcon) {
        if (noSecondaryAccession(experiment)) {
            return ImmutableList.of();
        }

        return experiment.getSecondaryAccessions().stream()
                .filter(accession -> belongsToArchive(accession, resourceTypeMapping))
                .map(accession -> getResourceLink(resourceTypeMapping, uriBuilder, createIcon, accession))
                .filter(Objects::nonNull)
                .collect(ImmutableList.toImmutableList());
    }

    private boolean belongsToArchive(String accession, Map<String, String> resourceTypeMapping) {
        return resourceTypeMapping.entrySet().stream()
                .anyMatch(entry -> accession.matches(entry.getKey()));
    }

    private ExternallyAvailableContent getResourceLink(Map<String, String> resourceTypeMapping,
                                                              UriBuilder uriBuilder,
                                                              Function<String, ExternallyAvailableContent.Description> createIcon,
                                                              String accession) {
        var link = uriBuilder.build(getPathSegment(resourceTypeMapping, accession), accession);
        return isUriValid(link) ?
                new ExternallyAvailableContent(link.toString(), createIcon.apply(accession)) :
                null;
    }

    private boolean noSecondaryAccession(Experiment<?> experiment) {
        return experiment.getSecondaryAccessions() == null || experiment.getSecondaryAccessions().isEmpty();
    }

    private String getPathSegment(Map<String, String> resourceTypeMapping, String accession) {
        return resourceTypeMapping.entrySet().stream()
                .filter(entry -> accession.matches(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse("");
    }

    public boolean isUriValid(URI uri) {
        try {
            var response = webClient
                    .get()
                    .uri(uri)
                    .exchange()
                    .block();
            return response != null && !response.statusCode().isError();
        } catch (Exception e) {
            return false;
        }
    }
}
