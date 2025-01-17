package uk.ac.ebi.atlas.trader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperimentConfiguration;
import uk.ac.ebi.atlas.resource.DataFileHub;

import javax.inject.Inject;

import java.nio.file.Path;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class ConfigurationTraderIT {
    @Inject
    private Path experimentsDirPath;

    @Inject
    private Path experimentDesignDirPath;

    private ConfigurationTrader subject;

    @Before
    public void setUp() {
        subject = new ConfigurationTrader(new DataFileHub(experimentsDirPath, experimentDesignDirPath));
    }

    @Test
    public void eProt1HasTwoMenuFilterFactors() {
        BaselineExperimentConfiguration bc = subject.getBaselineFactorsConfiguration("E-PROT-1");
        assertThat(bc.getMenuFilterFactorTypes().size(), greaterThan(1));
    }
}
