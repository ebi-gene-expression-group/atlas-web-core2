package uk.ac.ebi.atlas.trader;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Repository
public class ExperimentTraderDao {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ExperimentTraderDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public ImmutableSet<String> fetchPublicExperimentAccessions(ExperimentType... experimentTypes) {
        var experimentTypeNames =
                Stream.of(experimentTypes.length == 0 ? ExperimentType.values() : experimentTypes)
                        .map(ExperimentType::name)
                        .collect(toImmutableList());

        return ImmutableSet.copyOf(
                namedParameterJdbcTemplate.queryForList(
                        "SELECT accession FROM experiment WHERE private=FALSE AND type IN(:experimentTypes)",
                        ImmutableMap.of("experimentTypes", experimentTypeNames),
                        String.class));
    }

    @Cacheable("privateExperimentAccessions")
    public ImmutableSet<String> fetchPrivateExperimentAccessions(){
        return ImmutableSet.copyOf(
                namedParameterJdbcTemplate.queryForList(
                        "SELECT accession FROM experiment WHERE private=TRUE",
                        ImmutableMap.of(),
                        String.class));
    }
}
