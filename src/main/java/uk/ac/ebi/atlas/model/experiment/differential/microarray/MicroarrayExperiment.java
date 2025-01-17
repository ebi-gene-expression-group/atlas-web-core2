package uk.ac.ebi.atlas.model.experiment.differential.microarray;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.arraydesign.ArrayDesign;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.species.Species;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

public class MicroarrayExperiment extends DifferentialExperiment {
    private final ImmutableSet<ArrayDesign> arrayDesigns;

    public MicroarrayExperiment(@NotNull ExperimentType experimentType,
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
                                @NotNull Collection<ArrayDesign> arrayDesigns,
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
                contrasts,
                experimentalFactorHeaders,
                pubMedIds,
                dois,
                isPrivate,
                accessKey);

        checkArgument(
                !arrayDesigns.isEmpty(),
                accession + ": Microarray experiment must have at least one array design");
        this.arrayDesigns = ImmutableSet.copyOf(arrayDesigns);
    }

    @NotNull
    public List<String> getArrayDesignAccessions() {
        return arrayDesigns.stream()
                .map(ArrayDesign::getAccession)
                .collect(toList());
    }

    @NotNull
    public List<@NotNull String> getArrayDesignNames() {
        return arrayDesigns.stream()
                .map(ArrayDesign::getName)
                .collect(toList());
    }
}
