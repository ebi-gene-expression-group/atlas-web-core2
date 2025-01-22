package uk.ac.ebi.atlas.model.experiment.sdrf;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.OntologyTerm;

import java.util.Set;

@AutoValue
public abstract class SampleCharacteristic {
    public abstract String getHeader();
    public abstract String getValue();
    public abstract Set<OntologyTerm> getValueOntologyTerms();

    @NotNull
    public static SampleCharacteristic create(@NotNull String header,
                                              @NotNull String value) {
        return create(header, value, new ImmutableSet.Builder<OntologyTerm>().build());
    }

    @NotNull
    public static SampleCharacteristic create(@NotNull String header,
                                              @NotNull String value,
                                              @NotNull OntologyTerm...  ontologyTerms) {
        return create(header, value, new ImmutableSet.Builder<OntologyTerm>().add(ontologyTerms).build());
    }

    @NotNull
    private static SampleCharacteristic create(@NotNull String header,
                                               @NotNull String value,
                                               @NotNull Set<@NotNull OntologyTerm> ontologyTerms) {
        return new AutoValue_SampleCharacteristic(header, value, ontologyTerms);
    }
}
