package uk.ac.ebi.atlas.experimentpage.link;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToGeoIT {

    private static final String EXPECTED_DESCRIPTION_TYPE = "icon-geo";
    private static final String GEO_RESOURCE_DESCRIPTION = "GEO: ";

    private LinkToGeo subject;

    @BeforeEach
    void setUp() {
        subject = new LinkToGeo();
    }

    @Test
    void givenLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() {
        var secondaryAccessions = List.of("GSE150361", "GSE5454");
        var differentialExperiment =
                new ExperimentBuilder.DifferentialExperimentBuilder()
                        .withSecondaryAccessions(secondaryAccessions)
                        .build();

        assertThat(subject.get(differentialExperiment))
                .hasSize(secondaryAccessions.size())
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(secondaryAccessions.get(0)))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(secondaryAccessions.get(1)))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type()
                        .equals(EXPECTED_DESCRIPTION_TYPE));
    }

    @Test
    void givenExperimentHasDifferentGEOResources_ThenAvailableResourcesContainsCorrectGEOResourceLinks()
            throws URISyntaxException {
        Random rand = new Random();

        final String accessionParamName = "acc=";

        var secondaryAccessions = Stream.generate(() -> rand.nextBoolean() ? "GSE" : "GDS")
                .limit(20)
                .map(type -> type + Math.abs(rand.nextInt()))
                .collect(toImmutableList());
        var linkTypes = Map.ofEntries(
                entry("GSE", "redirect:https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc="),
                entry("GDS", "redirect:https://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=")
        );
        var experiment = new ExperimentBuilder.BaselineExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();

        var resourceLinks = subject.get(experiment);

        assertThat(resourceLinks).hasSize(secondaryAccessions.size());
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            var link = resourceLink.uri.toString();
            var accessionFromLink = link.substring(
                    link.lastIndexOf(accessionParamName) + accessionParamName.length());
            var accessionPrefixFromLink = accessionFromLink.substring(0, 3);
            var pathSegmentType = linkTypes.get(accessionPrefixFromLink);
            var expectedURL = pathSegmentType + accessionFromLink;
            ResourceLinkAssertionUtil.assertResourceLink(resourceLink,
                    expectedURL,
                    EXPECTED_DESCRIPTION_TYPE,
                    GEO_RESOURCE_DESCRIPTION + accessionFromLink);
        }
    }

    @Test
    void linksToGeoShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}
