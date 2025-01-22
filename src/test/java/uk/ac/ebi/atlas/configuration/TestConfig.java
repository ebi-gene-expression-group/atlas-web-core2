package uk.ac.ebi.atlas.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.atlas.bioentity.properties.ExpressedBioentityFinder;
import uk.ac.ebi.atlas.experiments.ExperimentCellCountDao;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.search.suggester.AnalyticsSuggesterService;
import uk.ac.ebi.atlas.species.SpeciesFinder;
import uk.ac.ebi.atlas.trader.ExperimentRepository;
import uk.ac.ebi.atlas.utils.BioentityIdentifiersReader;

import java.util.HashSet;
import java.util.stream.Stream;

@Configuration
// Enabling component scanning will also load BasePathsConfig, JdbcConfig and SolrConfig, so just using this class as
// application context is enough in integration tests
@ComponentScan(basePackages = "uk.ac.ebi.atlas")
public class TestConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ExperimentRepository experimentRepository() {
        return new ExperimentRepository() {
            @Override
            public Experiment getExperiment(String experimentAccession) {
                return null;
            }

            @Override
            public String getExperimentType(String experimentAccession) {
                return null;
            }

            @Override
            public ExperimentDesign getExperimentDesign(String experimentAccession) { return null; }
        };
    }

    @Bean
    public BioentityIdentifiersReader bioentityIdentifiersReader() {
        return new BioentityIdentifiersReader() {
            @Override
            protected int addBioentityIdentifiers(@NotNull HashSet<String> bioentityIdentifiers,
                                                  @NotNull ExperimentType experimentType) {
                return 0;
            }

            @Override
            public HashSet<String> getBioentityIdsFromExperiment(@NotNull String experimentAccession) {
                return new HashSet<>();
            }

            @Override
            public HashSet<String> getBioentityIdsFromExperiment(@NotNull String experimentAccession, boolean throwError) {
                return new HashSet<>();
            }
        };
    }

    @Bean
    public ExpressedBioentityFinder expressedBioentityFinder() {
        return bioentityIdentifier -> true;
    }

    @Bean
    public SpeciesFinder speciesFinder() {
        return new SpeciesFinder() {};
    }

    @Bean
    public AnalyticsSuggesterService analyticsSuggesterService() {
        return (query, species) -> Stream.empty();
    }

    @Bean
    public ExperimentCellCountDao experimentCellCountDao() {
        return __ -> 0;
    }
}
