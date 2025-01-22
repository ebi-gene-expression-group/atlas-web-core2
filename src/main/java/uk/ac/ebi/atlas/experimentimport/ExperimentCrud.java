package uk.ac.ebi.atlas.experimentimport;

import com.google.common.collect.ImmutableList;
import uk.ac.ebi.atlas.experimentimport.experimentdesign.ExperimentDesignFileWriterService;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.UUID;

public abstract class ExperimentCrud {
    protected final ExperimentCrudDao experimentCrudDao;
    private final ExperimentDesignFileWriterService experimentDesignFileWriterService;

    public ExperimentCrud(ExperimentCrudDao experimentCrudDao,
                          ExperimentDesignFileWriterService experimentDesignFileWriterService) {
        this.experimentCrudDao = experimentCrudDao;
        this.experimentDesignFileWriterService = experimentDesignFileWriterService;
    }

    // Create
    public abstract UUID createExperiment(String experimentAccession, boolean isPrivate);

    // Read
    public Optional<ExperimentDto> readExperiment(String experimentAccession) {
        return Optional.ofNullable(experimentCrudDao.readExperiment(experimentAccession));
    }

    public ImmutableList<ExperimentDto> readExperiments() {
        return experimentCrudDao.readExperiments();
    }

    // Update
    public void updateExperimentPrivate(String experimentAccession, boolean isPrivate) {
        experimentCrudDao.updateExperimentPrivate(experimentAccession, isPrivate);
    }

    // Delete
    public void deleteExperiment(String experimentAccession) {
        experimentCrudDao.deleteExperiment(experimentAccession);
    }

    public abstract void updateExperimentDesign(String experimentAccession);

    protected void updateExperimentDesign(ExperimentDesign experimentDesign, ExperimentDto experimentDto) {
        try {
            experimentDesignFileWriterService.writeExperimentDesignFile(
                    experimentDto.getExperimentAccession(),
                    experimentDto.getExperimentType(),
                    experimentDesign);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
