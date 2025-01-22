package uk.ac.ebi.atlas.experimentimport.idf;

import com.google.common.collect.ImmutableSet;
import uk.ac.ebi.atlas.model.Publication;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class IdfParserOutput {
    private final String title;
    private final ImmutableSet<String> secondaryAccessions;
    private final String experimentDescription;
    // Map of Pubmed IDs and publication titles
    private final List<Publication> publications;
    // Only expected for single cell
    private final int expectedClusters;
    // Curated selection of metadata fields (chosen from experiment characteristics)
    private final List<String> metadataFieldsOfInterest;

    public IdfParserOutput(String title,
                           Collection<String> secondaryAccessions,
                           String experimentDescription,
                           List<Publication> publications,
                           int expectedClusters,
                           List<String> metadataFieldsOfInterest) {
        this.title = title;
        this.secondaryAccessions = ImmutableSet.copyOf(secondaryAccessions);
        this.experimentDescription = experimentDescription;
        this.publications = publications;
        this.expectedClusters = expectedClusters;
        this.metadataFieldsOfInterest = metadataFieldsOfInterest;
    }

    public String getTitle() {
        return title;
    }

    public ImmutableSet<String> getSecondaryAccessions() {
        return secondaryAccessions;
    }

    public String getExperimentDescription() {
        return experimentDescription;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public int getExpectedClusters() {
        return expectedClusters;
    }

    public Set<String> getPubmedIds() {
        return publications.stream()
                .map(Publication::getPubmedId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public List<String> getMetadataFieldsOfInterest() {
        return metadataFieldsOfInterest;
    }

    public Set<String> getDois() {
        return publications
                .stream()
                .map(Publication::getDoi)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
