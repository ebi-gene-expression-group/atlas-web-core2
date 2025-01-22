package uk.ac.ebi.atlas.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getCustomUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentCollectionLink;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentLink;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentSetLink;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsFilteredBySpeciesAndExperimentType;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsFilteredBySpeciesUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsSummaryImageUrl;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getLinkWithEmptyLabel;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfig.class)
class UrlHelpersIT {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    @Test
    @DisplayName("UrlHelpers is a stateless utility class and cannot be instantiated")
    void utilityClass() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(UrlHelpers::new);
    }

    @Test
    @DisplayName("Experiments table link filtered by species points at /experiments?species={species}")
    void speciesHaveDoubleQoutesInUrl() throws Exception {
        var species = generateRandomSpecies();

        assertThat(new URL(getExperimentsFilteredBySpeciesUrl(species.getReferenceName())))
                .hasPath("/experiments")
                .hasParameter("species", "\"" + species.getReferenceName() + "\"");
    }

    @Test
    @DisplayName("Experiments table link filtered by species and experiment type points at /experiments?species={species}&experimentType={type}")
    void speciesAndTypeUrl() throws Exception {
        var species = generateRandomSpecies();
        var type = ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)];

        assertThat(new URL(getExperimentsFilteredBySpeciesAndExperimentType(species.getReferenceName(), type.name())))
                .hasPath("/experiments")
                .hasParameter("species", species.getReferenceName())
                .hasParameter("experimentType", type.name());
    }

    @Test
    @DisplayName("Summary image links point at /resources/images/experiments-summary/{image}.png")
    void imageUrl() throws Exception {
        var imageFileName = randomAlphabetic(5, 20);

        assertThat(new URL(getExperimentsSummaryImageUrl(imageFileName)))
                .hasPath("/resources/images/experiments-summary/" + imageFileName + ".png");
    }

    @Test
    @DisplayName("Arbitrary relative paths are well formed")
    void customUrl() throws Exception {
        var path = "/" + randomAlphabetic(5, 20);

        assertThat(new URL(getCustomUrl(path)))
                .hasPath(path);
    }

    @Test
    @DisplayName("Arbitrary absolute URLs are well formed and use HTTPS by default")
    void fullyQualifiedCustomUrl() throws Exception {
        var path = "/" + randomAlphabetic(5, 20);
        var host = new URL(generateRandomUrl()).getHost();

        assertThat(new URL(getCustomUrl(host, path)))
                .hasProtocol("https")
                .hasHost(host)
                .hasPath(path);
    }

    @Test
    @DisplayName("Arbitrary links can contain no text")
    void linkWithEmptyLabel() throws Exception {
        var url = generateRandomUrl();

        assertThat(getLinkWithEmptyLabel(url))
                .extracting("left", "right")
                .contains(Optional.empty(), Optional.of(url));
    }

    @Test
    @DisplayName("Links to experiments set can contain no text and point at /experiments?experimentDescription={keyword}")
    void experimentSetLink() throws Exception {
        var keyword = randomAlphabetic(3, 5);

        var result = getExperimentSetLink(keyword);
        assertThat(result.getLeft())
                .isEmpty();
        assertThat(new URL(result.getRight().get()))
                .hasParameter("experimentDescription", keyword);
    }

    @Test
    @DisplayName("Experiment links without a host point at /experiments/{accession}")
    void experimentLink() throws Exception {
        var experimentAccession = generateRandomExperimentAccession();
        var label = randomAlphabetic(3, 20);

        var result = getExperimentLink(label, experimentAccession);
        assertThat(result.getLeft())
                .isEqualTo(label);
        assertThat(new URL(result.getRight().get()))
                .hasPath("/experiments/" + experimentAccession);
    }

    @Test
    @DisplayName("Absolute experiment links point at https://{host}/experiments/{accession}")
    void fullyQualifiedExperimentLink() throws Exception {
        var experimentAccession = generateRandomExperimentAccession();
        var label = randomAlphabetic(3, 20);
        var host = new URL(generateRandomUrl()).getHost();

        var result = getExperimentLink(host, label, experimentAccession);
        assertThat(result.getLeft())
                .isEqualTo(label);
        assertThat(new URL(result.getRight().get()))
                .hasProtocol("https")
                .hasHost(host)
                .hasPath("/experiments/" + experimentAccession);
    }

    @Test
    @DisplayName("Links to experiments can contain no text")
    void labelInExperimentLinkCanBeOmitted() throws Exception {
        var experimentAccession = generateRandomExperimentAccession();

        var result = getExperimentLink(experimentAccession);
        assertThat(result.getLeft())
                .isEmpty();
        assertThat(new URL(result.getRight().get()))
                .hasPath("/experiments/" + experimentAccession);
    }

    @Test
    @DisplayName("Links to experiment collections are of the form /experiments?experimentProjects=\"{project-name}\"")
    void experimentCollectionLinksHasTheRightShape() throws Exception {
        var label = randomAlphabetic(10, 15);
        var collectionDescription = randomAlphabetic(10, 20);

        var result = getExperimentCollectionLink(label, collectionDescription);
        assertThat(result.getLeft())
                .hasValue(label);
        assertThat(new URL(result.getRight().orElseThrow()))
                .hasPath("/experiments")
                .hasParameter("experimentProjects", "\"" + collectionDescription + "\"");
    }
}