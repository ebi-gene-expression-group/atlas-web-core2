package uk.ac.ebi.atlas.testutils;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.utils.GsonProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public class JdbcUtils {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcUtils(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<String> fetchAllExperimentAccessions() {
        return jdbcTemplate.queryForList("SELECT accession FROM experiment", String.class);
    }

    public String fetchRandomExperimentAccession() {
        return jdbcTemplate.queryForObject(
                "SELECT accession FROM experiment ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchExperimentTypeByAccession(String experimentAccession) {
        SqlParameterSource namedParameters = new MapSqlParameterSource("accession", experimentAccession);

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT type FROM experiment WHERE accession = :accession",
                namedParameters,
                String.class
        );
    }

    public String fetchRandomExperimentAccession(ExperimentType... experimentTypes) {
        SqlParameterSource namedParameters =
                new MapSqlParameterSource(
                        "experiment_types",
                        Arrays.stream(experimentTypes).map(ExperimentType::toString).collect(toImmutableList()));

        return namedParameterJdbcTemplate.queryForObject(
                "SELECT accession FROM experiment WHERE type IN (:experiment_types) AND private=FALSE ORDER BY RANDOM() LIMIT 1",
                namedParameters,
                String.class);
    }

    public List<String> fetchPublicExperimentAccessions() {
        return jdbcTemplate.queryForList("SELECT accession FROM experiment WHERE private=FALSE", String.class);
    }

    public String fetchRandomPublicExperimentAccession() {
        return jdbcTemplate.queryForObject(
                "SELECT accession FROM experiment WHERE private=FALSE ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomSingleCellExperimentAccessionWithoutMarkerGenes() {
        // This is a bit of a naive approach that only really works with mock data. In reality, all experiments seen so
        // far have at least one marker gene for some k value. A better function would return a pair of (experiment
        // accession, k) that don't have marker genes.
        return jdbcTemplate.queryForObject(
                "SELECT cell_group_membership.experiment_accession " +
                        "FROM scxa_cell_group_membership AS cell_group_membership " +
                        "INNER JOIN scxa_cell_group_marker_genes AS marker_genes " +
                            "ON marker_genes.cell_group_id = cell_group_membership.cell_group_id " +
                        "INNER JOIN scxa_cell_group_marker_gene_stats AS marker_gene_stats " +
                            "ON marker_genes.id = marker_gene_stats.marker_id " +
                        "WHERE marker_genes.marker_probability > 0.05 " +
                        "ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomSingleCellExperimentAccessionWithMarkerGenes() {
        return jdbcTemplate.queryForObject(
                "SELECT " +
                        "cell_group.experiment_accession " +
                "FROM " +
                        "scxa_cell_group AS cell_group " +
                "INNER JOIN " +
                        "scxa_cell_group_marker_genes AS marker_genes " +
                        "ON marker_genes.cell_group_id = cell_group.id " +
                "INNER JOIN " +
                        "scxa_cell_group_marker_gene_stats AS marker_gene_stats " +
                        "ON marker_genes.id = marker_gene_stats.marker_id " +
                "WHERE " +
                        "marker_genes.marker_probability < 0.05 " +
                "ORDER BY " +
						"RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomArrayDesignAccession() {
        return jdbcTemplate.queryForObject(
                "SELECT arraydesign FROM designelement_mapping ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public List<String> fetchPublicSpecies() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT species FROM experiment WHERE private = FALSE",
                String.class);
    }

    public String fetchRandomPublicSpecies() {
        return jdbcTemplate.queryForObject(
                "SELECT species FROM experiment WHERE private = FALSE ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    @Deprecated // Use fetchPublicspecies
    public List<String> fetchSpeciesForSingleCellExperiments() {
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT species FROM experiment",
                String.class);
    }

    @Deprecated // Use fetchRandomPublicSpecies
    public String fetchRandomSpeciesForSingleCellExperiments() {
        return jdbcTemplate.queryForObject(
                "SELECT species FROM experiment ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomGene() {
        return jdbcTemplate.queryForObject(
                "SELECT gene_id FROM scxa_analytics ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public List<String> fetchRandomListOfGeneIds(int numberOfGeneIds) {
        return jdbcTemplate.queryForList(
                "SELECT gene_id FROM scxa_analytics ORDER BY RANDOM() LIMIT ?",
                String.class,
                numberOfGeneIds);
    }

    public String fetchRandomGeneFromSingleCellExperiment(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT gene_id FROM scxa_analytics WHERE experiment_accession=? ORDER BY RANDOM() LIMIT 1",
                String.class,
                experimentAccession);
    }

    public String fetchRandomMarkerGeneFromSingleCellExperiment(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT marker_genes.gene_id " +
                        "FROM scxa_cell_group_membership AS cell_group_membership " +
                        "INNER JOIN scxa_cell_group_marker_genes AS marker_genes " +
                            "ON marker_genes.cell_group_id = cell_group_membership.cell_group_id " +
                        "INNER JOIN scxa_cell_group_marker_gene_stats AS marker_gene_stats " +
                            "ON marker_genes.id = marker_gene_stats.marker_id " +
                        "WHERE " +
                            "cell_group_membership.experiment_accession =? " +
                            "AND marker_genes.marker_probability <= 0.05 " +
                        "ORDER BY " +
                            "RANDOM() LIMIT  1",
                String.class,
                experimentAccession);
    }

    public String fetchRandomCellFromExperiment(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT cell_id FROM scxa_analytics WHERE experiment_accession=? ORDER BY RANDOM() LIMIT 1",
                String.class,
                experimentAccession);
    }

    public List<String> fetchRandomListOfCells(int numberOfCells) {
        return jdbcTemplate.queryForList(
                "SELECT cell_id FROM scxa_analytics ORDER BY RANDOM() LIMIT ?",
                String.class,
                numberOfCells);
    }

    public List<String> fetchRandomListOfCellsFromExperiment(String experimentAccession, int numberOfCells) {
        return jdbcTemplate.queryForList(
                "SELECT cell_id FROM scxa_analytics  WHERE experiment_accession=? ORDER BY RANDOM() LIMIT ?",
                String.class,
                experimentAccession,
                numberOfCells);
    }

    public int fetchRandomPerplexityFromExperimentTSne(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT sdr.parameterisation->0->>'perplexity' as parameter " +
                            "FROM scxa_coords as coords " +
                            "INNER JOIN scxa_dimension_reduction sdr on sdr.id = coords.dimension_reduction_id " +
                            "WHERE sdr.experiment_accession=? " +
                            "AND sdr.parameterisation->0->>'perplexity' IS NOT NULL " +
                            "ORDER BY RANDOM() LIMIT 1;",
                Integer.class,
                experimentAccession);
    }

    public int fetchRandomPerplexityFromExperimentTSne(String experimentAccession, String geneId) {
        return jdbcTemplate.queryForObject(
                "SELECT parameterisation->0->>'perplexity' as parameter " +
                            "FROM scxa_coords AS coords " +
                            "INNER JOIN scxa_dimension_reduction sdr on sdr.id = coords.dimension_reduction_id " +
                            "LEFT JOIN scxa_analytics AS analytics " +
                            "ON analytics.experiment_accession=sdr.experiment_accession AND " +
                                "analytics.cell_id=coords.cell_id " +
                            "WHERE sdr.parameterisation->0->>'perplexity' IS NOT NULL AND " +
                            "sdr.experiment_accession=? AND analytics.gene_id=? " +
                            "ORDER BY RANDOM() LIMIT 1",
                Integer.class,
                experimentAccession,
                geneId);
    }

    public int fetchRandomNeighboursFromExperimentUmap(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT sdr.parameterisation->0->>'n_neighbors' as parameter " +
                            "FROM scxa_coords as coords " +
                            "INNER JOIN scxa_dimension_reduction sdr on sdr.id = coords.dimension_reduction_id " +
                            "WHERE sdr.experiment_accession=?  " +
                            "AND sdr.parameterisation->0->>'n_neighbors' IS NOT NULL " +
                            "ORDER BY RANDOM() LIMIT 1;",
                Integer.class,
                experimentAccession);
    }

    public ImmutableList<Integer> fetchKsFromCellGroups(String experimentAccession) {
        var variables = jdbcTemplate.queryForList(
                "SELECT DISTINCT(variable) FROM scxa_cell_group WHERE experiment_accession=?",
                String.class,
                experimentAccession);

        return variables.stream()
                .map(variableCandidate -> {
                    try {
                        return Integer.parseInt(variableCandidate);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toImmutableList());
    }

    public String fetchRandomKWithMarkerGene(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT " +
						"h.variable " +
				"FROM " +
						"scxa_cell_group_marker_genes m, " +
                        "scxa_cell_group_marker_gene_stats s, " +
						"scxa_cell_group h " +
				"WHERE " +
                        "h.variable ~ '[0123456789]+' AND " +
                        "m.cell_group_id = h.id AND " +
						"h.experiment_accession = ? AND " +
						"m.marker_probability < 0.05 AND " +
                        "m.id = s.marker_id " +
				"ORDER BY " +
						"RANDOM() " +
                "LIMIT 1",
                String.class,
                experimentAccession);
    }

    public String fetchRandomExperimentCollectionId() {
        return jdbcTemplate.queryForObject(
                "SELECT coll_id FROM collections ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomExperimentAccessionWithCollections() {
        return jdbcTemplate.queryForObject(
                "SELECT exp_acc FROM experiment2collection ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public Pair<String, String> fetchRandomVariableAndValue(String experimentAccession) {
        return jdbcTemplate.queryForObject(
                "SELECT variable, value FROM scxa_cell_group WHERE experiment_accession=? ORDER BY RANDOM() LIMIT 1",
                (rs, rowNum) -> Pair.of(rs.getString("variable"), rs.getString("value")),
                experimentAccession);

    }

	public String fetchRandomPlotMethod(String experimentAccession) {
		return jdbcTemplate.queryForObject(
				"SELECT method FROM scxa_coords " +
                                   "INNER JOIN scxa_dimension_reduction sdr on sdr.id = scxa_coords.dimension_reduction_id " +
                                   "WHERE experiment_accession=? ORDER BY RANDOM() LIMIT 1;",
				String.class,
				experimentAccession);
	}

	public Map<String, Integer> fetchRandomParameterisation(String experimentAccession, String plotMethod) {
		var parameterisationType = new TypeToken<List<Map<String, Integer>>>(){}.getType();
		List<Map<String, Integer>> parameterisation = jdbcTemplate.queryForObject(
                            "SELECT parameterisation FROM scxa_coords " +
                                    "INNER JOIN scxa_dimension_reduction sdr on sdr.id = scxa_coords.dimension_reduction_id " +
                                    "WHERE experiment_accession=? AND method=? " +
                                    "ORDER BY RANDOM() LIMIT 1",
				(rs, rowNum) -> GsonProvider.GSON.fromJson(rs.getString("parameterisation"), parameterisationType),
				experimentAccession,
				plotMethod);
		return parameterisation.get(0);
	}

	public void updatePublicExperimentAccessionToPrivate(String accession) {
		jdbcTemplate.update(
				"UPDATE experiment SET private=TRUE WHERE accession=?",
				accession);
	}

	public void updatePrivateExperimentAccessionToPublic(String accession) {
		jdbcTemplate.update(
				"UPDATE experiment SET private=FALSE WHERE accession=?",
				accession);
	}

    public String fetchExperimentAccessKey(String accession) {
        return jdbcTemplate.queryForObject(
                "SELECT access_key FROM Experiment WHERE accession=?",
                String.class,
                accession);
    }

    public String fetchExperimentAccessionByMaxPriority() {
        return jdbcTemplate.queryForObject(
                "SELECT experiment_accession " +
                        "FROM scxa_dimension_reduction " +
                        "WHERE  priority = (SELECT max(priority) FROM scxa_dimension_reduction) " +
                        "GROUP BY experiment_accession " +
                        "ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchRandomSingleCellExperimentAccessionWithInferredCellType() {
        return jdbcTemplate.queryForObject(
                "SELECT experiment_accession " +
                        "FROM scxa_cell_group " +
                        "WHERE variable LIKE 'inferred_cell_type%'" +
                        "ORDER BY RANDOM() LIMIT 1",
                String.class);
    }

    public String fetchSpeciesByExperimentAccession(String accession) {
        return jdbcTemplate.queryForObject(
                "SELECT species FROM experiment WHERE accession=?",
                String.class,
                accession);
    }
}
