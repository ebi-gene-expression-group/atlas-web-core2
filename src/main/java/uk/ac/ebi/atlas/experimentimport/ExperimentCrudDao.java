package uk.ac.ebi.atlas.experimentimport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.joining;

@Component
@Transactional(transactionManager = "txManager")
public class ExperimentCrudDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentCrudDao.class);
    private static final String PUBLICATION_SEPARATOR = ", ";

    private static final Function<String, ImmutableSet<String>> PUBLICATION_SPLITTER =
            str ->
                    Arrays.stream(str.split(PUBLICATION_SEPARATOR))
                            .filter(StringUtils::isNotBlank)
                            .map(String::trim)
                            .collect(toImmutableSet());
    private static final Function<Collection<String>, String> PUBLICATION_JOINER =
            publications ->
                    publications.stream()
                            .filter(StringUtils::isNotBlank)
                            .map(String::trim)
                            .collect(joining(PUBLICATION_SEPARATOR));
    private static final Function<Collection<String>, String> PUBLICATION_JOINER_OR_NULL =
            PUBLICATION_JOINER.andThen(str -> str.isBlank() ? null : str);

    private static final RowMapper<ExperimentDto> EXPERIMENT_DTO_ROW_MAPPER =
            (resultSet, __) ->
                    new ExperimentDto(
                        resultSet.getString("accession"),
                        ExperimentType.valueOf(resultSet.getString("type")),
                        resultSet.getString("species"),
                        Optional.ofNullable(resultSet.getString("pubmed_ids"))
                                .map(PUBLICATION_SPLITTER)
                                .orElse(ImmutableSet.of()),
                        Optional.ofNullable(resultSet.getString("dois"))
                                .map(PUBLICATION_SPLITTER)
                                .orElse(ImmutableSet.of()),
                        resultSet.getTimestamp("load_date"),
                        resultSet.getTimestamp("last_update"),
                        resultSet.getBoolean("private"),
                        resultSet.getString("access_key"));

    private final JdbcTemplate jdbcTemplate;

    public ExperimentCrudDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Create
    public void createExperiment(ExperimentDto experimentDto) {
        LOGGER.debug(
                "Adding row {}/{} to table 'experiment'",
                experimentDto.getExperimentAccession(),
                experimentDto.getAccessKey());
        jdbcTemplate.update(
                "INSERT INTO experiment " +
                "(accession, type, species, private, access_key, pubmed_ids, dois) VALUES (?, ?, ?, ?, ?, ?, ?)",
                experimentDto.getExperimentAccession(),
                experimentDto.getExperimentType().name(),
                experimentDto.getSpecies(),
                experimentDto.isPrivate(),
                experimentDto.getAccessKey(),
                PUBLICATION_JOINER_OR_NULL.apply(experimentDto.getPubmedIds()),
                PUBLICATION_JOINER_OR_NULL.apply(experimentDto.getDois()));
    }

    // Read
    @Transactional(readOnly = true)
    @Nullable
    public ExperimentDto readExperiment(String experimentAccession) {
        try {
            LOGGER.debug("Reading row {} from table 'experiment'", experimentAccession);
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM experiment WHERE accession=?",
                    EXPERIMENT_DTO_ROW_MAPPER,
                    experimentAccession);
        } catch (DataAccessException e) {
            LOGGER.warn(
                    "There was an error reading the 'experiment' table or accession {} could not be found",
                    experimentAccession);
            LOGGER.debug(e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    @Nullable
    public String getExperimentType(String experimentAccession) {
        try {
            LOGGER.debug("Get experiment type for experiment: {}.", experimentAccession);

            return jdbcTemplate.queryForObject(
                    "SELECT type FROM experiment WHERE accession = ?",
                    new Object[]{experimentAccession}, String.class);

        } catch (DataAccessException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    @Transactional(readOnly = true)
    public ImmutableList<ExperimentDto> readExperiments() {
        LOGGER.debug("Reading all rows from table 'experiment'");
        return ImmutableList.copyOf(
                jdbcTemplate.query(
                        "SELECT * FROM experiment",
                        EXPERIMENT_DTO_ROW_MAPPER));
    }

    // Update
    public void updateExperimentPrivate(String experimentAccession, boolean isPrivate) {
        LOGGER.debug("Updating privacy of row {} in table 'experiment' to {}...", experimentAccession, isPrivate);
        int updatedRecordsCount =
                jdbcTemplate.update(
                        "UPDATE experiment SET private=? WHERE accession=?",
                        isPrivate,
                        experimentAccession);
        LOGGER.debug("{} rows updated", updatedRecordsCount);
        checkState(updatedRecordsCount == 1);
    }

    public void updateExperiment(ExperimentDto experimentDto) {
        LOGGER.debug("Updating row {} in table 'experiment'...", experimentDto.getExperimentAccession());
        int updatedRecordsCount =
                jdbcTemplate.update(
                        "UPDATE experiment SET last_update=NOW(), private=?, pubmed_ids=?, dois=? " +
                        "WHERE accession=?",
                        experimentDto.isPrivate(),
                        PUBLICATION_JOINER_OR_NULL.apply(experimentDto.getPubmedIds()),
                        PUBLICATION_JOINER_OR_NULL.apply(experimentDto.getDois()),
                        experimentDto.getExperimentAccession());
        LOGGER.debug("{} rows affected", updatedRecordsCount);
        checkState(updatedRecordsCount == 1);
    }

    // Delete
    public void deleteExperiment(String experimentAccession) {
        LOGGER.debug("Deleting row {} in table 'experiment'", experimentAccession);
        int deletedRecordsCount =
                jdbcTemplate.update(
                        "DELETE FROM experiment WHERE accession=?",
                        experimentAccession);
        LOGGER.debug("{} rows affected", deletedRecordsCount);
        checkState(deletedRecordsCount == 1);
    }

}
