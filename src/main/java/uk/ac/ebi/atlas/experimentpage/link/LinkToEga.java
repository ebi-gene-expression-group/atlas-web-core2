package uk.ac.ebi.atlas.experimentpage.link;

import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.util.Map.entry;

@Component
public class LinkToEga {
    private static final UriBuilder EGA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ega")
                    .pathSegment("{0}")
                    .pathSegment("{1}");
    private static final Map<String, String> EGA_RESOURCE_TYPE_MAPPING = Map.ofEntries(
            entry("EGAD.*", "datasets"),
            entry("EGAS.*", "studies")
    );

    private static final Function<String, String> formatLabelToEga =
            arrayAccession -> MessageFormat.format("EGA: {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createEgaIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ega", label);

    private static final Function<String, ExternallyAvailableContent.Description> createIconForEga =
            formatLabelToEga.andThen(createEgaIcon);

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    public Collection<ExternallyAvailableContent> get(Experiment<?> experiment) {
        return new ResourceLinkGenerator().getLinks(experiment, EGA_RESOURCE_TYPE_MAPPING, EGA_URI_BUILDER, createIconForEga);
    }
}
