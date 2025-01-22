package uk.ac.ebi.atlas.experimentpage.link;

import org.junit.Test;
import uk.ac.ebi.atlas.model.Profile;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LinkToGeneIT {
    static class DummyProfile extends Profile {
        DummyProfile(String id, String name) {
            super(id, name);
        }

        @Override
        protected Profile createEmptyCopy() {
            return null;
        }
    }

    // Not comprehensive, if gene IDs need to use any of the following chars we need to use a URLEncoder
    private static final String[] ILLEGAL_CHARS = {"%", "^", "|", "<", ">", "`", "\"", "\\", "[", "]", "{", "}"};

    private final LinkToGene<DummyProfile> subject = new LinkToGene<>();

    // The hash will be set by the view, see search-results.jsp
    @Test
    public void givenGeneLinksToExperiment_ThenAvailableResourcesContainsThoseLinks() {
        assertThat(subject.apply(new DummyProfile("geneId", "geneName")).toString()).endsWith("geneId");
    }

    @Test
    public void uriSyntaxExceptionsAreWrapped() {
        for (String illegalChar: ILLEGAL_CHARS) {
            assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                    () -> subject.apply(new DummyProfile(illegalChar, ""))
            )
                    .withCauseExactlyInstanceOf(URISyntaxException.class)
                    .withMessageEndingWith("at index 6: genes/" + illegalChar);
        }
    }
}
