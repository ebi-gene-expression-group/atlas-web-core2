package uk.ac.ebi.atlas.model.arraydesign;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomArrayDesignAccession;

class ArrayDesignTest {
    @Test
    void unknownArrayDesignsArePopulatedWithTheirAccession() {
        String arrayDesignAccession = generateRandomArrayDesignAccession();
        assertThat(ArrayDesign.create(arrayDesignAccession))
                .hasFieldOrPropertyWithValue("accession", arrayDesignAccession)
                .hasFieldOrPropertyWithValue("name", arrayDesignAccession);
    }
}