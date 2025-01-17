package uk.ac.ebi.atlas.monitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.atlas.configuration.TestConfig;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
class PostgreSqlHealthServiceIT {
    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private PostgreSqlHealthService subject;

    @Test
    void assumeDbIsDownIfNoExperimentsCanBeFound() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        assertThat(subject.isDatabaseUp()).isFalse();
    }

    @Sql("/fixtures/gxa/experiment.sql")
    @Test
    void dbIsUpIfExperimentsCanBeFound() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isGreaterThan(0);
        assertThat(subject.isDatabaseUp()).isTrue();
    }
}
