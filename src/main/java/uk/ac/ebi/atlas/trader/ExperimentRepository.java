package uk.ac.ebi.atlas.trader;

import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;

public interface ExperimentRepository {

    Experiment getExperiment(String experimentAccession);

    String getExperimentType(String experimentAccession);

    ExperimentDesign getExperimentDesign(String experimentAccession);
}
