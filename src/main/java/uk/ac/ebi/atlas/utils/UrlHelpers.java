package uk.ac.ebi.atlas.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Optional;

public class UrlHelpers {
    public UrlHelpers() {
         throw new UnsupportedOperationException();
     }

    private static String getExperimentUrl(String accession) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/experiments/{accession}")
                .buildAndExpand(accession)
                .toUriString();
    }

    public static String getExperimentsFilteredBySpeciesUrl(String species) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/experiments")
                .query("species={species}")
                .buildAndExpand("\"" + species + "\"")
                .toUriString();
    }

    public static String getExperimentsFilteredBySpeciesAndExperimentType(String species, String type) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/experiments")
                .query("species={species}")
                .query("experimentType={type}")
                .buildAndExpand(species, type)
                .toUriString();
    }

    public static String getExperimentsSummaryImageUrl(String imageFileName) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/resources/images/experiments-summary/{imageFileName}.png")
                .buildAndExpand(imageFileName)
                .toUriString();
    }

    public static String getCustomUrl(String path) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(path)
                .build()
                .toUriString();
    }

    public static String getCustomUrl(String host, String path) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .scheme("https")
                .host(host)
                .path(path)
                .build()
                .toUriString();
    }

    private static String getExperimentSetUrl(String keyword) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/experiments")
                .query("experimentDescription={keyword}")
                .buildAndExpand(keyword)
                .toUriString();
    }

    private static String getExperimentUrl(String host, String accession) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .scheme("https")
                .host(host)
                .path("/experiments/{accession}")
                .buildAndExpand(accession)
                .toUriString();
    }

    private static String getExperimentCollectionUrl(String description) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/experiments")
                .query("experimentProjects=\"{description}\"")
                .buildAndExpand(description)
                .toUriString();
    }

    public static Pair<Optional<String>, Optional<String>> getExperimentSetLink(String keyword) {
        return getLinkWithEmptyLabel(getExperimentSetUrl(keyword));
    }

    public static Pair<String, Optional<String>> getExperimentLink(String label, String accession) {
        return Pair.of(label, Optional.of(getExperimentUrl(accession)));
    }

    public static Pair<String, Optional<String>> getExperimentLink(String host, String label, String accession) {
        return Pair.of(label, Optional.of(getExperimentUrl(host, accession)));
    }

    public static Pair<Optional<String>, Optional<String>> getLinkWithEmptyLabel(String link) {
        return Pair.of(Optional.empty(), Optional.of(link));
    }

    public static Pair<Optional<String>, Optional<String>> getExperimentLink(String accession) {
        return getLinkWithEmptyLabel(getExperimentUrl(accession));
    }

    public static Pair<Optional<String>, Optional<String>> getExperimentCollectionLink(String label, String description) {
        return Pair.of(Optional.of(label), Optional.of(getExperimentCollectionUrl(description)));
    }
}
