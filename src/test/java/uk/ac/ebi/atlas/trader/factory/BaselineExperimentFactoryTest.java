package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentTest;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;
import uk.ac.ebi.atlas.model.resource.XmlFileConfigurationException;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.PROTEOMICS_BASELINE;
import static uk.ac.ebi.atlas.model.experiment.ExperimentType.RNASEQ_MRNA_BASELINE;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateFilterFactors;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomAssayGroups;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BaselineExperimentFactoryTest {
    private final static ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private final static int ASSAY_GROUPS_MAX = 10;
    private final static int FACTOR_TYPES_MAX = 5;
    private final static String EXPERIMENT_ACCESSION = generateRandomExperimentAccession();

    private Species species;

    private ExperimentDto experimentDto;
    private IdfParserOutput idfParserOutput;
    private ExperimentDesign experimentDesign;
    private ImmutableList<String> technologyType;

    @Mock
    private ExperimentConfiguration configurationMock;

    @Mock
    private BaselineExperimentConfiguration baselineConfigurationMock;

    @Mock
    private ConfigurationTrader configurationTraderMock;

    @Mock
    private ExperimentTrader experimentRepositoryMock;

    @Mock
    private SpeciesFactory speciesFactoryMock;

    private BaselineExperimentFactory subject;

    @BeforeEach
    void setUp() {
        ExperimentTest.TestExperiment experiment = new ExperimentBuilder.TestExperimentBuilder().build();
        String experimentAccession = generateRandomExperimentAccession();
        species = generateRandomSpecies();
        when(speciesFactoryMock.create(species.getName()))
                .thenReturn(species);

        experimentDto = new ExperimentDto(
                experimentAccession,
                ImmutableList.of(RNASEQ_MRNA_BASELINE, PROTEOMICS_BASELINE).get(RNG.nextInt(2)),
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

        when(configurationMock.getAssayGroups())
                .thenReturn(generateRandomAssayGroups(RNG.nextInt(1, ASSAY_GROUPS_MAX)));
        when(configurationTraderMock.getExperimentConfiguration(experimentAccession))
                .thenReturn(configurationMock);

        int factorTypeCount = RNG.nextInt(1, FACTOR_TYPES_MAX);
        ImmutableTriple<String, ImmutableSet<String>, ImmutableSet<Factor>> filterFactors =
                generateFilterFactors(factorTypeCount, RNG.nextInt(factorTypeCount));

        when(baselineConfigurationMock.getDisclaimer()).thenReturn("");
        when(baselineConfigurationMock.getDefaultFilterFactors()).thenReturn(filterFactors.getRight());
        when(baselineConfigurationMock.getDefaultQueryFactorType()).thenReturn(filterFactors.getLeft());
        when(baselineConfigurationMock.getMenuFilterFactorTypes()).thenReturn(filterFactors.getMiddle().asList());
        when(baselineConfigurationMock.isOrderCurated()).thenReturn(RNG.nextBoolean());
        when(baselineConfigurationMock.getDataProviderUrl()).thenReturn(ImmutableList.of());
        when(baselineConfigurationMock.getDataProviderDescription()).thenReturn(ImmutableList.of());
        when(baselineConfigurationMock.getExperimentDisplayName()).thenReturn(randomAlphabetic(20));

        when(experimentRepositoryMock.getPublicExperiment(EXPERIMENT_ACCESSION))
                .thenReturn(experiment);

        when(configurationTraderMock.getBaselineFactorsConfiguration(experimentAccession))
                .thenReturn(baselineConfigurationMock);

        subject = new BaselineExperimentFactory(configurationTraderMock, speciesFactoryMock, experimentRepositoryMock);
    }

    // ExperimentDto comes from DB
    // IdfParserOutput comes from IDF file
    // ExperimentConfiguration comes from <exp_accession>-configuration.xml
    // BaselineExperimentConfiguration comes from <exp_accession>-factors.xml
    @Test
    void experimentIsProperlyPopulatedFromDatabaseIdfFactorsAndConfiguration() {
        var result = subject.create(experimentDto, experimentDesign, idfParserOutput, technologyType);
        assertThat(result)
                .isInstanceOf(BaselineExperiment.class)
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
                        configurationMock.getAssayGroups(),
                        experimentDto.getPubmedIds(),
                        experimentDto.getDois(),
                        baselineConfigurationMock.getExperimentDisplayName(),
                        baselineConfigurationMock.getDisclaimer(),
                        experimentDto.isPrivate());
        assertThat(result.getTechnologyType())
                .containsExactlyInAnyOrderElementsOf(ImmutableSet.copyOf(technologyType));
        assertThat(result.getDataProviderURL())
                .containsExactlyInAnyOrderElementsOf(
                        ImmutableSet.copyOf(baselineConfigurationMock.getDataProviderUrl()));
        assertThat(result.getDataProviderURL())
                .containsExactlyInAnyOrderElementsOf(
                        ImmutableSet.copyOf(baselineConfigurationMock.getDataProviderDescription()));
    }

    @Test
    void whenAlternativeExperimentIsFromSameDataSourceButConfigurationFileNotExists_thenExceptionThrown() {
        when(configurationTraderMock.getBaselineFactorsConfiguration(any()))
                .thenThrow(XmlFileConfigurationException.class);

        assertThatExceptionOfType(XmlFileConfigurationException.class).isThrownBy(
                () -> subject.create(experimentDto, experimentDesign, idfParserOutput, technologyType));
    }

    @Test
    void whenAlternativeExperimentIsFromDifferentDataSourceButAlternativeViewNotExists_thenExceptionThrown() {
        final String alternativeViewAccession = "DIFF" + experimentDto.getExperimentAccession();
        when(baselineConfigurationMock.getAlternativeViews())
                .thenReturn(List.of(alternativeViewAccession));
        when(configurationTraderMock.getBaselineFactorsConfiguration(any()))
                .thenReturn(baselineConfigurationMock);

        when(experimentRepositoryMock.getExperimentType(any()))
                .thenThrow(ResourceNotFoundException.class);

        subject = new BaselineExperimentFactory(configurationTraderMock, speciesFactoryMock, experimentRepositoryMock);

        assertThat(subject.create(experimentDto, experimentDesign, idfParserOutput, technologyType)
                .getAlternativeViewDescriptions())
                .contains(BaselineExperimentFactory.DESCRIPTION_FOR_MISSING_ALT_VIEW + alternativeViewAccession);
    }

    @Test
    void whenAlternativeExperimentIsFromSameDataSource_thenReturnViewByDescription() {
        final String alternativeViewAccession = experimentDto.getExperimentAccession();
        when(baselineConfigurationMock.getAlternativeViews())
                .thenReturn(List.of(alternativeViewAccession));
        when(configurationTraderMock.getBaselineFactorsConfiguration(alternativeViewAccession))
                .thenReturn(baselineConfigurationMock);

        subject = new BaselineExperimentFactory(configurationTraderMock, speciesFactoryMock, experimentRepositoryMock);

        final String expectedAlternativeViewDescription = "View by "
                + baselineConfigurationMock.getDefaultQueryFactorType()
                .toLowerCase()
                .replace("_", " ");
        assertThat(subject.create(experimentDto, experimentDesign, idfParserOutput, technologyType)
                .getAlternativeViewDescriptions())
                .contains(expectedAlternativeViewDescription);
    }

    @Test
    void whenAlternativeExperimentIsFromDifferentDataSource_thenReturnRelatedByDescription() {
        final String alternativeViewAccession = "DIFF" + experimentDto.getExperimentAccession();
        when(baselineConfigurationMock.getAlternativeViews())
                .thenReturn(List.of(alternativeViewAccession));
        when(configurationTraderMock.getBaselineFactorsConfiguration(alternativeViewAccession))
                .thenReturn(baselineConfigurationMock);
        when(experimentRepositoryMock.getExperimentType(any()))
                .thenReturn(experimentDto.getExperimentType().name());

        subject = new BaselineExperimentFactory(configurationTraderMock, speciesFactoryMock, experimentRepositoryMock);

        final String expectedAlternativeViewDescription = "Related "
                + ExperimentType.valueOf(
                        experimentRepositoryMock.getExperimentType(alternativeViewAccession))
                    .getHumanDescription()
                + " experiment";
        assertThat(subject.create(experimentDto, experimentDesign, idfParserOutput, technologyType)
                .getAlternativeViewDescriptions())
                .contains(expectedAlternativeViewDescription);

    }

    @Test
    void throwIfExperimentTypeIsNotBaseline() {
        experimentDto = new ExperimentDto(
                generateRandomExperimentAccession(),
                Arrays.stream(ExperimentType.values())
                        .filter(type -> !type.isBaseline())
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
                () -> subject.create(experimentDto, experimentDesign, idfParserOutput, technologyType));
    }
}
