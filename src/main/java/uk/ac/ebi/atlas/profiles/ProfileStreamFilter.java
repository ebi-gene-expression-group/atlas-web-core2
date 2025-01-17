package uk.ac.ebi.atlas.profiles;

import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.model.Profile;
import uk.ac.ebi.atlas.profiles.differential.ProfileStreamOptions;

import java.util.List;
import java.util.function.Predicate;

public class ProfileStreamFilter {
    protected ProfileStreamFilter() {
        throw new UnsupportedOperationException();
    }

    public static <R extends ReportsGeneExpression, O extends ProfileStreamOptions<R>, P extends Profile<R, ?, P>>
           Predicate<P> create(final O options) {

        final List<R> dataColumnsToReturn = options.getDataColumnsToReturn();
        return prof -> prof.isExpressedAnywhereOn(dataColumnsToReturn);

    }
}
