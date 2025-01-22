package uk.ac.ebi.atlas.solr;

import org.junit.Test;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;

import static org.assertj.core.api.Assertions.assertThat;

public class BioentityPropertyNameTest {

    @Test
    public void getByNameIsSafe() {
        assertThat(BioentityPropertyName.getByName("¯\\_(ツ)_/¯")).isEqualTo(null);
        assertThat(BioentityPropertyName.getByName("")).isEqualTo(null);
        assertThat(BioentityPropertyName.getByName(null)).isEqualTo(null);
    }
}
