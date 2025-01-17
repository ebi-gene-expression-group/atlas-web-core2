package uk.ac.ebi.atlas.bioentity.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesFactory;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.DESCRIPTION;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.ENSFAMILY_DESCRIPTION;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.ENSGENE;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.ENSPROTEIN;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.ENSTRANSCRIPT;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.FLYBASE_GENE_ID;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.PATHWAYID;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.WBPSGENE;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.WBPSPROTEIN;
import static uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName.WBPSTRANSCRIPT;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class BioEntityCardPropertiesIT {
    @Inject
    private SpeciesFactory speciesFactory;

    @Test
    void plantPathwayUrl() {
        assertThat(BioEntityCardProperties.getUrlTemplate(PATHWAYID, speciesFactory.create("arabidopsis thaliana")))
                .isEqualTo("http://plantreactome.gramene.org/content/detail/{0}");
    }

    @Test
    void nonPlantPathwayUrl() {
        assertThat(BioEntityCardProperties.getUrlTemplate(PATHWAYID, speciesFactory.create("homo sapiens")))
                .isEqualTo("https://reactome.org/content/detail/{0}");
        assertThat(BioEntityCardProperties.getUrlTemplate(PATHWAYID, speciesFactory.create("saccharomyces cerevisiae")))
                .isEqualTo("https://reactome.org/content/detail/{0}");
    }

    @Test
    void vertebrateEnsembleUrl() {
        Species species = speciesFactory.create("mus musculus");
        assertThat(BioEntityCardProperties.getUrlTemplate(ENSGENE, species))
                .isEqualTo("https://www.ensembl.org/" + species.getEnsemblName() + "/Gene/Summary?g={0}");
    }

    @Test
    void nonVertebrateEnsemblUrl() {
        Species species = speciesFactory.create("drosophila melanogaster");
        assertThat(BioEntityCardProperties.getUrlTemplate(ENSGENE, species))
                .isEqualTo("https://" + (species.getKingdom().equals("animals") ? "metazoa" : species.getKingdom()) +
                           ".ensembl.org/" + species.getEnsemblName() + "/Gene/Summary?g={0}");
    }

    @Test
    void fungiEnsemblUrl() {
        Species species = speciesFactory.create("saccharomyces cerevisiae");
        assertThat(BioEntityCardProperties.getUrlTemplate(ENSGENE, species))
                .isEqualTo(
                        "https://" + species.getKingdom() + ".ensembl.org/" + species.getEnsemblName() +
                        "/Gene/Summary?g={0}");
    }

    @Test
    void grameneEnsemblUrl() {
        Species species = speciesFactory.create("arabidopsis thaliana");
        assertThat(BioEntityCardProperties.getUrlTemplate(ENSGENE, species))
                .isEqualTo("http://ensembl.gramene.org/" + species.getEnsemblName() + "/Gene/Summary?g={0}");
    }

    @Test
    void wbpsAndEnsemblPropertiesAreEquivalent() {
        Species species = speciesFactory.create("schistosoma mansoni");
        assertThat(BioEntityCardProperties.getUrlTemplate(WBPSGENE, species))
                .isEqualTo(BioEntityCardProperties.getUrlTemplate(ENSGENE, species));
        assertThat(BioEntityCardProperties.getUrlTemplate(WBPSPROTEIN, species))
                .isEqualTo(BioEntityCardProperties.getUrlTemplate(ENSPROTEIN, species));
        assertThat(BioEntityCardProperties.getUrlTemplate(WBPSTRANSCRIPT, species))
                .isEqualTo(BioEntityCardProperties.getUrlTemplate(ENSTRANSCRIPT, species));
    }

    @Test
    void ensemblFamilyTemplateDoesNotUseThePropertyValueButTheGeneId() {
        Species species = speciesFactory.create("mus musculus");
        assertThat(BioEntityCardProperties.getUrlTemplate(ENSFAMILY_DESCRIPTION, species))
                .doesNotContain("{0}")
                .contains("{1}");
    }

    @Test
    void bioentityPropertyWithNoUrl() {
        Species species = speciesFactory.create("drosophila melanogaster");
        // Either a property which we didn’t deem relevant enough to be included in the gene information tab...
        assertThat(BioEntityCardProperties.getUrlTemplate(FLYBASE_GENE_ID, species))
                .isEmpty();
        // ... or something else
        assertThat(BioEntityCardProperties.getUrlTemplate(DESCRIPTION, species))
                .isEmpty();
    }

    @Test
    void unknownSpeciesReturnsEmptyTemplate() {
        for (BioentityPropertyName propertyName : BioEntityCardProperties.linkedPropertynames()) {
            if (propertyName.toString().startsWith("ENS") || propertyName.toString().startsWith("WBPS")) {
                assertThat(BioEntityCardProperties.getUrlTemplate(
                        propertyName, speciesFactory.createUnknownSpecies())).isEmpty();
            } else {
                assertThat(BioEntityCardProperties.getUrlTemplate(
                        propertyName, speciesFactory.createUnknownSpecies())).isNotEmpty();
            }
        }
    }
}
