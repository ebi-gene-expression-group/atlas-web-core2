package uk.ac.ebi.atlas.trader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.TestExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;

@ExtendWith(MockitoExtension.class)
class ExperimentTraderTest {
    private static final Random RNG = ThreadLocalRandom.current();
    private static final int MAX_EXPERIMENTS = 1000;

    @Mock
    private ExperimentTraderDao experimentTraderDaoMock;

    @Mock
    private ExperimentRepository experimentRepositoryMock;

    private ExperimentTrader subject;

    @BeforeEach
    void setUp() {
        subject = new ExperimentTrader(experimentTraderDaoMock, experimentRepositoryMock);
    }

    @Test
    void analyticsRetrievesPrivateExperiments() {
        var experiment = new TestExperimentBuilder().withPrivate(true).build();
        when(experimentRepositoryMock.getExperiment(experiment.getAccession()))
                .thenReturn(experiment);

        assertThat(subject.getExperimentForAnalyticsIndex(experiment.getAccession()))
                .isEqualTo(experiment)
                .hasFieldOrPropertyWithValue("isPrivate", true);
    }

    @Test
    void publicExperimentsDontNeedAccessKey() {
        var experiment = new TestExperimentBuilder().withPrivate(false).build();
        when(experimentRepositoryMock.getExperiment(experiment.getAccession()))
                .thenReturn(experiment);

        assertThat(subject.getPublicExperiment(experiment.getAccession()))
                .isEqualTo(experiment)
                .hasFieldOrPropertyWithValue("isPrivate", false);
    }

    @Test
    void privateExperimentsNeedTheRightAccessKey() {
        var experiment = new TestExperimentBuilder().withPrivate(true).build();
        when(experimentRepositoryMock.getExperiment(experiment.getAccession()))
                .thenReturn(experiment);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> subject.getPublicExperiment(experiment.getAccession()));
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> subject.getExperiment(experiment.getAccession(), generateBlankString()));
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> subject.getExperiment(experiment.getAccession(), UUID.randomUUID().toString()));

        assertThat(subject.getExperiment(experiment.getAccession(), experiment.getAccessKey()))
                .isEqualTo(experiment);
    }

    @Test
    void accessKeyIsCaseInsensitive() {
        var experiment = new TestExperimentBuilder().withPrivate(true).build();
        when(experimentRepositoryMock.getExperiment(experiment.getAccession()))
                .thenReturn(experiment);

        assertThat(subject.getExperiment(experiment.getAccession(), experiment.getAccessKey().toLowerCase()))
                .isEqualTo(subject.getExperiment(experiment.getAccession(), experiment.getAccessKey().toUpperCase()))
                .isEqualTo(experiment);
    }

    @Test
    void canReturnPublicExperimentsByType() {
        var experiments = IntStream.range(0, RNG.nextInt(MAX_EXPERIMENTS)).boxed()
                .map(__ -> new TestExperimentBuilder().withPrivate(false).build())
                .collect(toImmutableSet());

        experiments.forEach(experiment ->
                when(experimentRepositoryMock.getExperiment(experiment.getAccession())).thenReturn(experiment));

        experiments.stream()
                .map(Experiment::getType)
                .distinct()
                .forEach(experimentType -> {
                    when(experimentTraderDaoMock.fetchPublicExperimentAccessions(experimentType))
                            .thenReturn(
                                    experiments.stream()
                                            .filter(experiment -> experiment.getType().equals(experimentType))
                                            .map(Experiment::getAccession)
                                            .collect(toImmutableSet()));
                });

        experiments.stream()
                .map(Experiment::getType)
                .distinct()
                .forEach(type ->
                        assertThat(subject.getPublicExperiments(type))
                                .extracting("type")
                                .containsOnly(type));

        assertThat(experiments.stream().map(Experiment::getType).distinct())
                .allSatisfy(type ->
                        assertThat(subject.getPublicExperiments(type)).extracting("type").containsOnly(type));
    }

    @Test
    void ifNoTypeIsSpecifiedReturnAllPublicExperiments() {
        var experiments = IntStream.range(0, RNG.nextInt(MAX_EXPERIMENTS)).boxed()
                .map(__ -> new TestExperimentBuilder().build())
                .collect(toImmutableSet());

        when(experimentTraderDaoMock.fetchPublicExperimentAccessions())
                .thenReturn(
                        experiments.stream()
                                .filter(experiment -> !experiment.isPrivate())
                                .map(Experiment::getAccession)
                                .collect(toImmutableSet()));

        experiments.stream()
                .filter(experiment -> !experiment.isPrivate())
                .forEach(experiment ->
                when(experimentRepositoryMock.getExperiment(experiment.getAccession())).thenReturn(experiment));

        assertThat(subject.getPublicExperiments())
                .containsExactlyInAnyOrderElementsOf(
                        experiments.stream()
                                .filter(experiment -> !experiment.isPrivate())
                                .collect(toImmutableSet()));
    }

    @Test
    void whenExperimentsDoesNotExists_thenThrowException() {
        var experiment = new TestExperimentBuilder().build();
        var experimentAccession = experiment.getAccession();
        when(experimentRepositoryMock.getExperimentType(experimentAccession))
                .thenThrow(ResourceNotFoundException.class);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(
                        () -> subject.getExperimentType(experimentAccession)
                );
    }

    @Test
    void whenExperimentExists_thenReturnsItsType() {
        var experiment = new TestExperimentBuilder().build();
        var experimentAccession = experiment.getAccession();
        final String originalExperimentType = experiment.getType().name();
        when(experimentRepositoryMock.getExperimentType(experimentAccession))
                .thenReturn(originalExperimentType);

        var experimentType = subject.getExperimentType(experimentAccession);
        assertThat(experimentType).isEqualTo(originalExperimentType);
        assertThat(ExperimentType.valueOf(experimentType)).isEqualTo(experiment.getType());
    }
}