package uk.ac.ebi.atlas.experimentpage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.experimentimport.idf.IdfParser;
import uk.ac.ebi.atlas.experiments.ExperimentCellCountDao;
import uk.ac.ebi.atlas.model.Publication;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.Regulation;
import uk.ac.ebi.atlas.model.experiment.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.utils.EuropePmcClient;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
public class ExperimentAttributesService {
    private final EuropePmcClient europePmcClient;
    private final IdfParser idfParser;
    private final ExperimentCellCountDao experimentCellCountDao;

    public ExperimentAttributesService(EuropePmcClient europePmcClient,
                                       IdfParser idfParser,
                                       ExperimentCellCountDao experimentCellCountDao) {
        this.europePmcClient = europePmcClient;
        this.idfParser = idfParser;
        this.experimentCellCountDao = experimentCellCountDao;
    }

    @Cacheable(cacheNames = "experimentAttributes", key = "#experiment.getAccession()")
    public Map<String, Object> getAttributes(Experiment<? extends ReportsGeneExpression> experiment) {
        Map<String, Object> result = new HashMap<>();
        result.put("experimentAccession", experiment.getAccession());
        result.put("experimentDescription", experiment.getDescription());
        result.put("type", experiment.getType().getHumanDescription());
        result.putAll(experiment.getSpecies().getAttributes());
        result.put("pubMedIds", experiment.getPubMedIds());
        result.put("dois", experiment.getDois());
        result.put("disclaimer", experiment.getDisclaimer());
        result.put("lastUpdated", new SimpleDateFormat("dd-MM-yyyy").format(experiment.getLastUpdate()));
        result.put("numberOfAssays", experiment.getAnalysedAssays().size());
        result.put("factors", experiment.getExperimentalFactorHeaders());

        if (!experiment.getDois().isEmpty()) {
            result.put("publications", getPublicationsByDoi(experiment.getDois()));
        } else if (!experiment.getPubMedIds().isEmpty()) {
            result.put("publications", getPublicationsByPubmedId(experiment.getPubMedIds()));
        }

        result.put("longDescription", idfParser.parse(experiment.getAccession()).getExperimentDescription());

        // We want this to show up in Google searches.
        result.put("pageDescription", experiment.getDescription());

        // Extra information to show on experiment page (if they were provided in <expAcc>-factors.xml file)
        result.put("dataProviderURL", experiment.getDataProviderURL());
        result.put("dataProviderDescription", experiment.getDataProviderDescription().asList());
        result.put("alternativeViews", experiment.getAlternativeViews());
        result.put("alternativeViewDescriptions", experiment.getAlternativeViewDescriptions().asList());

        // TODO This could probably be improved...
        if (experiment instanceof MicroarrayExperiment) {
            MicroarrayExperiment microarrayExperiment = (MicroarrayExperiment) experiment;

            result.put("arrayDesignAccessions", microarrayExperiment.getArrayDesignAccessions());
            result.put("arrayDesignNames", microarrayExperiment.getArrayDesignNames());
        } else if (experiment instanceof DifferentialExperiment) {
            result.put("regulationValues", Regulation.values());
            result.put("contrasts", experiment.getDataColumnDescriptors());
        }

        return result;
    }

    @Cacheable("cellCounts")
    public int getCellCount(String experimentAccession) {
        return experimentCellCountDao.fetchNumberOfCellsByExperimentAccession(experimentAccession);
    }

    private ImmutableList<Publication> getPublicationsByDoi(Collection<String> identifiers) {
        return identifiers.stream()
                .map(europePmcClient::getPublicationByDoi)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toImmutableList());
    }

    private ImmutableList<Publication> getPublicationsByPubmedId(Collection<String> identifiers) {
        return identifiers.stream()
                .map(europePmcClient::getPublicationByPubmedId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toImmutableList());
    }
}
