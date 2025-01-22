package uk.ac.ebi.atlas.experimentpage.link;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToEnaIT {

    private static final String EXPECTED_DESCRIPTION_TYPE = "icon-ena";
    private static final String ENA_RESOURCE_DESCRIPTION = "ENA: ";

    private LinkToEna subject;

    @BeforeEach
    void setUp() {
        subject = new LinkToEna();
    }

    @Test
    void givenLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() {
        var enaAccessions= List.of("ERP4545", "ERP4546");
        var RnaSeqBaselineExperiment =
                new ExperimentBuilder.BaselineExperimentBuilder()
                        .withSecondaryAccessions(enaAccessions)
                        .build();

        assertThat(subject.get(RnaSeqBaselineExperiment))
                .hasSize(enaAccessions.size())
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(enaAccessions.get(0)))
                .anyMatch(externallyAvailableContent ->
                        externallyAvailableContent.uri.toString().endsWith(enaAccessions.get(1)))
                .anyMatch(externallyAvailableContent -> externallyAvailableContent.description.type()
                        .equals(EXPECTED_DESCRIPTION_TYPE));
    }

    @Test
    void givenExperimentHasDifferentENAResources_ThenAvailableResourcesContainsCorrectENAResourceLinks()
            throws URISyntaxException {
        Random rand = new Random();

        var secondaryAccessions =
                Stream.generate(() -> rand.nextBoolean() ? "ERP" : rand.nextBoolean() ? "SRP" : "DRP")
                        .limit(20)
                        .map(type -> type + Math.abs(rand.nextInt()))
                        .collect(toImmutableList());
        var pathSegment = "redirect:https://www.ebi.ac.uk/ena/browser/view/";

        var experiment = new ExperimentBuilder.BaselineExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();

        var resourceLinks = subject.get(experiment);

        assertThat(resourceLinks).hasSize(secondaryAccessions.size());
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            var link = resourceLink.uri.toString();
            var accessionFromLink = link.substring(link.lastIndexOf("/") + 1);
            var expectedURL = pathSegment + accessionFromLink;
            ResourceLinkAssertionUtil.assertResourceLink(resourceLink,
                    expectedURL,
                    EXPECTED_DESCRIPTION_TYPE,
                    ENA_RESOURCE_DESCRIPTION + accessionFromLink);
        }
    }

    @Test
    void linksToEnaShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

}