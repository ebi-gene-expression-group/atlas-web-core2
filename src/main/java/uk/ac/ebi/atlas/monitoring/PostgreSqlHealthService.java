package uk.ac.ebi.atlas.monitoring;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PostgreSqlHealthService {
    private final JdbcTemplate jdbcTemplate;

    public PostgreSqlHealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean isDatabaseUp() {
        return countExperiments() > 1;
    }

    @Transactional(transactionManager = "txManager", readOnly = true)
    protected int countExperiments() {
        return Optional
                .ofNullable(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM experiment", Integer.class))
                .orElse(0);
    }
}
