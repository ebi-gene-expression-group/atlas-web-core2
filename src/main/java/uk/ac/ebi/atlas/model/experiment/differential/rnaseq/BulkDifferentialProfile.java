package uk.ac.ebi.atlas.model.experiment.differential.rnaseq;

import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExpression;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialProfile;

public class BulkDifferentialProfile extends DifferentialProfile<DifferentialExpression, BulkDifferentialProfile> {
    public BulkDifferentialProfile(String geneId, String geneName) {
        super(geneId, geneName);
    }

    @Override
    protected BulkDifferentialProfile createEmptyCopy() {
        return new BulkDifferentialProfile(getId(), getName());
    }
}
