package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesign;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;

import java.net.URI;
import java.util.Map;
import java.util.Random;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Map.entry;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.download.ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;

class LinkToArrayExpressIT {

    private LinkToArrayExpress subject;
    private ResourceLinkGenerator mockResourceLinkGenerator;

    @BeforeEach
    void setUp() {
        mockResourceLinkGenerator = Mockito.mock(ResourceLinkGenerator.class);
        subject = new LinkToArrayExpress(mockResourceLinkGenerator);
    }

    @Test
    void whenExperimentNotExistsInArrayExpress_thenNoLinksProvided() {
        when(mockResourceLinkGenerator.isUriValid(any(URI.class))).thenReturn(Boolean.FALSE);

        var differentialExperiment = new ExperimentBuilder.DifferentialExperimentBuilder().build();

        assertThat(subject.get(differentialExperiment)).isEmpty();
    }

    @Test
    void whenMicroArrayExperimentNotExistsInArrayExpress_thenNoLinksProvided() {
        when(mockResourceLinkGenerator.isUriValid(any(URI.class))).thenReturn(Boolean.FALSE);
        var microarrayExperiment =
                new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withArrayDesigns(ImmutableList.of(ArrayDesign.create(randomAlphanumeric(10))))
                        .build();

        var resourceLinks = subject.get(microarrayExperiment);

        assertThat(resourceLinks).isEmpty();
    }

    @Test
    void whenMicroArrayExperimentExistsInArrayExpress_thenCorrectLinksProvided() {
        when(mockResourceLinkGenerator.isUriValid(any(URI.class))).thenReturn(Boolean.TRUE);

        var arrayDesignSize = 10;
        var linkTypes = Map.ofEntries(
                entry("E-MEXP", "studies/"),
                entry("A-AFFY", "arrays/")
        );
        var microarrayExperiment = buildMicroarrayExperiment(arrayDesignSize);

        var resourceLinks = subject.get(microarrayExperiment);

        assertThat(resourceLinks).hasSize(arrayDesignSize + 1);
        for (ExternallyAvailableContent resourceLink : resourceLinks) {
            var link = resourceLink.uri.toString();
            var accessionPrefixFromLink = link.substring(link.lastIndexOf("/") + 1)
                    .substring(0, 6);
            var pathSegmentType = linkTypes.get(accessionPrefixFromLink);
            var expectedURLRegexp = ".*/arrayexpress/" + pathSegmentType + accessionPrefixFromLink + ".*";
            assertThat(link).matches(expectedURLRegexp);
        }
    }

    @Test
    void linksToArrayExpressShowInSupplementaryInformationTab() {
        assertThat(subject.contentType())
                .isEqualTo(SUPPLEMENTARY_INFORMATION);
    }

    private MicroarrayExperiment buildMicroarrayExperiment(int arrayDesignsSize) {
        Random rand = new Random();

        var aeAccessionPrefix = "E-MEXP-";
        var arrayDesignPrefix = "A-AFFY-";
        var accession = aeAccessionPrefix + Math.abs(rand.nextInt());
        var arrayDesigns = rand.ints(arrayDesignsSize, 1, 9999)
                .mapToObj(postfix -> arrayDesignPrefix + postfix)
                .map(ArrayDesign::create)
                .collect(toImmutableList());

        return new ExperimentBuilder.MicroarrayExperimentBuilder()
                        .withExperimentAccession(accession)
                        .withArrayDesigns(arrayDesigns)
                        .build();
    }
}
