package uk.ac.ebi.atlas.experimentpage.link;

import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.util.Map.entry;

@Component
public class LinkToGeo {
    private static final UriBuilder GEO_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ncbi.nlm.nih.gov")
                    .pathSegment("geo")
                    .pathSegment("query")
                    .pathSegment("{0}")
                    .queryParam("acc", "{1}");
    private static final Map<String, String> GEO_RESOURCE_TYPE_MAPPING = Map.ofEntries(
            entry(".*G(SE|DS).*", "acc.cgi")
    );

    private static final Function<String, String> formatLabelToGeo =
            arrayAccession -> MessageFormat.format("GEO: {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createGeoIcon =
            label -> ExternallyAvailableContent.Description.create("icon-geo", label);

    private static final Function<String, ExternallyAvailableContent.Description> createIconForGeo =
            formatLabelToGeo.andThen(createGeoIcon);

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    public Collection<ExternallyAvailableContent> get(Experiment<? extends ReportsGeneExpression> experiment) {
        return new ResourceLinkGenerator().getLinks(experiment, GEO_RESOURCE_TYPE_MAPPING, GEO_URI_BUILDER, createIconForGeo);
    }

}
