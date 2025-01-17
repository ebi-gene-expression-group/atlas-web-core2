package uk.ac.ebi.atlas.profiles.stream;

import uk.ac.ebi.atlas.commons.streams.ObjectInputStream;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.model.Expression;
import uk.ac.ebi.atlas.model.Profile;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.profiles.differential.ProfileStreamOptions;

import java.util.Collection;

public interface CreatesProfileStream<
        R extends ReportsGeneExpression,
        X extends Expression,
        E extends Experiment<R>,
        O extends ProfileStreamOptions<R>,
        P extends Profile<R, X, P>>  {
    ObjectInputStream<P> create(E experiment, O options, Collection<String> keepGeneIds);
}
