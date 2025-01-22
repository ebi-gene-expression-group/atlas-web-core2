package uk.ac.ebi.atlas.experimentimport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.experimentimport.experimentdesign.ExperimentDesignFileWriterService;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;

@ExtendWith(MockitoExtension.class)
class ExperimentCrudTest {
    private final static Random RNG = ThreadLocalRandom.current();
    private final static int MAXIMUM_OF_EXPERIMENT_DTOS = 1000;

    private static class ExperimentCrudImpl extends ExperimentCrud {
        private ExperimentCrudImpl(ExperimentCrudDao experimentCrudDao,
                                   ExperimentDesignFileWriterService experimentDesignFileWriterService) {
            super(experimentCrudDao,
                  experimentDesignFileWriterService);
        }

        @Override
        public UUID createExperiment(String experimentAccession, boolean isPrivate) {
            return UUID.randomUUID();
        }

        @Override
        public void updateExperimentDesign(String experimentAccession) { }
    }

    @Mock
    private ExperimentCrudDao experimentCrudDaoMock;

    @Mock
    private ExperimentDesignFileWriterService experimentDesignFileWriterServiceMock;

    private ExperimentCrud subject;

    @BeforeEach
    void setUp() {
        subject = new ExperimentCrudImpl(experimentCrudDaoMock, experimentDesignFileWriterServiceMock);
    }

    @Test
    void returnEmptyWhenExperimentCannotBeFound() {
        when(experimentCrudDaoMock.readExperiment(anyString()))
                .thenReturn(null);
        assertThat(subject.readExperiment(generateRandomExperimentAccession()))
                .isEmpty();
    }

    @Test
    void returnListOfDtos() {
        var experimentDtos = IntStream.range(0, MAXIMUM_OF_EXPERIMENT_DTOS)
                .boxed()
                .map(__ -> ExperimentDtoTest.generateRandomExperimentDto())
                .collect(toImmutableSet());
        when(experimentCrudDaoMock.readExperiments())
                .thenReturn(experimentDtos.asList());

        assertThat(subject.readExperiments())
                .hasSameSizeAs(experimentDtos);
    }

    @Test
    void returnPresentWhenExperimentCanBeFound() {
        when(experimentCrudDaoMock.readExperiment(anyString()))
                .thenReturn(ExperimentDtoTest.generateRandomExperimentDto());
        assertThat(subject.readExperiment(generateRandomExperimentAccession()))
                .isPresent();
    }

    @Test
    void updateCallsUpdateInDao() {
        doNothing().when(experimentCrudDaoMock).updateExperimentPrivate(anyString(), anyBoolean());

        var experimentAccession = generateRandomExperimentAccession();
        var isPrivate = RNG.nextBoolean();
        subject.updateExperimentPrivate(experimentAccession, isPrivate);
        verify(experimentCrudDaoMock).updateExperimentPrivate(experimentAccession, isPrivate);
    }

    @Test
    void deleteCallsDeleteInDao() {
        doNothing().when(experimentCrudDaoMock).deleteExperiment(anyString());

        var experimentAccession = generateRandomExperimentAccession();
        subject.deleteExperiment(experimentAccession);
        verify(experimentCrudDaoMock).deleteExperiment(experimentAccession);
    }

    @Test
    void updateDesignCallsWriteInWriterService() throws IOException {
        doNothing()
                .when(experimentDesignFileWriterServiceMock)
                .writeExperimentDesignFile(anyString(), any(ExperimentType.class), any(ExperimentDesign.class));

        var experimentDto = ExperimentDtoTest.generateRandomExperimentDto();
        var experimentDesign = new ExperimentDesign();
        subject.updateExperimentDesign(experimentDesign, experimentDto);
        verify(experimentDesignFileWriterServiceMock)
                .writeExperimentDesignFile(
                        experimentDto.getExperimentAccession(),
                        experimentDto.getExperimentType(),
                        experimentDesign);
    }

    @Test
    void exceptionsThrownWhenWritingExperimentDesignsAreWrapped() throws IOException {
        doThrow(IOException.class)
                .when(experimentDesignFileWriterServiceMock).writeExperimentDesignFile(any(), any(), any());
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() ->
                        subject.updateExperimentDesign(
                                new ExperimentDesign(), ExperimentDtoTest.generateRandomExperimentDto()));
    }
}