package uk.ac.ebi.atlas.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class StringUtilTest {
    private static final String UBERON_2000098_URL = "http://purl.obolibrary.org/obo/UBERON_2000098";

    @Test
    void utilityClassCannotBeInstantiated() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(StringUtil::new);
    }

    @Test
    void splitAtLastSlash() {
        assertThat(StringUtil.splitAtLastSlash(UBERON_2000098_URL))
                .isEqualTo(new String[]{"http://purl.obolibrary.org/obo/", "UBERON_2000098"});
        assertThat(StringUtil.splitAtLastSlash("foobar")).isEqualTo(new String[]{"foobar"});
    }

    @Test
    void escapeDoubleQuotes() {
        String s;
        do {
            s = RandomStringUtils.random(20);
        } while (!s.contains("\""));

        String[] split = StringUtil.escapeDoubleQuotes(s).split("\"");
        for (int i = 0; i < split.length - 1; i++) {
            assertThat(split[i]).endsWith("\\");
        }
    }

    @Test
    void suffixAfterLastSlashTrimsEverythingBeforeTheLastSlash() {
        String randomWord = randomAlphanumeric(20);
        assertThat(StringUtil.suffixAfterLastSlash(randomWord))
                .isEqualTo(StringUtil.suffixAfterLastSlash("/" + randomWord))
                .isEqualTo(StringUtil.suffixAfterLastSlash(randomAlphanumeric(20) + "/" + randomWord))
                .isEqualTo(randomWord);

        assertThat(StringUtil.suffixAfterLastSlash(""))
                .isEmpty();
    }

    @Test
    void snakeCaseToDisplayName() {
        assertThat(StringUtil.snakeCaseToDisplayName("biopsy_site"))
                .isEqualTo("Biopsy site");
    }

    @Test
    void wordsToSnakecase() {
        assertThat(StringUtil.wordsToSnakeCase("FACS Marker"))
                .isEqualTo("facs_marker");
    }
}
