package uk.ac.ebi.atlas.profiles.writer;

import uk.ac.ebi.atlas.experimentpage.context.BulkDifferentialRequestContext;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExpression;
import uk.ac.ebi.atlas.model.experiment.differential.rnaseq.BulkDifferentialProfile;

import javax.inject.Named;

@Named
public class BulkDifferentialProfilesWriterFactory extends
        DifferentialProfilesWriterFactory<DifferentialExpression, BulkDifferentialProfile, BulkDifferentialRequestContext> {
}
