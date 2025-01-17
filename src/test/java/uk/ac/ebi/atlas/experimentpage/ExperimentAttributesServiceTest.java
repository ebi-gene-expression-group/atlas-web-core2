package uk.ac.ebi.atlas.experimentpage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParser;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.experiments.ExperimentCellCountDao;
import uk.ac.ebi.atlas.model.Publication;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.testutils.MockExperiment;
import uk.ac.ebi.atlas.utils.EuropePmcClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentAttributesServiceTest {

    private static final String[] BASELINE_EXPERIMENT_ATTRIBUTES = {
            "experimentAccession", "experimentDescription", "type", "pubMedIds", "dois", "disclaimer",
            "pageDescription", "dataProviderURL", "dataProviderDescription", "alternativeViews",
            "alternativeViewDescriptions"
    };

    private static final String[] DIFFERENTIAL_EXPERIMENT_ATTRIBUTES = {"regulationValues", "contrasts"};

    private static final String[] MICROARRAY_EXPERIMENT_ATTRIBUTES = {"arrayDesignAccessions", "arrayDesignNames"};

    @Mock
    private EuropePmcClient europePmcClientMock;
    @Mock
    private IdfParser idfParser;
    @Mock
    private ExperimentCellCountDao experimentCellCountDaoMock;

    @InjectMocks
    private ExperimentAttributesService subject;

    private static final String EXPERIMENT_ACCESSION = "E-MTAB-5061";

    @Test
    void getAttributesForBaselineExperimentWithNoPublications() {
        when(europePmcClientMock.getPublicationByDoi(anyString())).thenReturn(Optional.empty());
        when(idfParser.parse(any()))
                .thenReturn(
                        new IdfParserOutput(
                                "title", ImmutableSet.of(),"description", ImmutableList.of(), 0, ImmutableList.of()));

        var baselineExperiment = MockExperiment.createBaselineExperiment("FOOBAR");
        var result = subject.getAttributes(baselineExperiment);

        assertThat(result)
                .containsKeys(BASELINE_EXPERIMENT_ATTRIBUTES)
                .doesNotContainKeys(DIFFERENTIAL_EXPERIMENT_ATTRIBUTES)
                .doesNotContainKeys(MICROARRAY_EXPERIMENT_ATTRIBUTES)
                .extracting("experimentAccession", "type", "publications")
                .contains("FOOBAR", ExperimentType.RNASEQ_MRNA_BASELINE.getHumanDescription(), ImmutableList.of());
    }

    @Test
    void getAttributesForBaselineExperimentWithPublicationsFromDois() {
        var dois = ImmutableList.of("100.100/doi", "999.100/another-doi");

        when(europePmcClientMock.getPublicationByDoi("100.100/doi"))
                .thenReturn(Optional.of(new Publication("", "100.100/doi", "Publication 1")));
        when(europePmcClientMock.getPublicationByDoi("999.100/another-doi"))
                .thenReturn(Optional.of(new Publication("", "999.100/another-doi", "Publication 2")));
        when(idfParser.parse(any()))
                .thenReturn(
                        new IdfParserOutput(
                                "title", ImmutableSet.of(),"description", ImmutableList.of(), 0, ImmutableList.of()));

        var baselineExperiment = MockExperiment.createBaselineExperiment(ImmutableList.of(), dois);
        var result = subject.getAttributes(baselineExperiment);

        assertThat(result).extracting("publications").isNotEmpty();
    }

    @Test
    void getAttributesForBaselineExperimentWithPublicationsFromPubmedIds() {
        var pubmedIds = ImmutableList.of("1123", "1235");

        when(europePmcClientMock.getPublicationByPubmedId("1123"))
                .thenReturn(Optional.of(new Publication("1123", "100.100/doi", "Publication 1")));
        when(europePmcClientMock.getPublicationByPubmedId("1235"))
                .thenReturn(Optional.of(new Publication("1235", "999.100/another-doi", "Publication 2")));
        when(idfParser.parse(any()))
                .thenReturn(new IdfParserOutput("title", ImmutableSet.of(),
                        "description", ImmutableList.of(), 0, ImmutableList.of()));

        var baselineExperiment = MockExperiment.createBaselineExperiment(pubmedIds, ImmutableList.of());
        var result = subject.getAttributes(baselineExperiment);

        assertThat(result).extracting("publications").isNotEmpty();
    }

    @Test
    void getAttributesForDifferentialExperiment() {
        when(idfParser.parse(any()))
                .thenReturn(new IdfParserOutput("title", ImmutableSet.of(),
                        "description", ImmutableList.of(), 0, ImmutableList.of()));

        var differentialExperiment = MockExperiment.createDifferentialExperiment();
        var result = subject.getAttributes(differentialExperiment);

        assertThat(result)
                .containsKeys(BASELINE_EXPERIMENT_ATTRIBUTES)
                .containsKeys(DIFFERENTIAL_EXPERIMENT_ATTRIBUTES)
                .doesNotContainKeys(MICROARRAY_EXPERIMENT_ATTRIBUTES);
    }

    @Test
    void getAttributesForMicroarrayExperiment() {
        when(idfParser.parse(any()))
                .thenReturn(new IdfParserOutput("title", ImmutableSet.of(),
                        "description", ImmutableList.of(), 0, ImmutableList.of()));

        var microarrayExperiment = MockExperiment.createMicroarrayExperiment();
        var result = subject.getAttributes(microarrayExperiment);

        assertThat(result)
                .containsKeys(BASELINE_EXPERIMENT_ATTRIBUTES)
                .containsKeys(MICROARRAY_EXPERIMENT_ATTRIBUTES)
                .doesNotContainKeys(DIFFERENTIAL_EXPERIMENT_ATTRIBUTES);
    }

    @Test
    @DisplayName("Valid cell count from database")
    void validCellCount() {
        var expectedCellCount = 100;
        when(experimentCellCountDaoMock.fetchNumberOfCellsByExperimentAccession(EXPERIMENT_ACCESSION))
                .thenReturn(expectedCellCount);

        assertThat(subject.getCellCount(EXPERIMENT_ACCESSION))
                .isEqualTo(expectedCellCount);
    }
}
