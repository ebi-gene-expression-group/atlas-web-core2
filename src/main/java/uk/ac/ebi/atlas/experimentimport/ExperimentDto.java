package uk.ac.ebi.atlas.experimentimport;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.springframework.lang.Nullable;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import java.sql.Timestamp;
import java.util.Collection;

public class ExperimentDto {
    private final String experimentAccession;
    private final ExperimentType experimentType;
    private final String species;
    private final ImmutableSet<String> pubmedIds;
    private final ImmutableSet<String> dois;
    @Nullable
    private final Timestamp loadDate;
    @Nullable
    private final Timestamp lastUpdate;
    private final boolean isPrivate;
    private final String accessKey;

    public ExperimentDto(String experimentAccession,
                         ExperimentType experimentType,
                         String species,
                         Collection<String> pubmedIds,
                         Collection<String> dois,
                         Timestamp loadDate,
                         Timestamp lastUpdate,
                         boolean isPrivate,
                         String accessKey) {
        this.experimentAccession = experimentAccession;
        this.experimentType = experimentType;
        this.species = species;
        this.pubmedIds = ImmutableSet.copyOf(pubmedIds);
        this.dois = ImmutableSet.copyOf(dois);
        this.loadDate = loadDate;
        this.lastUpdate = lastUpdate;
        this.isPrivate = isPrivate;
        this.accessKey = accessKey;
    }

    public ExperimentDto(String experimentAccession,
                         ExperimentType experimentType,
                         String species,
                         Collection<String> pubmedIds,
                         Collection<String> dois,
                         boolean isPrivate,
                         String accessKey) {
        this.experimentAccession = experimentAccession;
        this.experimentType = experimentType;
        this.species = species;
        this.pubmedIds = ImmutableSet.copyOf(pubmedIds);
        this.dois = ImmutableSet.copyOf(dois);
        this.loadDate = null;
        this.lastUpdate = null;
        this.isPrivate = isPrivate;
        this.accessKey = accessKey;
    }

    public String getExperimentAccession() {
        return experimentAccession;
    }
    public ExperimentType getExperimentType() {
        return experimentType;
    }
    public boolean isPrivate() {
        return isPrivate;
    }
    @Nullable
    public Timestamp getLoadDate() { return loadDate; }
    @Nullable
    public Timestamp getLastUpdate() {
        return lastUpdate;
    }
    public String getAccessKey() {
        return accessKey;
    }
    public String getSpecies() {
        return species;
    }
    public ImmutableSet<String> getPubmedIds() {
        return pubmedIds;
    }
    public ImmutableSet<String> getDois() {
        return dois;
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.add("accession", new JsonPrimitive(experimentAccession));
        result.add("type", new JsonPrimitive(experimentType.name()));
        result.add("species", new JsonPrimitive(species));
        JsonArray pubmedIdsArray = new JsonArray();
        pubmedIds.forEach(id -> pubmedIdsArray.add(new JsonPrimitive(id)));
        JsonArray doisArray = new JsonArray();
        dois.forEach(doi -> doisArray.add(new JsonPrimitive(doi)));
        result.add("pubmedIds", pubmedIdsArray);
        result.add("dois", doisArray);
        result.add("isPrivate", new JsonPrimitive(isPrivate));
        result.add("accessKey", new JsonPrimitive(accessKey));
        result.add("loadDate", new JsonPrimitive(loadDate == null ? "" : loadDate.toString()));
        result.add("lastUpdate", new JsonPrimitive(lastUpdate == null ? "" : lastUpdate.toString()));

        return result;
    }

    @Override
    public int hashCode() {
        return experimentAccession.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExperimentDto) {
            ExperimentDto other = (ExperimentDto) obj;
            return this.experimentAccession.equals(other.experimentAccession);
        }
        return false;
    }
}
