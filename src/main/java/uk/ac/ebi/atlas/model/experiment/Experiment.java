package uk.ac.ebi.atlas.model.experiment;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.species.Species;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

// The displayName is a bit confusing - it's used for baseline landing page and I think only there.
// There's also a title which is fetched from the IDF file.
public abstract class Experiment<R extends ReportsGeneExpression> implements Serializable {
    private final ExperimentType type;
    private final ImmutableSet<String> secondaryAccessions;
    private final ImmutableSet<String> technologyType;
    private final String accession;
    protected final String description;
    private final Date loadDate;
    private final Date lastUpdate;
    private final Species species;
    private final ImmutableMap<String, R> id2ExpressedSamples;
    private final ImmutableSet<String> experimentalFactorHeaders;
    private final ImmutableSet<String> pubMedIds;
    private final ImmutableSet<String> dois;
    private final String displayName;
    private final String disclaimer;
    private final ImmutableSet<String> dataProviderUrls;
    private final ImmutableSet<String> dataProviderDescriptions;
    private final ImmutableSet<String> alternativeViews;
    private final ImmutableSet<String> alternativeViewDescriptions;
    private final ExperimentDisplayDefaults experimentDisplayDefaults;
    private final boolean isPrivate;
    private final String accessKey;

    public Experiment(
                      @NotNull ExperimentType type,
                      @NotNull String accession,
                      @Nullable Collection<String> secondaryAccessions,
                      @NotNull String description,
                      @NotNull Date loadDate,
                      @NotNull Date lastUpdate,
                      @NotNull Species species,
                      @NotNull Collection<@NotNull String> technologyType,
                      @NotNull Collection<@NotNull R> expressedSamples,
                      @NotNull ImmutableSet<@NotNull String> experimentalFactorHeaders,
                      @NotNull Collection<@NotNull String> pubMedIds,
                      @NotNull Collection<@NotNull String> dois,
                      @NotNull String displayName,
                      @NotNull String disclaimer,
                      @NotNull Collection<@NotNull String> dataProviderUrls,
                      @NotNull Collection<@NotNull String> dataProviderDescriptions,
                      @NotNull Collection<@NotNull String> alternativeViews,
                      @NotNull Collection<@NotNull String> alternativeViewDescriptions,
                      @NotNull ExperimentDisplayDefaults experimentDisplayDefaults,
                      boolean isPrivate,
                      @NotNull String accessKey) {
        checkArgument(isNotBlank(accession));
        checkArgument(isNotBlank(description));
        checkArgument(!species.isUnknown());
        checkArgument(!expressedSamples.isEmpty());
        checkArgument(dataProviderUrls.size() == dataProviderDescriptions.size());
        checkArgument(alternativeViews.size() == alternativeViewDescriptions.size());
        checkArgument(isNotBlank(accessKey));

        this.type = type;
        this.accession = accession;
        this.secondaryAccessions = ImmutableSet.copyOf(secondaryAccessions);
        this.description = description;
        this.loadDate = loadDate;
        this.lastUpdate = lastUpdate;
        this.species = species;
        this.technologyType = ImmutableSet.copyOf(technologyType);
        this.experimentalFactorHeaders = experimentalFactorHeaders;
        this.pubMedIds = pubMedIds.stream().sorted().collect(toImmutableSet());
        this.dois = dois.stream().sorted().collect(toImmutableSet());
        this.displayName = isBlank(displayName) ? accession : displayName;
        this.disclaimer = disclaimer;
        this.dataProviderUrls = ImmutableSet.copyOf(dataProviderUrls);
        this.dataProviderDescriptions = ImmutableSet.copyOf(dataProviderDescriptions);
        this.alternativeViews = ImmutableSet.copyOf(alternativeViews);
        this.alternativeViewDescriptions = ImmutableSet.copyOf(alternativeViewDescriptions);
        this.experimentDisplayDefaults = experimentDisplayDefaults;
        this.isPrivate = isPrivate;
        this.accessKey = accessKey;

        id2ExpressedSamples =
                expressedSamples.stream().collect(toImmutableMap(ReportsGeneExpression::getId, identity()));
    }

    @Nullable
    public ImmutableSet<String>  getSecondaryAccessions() { return secondaryAccessions; }

    @NotNull
    public ImmutableSet<String>  getTechnologyType() { return technologyType; }

    @NotNull
    public ImmutableList<R> getDataColumnDescriptors() {
        return ImmutableList.copyOf(id2ExpressedSamples.values());
    }

    @Nullable
    public R getDataColumnDescriptor(@NotNull String id) {
        return id2ExpressedSamples.get(id);
    }

    @NotNull
    public ExperimentType getType() {
        return type;
    }

    @NotNull
    public ImmutableSet<String> getExperimentalFactorHeaders() {
        return experimentalFactorHeaders;
    }

    @NotNull
    public ExperimentDisplayDefaults getDisplayDefaults() {
        return experimentDisplayDefaults;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getAccession() {
        return accession;
    }

    @NotNull
    public Species getSpecies() {
        return species;
    }

    @NotNull
    public String getDisclaimer() {
        return disclaimer;
    }

    @NotNull
    public Date getLoadDate() {
        return loadDate;
    }

    @NotNull
    public Date getLastUpdate() {
        return lastUpdate;
    }

    @NotNull
    public ImmutableSet<String> getPubMedIds() {
        return pubMedIds;
    }

    @NotNull
    public ImmutableSet<String> getDois() {
        return dois;
    }

    @NotNull
    public ImmutableSet<String> getDataProviderDescription() {
        return dataProviderDescriptions;
    }

    @NotNull
    public ImmutableSet<String> getDataProviderURL() {
        return dataProviderUrls;
    }

    @NotNull
    public ImmutableSet<String> getAlternativeViews() {
        return alternativeViews;
    }

    @NotNull
    public ImmutableSet<String> getAlternativeViewDescriptions() {
        return alternativeViewDescriptions;
    }

    @NotNull
    public ImmutableSet<String> getAnalysedAssays() {
        return id2ExpressedSamples.values().stream()
                .flatMap(dataColumnDescriptor -> dataColumnDescriptor.getAssayIds().stream())
                .collect(toImmutableSet());
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    @NotNull
    public String getAccessKey() {
        return accessKey;
    }

    @NotNull
    public ImmutableList<ImmutableMap<String, String>> getGenomeBrowsers() {
        return type.isMicroRna() ? ImmutableList.of() : species.getGenomeBrowsers();
    }

    @NotNull
    public ImmutableSet<String> getGenomeBrowserNames() {
        return getGenomeBrowsers().stream()
                .map(map -> map.get("name"))
                .collect(toImmutableSet());
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (Experiment<R>) o;
        return Objects.equal(accession, that.accession);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accession);
    }

    @NotNull
    protected abstract ImmutableList<JsonObject> propertiesForAssay(@NotNull String runOrAssay);
}
