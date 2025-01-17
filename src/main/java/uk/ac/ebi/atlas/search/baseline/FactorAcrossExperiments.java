package uk.ac.ebi.atlas.search.baseline;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.model.OntologyTerm;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

public class FactorAcrossExperiments extends ReportsGeneExpression implements Comparable<FactorAcrossExperiments> {
    private final Set<OntologyTerm> valueOntologyTerms;

    public FactorAcrossExperiments(Factor factor) {
        this(factor.getValue(), factor.getValueOntologyTerms());
    }

    private FactorAcrossExperiments(String label, Set<OntologyTerm> valueOntologyTerms) {
        super(label, ImmutableSet.of());
        this.valueOntologyTerms = valueOntologyTerms;
    }

    public Stream<OntologyTerm> getValueOntologyTerms() {
        return valueOntologyTerms.stream();
    }

    @Override
    public int compareTo(@NotNull FactorAcrossExperiments o) {
        return Comparator.comparing(FactorAcrossExperiments::getId).compare(this, o);
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("assayGroupId", id);
        o.addProperty("factorValue", id);
        o.add("factorValueOntologyTermId", OntologyTerm.jsonForHeaders(valueOntologyTerms));
        return o;
    }
}
