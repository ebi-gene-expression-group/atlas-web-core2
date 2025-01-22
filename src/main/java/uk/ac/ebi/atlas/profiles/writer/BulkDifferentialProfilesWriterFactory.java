package uk.ac.ebi.atlas.profiles.writer;

import org.springframework.stereotype.Controller;
import uk.ac.ebi.atlas.experimentpage.context.BulkDifferentialRequestContext;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExpression;
import uk.ac.ebi.atlas.model.experiment.differential.rnaseq.BulkDifferentialProfile;

@Controller
public class BulkDifferentialProfilesWriterFactory extends
        DifferentialProfilesWriterFactory<DifferentialExpression, BulkDifferentialProfile, BulkDifferentialRequestContext> {
}
