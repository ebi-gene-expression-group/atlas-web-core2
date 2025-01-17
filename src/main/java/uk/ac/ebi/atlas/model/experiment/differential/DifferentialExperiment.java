package uk.ac.ebi.atlas.model.experiment.differential;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDisplayDefaults;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.species.Species;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class DifferentialExperiment extends Experiment<Contrast> {
    private final Set<Contrast> contrastsWithCttvPrimaryAnnotation;

    public DifferentialExperiment(@NotNull ExperimentType experimentType,
                                  @NotNull String accession,
                                  @NotNull Collection<String> secondaryAccessions,
                                  @NotNull String description,
                                  @NotNull Date loadDate,
                                  @NotNull Date lastUpdate,
                                  @NotNull Species species,
                                  @NotNull Collection<String> technologyType,
                                  @NotNull Collection<Pair<Contrast, Boolean>> contrasts,
                                  @NotNull ImmutableSet<String> experimentalFactorHeaders,
                                  @NotNull Collection<String> pubMedIds,
                                  @NotNull Collection<String> dois,
                                  boolean isPrivate,
                                  @NotNull String accessKey) {
        super(
                experimentType,
                accession,
                secondaryAccessions,
                description,
                loadDate,
                lastUpdate,
                species,
                technologyType,
                contrasts.stream().map(Pair::getLeft).collect(toList()),
                experimentalFactorHeaders,
                pubMedIds,
                dois,
                "",
                "",
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                ExperimentDisplayDefaults.create(),
                isPrivate,
                accessKey);

        ImmutableSet<ImmutableSet<String>> uniqueAnalysedPairs = contrasts.stream()
                .map(Pair::getLeft)
                .map(contrast -> ImmutableSet.of(contrast.getReferenceAssayGroup().getId(), contrast.getTestAssayGroup().getId()))
                .collect(toImmutableSet());
        checkArgument(
                uniqueAnalysedPairs.size() == contrasts.size(),
                accession + ": Experiment cannot contain two contrasts with the same reference and test assay groups");

        this.contrastsWithCttvPrimaryAnnotation =
                contrasts.stream().filter(Pair::getRight).map(Pair::getLeft).collect(toSet());
    }

    public boolean doesContrastHaveCttvPrimaryAnnotation(@NotNull Contrast contrast) {
        return contrastsWithCttvPrimaryAnnotation.contains(contrast);
    }

    @Override
    @NotNull
    protected ImmutableList<JsonObject> propertiesForAssay(@NotNull String runOrAssay) {
        var contrastsWhereAssayIsInReferenceAssayGroup =
                getDataColumnDescriptors().stream()
                        .filter(contrast -> contrast.getReferenceAssayGroup().getAssayIds().contains(runOrAssay))
                        .collect(toImmutableList());
        var contrastsWhereAssayIsInTestAssayGroup =
                getDataColumnDescriptors().stream()
                        .filter(contrast -> contrast.getTestAssayGroup().getAssayIds().contains(runOrAssay))
                        .collect(toImmutableList());

        // Assay is not in either test or reference assay groups
        if (contrastsWhereAssayIsInReferenceAssayGroup.isEmpty() && contrastsWhereAssayIsInTestAssayGroup.isEmpty()) {
            var jsonObject = new JsonObject();
            jsonObject.addProperty("contrastName", "none");
            jsonObject.addProperty("referenceOrTest", "");
            return ImmutableList.of(jsonObject);
        }

        return Streams.concat(
                contrastsWhereAssayIsInReferenceAssayGroup.stream()
                        .map(DifferentialExperiment::referenceContrastToJson),
                contrastsWhereAssayIsInTestAssayGroup.stream()
                        .map(DifferentialExperiment::testContrastToJson))
                .collect(toImmutableList());
    }

    static private JsonObject testContrastToJson(Contrast contrast) {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("contrastName", contrast.getDisplayName());
        jsonObject.addProperty("referenceOrTest", "test");
        return jsonObject;
    }

    static private JsonObject referenceContrastToJson(Contrast contrast) {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("contrastName", contrast.getDisplayName());
        jsonObject.addProperty("referenceOrTest", "reference");
        return jsonObject;
    }

}
