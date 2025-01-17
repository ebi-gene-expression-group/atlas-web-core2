package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.resource.XmlFileConfigurationException;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class BaselineExperimentFactory implements ExperimentFactory<BaselineExperiment> {

    public static final String DESCRIPTION_FOR_MISSING_ALT_VIEW = "Missing alternative view: ";
    private static final Logger LOGGER = LoggerFactory.getLogger(BaselineExperimentFactory.class);
    private final ConfigurationTrader configurationTrader;
    private final SpeciesFactory speciesFactory;
    private final ExperimentTrader experimentRepository;

    @Autowired
    public BaselineExperimentFactory(ConfigurationTrader configurationTrader,
                                     SpeciesFactory speciesFactory,
                                     @Lazy ExperimentTrader experimentRepository) {
        this.configurationTrader = configurationTrader;
        this.speciesFactory = speciesFactory;
        this.experimentRepository = experimentRepository;
    }

    @Override
    public BaselineExperiment create(ExperimentDto experimentDto,
                                     ExperimentDesign experimentDesign,
                                     IdfParserOutput idfParserOutput,
                                     Collection<String> technologyType) {
        checkArgument(
                experimentDto.getExperimentType().isBaseline(),
                "Experiment type " + experimentDto.getExperimentType() + " is not of type baseline");

        var configuration = configurationTrader.getExperimentConfiguration(experimentDto.getExperimentAccession());
        var factorsConfig = configurationTrader.getBaselineFactorsConfiguration(experimentDto.getExperimentAccession());
        var alternativeViewAccessions = factorsConfig.getAlternativeViews();
        var alternativeViewDescriptions = extractAlternativeViewDescriptions(experimentDto.getExperimentAccession(), alternativeViewAccessions);
        var experimentalFactorHeaders = ImmutableSet.copyOf(experimentDesign.getFactorHeaders());
        var assayId2Factor = ImmutableMap.copyOf(experimentDesign.getAssayId2FactorMap());

        return new BaselineExperiment(
                experimentDto.getExperimentType(),
                experimentDto.getExperimentAccession(),
                idfParserOutput.getSecondaryAccessions(),
                idfParserOutput.getTitle(),
                experimentDto.getLoadDate(),
                experimentDto.getLastUpdate(),
                speciesFactory.create(experimentDto.getSpecies()),
                technologyType,
                configuration.getAssayGroups(),
                experimentalFactorHeaders,
                experimentDto.getPubmedIds(),
                experimentDto.getDois(),
                factorsConfig.getExperimentDisplayName(),
                factorsConfig.getDisclaimer(),
                factorsConfig.getDataProviderUrl(),
                factorsConfig.getDataProviderDescription(),
                alternativeViewAccessions,
                alternativeViewDescriptions,
                ExperimentDisplayDefaults.create(
                        factorsConfig.getDefaultQueryFactorType(),
                        factorsConfig.getDefaultFilterFactors(),
                        factorsConfig.getMenuFilterFactorTypes(),
                        factorsConfig.isOrderCurated()),
                experimentDto.isPrivate(),
                experimentDto.getAccessKey(),
                assayId2Factor);
    }

    private Collection<String>
    extractAlternativeViewDescriptions(String experimentAccession, List<String> alternativeViewAccessions) {
        final List<String> alternativeViewDescriptions = new ArrayList<>();

        alternativeViewAccessions.forEach(alternativeAccession -> {
            try {
                alternativeViewDescriptions.add(isFromSameDataSource(experimentAccession, alternativeAccession) ?
                        "View by " + getDefaultFactorTypeForAlternativeView(alternativeAccession)
                        :
                        "Related " + getExperimentType(alternativeAccession) + " experiment");
            } catch (ResourceNotFoundException e) {
                LOGGER.error("Experiment: {} has a "
                        + DESCRIPTION_FOR_MISSING_ALT_VIEW.toLowerCase() + alternativeAccession, experimentAccession);
                alternativeViewDescriptions.add(DESCRIPTION_FOR_MISSING_ALT_VIEW + alternativeAccession);
            } catch (XmlFileConfigurationException e) {
                LOGGER.error("Experiment: {} has an alternative view {} that has a missing configuration file.",
                        experimentAccession, alternativeAccession);
                alternativeViewDescriptions.add(alternativeAccession);
            }
        });

        return alternativeViewDescriptions;
    }

    private String getExperimentType(String alternativeAccession) {
        return ExperimentType.valueOf(experimentRepository.getExperimentType(alternativeAccession)).getHumanDescription();
    }

    private static boolean isFromSameDataSource(String experimentAccession, String altViewAccession) {
        return altViewAccession.substring(2, 6).equals(experimentAccession.substring(2, 6));
    }

    private String getDefaultFactorTypeForAlternativeView(String altViewAccession) {
        return configurationTrader.getBaselineFactorsConfiguration(altViewAccession)
                .getDefaultQueryFactorType()
                .toLowerCase()
                .replace("_", " ");
    }
}
