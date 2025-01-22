package uk.ac.ebi.atlas.experimentimport.experimentdesign;

import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.resource.DataFileHub;

import java.io.IOException;

@Component
public class ExperimentDesignFileWriterService {
    private final DataFileHub dataFileHub;

    public ExperimentDesignFileWriterService(DataFileHub dataFileHub) {
        this.dataFileHub = dataFileHub;
    }

    public void writeExperimentDesignFile(String accession, ExperimentType experimentType, ExperimentDesign
            experimentDesign) throws IOException {
        new ExperimentDesignFileWriter(
                dataFileHub.getExperimentFiles(accession).experimentDesignWrite.get(),
                experimentType).write(experimentDesign);
    }
}
