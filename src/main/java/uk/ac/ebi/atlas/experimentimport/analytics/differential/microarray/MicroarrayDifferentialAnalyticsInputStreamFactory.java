package uk.ac.ebi.atlas.experimentimport.analytics.differential.microarray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.atlas.model.resource.AtlasResource;
import uk.ac.ebi.atlas.resource.DataFileHub;

import java.io.IOException;

@Controller
public class MicroarrayDifferentialAnalyticsInputStreamFactory {

    private DataFileHub dataFileHub;

    @Autowired
    public MicroarrayDifferentialAnalyticsInputStreamFactory(DataFileHub dataFileHub) {
        this.dataFileHub = dataFileHub;
    }

    public MicroarrayDifferentialAnalyticsInputStream create(String experimentAccession, String arrayDesign)
    throws IOException {
        AtlasResource<?> analyticsResource =
                dataFileHub.getMicroarrayExperimentFiles(experimentAccession, arrayDesign).analytics;
        return new MicroarrayDifferentialAnalyticsInputStream(analyticsResource.getReader(), experimentAccession);
    }
}
