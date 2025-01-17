package uk.ac.ebi.atlas.experimentimport.sdrf;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.testutils.MockDataFileHub;
import uk.ac.ebi.atlas.testutils.RandomDataTestUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SdrfParserTest {

    private static final String CHARACTERISTICS = "characteristics";
    private static final String FACTORS = "factorvalue";

    private static final String[][] SDRF_TXT_MIXED_SPACING = {
            {"Source Name", "Characteristics [organism]", "Characteristics[developmental stage]", "Characteristics [organism part]", "Factor Value[organism part]", "FactorValue [organism]", "Comment[library construction]"},
            {"first_source", "homo sapiens", "adult", "liver", "liver", "homo sapiens", "smart-seq2"},
            {"first_source", "homo sapiens", "adult", "liver", "liver", "homo sapiens", "smart-seq2"},
            {"first_source", "homo sapiens", "adult", "liver", "liver", "homo sapiens", "smart-seq1"},
            {"first_source", "homo sapiens", "adult", "liver", "liver", "homo sapiens", "Smart-seq1"}

    };

    private static final String[][] SDRF_TXT_NO_FACTORS = {
            {"Source Name", "Characteristics [organism]", "Characteristics[developmental stage]", "Characteristics [organism part]"},
            {"first_source", "homo sapiens", "adult", "liver"}
    };

    private static final String[][] SDRF_TXT_MIXED_SPACING_NO_ORGANISM_PART = {
            {"Source Name", "Characteristics [organism]", "Characteristics[developmental stage]", "Characteristics [organism part]", "Factor Value[organism part]", "FactorValue [organism]"},
            {"first_source", "homo sapiens", "adult", "liver", "liver", "homo sapiens"},
            {"first_source", "homo sapiens", "adult", "liver", "liver", "homo sapiens"},
            {"first_source", "homo sapiens", "adult", "liver", "liver", "homo sapiens"}
    };

    private MockDataFileHub dataFileHub;

    private SdrfParser subject;

    @BeforeEach
    void setUp() {
        dataFileHub = MockDataFileHub.create();

        subject = new SdrfParser(dataFileHub);
    }

    @Test
    void returnEmptyStringWithMissingContent() {
        String experimentAccession = RandomDataTestUtils.generateRandomExperimentAccession();

        dataFileHub.addSdrfFile(experimentAccession, Arrays.asList(SDRF_TXT_MIXED_SPACING_NO_ORGANISM_PART));
        assertThat(subject.parseSingleCellTechnologyType(experimentAccession))
                .isEqualTo(ImmutableList.of());
    }

    @Test
    void parseUniqueTechnologyType() {
        String experimentAccession = RandomDataTestUtils.generateRandomExperimentAccession();

        dataFileHub.addSdrfFile(experimentAccession, Arrays.asList(SDRF_TXT_MIXED_SPACING));
        assertThat(subject.parseSingleCellTechnologyType(experimentAccession))
                .isEqualTo(ImmutableList.of("smart-seq2", "smart-seq1"));
    }

    @Test
    void parseHeaderWithCharacteristicsAndFactors() {
        String experimentAccession = RandomDataTestUtils.generateRandomExperimentAccession();

        dataFileHub.addSdrfFile(experimentAccession, Arrays.asList(SDRF_TXT_MIXED_SPACING));

        var result = subject.parseHeader(experimentAccession);

        assertThat(result)
                .hasSize(2)
                .containsOnlyKeys(CHARACTERISTICS, FACTORS);

        assertThat(result.get(CHARACTERISTICS))
                .containsExactly("organism", "developmental stage", "organism part");

        assertThat(result.get(FACTORS))
                .containsExactly("organism part", "organism");
    }

    @Test
    void parseHeaderWithCharacteristicsOnly() {
        String experimentAccession = RandomDataTestUtils.generateRandomExperimentAccession();

        dataFileHub.addSdrfFile(experimentAccession, Arrays.asList(SDRF_TXT_NO_FACTORS));

        var result = subject.parseHeader(experimentAccession);

        assertThat(result)
                .hasSize(1)
                .containsOnlyKeys(CHARACTERISTICS)
                .doesNotContainKeys(FACTORS);

        assertThat(result.get(CHARACTERISTICS))
                .containsExactly("organism", "developmental stage", "organism part");
    }

}
