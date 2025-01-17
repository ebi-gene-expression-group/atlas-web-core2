package uk.ac.ebi.atlas.model.experiment;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

// Originally Atlas displayed nested menus, in the order specified by menuFilterFactorTypes, that you would
// sequentially navigate to display a slice of the experiment (e.g. ORGANISM_PART=lung, DISEASE=asthma). That’s why
// the factor types tag is called <menuFilterFactorTypes>. With the current UI the order of the factors isn’t relevant,
// although ImmutableSet keeps insertion order (which is fine). Right now, <defaultFilterFactors> is the slice that is
// shown initially in the heatmap (bottom of the left sidebar in the experiment page).
@AutoValue
public abstract class ExperimentDisplayDefaults {
    public abstract String getDefaultQueryFactorType();
    public abstract ImmutableMap<String, String> getDefaultFilterValues();
    public abstract ImmutableSet<String> getFactorTypes();
    public abstract boolean isColumnOrderPreserved();

    /* <exp_accession>-factors.xml:
       <defaultFilterFactors>
           <filterFactor>
               <type>TIME</type>
               <value>4 day</value>
           </filterFactor>
           <filterFactor>
               <type>CELL_TYPE</type>
               <value>CD4+ T cell</value>
           </filterFactor>
           <filterFactor>
               <type>INFECT</type>
               <value>Plasmodium chabaudi chabaudi</value>
           </filterFactor>
       </defaultFilterFactors>
       <defaultQueryFactorType>INDIVIDUAL</defaultQueryFactorType>
       <menuFilterFactorTypes>CELL_TYPE, INDIVIDUAL, INFECT, TIME</menuFilterFactorTypes>
    */
    @NotNull
    public static ExperimentDisplayDefaults create(@NotNull String defaultQueryFactorType,
                                                   @NotNull Collection<@NotNull Factor> defaultFilterFactors,
                                                   @NotNull Collection<@NotNull String> factorTypes,
                                                   boolean isColumnOrderPreserved) {
        checkArgument(
                isNotBlank(defaultQueryFactorType),
                "Default query factor type cannot be blank");
        checkArgument(
                factorTypes.isEmpty() || factorTypes.contains(defaultQueryFactorType),
                "Default query factor type must be present in the factor types (check <menuFilterFactorTypes>)");
        checkArgument(
                !defaultFilterFactors.stream()
                        .map(Factor::getType)
                        .collect(toImmutableSet())
                        .contains(defaultQueryFactorType),
                "Default query factor type cannot be part of the default filters factor types");

        return new AutoValue_ExperimentDisplayDefaults(
                defaultQueryFactorType,
                defaultFilterFactors.stream().collect(toImmutableMap(Factor::getType, Factor::getValue)),
                factorTypes.stream().collect(toImmutableSet()),
                isColumnOrderPreserved);
    }

    @NotNull
    public static ExperimentDisplayDefaults create() {
        return new AutoValue_ExperimentDisplayDefaults("", ImmutableMap.of(), ImmutableSet.of(), false);
    }

    @NotNull
    public Optional<String> defaultFilterValuesForFactor(@NotNull String factorHeader) {
        String normalizedFactorHeader = Factor.normalize(factorHeader);
        return Optional.ofNullable(getDefaultFilterValues().get(normalizedFactorHeader));
    }
}
