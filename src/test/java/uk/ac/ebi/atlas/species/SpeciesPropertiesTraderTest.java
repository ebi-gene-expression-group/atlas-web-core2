package uk.ac.ebi.atlas.species;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;

public class SpeciesPropertiesTraderTest {
    private Path location;
    private int speciesPropertiesCountBeforeRefresh;

    private SpeciesPropertiesTrader subject;

    @BeforeEach
    public void setUp() throws IOException {
        subject = new SpeciesPropertiesTrader();
        location =
                createSpeciesPropertiesFile(
                        SpeciesPropertiesTraderTest.class.getResourceAsStream("species-properties.json"));
        subject.setSpeciesPropertiesDao(new SpeciesPropertiesDao(location));

        subject.refresh();
        speciesPropertiesCountBeforeRefresh = subject.getAll().size();
    }

    @Test
    void getAll() {
        assertThat(subject.getAll().size()).isGreaterThan(50);
    }

    @Test
    void get() {
        assertThat(subject.get("Homo sapiens").referenceName())
                .isEqualTo("homo sapiens");
        assertThat(subject.get("Homo sapiens").ensemblName())
                .isEqualTo("Homo_sapiens");
        assertThat(subject.get("Homo sapiens").kingdom())
                .isEqualTo("animals");
        assertThat(subject.get("Hordeum vulgare").getResourcesOfType("genome_browser")).isNotEmpty();
    }

    @Test
    void refresh() throws IOException {
        Files.copy(
                SpeciesPropertiesTraderTest.class.getResourceAsStream("species-tyrannosaurus-rex.json"),
                location,
                REPLACE_EXISTING);
        subject.refresh();

        assertThat(subject.getAll()).hasSize(1);
        assertThat(subject.getAll().iterator().next().referenceName()).isEqualTo("tyrannosaurus rex");

    }

    @Test
    void refreshDescribesChanges() throws IOException {
        var refreshMessage = subject.refresh();
        assertThat(refreshMessage).isEqualTo("No changes were made to the reference species");

        Files.copy(
                SpeciesPropertiesTraderTest.class.getResourceAsStream("species-tyrannosaurus-rex.json"),
                location,
                REPLACE_EXISTING);
        refreshMessage = subject.refresh();

        var removedSpeciesPattern = Pattern.compile(".*\\[(.+)] removed.*");
        var matcher = removedSpeciesPattern.matcher(refreshMessage);
        matcher.find();
        assertThat(matcher.group(1).split(", ")).hasSize(speciesPropertiesCountBeforeRefresh);
        assertThat(refreshMessage).containsSubsequence("[Tyrannosaurus_rex] added");
    }

    @Test
    void failedRefreshKeepsOldProperties() {
        try {
            Files.copy(
                    toInputStream("invalid JSON contents", StandardCharsets.UTF_8),
                    location.resolve("species/species-properties.json"));
            subject.refresh();
            // We should never get here
            throw new RuntimeException();
        } catch (IOException e) {
            assertThat(subject.getAll()).hasSize(speciesPropertiesCountBeforeRefresh);
        }
    }

    @Test
    void unknownSpecies() {
        assertThat(subject.get(generateBlankString())).isEqualTo(SpeciesProperties.UNKNOWN);
        var namedUnknownSpecies = subject.get("foo bar");
        assertThat(namedUnknownSpecies.referenceName()).isEqualTo("foo bar");
        assertThat(namedUnknownSpecies.ensemblName()).isEqualTo("Foo_bar");
        assertThat(namedUnknownSpecies.kingdom()).isEmpty();
        assertThat(namedUnknownSpecies.defaultQueryFactorType()).isEmpty();
        assertThat(namedUnknownSpecies.resources()).isEmpty();
    }

    @Test
    void speciesNamesAreNormalised() {
        assertThat(subject.get("homo sapiens"))
                .isNotEqualTo(SpeciesProperties.UNKNOWN)
                .isEqualTo(subject.get("HoMo_SaPieNs"))
                .isEqualTo(subject.get("Homo sapiens"));

        assertThat(subject.get("hordeum vulgare"))
                .isNotEqualTo(SpeciesProperties.UNKNOWN)
                .isEqualTo(subject.get("Hordeum vulgare subsp. vulgare"))
                .isEqualTo(subject.get("Hordeum_vulgare"));
    }

    @Test
    void appliesExceptions() {
        assertThat(subject.get("canis lupus").referenceName())
                .isEqualTo(subject.get("Canis_lupus").referenceName())
                .isEqualTo(subject.get("canis_lupus_familiaris").referenceName())
                .isEqualTo(subject.get("Canis_lupus familiaris").referenceName())
                .isEqualTo(subject.get("Canis Lupus Familiari").referenceName())
                .isEqualTo("canis familiaris");
    }

    private Path createSpeciesPropertiesFile(InputStream in) throws IOException {
        var tempDirPath = Files.createTempDirectory("");
        tempDirPath.toFile().deleteOnExit();
        Files.copy(in, tempDirPath.resolve("species-properties.json"));
        return tempDirPath.resolve("species-properties.json");
    }
}
