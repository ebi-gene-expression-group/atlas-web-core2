package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sample.Cell;
import uk.ac.ebi.atlas.model.experiment.singlecell.SingleCellBaselineExperiment;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.naturalOrder;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.SINGLE_CELL_RNASEQ_MRNA_BASELINE;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomRnaSeqRunId;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SingleCellBaselineExperimentFactoryTest {
    private final static ThreadLocalRandom RNG = ThreadLocalRandom.current();

    private Species species;

    private ExperimentDto experimentDto;
    private IdfParserOutput idfParserOutput;
    private ImmutableSortedSet<String> cellIds;
    private ImmutableList<String> technologyType;

    @Mock
    private ExperimentDesign experimentDesignMock;

    @Mock
    private SpeciesFactory speciesFactoryMock;

    private SingleCellBaselineExperimentFactory subject;

    @BeforeEach
    void setUp() {
        String experimentAccession = generateRandomExperimentAccession();
        species = generateRandomSpecies();
        when(speciesFactoryMock.create(species.getName()))
                .thenReturn(species);

        experimentDto = new ExperimentDto(
                experimentAccession,
                SINGLE_CELL_RNASEQ_MRNA_BASELINE,
                species.getName(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                new Timestamp(new Date().getTime()),
                new Timestamp(new Date().getTime()),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());

        idfParserOutput = new IdfParserOutput(
                randomAlphabetic(20),
                ImmutableSet.of(),
                randomAlphabetic(100),
                ImmutableList.of(),
                RNG.nextInt(20),
                ImmutableList.of());

        technologyType = ImmutableList.of(randomAlphabetic(20), randomAlphabetic(20));

        cellIds =
                IntStream.range(0, 1000).boxed()
                        .map(__ -> generateRandomRnaSeqRunId())
                        .collect(toImmutableSortedSet(naturalOrder()));
        when(experimentDesignMock.getAllRunOrAssay())
                .thenReturn(cellIds);

        subject = new SingleCellBaselineExperimentFactory(speciesFactoryMock);
    }

    // ExperimentDto comes from DB
    // IdfParserOutput comes from IDF file
    // ExperimentConfiguration comes from <exp_accession>-configuration.xml
    @Test
    void experimentIsProperlyPopulatedFromDatabaseIdfFactorsAndConfiguration() {
        when(experimentDesignMock.getFactorHeaders()).thenReturn(ImmutableSet.of());
        var result = subject.create(experimentDto, experimentDesignMock, idfParserOutput, technologyType);
        assertThat(result)
                .isInstanceOf(SingleCellBaselineExperiment.class)
                .extracting(
                        "type",
                        "description",
                        "lastUpdate",
                        "species",
                        "dataColumnDescriptors",
                        "pubMedIds",
                        "dois",
                        "displayName",
                        "disclaimer",
                        "private")
                .containsExactly(
                        experimentDto.getExperimentType(),
                        idfParserOutput.getTitle(),
                        experimentDto.getLastUpdate(),
                        species,
                        cellIds.stream().map(Cell::new).collect(toImmutableList()),
                        experimentDto.getPubmedIds(),
                        experimentDto.getDois(),
                        experimentDto.getExperimentAccession(),
                        "",
                        experimentDto.isPrivate());
        assertThat(result.getTechnologyType())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(technologyType));
        assertThat(result.getDataProviderURL())
                .isEmpty();
        assertThat(result.getDataProviderURL())
                .isEmpty();
    }

    @Test
    void throwIfExperimentTypeIsNotSingleCell() {
        experimentDto = new ExperimentDto(
                generateRandomExperimentAccession(),
                Arrays.stream(ExperimentType.values())
                        .filter(type -> !type.isSingleCell())
                        .findAny()
                        .orElseThrow(RuntimeException::new),
                species.getName(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                new Timestamp(new Date().getTime()),
                new Timestamp(new Date().getTime()),
                RNG.nextBoolean(),
                UUID.randomUUID().toString());

        assertThatIllegalArgumentException().isThrownBy(
                () -> subject.create(experimentDto, experimentDesignMock, idfParserOutput, technologyType));
    }
}
