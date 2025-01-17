package uk.ac.ebi.atlas.experimentpage.link;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.download.ExternallyAvailableContent;
import uk.ac.ebi.atlas.model.experiment.Experiment;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LinkToPride {
    private static final Function<String, String> formatLabel =
            secondaryAccession -> MessageFormat.format("PRIDE Archive: project {0}", secondaryAccession);
    private static final Function<String, String> formatLink =
            secondaryAccession -> MessageFormat.format("https://www.ebi.ac.uk/pride/archive/projects/{0}", secondaryAccession);

    private static final Function<String, ExternallyAvailableContent.Description> createIcon =
            formatLabel.andThen(label -> ExternallyAvailableContent.Description.create("icon-pride", label));

    public List<ExternallyAvailableContent> get(Experiment<?> experiment) {
        var secondaryAccessions = experiment.getSecondaryAccessions();

        if (noSecondaryAccession(secondaryAccessions)) {
            return getExternallyAvailableContents(secondaryAccessions);
        } else {
            return emptyContent();
        }
    }

    private boolean noSecondaryAccession(ImmutableSet<String> secondaryAccessions) {
        return secondaryAccessions != null && !secondaryAccessions.isEmpty();
    }

    @NotNull
    private static List<ExternallyAvailableContent> emptyContent() {
        return Collections.emptyList();
    }

    @NotNull
    private static List<ExternallyAvailableContent> getExternallyAvailableContents(ImmutableSet<String> secondaryAccessions) {
        return secondaryAccessions.stream()
                .map(secondaryAccession -> new ExternallyAvailableContent(
                                formatLink.apply(secondaryAccession),
                                createIcon.apply(secondaryAccession)
                        )
                )
                .collect(Collectors.toList());
    }

    public ExternallyAvailableContent.ContentType contentType() {
        return ExternallyAvailableContent.ContentType.SUPPLEMENTARY_INFORMATION;
    }
}
