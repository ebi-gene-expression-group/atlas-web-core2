package uk.ac.ebi.atlas.search.baseline;

import com.google.common.collect.ImmutableList;
import uk.ac.ebi.atlas.model.GeneProfilesList;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BaselineExperimentProfilesList extends GeneProfilesList<BaselineExperimentProfile> {
    public BaselineExperimentProfilesList() {
    }

    public List<FactorAcrossExperiments> getFactorsAcrossExperiments() {
        var result = new TreeSet<FactorAcrossExperiments>();
        for (var baselineExperimentProfile : this) {
            result.addAll(
                    baselineExperimentProfile.getConditions()
                            .stream()
                            .filter(condition -> isNotBlank(condition.getId()))
                            .collect(toImmutableList()));
        }

        return ImmutableList.copyOf(result);
    }
}
