package uk.ac.ebi.atlas.trader;

import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ExperimentTrader {
    private final ExperimentTraderDao experimentTraderDao;
    private final ExperimentRepository experimentRepository;

    public ExperimentTrader(ExperimentTraderDao experimentTraderDao,
                            ExperimentRepository experimentRepository) {
        this.experimentTraderDao = experimentTraderDao;
        this.experimentRepository = experimentRepository;
    }

    // Under most circumstances you should use getExperiment(experimentAccession, accessKey). This method will return
    // any experiment, public or private, disregarding the private flag and without requiring the access key.
    // Use with care!
    public Experiment getExperimentForAnalyticsIndex(String experimentAccession) {
        return experimentRepository.getExperiment(experimentAccession);
    }

    public Experiment getPublicExperiment(String experimentAccession) {
        var experiment = experimentRepository.getExperiment(experimentAccession);

        if (!experiment.isPrivate()) {
            return experiment;
        }

        throw new ResourceNotFoundException(
                "Public experiment " + experimentAccession + " could not be found");
    }

    public Experiment getExperiment(String experimentAccession, String accessKey) {
        if (isBlank(accessKey)) {
            return getPublicExperiment(experimentAccession);
        }

        var experiment = experimentRepository.getExperiment(experimentAccession);

        if (experiment.getAccessKey().equalsIgnoreCase(accessKey)) {
            return experiment;
        }

        throw new ResourceNotFoundException(
                "Experiment " + experimentAccession + " could not be found or bad access key");
    }

    public ImmutableSet<Experiment> getPublicExperiments(ExperimentType... types) {
        return experimentTraderDao.fetchPublicExperimentAccessions(types).stream()
                .map(this::getPublicExperiment)
                .collect(toImmutableSet());
    }

    public String getExperimentType(String experimentAccession) {
        return experimentRepository.getExperimentType(experimentAccession);
    }

    public ExperimentDesign getExperimentDesign(String experimentAccession) {
        return experimentRepository.getExperimentDesign(experimentAccession);
    }
}
