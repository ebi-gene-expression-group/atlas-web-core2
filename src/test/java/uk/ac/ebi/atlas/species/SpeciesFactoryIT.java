package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SpeciesFactoryIT {
    @Autowired
    private SpeciesFactory subject;

    @Autowired
    private SpeciesPropertiesTrader speciesPropertiesTrader;

    @Test
    void differentSpeciesSameProperties() {
        var oryzaJaponica = subject.create("Oryza sativa Japonica group");
        var oryzaIndica = subject.create("Oryza sativa Indica group");

        assertThat(oryzaJaponica.isUnknown()).isFalse();
        assertThat(oryzaIndica.isUnknown()).isFalse();

        assertThat(oryzaJaponica.getName()).isNotEqualTo(oryzaIndica.getName());
        assertThat(oryzaJaponica.getReferenceName()).isEqualTo(oryzaIndica.getReferenceName());
        assertThat(oryzaJaponica.getEnsemblName()).isEqualTo(oryzaIndica.getEnsemblName());
        assertThat(oryzaJaponica.getDefaultQueryFactorType()).isEqualTo(oryzaIndica.getDefaultQueryFactorType());
        assertThat(oryzaJaponica.getKingdom()).isEqualTo(oryzaIndica.getKingdom());

        assertThat(oryzaJaponica.isPlant()).isTrue();
        assertThat(oryzaIndica.isPlant()).isTrue();
    }

    @Test
    void speciesComeWithGenomeBrowsers() {
        // Currently this is the only resource we care about
        assertThat(subject.create("homo sapiens").getGenomeBrowsers()).size().isGreaterThan(0);
    }

    @Test
    void unknownSpeciesHasOnlyEnsemblName() {
        assertThat(subject.create("foobar"))
                .hasFieldOrPropertyWithValue("ensemblName", "Foobar")
                .hasFieldOrPropertyWithValue("defaultQueryFactorType", "")
                .hasFieldOrPropertyWithValue("kingdom", "")
                .hasFieldOrPropertyWithValue("genomeBrowsers", ImmutableList.of());
    }

    @Test
    void emptySpecies() {
        assertThat(subject.create("").isUnknown()).isTrue();
    }

    @Test
    void nullSpecies() {
        assertThat(subject.create(null).isUnknown()).isTrue();
    }

    @Test
    void createUnknownSpecies() {
        assertThat(subject.createUnknownSpecies().isUnknown()).isTrue();
    }

    @Test
    public void oneSpeciesIsHuman() {
        var result = new HashSet<String>();
        for (var speciesProperties : speciesPropertiesTrader.getAll()) {
            if (subject.create(speciesProperties.ensemblName()).isUs()) {
                result.add(speciesProperties.ensemblName());
            }
        }

        assertThat(result).hasSize(1);
    }
}
