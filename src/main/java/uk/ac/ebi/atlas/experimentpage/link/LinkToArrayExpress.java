package uk.ac.ebi.atlas.experimentpage.link;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public class LinkToArrayExpress {

    private final ResourceLinkGenerator resourceLinkGenerator;

    public LinkToArrayExpress(ResourceLinkGenerator resourceLinkGenerator) {
        this.resourceLinkGenerator = resourceLinkGenerator;
    }

    private static final UriBuilder BIOSTUDIES_API_URI_BUILDER =
            new DefaultUriBuilderFactory().builder()
                    .scheme("https")
                    .host("www.ebi.ac.uk")
                    .pathSegment("biostudies")
                    .pathSegment("arrayexpress")
                    .pathSegment("{0}")
                    .pathSegment("{1}");

    private static final Function<Experiment<?>, String> formatLabelToExperiment =
            e -> MessageFormat.format("ArrayExpress: experiment {0}", e.getAccession());
    private static final Function<String, String> formatLabelToArray =
            arrayAccession -> MessageFormat.format("ArrayExpress: array design {0}", arrayAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createIcon =
            label -> ExternallyAvailableContent.Description.create("icon-ae", label);

    private static final Function<Experiment<?>, ExternallyAvailableContent.Description> createIconForExperiment =
            formatLabelToExperiment.andThen(createIcon);
    private static final Function<String, ExternallyAvailableContent.Description> createIconForArray =
            formatLabelToArray.andThen(createIcon);

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }

    public Collection<ExternallyAvailableContent> get(Experiment<?> experiment) {

        var externalLinkFromExperiment = Stream.of(experiment.getAccession())
                .map(accession -> BIOSTUDIES_API_URI_BUILDER.build("studies", accession))
                .filter(resourceLinkGenerator::isUriValid)
                .map(uri -> new ExternallyAvailableContent(
                        uri.toString(),
                        createIconForExperiment.apply(experiment)));
        if (experiment.getType().isMicroarray()) {
            return Stream.concat(
                            externalLinkFromExperiment,
                            ((MicroarrayExperiment) experiment).getArrayDesignAccessions().stream()
                                    .parallel()
                                    .map(accession -> Pair.of(BIOSTUDIES_API_URI_BUILDER.build("arrays", accession), accession))
                                    .filter(uriAccession -> resourceLinkGenerator.isUriValid(uriAccession.getLeft()))
                                    .map(uriAccession -> new ExternallyAvailableContent(
                                            uriAccession.getLeft().toString(),
                                            createIconForArray.apply(uriAccession.getRight()))))
                    .collect(toImmutableList());
        }

         return externalLinkFromExperiment.collect(toImmutableList());
    }
}
