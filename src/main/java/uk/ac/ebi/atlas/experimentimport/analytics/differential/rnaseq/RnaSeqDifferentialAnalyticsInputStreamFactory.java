package uk.ac.ebi.atlas.experimentimport.analytics.differential.rnaseq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.atlas.model.resource.AtlasResource;
import uk.ac.ebi.atlas.resource.DataFileHub;

import java.io.IOException;

@Controller
public class RnaSeqDifferentialAnalyticsInputStreamFactory {

    private final DataFileHub dataFileHub;

    @Autowired
    public RnaSeqDifferentialAnalyticsInputStreamFactory(DataFileHub dataFileHub) {
        this.dataFileHub = dataFileHub;
    }

    public RnaSeqDifferentialAnalyticsInputStream create(String experimentAccession) throws IOException {
        AtlasResource<?> resource = dataFileHub.getBulkDifferentialExperimentFiles(experimentAccession).analytics;
        return new RnaSeqDifferentialAnalyticsInputStream(resource.getReader(), resource.toString());
    }
}
