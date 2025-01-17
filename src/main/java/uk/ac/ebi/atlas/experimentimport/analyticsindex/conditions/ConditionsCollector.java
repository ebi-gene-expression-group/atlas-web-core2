package uk.ac.ebi.atlas.experimentimport.analyticsindex.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.ExperimentDesign;
import uk.ac.ebi.atlas.model.experiment.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.trader.ExperimentDesignParser;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Component
public class ConditionsCollector {
    private final EfoLookupService efoLookupService;
    private final ExperimentDesignParser experimentDesignParser;

    public ConditionsCollector(EfoLookupService efoLookupService,
                               ExperimentDesignParser experimentDesignParser) {
        this.efoLookupService = efoLookupService;
        this.experimentDesignParser = experimentDesignParser;
    }

    // Given an experiment and an assay accession, retrieves the EFO terms from the experiment design and adds to the
    // set all the parents plus the factor values and sample characteristics
    private Set<String> collectAssayProperties(Experiment experiment,
                                               String assayAccession,
                                               ImmutableSet<String> ontologyIds) {
        var parsedExperimentDesign = experimentDesignParser.parse(experiment.getAccession());

        return
                Stream.of(
                        ontologyIds,
                        efoLookupService.getLabels(ontologyIds),
                        parsedExperimentDesign
                                .getFactorValues(assayAccession)
                                .values(),
                        parsedExperimentDesign
                                .getSampleCharacteristicsValues(assayAccession)
                                .values())
                .flatMap(Collection::stream)
                .collect(toSet());
    }

    private ImmutableSetMultimap<String, String> mapAssayAccessionsToOntologyTerms(ExperimentDesign experimentDesign) {
        return efoLookupService.expandOntologyTerms(experimentDesign.getAllOntologyTermIdsByAssayAccession());
    }

    // Visits all assays in each factor and builds-collects the conditions in a stream
    public Stream<Condition> getConditions(BaselineExperiment experiment) {
        var assayAccession2OntologyTerms =
                mapAssayAccessionsToOntologyTerms(experimentDesignParser.parse(experiment.getAccession()));

        return experiment.getDataColumnDescriptors().stream()
                .flatMap(assayGroup ->
                        assayGroup.getAssayIds().stream()
                                .map(assayAccession ->
                                        new Condition(
                                                experiment.getAccession(),
                                                assayGroup.getId(),
                                                collectAssayProperties(
                                                        experiment,
                                                        assayAccession,
                                                        assayAccession2OntologyTerms.get(assayAccession)))));
    }

    // Visits reference and test assays in each contrast and builds-collects the conditions in a stream
    public Stream<DifferentialCondition> getConditions(DifferentialExperiment experiment) {
        var assayAccession2OntologyTerms =
                mapAssayAccessionsToOntologyTerms(experimentDesignParser.parse(experiment.getAccession()));

        return experiment.getDataColumnDescriptors().stream()
                .flatMap(contrast ->
                        Stream.of(contrast.getReferenceAssayGroup(), contrast.getTestAssayGroup())
                                .flatMap(assayGroup ->
                                        assayGroup.getAssayIds().stream()
                                                .map(assayAccession -> new DifferentialCondition(
                                                        experiment.getAccession(),
                                                        assayGroup.getId(),
                                                        contrast.getId(),
                                                        collectAssayProperties(
                                                                experiment,
                                                                assayAccession,
                                                                assayAccession2OntologyTerms.get(assayAccession))))));
    }
}
