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
public class LinkToEna {
    private static final UriBuilder ENA_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("ena")
                    .pathSegment("browser")
                    .pathSegment("{0}")
                    .pathSegment("{1}");
    private static final Map<String, String> ENA_RESOURCE_TYPE_MAPPING = Map.ofEntries(
            entry("[DES]RP.*", "view")
    );

    private static final Function<String, String> formatLabelToEna =
            arrayAccession -> MessageFormat.format("ENA: {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createEnaIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ena", label);

    private static final Function<String, ExternallyAvailableContent.Description> createIconForEna =
            formatLabelToEna.andThen(createEnaIcon);

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    public Collection<ExternallyAvailableContent> get(Experiment<?> experiment) {
        return new ResourceLinkGenerator().getLinks(experiment, ENA_RESOURCE_TYPE_MAPPING, ENA_URI_BUILDER, createIconForEna);
    }
}
