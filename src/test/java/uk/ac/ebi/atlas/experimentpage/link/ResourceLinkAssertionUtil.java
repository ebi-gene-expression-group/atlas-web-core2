package uk.ac.ebi.atlas.experimentpage.link;

import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceLinkAssertionUtil {

    public static void assertResourceLink(ExternallyAvailableContent resourceLink,
                                          String uri,
                                          String descriptionType,
                                          String description) throws URISyntaxException {
        assertThat(resourceLink)
                .hasFieldOrPropertyWithValue(
                        "uri",
                        new URI(uri))
                .hasFieldOrPropertyWithValue(
                        "description",
                        ExternallyAvailableContent.Description.create(descriptionType, description));
    }
}
