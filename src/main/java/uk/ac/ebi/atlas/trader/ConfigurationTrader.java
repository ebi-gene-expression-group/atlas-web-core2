package uk.ac.ebi.atlas.trader;

import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperimentConfiguration;
import uk.ac.ebi.atlas.resource.DataFileHub;

@Component
public class ConfigurationTrader {
    private final DataFileHub dataFileHub;

    public ConfigurationTrader(DataFileHub dataFileHub) {
        this.dataFileHub = dataFileHub;
    }

    // <exp_accession>-configuration.xml
    public ExperimentConfiguration getExperimentConfiguration(String experimentAccession) {
        return new ExperimentConfiguration(
                dataFileHub.getExperimentFiles(experimentAccession).configuration.get());
    }

    // <exp_accession>-factors.xml
    public BaselineExperimentConfiguration getBaselineFactorsConfiguration(String experimentAccession) {
        return new BaselineExperimentConfiguration(
                dataFileHub.getBaselineExperimentFiles(experimentAccession).factors.get());
    }
}
