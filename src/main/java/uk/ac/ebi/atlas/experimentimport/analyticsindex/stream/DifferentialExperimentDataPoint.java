package uk.ac.ebi.atlas.experimentimport.analyticsindex.stream;

import uk.ac.ebi.atlas.experimentimport.analytics.differential.DifferentialAnalytics;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.Regulation;

public class DifferentialExperimentDataPoint extends ExperimentDataPoint {

    public DifferentialExperimentDataPoint(DifferentialExperiment experiment,
                                           DifferentialAnalytics differentialAnalytics,
                                           String conditionSearch,
                                           int numReplicates) {
        super(differentialAnalytics.getGeneId(), experiment, conditionSearch);

        propertyMap.put("factors", experiment.getExperimentalFactorHeaders());
        propertyMap.put("regulation", Regulation.valueOf(differentialAnalytics.getFoldChange()).toString());
        propertyMap.put("contrast_id", differentialAnalytics.getContrastId());
        propertyMap.put("num_replicates", numReplicates);
        propertyMap.put("fold_change", differentialAnalytics.getFoldChange());
        propertyMap.put("p_value", differentialAnalytics.getpValue());
    }
}
