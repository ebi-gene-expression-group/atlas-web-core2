package uk.ac.ebi.atlas.experimentpage.link;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.ExperimentTest;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class ResourceLinkGeneratorTest {

    private static UriBuilder uriBuilder;
    private static Function<String, ExternallyAvailableContent.Description> createIcon;
    Experiment<?> experiment;

    private final ResourceLinkGenerator subject = spy(new ResourceLinkGenerator());

    @BeforeEach
    void setUp() {
        uriBuilder = mock(UriBuilder.class);
        Function<String, String> formatLabelToArchive =
                accession -> MessageFormat.format("Label text: {0}", accession);
        Function<String, ExternallyAvailableContent.Description> createArchiveIcon =
                label -> ExternallyAvailableContent.Description.create("icon-archive", label);
        createIcon = formatLabelToArchive.andThen(createArchiveIcon);
    }

    @Test
    void whenExperimentHasNoSecondaryAccessions_thenEmptyListReturnedForLinks() {
        var resourceTypeMapping = Map.ofEntries(
                entry("EGAD.*", "datasets"),
                entry("EGAS.*", "studies")
        );
        final List<String> secondaryAccessions = List.of();
        experiment = createTestExperiment(secondaryAccessions);

        var links = subject.getLinks(experiment, resourceTypeMapping, uriBuilder, createIcon);

        assertThat(links).isEmpty();
    }

    @Test
    void whenExperimentHasSecondaryAccessions_thenAListOfLinksReturned()
            throws URISyntaxException {
        var mockedUri = "https://example.org/foo";
        when(uriBuilder.build(any(), any())).thenReturn(new URI(mockedUri));
        doReturn(true).when(subject).isUriValid(any());

        var resourceTypeMapping = Map.ofEntries(
                entry("EGAD.*", "datasets"),
                entry("EGAS.*", "studies")
        );
        final List<String> matchingSecondaryAccessions = List.of("EGAD1234", "EGAS5678");
        experiment = createTestExperiment(matchingSecondaryAccessions);

        var links = subject.getLinks(experiment, resourceTypeMapping, uriBuilder, createIcon);

        assertThat(links).hasSize(matchingSecondaryAccessions.size());
    }

    @Test
    void whenExperimentHasSecondaryAccessions_onlyLinksReturnedThatMatchingGivenResourceTypeMapping()
            throws URISyntaxException {
        var mockedUri = "https://example.org/foo";
        when(uriBuilder.build(any(), any())).thenReturn(new URI(mockedUri));
        doReturn(true).when(subject).isUriValid(any());

        var resourceTypeMapping = Map.ofEntries(
                entry("EGAD.*", "datasets"),
                entry("EGAS.*", "studies")
        );
        var matchingSecondaryAccessions = List.of("EGAD1234", "EGAS5678");
        var notMatchingSecondaryAccessions = List.of("GSE5678");
        var secondaryAccessions = Stream.concat(matchingSecondaryAccessions.stream(),
                        notMatchingSecondaryAccessions.stream())
                .collect(Collectors.toList());

        experiment = createTestExperiment(secondaryAccessions);

        var links = subject.getLinks(experiment, resourceTypeMapping, uriBuilder, createIcon);

        assertThat(links).hasSize(matchingSecondaryAccessions.size());
    }

    @Test
    void whenURIisInvalid_thenReturnsFalse() throws URISyntaxException {
        var invalidURI = new URI("https://__notvalid___example.com/foo");

        assertThat(subject.isUriValid(invalidURI)).isFalse();
    }

    @Test
    void whenURIisValid_thenReturnsTrue() throws URISyntaxException {
        var validURI = new URI("https://bbc.co.uk/");

        assertThat(subject.isUriValid(validURI)).isTrue();
    }

    private static ExperimentTest.TestExperiment createTestExperiment(List<String> secondaryAccessions) {
        return new ExperimentBuilder.TestExperimentBuilder()
                .withSecondaryAccessions(secondaryAccessions)
                .build();
    }
}