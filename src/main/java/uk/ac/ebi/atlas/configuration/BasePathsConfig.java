package uk.ac.ebi.atlas.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@PropertySource("classpath:configuration.properties")
public class BasePathsConfig {
    private final String dataFilesLocation;
    private final String experimentFilesLocation;
    private final String experimentDesignLocation;

    public BasePathsConfig(@Value("${data.files.location}") String dataFilesLocation,
                           @Value("${experiment.files.location}") String experimentFilesLocation,
                           @Value("${experiment.design.location}") String experimentDesignLocation) {
        this.dataFilesLocation = dataFilesLocation;
        this.experimentFilesLocation = experimentFilesLocation;
        this.experimentDesignLocation = experimentDesignLocation;
    }

    @Bean
    public Path dataFilesPath() {
        return Paths.get(dataFilesLocation);
    }

    @Bean
    public Path experimentsDirPath() {
        return Paths.get(experimentFilesLocation);
    }

    @Bean
    public Path experimentDesignDirPath() {
        return Paths.get(experimentDesignLocation);
    }

    @Bean
    public Path bioentityPropertiesDirPath() {
        return dataFilesPath().resolve("bioentity_properties");
    }

    @Profile("!cli")
    @Bean
    public Path goPoFilePath() {
        return bioentityPropertiesDirPath().resolve("go-po.id-term-depth.tsv");
    }

    @Profile("!cli")
    @Bean
    public Path interProFilePath() {
        return bioentityPropertiesDirPath().resolve("interpro.term-id-type.tsv");
    }

    @Profile("cli")
    @Bean
    public Path annotationsDirPath() {
        return bioentityPropertiesDirPath().resolve("annotations");
    }

    @Profile("cli")
    @Bean
    public Path arrayDesignsDirPath() {
        return bioentityPropertiesDirPath().resolve("array_designs");
    }

    @Profile("cli")
    @Bean
    public Path reactomeDirPath() {
        return bioentityPropertiesDirPath().resolve("reactome");
    }

    @Bean
    public Path speciesPropertiesFilePath() {
        return experimentsDirPath().resolve("species-properties.json");
    }

    @Bean
    public Path atlasInformationFilePath() {
        return experimentsDirPath().resolve("release-metadata.json");
    }
}
