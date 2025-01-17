package uk.ac.ebi.atlas.model.experiment.sample;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomRnaSeqRunId;

class CellTest {
    @Test
    void cellIdIsTheOnlyAssayId() {
        String id = generateRandomRnaSeqRunId();
        assertThat(new Cell(id).getAssayIds())  // "This isn’t even my final form!"
                .containsExactly(id);
    }
}