package uk.ac.ebi.atlas.experiments;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.testutils.MockExperiment;
import uk.ac.ebi.atlas.trader.ExperimentTrader;
import uk.ac.ebi.atlas.trader.ExperimentTraderDao;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentJsonServiceTest {
    private static final String EXPERIMENT_ACCESSION = generateRandomExperimentAccession();

    @Mock
    private ExperimentTrader experimentTraderMock;

    @Mock
    private ExperimentJsonSerializer experimentJsonSerializerMock;

    private ExperimentJsonService subject;

    @Before
    public void setUp() throws Exception {
        var experiment = MockExperiment.createBaselineExperiment(EXPERIMENT_ACCESSION);

        when(experimentTraderMock.getPublicExperiments())
                .thenReturn(ImmutableSet.of(experiment));
        when(experimentJsonSerializerMock.serialize(experiment))
                .thenReturn(getMockSerializedExperiment(experiment));

        subject = new ExperimentJsonService(experimentTraderMock, experimentJsonSerializerMock);
    }

    @Test
    public void sizeIsRightForNonParameterisedExperimentJsonMethod() {
        assertThat(subject.getPublicExperimentsJson()).hasSize(1);
    }

    @Test
    public void formatIsInSyncWithWhatWeExpectAndTheDataOfMockBaselineExperiment() {
        var result = subject.getPublicExperimentsJson().iterator().next();

        assertThat(result.get("experimentType").getAsString()).isEqualTo("Baseline");
        assertThat(result.get("experimentAccession").getAsString()).isEqualToIgnoringCase(EXPERIMENT_ACCESSION);
        assertThat(result.get("experimentDescription").getAsString()).isNotEmpty();
        assertThat(result.get("loadDate").getAsString()).isNotEmpty();
        assertThat(result.get("lastUpdate").getAsString()).isNotEmpty();
        assertThat(result.get("numberOfAssays").getAsInt()).isGreaterThan(0);

        assertThat(result.get("species").getAsString()).isNotEmpty();
        assertThat(result.get("kingdom").getAsString()).isNotEmpty();

        assertThat(result.has("experimentalFactors")).isTrue();
    }

    private JsonObject getMockSerializedExperiment(Experiment<?> experiment) {
        var jsonObject = new JsonObject();

        jsonObject.addProperty("experimentAccession", experiment.getAccession());
        jsonObject.addProperty("experimentDescription", experiment.getDescription());
        jsonObject.addProperty("species", experiment.getSpecies().getName());
        jsonObject.addProperty("kingdom", experiment.getSpecies().getKingdom());
        jsonObject.addProperty(
                "loadDate", new SimpleDateFormat("dd-MM-yyyy")
                        .format(experiment.getLoadDate()));
        jsonObject.addProperty(
                "lastUpdate", new SimpleDateFormat("dd-MM-yyyy")
                        .format(experiment.getLastUpdate()));
        jsonObject.addProperty(
                "numberOfAssays", experiment.getAnalysedAssays().size());
        jsonObject.addProperty(
                "rawExperimentType", experiment.getType().toString());
        jsonObject.addProperty(
                "experimentType", experiment.getType().isBaseline() ? "Baseline" : "Differential");
        jsonObject.add("technologyType", GSON.toJsonTree(experiment.getTechnologyType()));
        jsonObject.add(
                "experimentalFactors",
                GSON.toJsonTree(experiment.getExperimentalFactorHeaders()));
        jsonObject.add(
                "experimentProjects",
                GSON.toJsonTree(List.of()));
        return jsonObject;
    }
}
