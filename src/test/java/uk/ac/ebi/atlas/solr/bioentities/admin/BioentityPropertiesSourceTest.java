package uk.ac.ebi.atlas.solr.bioentities.admin;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.resource.BioentityPropertyFile;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertiesSource;
import uk.ac.ebi.atlas.solr.bioentities.BioentityProperty;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.species.SpeciesProperties;
import uk.ac.ebi.atlas.species.SpeciesPropertiesDao;
import uk.ac.ebi.atlas.species.SpeciesPropertiesTrader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.DESIGN_ELEMENT;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.PATHWAYID;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.PATHWAYNAME;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

class BioentityPropertiesSourceTest {
    private Path bioentityPropertiesDirectoryLocation;
    private static final SpeciesProperties HUMAN =
            SpeciesProperties.create("Homo_sapiens", "ORGANISM_PART", "animals", ImmutableList.of());

    private BioentityPropertiesSource subject;

    @BeforeEach
    void setUp() throws Exception {
        bioentityPropertiesDirectoryLocation = Files.createTempDirectory("");
        bioentityPropertiesDirectoryLocation.toFile().deleteOnExit();

        var speciesPropertiesDao = mock(SpeciesPropertiesDao.class);
        when(speciesPropertiesDao.fetchAll()).thenReturn(ImmutableList.of(HUMAN));

        var speciesPropertiesTrader = new SpeciesPropertiesTrader();
        speciesPropertiesTrader.setSpeciesPropertiesDao(speciesPropertiesDao);
        speciesPropertiesTrader.refresh();

        subject =
                new BioentityPropertiesSource(
                        Files.createDirectories(bioentityPropertiesDirectoryLocation.resolve("annotations")),
                        Files.createDirectories(bioentityPropertiesDirectoryLocation.resolve("array_designs")),
                        Files.createDirectories(bioentityPropertiesDirectoryLocation.resolve("reactome")),
                        new SpeciesFactory(speciesPropertiesTrader));
    }

    private void addTemporaryFile(String where, String fileName, Collection<String[]> lines) throws IOException {
        var path = Files.createFile(bioentityPropertiesDirectoryLocation.resolve(where).resolve(fileName));
        path.toFile().deleteOnExit();
        Files.write(path, lines.stream().map(l -> Joiner.on("\t").join(l)).collect(toList()), StandardCharsets.UTF_8);
    }

    @Test
    void noFilesMeansEmptyStreams() {
        assertThat(subject.getAnnotationFiles()).isEmpty();
        assertThat(subject.getArrayDesignMappingFiles()).isEmpty();
        assertThat(subject.getReactomePropertyFiles()).isEmpty();
    }

    @Test
    void oddFilesSkipped() throws Exception {
        addTemporaryFile(
                "annotations",
                "not-a-right-name.tsv",
                ImmutableList.of());

        addTemporaryFile(
                "annotations",
                "Homo_sapiens.ensprotein.tsv",
                ImmutableList.of());

        addTemporaryFile(
                "annotations",
                "Homo_sapiens.enstranscript.tsv",
                ImmutableList.of());

        assertThat(subject.getAnnotationFiles()).isEmpty();

        addTemporaryFile(
                "annotations",
                "Homo_sapiens.ensgene.tsv",
                ImmutableList.of());

        assertThat(subject.getAnnotationFiles()).hasSize(1);

        addTemporaryFile(
                "annotations",
                "Sleazy_worm.wbpsgene.tsv",
                ImmutableList.of());

        assertThat(subject.getAnnotationFiles()).hasSize(2);
    }

    private void assertHasOneGoodResource(Stream<? extends BioentityPropertyFile> s) {
        var bioentityPropertyFiles = s.collect(toImmutableList());
        assertThat(bioentityPropertyFiles).hasSize(1);
        assertThat(bioentityPropertyFiles.get(0).existsAndIsNonEmpty()).isTrue();
    }

    @Test
    void goodButEmptyAnnotationFile() throws Exception {
        addTemporaryFile(
                "annotations",
                "Homo_sapiens.ensgene.tsv",
                ImmutableList.of(
                        new String[]{"ensgene", "synonym", "ensfamily_description", "goterm"}));

        assertHasOneGoodResource(subject.getAnnotationFiles());

        assertThat(
                subject.getAnnotationFiles()
                       .findFirst()
                       .orElseThrow(RuntimeException::new)
                       .species
                       .getEnsemblName())
                .isEqualTo(HUMAN.ensemblName());
    }

    @Test
    void unknownSpeciesAreFilledWithTheirName()
                throws Exception {
        var unknownSpecies = generateRandomSpecies();

        addTemporaryFile(
                "annotations",
                unknownSpecies.getEnsemblName() + ".ensgene.tsv",
                ImmutableList.of(
                        new String[]{"ensgene", "synonym", "ensfamily_description", "goterm"}));

        assertHasOneGoodResource(subject.getAnnotationFiles());

        assertThat(
                subject.getAnnotationFiles()
                       .findFirst()
                       .orElseThrow(RuntimeException::new)
                       .species
                       .getEnsemblName())
                .isEqualTo(unknownSpecies.getEnsemblName());
    }

    @Test
    void goodBasicFile() throws Exception {
        addTemporaryFile(
                "annotations",
                "Homo_sapiens.ensgene.tsv",
                ImmutableList.of(
                        new String[]{"ensgene", "synonym"},
                        new String[]{"id", "synonym_value"}));

        assertHasOneGoodResource(subject.getAnnotationFiles());

        assertThat(
                subject.getAnnotationFiles()
                       .findFirst()
                       .orElseThrow(RuntimeException::new)
                       .get())
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty("id", "Homo_sapiens", "ensgene", "id"),
                        new BioentityProperty("id", "Homo_sapiens", "synonym", "synonym_value"));
    }

    @Test
    void goodMirnaFile() throws Exception {
        addTemporaryFile(
                "annotations",
                "Homo_sapiens.mature_mirna.tsv",
                ImmutableList.of(
                        new String[]{"mirbase_accession", "symbol", "mirbase_name", "mirbase_sequence"},
                        new String[]{"MIMAT0001535", "cfa-miR-448", "miR-448", "UUGCAUAUGUAGGAUGUCCCAU"}));

        assertHasOneGoodResource(subject.getAnnotationFiles());

        assertThat(
                subject.getAnnotationFiles()
                       .findFirst()
                       .orElseThrow(RuntimeException::new)
                       .get())
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty(
                                "MIMAT0001535", "Homo_sapiens", "mirbase_accession", "MIMAT0001535"),
                        new BioentityProperty(
                                "MIMAT0001535", "Homo_sapiens", "symbol", "cfa-miR-448"),
                        new BioentityProperty(
                                "MIMAT0001535", "Homo_sapiens", "mirbase_name", "miR-448"),
                        new BioentityProperty(
                                "MIMAT0001535", "Homo_sapiens", "mirbase_sequence", "UUGCAUAUGUAGGAUGUCCCAU"));
    }

    @Test
    void goodMultipleFiles() throws Exception {
        addTemporaryFile(
                "annotations",
                "Homo_sapiens.ensgene.tsv",
                ImmutableList.of(
                        new String[]{"ensgene", "synonym"},
                        new String[]{"id", "synonym_value"}));

        var unknownSpecies = generateRandomSpecies();
        addTemporaryFile(
                "annotations",
                unknownSpecies.getEnsemblName() + ".ensgene.tsv",
                ImmutableList.of(
                        new String[]{"ensgene", "synonym", "other"},
                        new String[]{"id", "synonym_value", "other_value"}));

        assertThat(
                subject.getAnnotationFiles()
                       .flatMap(BioentityPropertiesSource.AnnotationFile::get))
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty("id", "Homo_sapiens", "ensgene", "id"),
                        new BioentityProperty("id", "Homo_sapiens", "synonym", "synonym_value"),
                        new BioentityProperty("id", unknownSpecies.getEnsemblName(), "ensgene", "id"),
                        new BioentityProperty("id", unknownSpecies.getEnsemblName(), "synonym", "synonym_value"),
                        new BioentityProperty("id", unknownSpecies.getEnsemblName(), "other", "other_value"));
    }

    @Test
    void annotationFileSkipsEmptyProperties() throws Exception {
        addTemporaryFile(
                "annotations",
                "Homo_sapiens.ensgene.tsv",
                ImmutableList.of(
                        new String[]{"ensgene", "synonym", "other"},
                        new String[]{"id", "synonym_value", ""}));

        assertThat(
                subject.getAnnotationFiles()
                       .flatMap(BioentityPropertiesSource.AnnotationFile::get))
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty("id", "Homo_sapiens", "ensgene", "id"),
                        new BioentityProperty("id", "Homo_sapiens", "synonym", "synonym_value"));
    }

    @Test
    void annotationFileSplitsPropertiesOnSeparator() throws Exception {
        addTemporaryFile("annotations", "Homo_sapiens.ensgene.tsv", ImmutableList.of(
                new String[]{"ensgene", "synonym"},
                new String[]{"id", "v1@@v2"}));

        assertThat(
                subject.getAnnotationFiles()
                       .flatMap(BioentityPropertiesSource.AnnotationFile::get))
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty("id", "Homo_sapiens", "ensgene", "id"),
                        new BioentityProperty("id", "Homo_sapiens", "synonym", "v1"),
                        new BioentityProperty("id", "Homo_sapiens", "synonym", "v2"));
    }

    @Test
    void arrayDesignsHaveNoHeader() throws Exception {
        addTemporaryFile(
                "array_designs",
                "Homo_sapiens.A-AFFY-1.tsv",
                ImmutableList.of(new String[]{"ENSG00000000003", "39361_f_at"}));

        assertThat(
                subject.getArrayDesignMappingFiles()
                       .flatMap(BioentityPropertiesSource.ArrayDesignMappingFile::get))
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty("ENSG00000000003", "Homo_sapiens", DESIGN_ELEMENT.name, "39361_f_at"));
    }

    @Test
    void arrayDesignsCanHaveEmptyProperties() throws Exception {
        addTemporaryFile(
                "array_designs",
                "Homo_sapiens.A-AFFY-1.tsv",
                ImmutableList.of(new String[]{"ENSG00000000003", ""}));

        assertThat(
                subject.getArrayDesignMappingFiles()
                       .flatMap(BioentityPropertiesSource.ArrayDesignMappingFile::get))
                .isEmpty();
    }

    @Test
    void arrayDesignsCanComeFromDifferentFiles() throws Exception {
        addTemporaryFile(
                "array_designs",
                "Homo_sapiens.A-AFFY-1.tsv",
                ImmutableList.of(new String[]{"ENSG00000000003", "39361_f_at"}));

        addTemporaryFile(
                "array_designs",
                "Homo_sapiens.A-AFFY-2.tsv",
                ImmutableList.of(new String[]{"ENSG00000000003", "something_different_at"}));

        assertThat(
                subject.getArrayDesignMappingFiles()
                       .flatMap(BioentityPropertiesSource.ArrayDesignMappingFile::get))
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty(
                                "ENSG00000000003", "Homo_sapiens", DESIGN_ELEMENT.name, "39361_f_at"),
                        new BioentityProperty(
                                "ENSG00000000003", "Homo_sapiens", DESIGN_ELEMENT.name, "something_different_at"));
    }

    @Test
    void reactomeTypicalFile() throws Exception {
        addTemporaryFile(
                "reactome",
                "Homo_sapiens.reactome.tsv",
                ImmutableList.of(
                        new String[]{"ensgene", "pathwayid", "pathwayname"},
                        new String[]{"ENSG00000000419", "R-HSA-162699", "Synthesis of dolichyl-phosphate mannose"}));

        assertThat(
                subject.getReactomePropertyFiles()
                       .flatMap(BioentityPropertiesSource.ReactomePropertyFile::get))
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty(
                                "ENSG00000000419",
                                "Homo_sapiens",
                                PATHWAYID.name,
                                "R-HSA-162699"),
                        new BioentityProperty(
                                "ENSG00000000419",
                                "Homo_sapiens",
                                PATHWAYNAME.name,
                                "Synthesis of dolichyl-phosphate mannose"));
    }

    @Test
    void reactomeHeaderIsIgnored() throws Exception {
        addTemporaryFile(
                "reactome",
                "Homo_sapiens.reactome.tsv",
                ImmutableList.of(
                        new String[]{"header", "is", "ignored"},
                        new String[]{"ENSG00000000419", "R-HSA-162699", "Synthesis of dolichyl-phosphate mannose"}));

        assertThat(
                subject.getReactomePropertyFiles()
                       .flatMap(BioentityPropertiesSource.ReactomePropertyFile::get))
                .asList()
                .containsExactlyInAnyOrder(
                        new BioentityProperty(
                                "ENSG00000000419",
                                "Homo_sapiens",
                                PATHWAYID.name,
                                "R-HSA-162699"),
                        new BioentityProperty(
                                "ENSG00000000419",
                                "Homo_sapiens",
                                PATHWAYNAME.name,
                                "Synthesis of dolichyl-phosphate mannose"));
    }
}
