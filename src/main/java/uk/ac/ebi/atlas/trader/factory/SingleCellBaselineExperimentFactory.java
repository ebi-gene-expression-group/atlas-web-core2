package uk.ac.ebi.atlas.trader.factory;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.ExperimentDto;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParserOutput;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.singlecell.SingleCellBaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.sample.Cell;
import uk.ac.ebi.atlas.species.SpeciesFactory;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

@Component
public class SingleCellBaselineExperimentFactory implements ExperimentFactory<SingleCellBaselineExperiment> {
    private final SpeciesFactory speciesFactory;

    public SingleCellBaselineExperimentFactory(SpeciesFactory speciesFactory) {
        this.speciesFactory = speciesFactory;
    }

    @Override
    public SingleCellBaselineExperiment create(ExperimentDto experimentDto,
                                               ExperimentDesign experimentDesign,
                                               IdfParserOutput idfParserOutput,
                                               Collection<String> technologyType) {
        checkArgument(
                experimentDto.getExperimentType().isSingleCell(),
                "Experiment type " + experimentDto.getExperimentType() + " is not of type single cell");

        return new SingleCellBaselineExperiment(
                experimentDto.getExperimentType(),
                experimentDto.getExperimentAccession(),
                idfParserOutput.getSecondaryAccessions(),
                idfParserOutput.getTitle(),
                experimentDto.getLoadDate(),
                experimentDto.getLastUpdate(),
                speciesFactory.create(experimentDto.getSpecies()),
                technologyType,
                getCells(experimentDesign),
                getExperimentalFactorHeaders(experimentDesign),
                experimentDto.getPubmedIds(),
                experimentDto.getDois(),
                "",
                experimentDto.isPrivate(),
                experimentDto.getAccessKey());
    }

    private ImmutableSet<String> getExperimentalFactorHeaders(ExperimentDesign experimentDesign) {
        return ImmutableSet.copyOf(experimentDesign.getFactorHeaders());
    }

    private List<Cell> getCells(ExperimentDesign experimentDesign) {
        return experimentDesign.getAllRunOrAssay().stream().map(Cell::new).collect(toList());
    }
}
