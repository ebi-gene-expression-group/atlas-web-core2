package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomPrideExperimentAccession;

@ExtendWith(MockitoExtension.class)
class LinkToPrideIT {
    private LinkToPride subject;

    @Mock
    BaselineExperiment baselineExperimentMock;

    @BeforeEach
    void setUp() {
        subject = new LinkToPride();
    }

    private static final String PRIDE_URI = "redirect:https://www.ebi.ac.uk/pride/archive/projects/";
    private static final String EXPECTED_DESCRIPTION_TYPE = "icon-pride";
    private static final String PRIDE_DESCRIPTION = "PRIDE Archive: project ";

    @Test
    void givenLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() throws URISyntaxException {
        var secondaryAccessions = List.of(generateRandomPrideExperimentAccession());
        var experiment = new ExperimentBuilder.TestExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();

        var resourceLinks = subject.get(experiment);

        final String secondaryAccession = secondaryAccessions.get(0);
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            ResourceLinkAssertionUtil.assertResourceLink(resourceLink,
                    PRIDE_URI + secondaryAccession,
                    EXPECTED_DESCRIPTION_TYPE,
                    PRIDE_DESCRIPTION + secondaryAccession);
        }
    }

    @Test
    void givenMultipleLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() throws URISyntaxException {
        Random rand = new Random();

        var secondaryAccessions = rand.ints(20, 0, 9999)
                .mapToObj(index -> "PXD" + index)
                .collect(toImmutableList());

        var experiment = new ExperimentBuilder.TestExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();

        var resourceLinks = subject.get(experiment);

        assertThat(resourceLinks).hasSize(secondaryAccessions.size());

        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            var link = resourceLink.uri.toString();
            var accessionPrefixFromLink = link.substring(
                            link.lastIndexOf("/") + 1);
            ResourceLinkAssertionUtil.assertResourceLink(resourceLink,
                    PRIDE_URI + accessionPrefixFromLink,
                    EXPECTED_DESCRIPTION_TYPE,
                    PRIDE_DESCRIPTION + accessionPrefixFromLink);
        }
    }

    @Test
    void whenNoExternalResourceAvailableForExperiment_NoLinksAndIconPointAtPride() {
        when(baselineExperimentMock.getSecondaryAccessions()).thenReturn(ImmutableSet.of());
        assertThat(subject.get(baselineExperimentMock))
                .hasSize(0);
    }

    @Test
    void whenExternalResourceAvailableToPrideExperiment_thenShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }
}
