package uk.ac.ebi.atlas.experiments.collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

@Repository
@Transactional(transactionManager = "txManager", readOnly = true)
public class ExperimentCollectionDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentCollectionDao.class);
    
    private static final RowMapper<ExperimentCollection> EXPERIMENT_COLLECTION_ROW_MAPPER =
            (resultSet, __) ->
                    ExperimentCollection.create(
                            resultSet.getString("coll_id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getBytes("icon"));

    private final JdbcTemplate jdbcTemplate;

    public ExperimentCollectionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // TODO Since collections aren’t in bulk yet return an Optional (remove when table is in the shared schema)  
    public final static String SELECT_COLLECTION_STATEMENT = "SELECT * FROM collections WHERE coll_id=?";
    @Cacheable("experimentCollections")
    public Optional<ExperimentCollection> findCollection(String id) {
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(SELECT_COLLECTION_STATEMENT, EXPERIMENT_COLLECTION_ROW_MAPPER, id));
        } catch (Exception e) {
            LOGGER.warn(Arrays.deepToString(e.getStackTrace()));
            return Optional.empty();
        }
    }
}
