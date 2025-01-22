package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.PROTEOMICS_DIFFERENTIAL;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProteomicsDifferentialExperimentFactoryTest {
    private final static ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private final static int CONTRASTS_MAX = 10;

    private Species species;

    private ExperimentDto experimentDto;
    private ExperimentDto proteomicsExperimentDto;
    private IdfParserOutput idfParserOutput;
    private ExperimentDesign experimentDesign;
    private ImmutableList<String> technologyType;

    @Mock
    private ExperimentConfiguration configurationMock;

    @Mock
    private ConfigurationTrader configurationTraderMock;

    @Mock
    private SpeciesFactory speciesFactoryMock;

    private ProteomicsDifferentialExperimentFactory subject;

    @BeforeEach
    void setUp() {
        String experimentAccession = generateRandomExperimentAccession();
        species = generateRandomSpecies();
        when(speciesFactoryMock.create(species.getName()))
                .thenReturn(species);

        proteomicsExperimentDto = new ExperimentDto(
                experimentAccession,
                PROTEOMICS_DIFFERENTIAL,
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

        experimentDesign = new ExperimentDesign();

        when(configurationTraderMock.getExperimentConfiguration(experimentAccession))
                .thenReturn(configurationMock);

        subject = new ProteomicsDifferentialExperimentFactory(configurationTraderMock, speciesFactoryMock);
    }

    // ExperimentDto comes from DB
    // IdfParserOutput comes from IDF file
    // ExperimentConfiguration comes from <exp_accession>-configuration.xml
    @Test
    void experimentIsProperlyPopulatedFromDatabaseIdfFactorsAndConfiguration() {
        ImmutableList<Contrast> contrasts = generateRandomContrasts(RNG.nextInt(1, CONTRASTS_MAX), false);
        when(configurationMock.getContrastAndAnnotationPairs())
                .thenReturn(
                        contrasts.stream()
                                .map(contrast -> Pair.of(contrast, RNG.nextBoolean()))
                                .collect(toImmutableList()));

        var proteomicsResult = subject.create(proteomicsExperimentDto, experimentDesign, idfParserOutput, technologyType);
        assertThat(proteomicsResult)
                .isInstanceOf(DifferentialExperiment.class)
                .isNotInstanceOf(MicroarrayExperiment.class)
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
                        proteomicsExperimentDto.getExperimentType(),
                        idfParserOutput.getTitle(),
                        proteomicsExperimentDto.getLastUpdate(),
                        species,
                        contrasts,
                        proteomicsExperimentDto.getPubmedIds(),
                        proteomicsExperimentDto.getDois(),
                        proteomicsExperimentDto.getExperimentAccession(),
                        "",
                        proteomicsExperimentDto.isPrivate());
        assertThat(proteomicsResult.getTechnologyType())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(technologyType));
        assertThat(proteomicsResult.getDataProviderURL())
                .isEmpty();
        assertThat(proteomicsResult.getDataProviderURL())
                .isEmpty();
    }

    @Test
    void throwIfExperimentTypeIsNotProteomicsDifferential() {
        experimentDto = new ExperimentDto(
                generateRandomExperimentAccession(),
                Arrays.stream(ExperimentType.values())
                        .filter(type -> !type.isProteomicsBaseline())
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
                () -> subject.create(experimentDto, experimentDesign, idfParserOutput, technologyType)
        );
    }
}
