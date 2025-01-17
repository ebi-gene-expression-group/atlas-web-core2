package uk.ac.ebi.atlas.experiments.collections;

import com.google.common.collect.ImmutableList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional(transactionManager = "txManager", readOnly = true)
@Component
public class ExperimentCollectionsFinderDao {
    private final JdbcTemplate jdbcTemplate;

    public ExperimentCollectionsFinderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // We don’t need to be concerned about the validity of the experiment accession because exp_acc is a foreign key
    private static final String SELECT_COLLECTION_IDS_STATEMENT =
            "SELECT coll_id FROM experiment2collection WHERE exp_acc = ?";
    public ImmutableList<String> findExperimentCollectionIds(String experimentAccession) {
        try {
            return ImmutableList.copyOf(
                    jdbcTemplate.queryForList(SELECT_COLLECTION_IDS_STATEMENT, String.class, experimentAccession));
        } catch (Exception e) {
            return ImmutableList.of();
        }
    }
}
