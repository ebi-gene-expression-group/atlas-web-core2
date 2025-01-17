package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ebi.atlas.model.OntologyTerm;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;
import uk.ac.ebi.atlas.model.experiment.sdrf.FactorSet;
import uk.ac.ebi.atlas.model.experiment.sdrf.SampleCharacteristic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 *  ExperimentDesign stores factors and characteristics per _assay_ and other information
 *  needed to render the experiment design page. On experiment import, it is created from
 *  the condensed SDRF and SDRF  files and persisted into ExpDesign files. The ExpDesign
 *  files act as a cache of relevant information in the SDRF, because parsing the SDRF is
 *  an expensive operation.
 *
 *  ExperimentalFactors also has factor information, but per _assay group_.
 *  ExperimentalFactors is used to render the experiment page.
 *
 *  An important distinction note: factor headers are NOT the same as factor types.
 *  Factor headers are not normalized (see Factor.normalize), where as factor types
 *  (i.e.: Factor::getType) are.
 */
public class ExperimentDesign implements Serializable {
    // Headers retrieved from the condensed SDRF file
    private Set<String> sampleCharacteristicHeaders = new LinkedHashSet<>();
    private Set<String> factorHeaders = new LinkedHashSet<>();

    // Headers retrieved from the SDRF file, which maintain a curated order
    private Set<String> orderedSampleCharacteristicHeaders = new LinkedHashSet<>();
    private Set<String> orderedFactorHeaders = new LinkedHashSet<>();

    // Assay ID -> sample characteristics
    private Map<String, SampleCharacteristics> assayId2SampleCharacteristic = new HashMap<>();
    // getHeader, value
    private class SampleCharacteristics extends HashMap<String, SampleCharacteristic> { }
    // Assay ID -> factors
    private Map<String, FactorSet> assayId2Factor = new HashMap<>();

    private Map<String, String> arrayDesigns = new HashMap<>();
    private List<String> assayHeaders = new ArrayList<>();

    public void putSampleCharacteristic(String runOrAssay,
                                        String sampleCharacteristicHeader,
                                        String sampleCharacteristicValue) {
        SampleCharacteristic sampleCharacteristic =
                SampleCharacteristic.create(sampleCharacteristicHeader, sampleCharacteristicValue);
        putSampleCharacteristic(runOrAssay, sampleCharacteristicHeader, sampleCharacteristic);
    }

    public void putSampleCharacteristic(String runOrAssay,
                                        String sampleHeader,
                                        SampleCharacteristic sampleCharacteristic) {
        if (!assayId2SampleCharacteristic.containsKey(runOrAssay)) {
            assayId2SampleCharacteristic.put(runOrAssay, new SampleCharacteristics());
        }
        assayId2SampleCharacteristic.get(runOrAssay).put(sampleHeader, sampleCharacteristic);
        sampleCharacteristicHeaders.add(sampleHeader);
    }

    public void putFactor(String runOrAssay, String factorHeader, String factorValue) {
        putFactor(runOrAssay, factorHeader, factorValue, new OntologyTerm[0]);
    }

    public void putFactor(String runOrAssay,
                          String factorHeader,
                          String factorValue,
                          OntologyTerm... factorOntologyTerms) {
        var factor = new Factor(factorHeader, factorValue, factorOntologyTerms);
        if (!assayId2Factor.containsKey(runOrAssay)) {
            assayId2Factor.put(runOrAssay, new FactorSet());
        }
        assayId2Factor.get(runOrAssay).add(factor);
        factorHeaders.add(factorHeader);
    }

    public void putArrayDesign(String runOrAssay, String arrayDesign) {
        arrayDesigns.put(runOrAssay, arrayDesign);
    }

    public String getArrayDesign(String runOrAssay) {
        return arrayDesigns.get(runOrAssay);
    }

    public void addAssayHeader(String assayHeader) {
        assayHeaders.add(assayHeader);
    }

    public void setOrderedSampleCharacteristicHeaders(Set<String> orderedSampleCharacteristicHeaders) {
        this.orderedSampleCharacteristicHeaders = orderedSampleCharacteristicHeaders;
    }

    public void setOrderedFactorHeaders(Set<String> orderedFactorHeaders) {
        this.orderedFactorHeaders = orderedFactorHeaders;
    }

    // Assay headers are column headers such as Sample, Run, Array... (everything but sample characteristics, factors
    // and their ontology terms)
    public List<String> getAssayHeaders() {
        return assayHeaders;
    }

    public ImmutableSet<String> getSampleCharacteristicHeaders() {
        if(!orderedSampleCharacteristicHeaders.isEmpty()) {
            return ImmutableSet.<String>builder()
                    .addAll(orderedSampleCharacteristicHeaders)
                    .addAll(sampleCharacteristicHeaders)
                    .build();
        }
        return ImmutableSet.<String>builder().addAll(sampleCharacteristicHeaders).build();
    }

    // Factor headers are not normalized (see Factor::normalize), unlike factor type !
    public ImmutableSet<String> getFactorHeaders() {
        if(!orderedFactorHeaders.isEmpty()) {
            return ImmutableSet.<String>builder().addAll(orderedFactorHeaders).addAll(factorHeaders).build();
        }
        return ImmutableSet.<String>builder().addAll(factorHeaders).build();
    }

    @Nullable
    public SampleCharacteristic getSampleCharacteristic(String runOrAssay, String sampleHeader) {
        var sampleCharacteristics = this.assayId2SampleCharacteristic.get(runOrAssay);
        return (sampleCharacteristics == null) ? null :  sampleCharacteristics.get(sampleHeader);
    }

    @Nullable
    public Factor getFactor(String runOrAssay, String factorHeader) {
        var factorSet = assayId2Factor.get(runOrAssay);
        if (factorSet == null) {
            return null;
        }
        return factorSet.factorOfType(Factor.normalize(factorHeader));
    }

    @Nullable
    public String getFactorValue(String runOrAssay, String factorHeader) {
        var factorSet = assayId2Factor.get(runOrAssay);
        if (factorSet != null) {

            var factor = factorSet.factorOfType(Factor.normalize(factorHeader));
            return factor == null ? null : factor.getValue();
        }
        return null;
    }

    public ImmutableSetMultimap<String, String> getAllOntologyTermIdsByAssayAccession() {
        ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();

        getFactorOntologyTerms().forEach(builder::putAll);
        getCharacteristicOntologyTerms().forEach(builder::putAll);

        return builder.build();
    }

    private Map<String, Set<String>> getFactorOntologyTerms() {
        return assayId2Factor.entrySet().stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        entry ->
                                stream(entry.getValue().spliterator(), false)
                                        .flatMap(factor ->
                                                factor.getValueOntologyTerms().stream())
                                        .map(OntologyTerm::accession)
                                        .collect(toSet())));
    }

    private Map<String, Set<String>> getCharacteristicOntologyTerms() {
        return assayId2SampleCharacteristic.entrySet().stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        entry ->
                                entry.getValue().values().stream()
                                        .flatMap(sampleCharacteristic ->
                                                sampleCharacteristic.getValueOntologyTerms().stream())
                                        .map(OntologyTerm::accession)
                                        .collect(toSet())));
    }

    public Map<String, String> getFactorValues(String runOrAssay) {
        var factorSet = assayId2Factor.get(runOrAssay);

        if (factorSet == null) {
            return ImmutableMap.of();
        }
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (var factor : factorSet) {
            builder.put(factor.getHeader(), factor.getValue());
        }

        return builder.build();
    }

    @Nullable
    public FactorSet getFactors(@NotNull String runOrAssay) {
        if (assayId2Factor.containsKey(runOrAssay)) {
            return assayId2Factor.get(runOrAssay);
        }
        return null;
    }

    public Collection<SampleCharacteristic> getSampleCharacteristics(String runOrAssay) {
        var sampleCharacteristics = this.assayId2SampleCharacteristic.get(runOrAssay);
        return (sampleCharacteristics == null ? new SampleCharacteristics() : sampleCharacteristics).values();
    }

    // returns header, value
    public Map<String, String> getSampleCharacteristicsValues(String runOrAssay) {
        return assayId2SampleCharacteristic.getOrDefault(runOrAssay, new SampleCharacteristics()).entrySet().stream()
                .collect(toImmutableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getValue()));
    }

    @NotNull
    public ImmutableSortedSet<String> getAllRunOrAssay() {
        return ImmutableSortedSet.copyOf(assayId2SampleCharacteristic.keySet());
    }

    public String getSpeciesForAssays(Set<String> assayAccessions) {
        for (String assayAccession: assayAccessions) {
            Map<String, String> assaySamples = getSampleCharacteristicsValues(assayAccession);

            for (var sampleName : assaySamples.keySet()) {
                if ("organism".equalsIgnoreCase(sampleName)) {
                    return assaySamples.get(sampleName);
                }
            }
        }

        return "";
    }

    public Map<String, FactorSet> getAssayId2FactorMap() {
        return assayId2Factor;
    }
}
