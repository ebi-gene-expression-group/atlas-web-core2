package uk.ac.ebi.atlas.experiments;

public interface ExperimentCellCountDao {
    Integer fetchNumberOfCellsByExperimentAccession(String experimentAccession);
}
