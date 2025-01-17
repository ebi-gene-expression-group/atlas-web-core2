package uk.ac.ebi.atlas.experimentimport;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.controllers.ResourceNotFoundException;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.testutils.JdbcUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExperimentCrudDaoIT {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private static final int MAXIMUM_EXPERIMENT_COUNT = 1000;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private JdbcUtils jdbcUtils;

    @Inject
    private ExperimentCrudDao subject;

    @AfterEach
    void cleanExperimentTable() {
        // I would’ve liked to use @Transactional at the class level, but in that case the test
        // updateExperimentBumpsLastUpdate fails because in H2 NOW() returns the same value within a transaction:
        // https://h2database.com/html/functions.html#localtimestamp
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "experiment");
    }

    @Test
    void createExperiment() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = generateRandomExperimentDto();
        subject.createExperiment(experimentDto);

        assertThat(jdbcUtils.fetchAllExperimentAccessions())
                .containsExactly(experimentDto.getExperimentAccession());
    }

    @Test
    void readExperiment() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = generateRandomExperimentDto();
        subject.createExperiment(experimentDto);

        assertThat(subject.readExperiment(experimentDto.getExperimentAccession()))
                .extracting(
                        "experimentAccession",
                        "experimentType",
                        "species",
                        "pubmedIds",
                        "dois",
                        "isPrivate")
                .containsExactly(
                        experimentDto.getExperimentAccession(),
                        experimentDto.getExperimentType(),
                        experimentDto.getSpecies(),
                        experimentDto.getPubmedIds(),
                        experimentDto.getDois(),
                        experimentDto.isPrivate());
    }

    @Test
    void readExperiments() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDtos = IntStream.range(0, MAXIMUM_EXPERIMENT_COUNT)
                .boxed()
                .map(__ -> generateRandomExperimentDto())
                .collect(toImmutableSet());
        experimentDtos.forEach(subject::createExperiment);

        assertThat(subject.readExperiments())
                .hasSameSizeAs(experimentDtos)
                .extracting("experimentAccession")
                .containsExactlyInAnyOrderElementsOf(
                        experimentDtos.stream().map(ExperimentDto::getExperimentAccession).collect(toImmutableSet()));
    }

    @Test
    void createExperimentWithoutDatesDefaultsToNow() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = generateRandomExperimentDto();
        assertThat(experimentDto)
                .extracting("lastUpdate", "loadDate")
                .containsOnlyNulls();

        subject.createExperiment(experimentDto);
        var retrievedExperimentDto = subject.readExperiment(experimentDto.getExperimentAccession());

        assertThat(retrievedExperimentDto.getLastUpdate()).isEqualTo(retrievedExperimentDto.getLoadDate());
        assertThat(retrievedExperimentDto.getLastUpdate()).isInSameDayAs(new Date());
        assertThat(retrievedExperimentDto.getLoadDate()).isInSameDayAs(new Date());
    }

    @Test
    void returnsNullIfExperimentCannotBeFound() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        assertThat(subject.readExperiment(generateRandomExperimentAccession()))
                .isNull();
    }

    @Test
    void updateExperimentPrivate() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = generateRandomExperimentDto();
        subject.createExperiment(experimentDto);
        subject.updateExperimentPrivate(experimentDto.getExperimentAccession(), !experimentDto.isPrivate());

        assertThat(subject.readExperiment(experimentDto.getExperimentAccession()))
                .hasFieldOrPropertyWithValue("private", !experimentDto.isPrivate());
    }

    @Test
    void updateExperimentLeavesLoadDateUnchangedAndBumpsLastUpdate() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = generateRandomExperimentDto();
        subject.createExperiment(experimentDto);
        var retrievedExperimentDto = subject.readExperiment(experimentDto.getExperimentAccession());
        subject.updateExperiment(experimentDto);
        var updatedExperimentDto = subject.readExperiment(experimentDto.getExperimentAccession());

        assertThat(updatedExperimentDto.getLoadDate()).isEqualTo(retrievedExperimentDto.getLoadDate());
        assertThat(updatedExperimentDto.getLastUpdate()).isAfter(updatedExperimentDto.getLoadDate());
    }

    @Test
    void deleteExperiment() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = generateRandomExperimentDto();
        subject.createExperiment(experimentDto);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isOne();
        subject.deleteExperiment(experimentDto.getExperimentAccession());
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
    }

    @Test
    void throwIfDeletingAnExperimentFails() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        assertThatIllegalStateException()
                .isThrownBy(() -> subject.deleteExperiment(generateRandomExperimentAccession()));
    }

    @Test
    void throwIfUpdatingAnExperimentFails() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        assertThatIllegalStateException()
                .isThrownBy(() -> subject.updateExperiment(generateRandomExperimentDto()));
    }

    @Test
    void emptyPublicationsAreInsertedAsNull() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = generateRandomExperimentDto();
        subject.createExperiment(experimentDto);

        assertThat(jdbcTemplate.queryForObject("SELECT pubmed_ids FROM experiment", String.class)).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT dois FROM experiment", String.class)).isNull();
    }

    @Test
    void publicationsAreProperlyParsed() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = ExperimentDtoTest.generateRandomExperimentDto();
        subject.createExperiment(experimentDto);

        assertThat(subject.readExperiment(experimentDto.getExperimentAccession()).getPubmedIds())
                .containsExactlyInAnyOrderElementsOf(experimentDto.getPubmedIds());
        assertThat(subject.readExperiment(experimentDto.getExperimentAccession()).getDois())
                .containsExactlyInAnyOrderElementsOf(experimentDto.getDois());
    }

    @Test
    void whenExperimentDoesNotExists_thenReturnResourceNotFoundException() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();

        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(
                () -> subject.getExperimentType(any())
        );
    }

    @Test
    void whenExperimentExists_thenReturnsItsType() {
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isZero();
        var experimentDto = ExperimentDtoTest.generateRandomExperimentDto();
        subject.createExperiment(experimentDto);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "experiment")).isOne();

        var experimentType = subject.getExperimentType(experimentDto.getExperimentAccession());
        assertThat(experimentType)
                .isEqualTo(experimentDto.getExperimentType().name());
        assertThat(ExperimentType.valueOf(experimentType)).isEqualTo(experimentDto.getExperimentType());
    }

    private ExperimentDto generateRandomExperimentDto() {
        return new ExperimentDto(
                        generateRandomExperimentAccession(),
                        ExperimentType.values()[RNG.nextInt(ExperimentType.values().length)],
                        generateRandomSpecies().getName(),
                        ImmutableSet.of(),
                        ImmutableSet.of(),
                        RNG.nextBoolean(),
                        UUID.randomUUID().toString());
    }
}