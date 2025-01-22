package uk.ac.ebi.atlas.home;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.home.AtlasInformationDataType.EFO;
import static uk.ac.ebi.atlas.home.AtlasInformationDataType.EFO_URL;
import static uk.ac.ebi.atlas.home.AtlasInformationDataType.EG;
import static uk.ac.ebi.atlas.home.AtlasInformationDataType.ENSEMBL;
import static uk.ac.ebi.atlas.home.AtlasInformationDataType.WBPS;

class AtlasInformationDaoTest {
    @Test
    void ifMetadataFileDoesNotExistReturnUnknownForAllFields() {
        var subject = new AtlasInformationDao(Paths.get(randomAlphabetic(1, 10)));
        assertThat(subject.atlasInformation.get())
                .containsOnly(
                        createMapEntry(ENSEMBL.getId(), "unknown"),
                        createMapEntry(EG.getId(), "unknown"),
                        createMapEntry(WBPS.getId(), "unknown"),
                        createMapEntry(EFO.getId(), "unknown"),
                        createMapEntry(EFO_URL.getId(), "unknown"));
    }

    @Test
    void ifMetadataFileIsBrokenReturnUnknownForAllFields() {
        var path = createTempFileWithContents(
                "{\n" +
                "  \"ensembl\": \"96\"," +
                "  \"ensembl_genomes\": \"43\"," +
                "  \"wormbase_parasite\": \"12\"," +
                "  \"efo\":" +
                "}");

        var subject = new AtlasInformationDao(path);
        assertThat(subject.atlasInformation.get())
                .containsOnly(
                        createMapEntry(ENSEMBL.getId(), "unknown"),
                        createMapEntry(EG.getId(), "unknown"),
                        createMapEntry(WBPS.getId(), "unknown"),
                        createMapEntry(EFO.getId(), "unknown"),
                        createMapEntry(EFO_URL.getId(), "unknown"));
    }

    @Test
    void ifMetadataFileIsIncompleteReturnUnknownForRemainingFields() {
        var path = createTempFileWithContents(
                "{\n" +
                "  \"ensembl\": \"96\"," +
                "  \"ensembl_genomes\": \"43\"," +
                "  \"wormbase_parasite\": \"12\"" +
                "}");

        var subject = new AtlasInformationDao(path);
        assertThat(subject.atlasInformation.get())
                .containsOnly(
                        createMapEntry(ENSEMBL.getId(), "96"),
                        createMapEntry(EG.getId(), "43"),
                        createMapEntry(WBPS.getId(), "12"),
                        createMapEntry(EFO.getId(), "unknown"),
                        createMapEntry(EFO_URL.getId(), "unknown"));
    }

    @Test
    void foo() {
        var path = createTempFileWithContents(
                "{\n" +
                "  \"ensembl\": \"96\"," +
                "  \"ensembl_genomes\": \"43\"," +
                "  \"wormbase_parasite\": \"12\"," +
                "  \"efo\": \"3.5.1\"," +
                "  \"efo_url\": \"www.foo.com\"" +
                "}");

        var subject = new AtlasInformationDao(path);
        assertThat(subject.atlasInformation.get())
                .containsOnly(
                        createMapEntry(ENSEMBL.getId(), "96"),
                        createMapEntry(EG.getId(), "43"),
                        createMapEntry(WBPS.getId(), "12"),
                        createMapEntry(EFO.getId(), "3.5.1"),
                        createMapEntry(EFO_URL.getId(), "www.foo.com")
                );
    }

    private static <K, V> Map.Entry<K, V> createMapEntry(K key, V value) {
        return ImmutableMap.of(key, value).entrySet().iterator().next();
    }

    private static Path createTempFileWithContents(String str) {
        try {
            var path = Files.createTempFile("", "");
            var writer = new BufferedWriter(new FileWriter(path.toFile()));
            writer.write(str);
            writer.flush();
            writer.close();
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
