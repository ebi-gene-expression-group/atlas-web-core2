package uk.ac.ebi.atlas.home.species;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;

@Component
@Transactional(transactionManager = "txManager", readOnly = true)
public class SpeciesSummaryDao {
    protected final SpeciesFactory speciesFactory;
    protected final JdbcTemplate jdbcTemplate;

    public SpeciesSummaryDao(JdbcTemplate jdbcTemplate, SpeciesFactory speciesFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.speciesFactory = speciesFactory;
    }

    public ImmutableList<Triple<Species, ExperimentType, Long>> getExperimentCountBySpeciesAndExperimentType() {
        return ImmutableList.copyOf(jdbcTemplate.query(
                "SELECT species, type, COUNT(species) c FROM experiment WHERE private=FALSE GROUP BY type, species",
                (resultSet, __) ->
                    Triple.of(
                            speciesFactory.create(resultSet.getString("species")),
                            ExperimentType.valueOf(resultSet.getString("type")),
                            resultSet.getLong("c"))));
    }
}
