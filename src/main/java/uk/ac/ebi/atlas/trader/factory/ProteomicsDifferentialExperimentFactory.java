package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class ProteomicsDifferentialExperimentFactory implements ExperimentFactory<DifferentialExperiment> {
    private final ConfigurationTrader configurationTrader;
    private final SpeciesFactory speciesFactory;

    public ProteomicsDifferentialExperimentFactory(ConfigurationTrader configurationTrader,
                                                   SpeciesFactory speciesFactory) {
        this.configurationTrader = configurationTrader;
        this.speciesFactory = speciesFactory;
    }

    @Override
    public DifferentialExperiment create(ExperimentDto experimentDto,
                                         ExperimentDesign experimentDesign,
                                         IdfParserOutput idfParserOutput,
                                         Collection<String> technologyType) {
        checkArgument(
                experimentDto.getExperimentType().isProteomicsDifferential(),
                "Experiment type " + experimentDto.getExperimentType() +
                        " is not of type proteomics differential");

        ExperimentConfiguration experimentConfiguration =
                configurationTrader.getExperimentConfiguration(experimentDto.getExperimentAccession());
        var experimentalFactorHeaders = ImmutableSet.copyOf(experimentDesign.getFactorHeaders());

        return new DifferentialExperiment(
                experimentDto.getExperimentType(),
                experimentDto.getExperimentAccession(),
                idfParserOutput.getSecondaryAccessions(),
                idfParserOutput.getTitle(),
                experimentDto.getLoadDate(),
                experimentDto.getLastUpdate(),
                speciesFactory.create(experimentDto.getSpecies()),
                technologyType,
                experimentConfiguration.getContrastAndAnnotationPairs(),
                experimentalFactorHeaders,
                experimentDto.getPubmedIds(),
                experimentDto.getDois(),
                experimentDto.isPrivate(),
                experimentDto.getAccessKey());
    }
}
