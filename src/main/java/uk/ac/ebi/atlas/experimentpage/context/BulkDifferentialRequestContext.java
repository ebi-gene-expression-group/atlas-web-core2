package uk.ac.ebi.atlas.experimentpage.context;

import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.web.DifferentialRequestPreferences;

public class BulkDifferentialRequestContext
       extends DifferentialRequestContext<DifferentialExperiment, DifferentialRequestPreferences> {
    public BulkDifferentialRequestContext(DifferentialRequestPreferences requestPreferences, DifferentialExperiment experiment) {
        super(requestPreferences, experiment);
    }
}
