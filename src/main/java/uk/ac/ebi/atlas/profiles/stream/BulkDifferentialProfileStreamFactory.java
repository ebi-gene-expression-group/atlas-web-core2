package uk.ac.ebi.atlas.profiles.stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import uk.ac.ebi.atlas.commons.streams.ObjectInputStream;
import uk.ac.ebi.atlas.experimentpage.context.BulkDifferentialRequestContext;
import uk.ac.ebi.atlas.model.experiment.differential.rnaseq.BulkDifferentialProfile;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.experiment.differential.DifferentialExpression;
import uk.ac.ebi.atlas.resource.DataFileHub;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

@Named
public class BulkDifferentialProfileStreamFactory
        extends ProfileStreamFactory<Contrast,
                                     DifferentialExpression,
                                     DifferentialExperiment,
        BulkDifferentialRequestContext,
        BulkDifferentialProfile> {

    private final CreatesProfilesFromTsvFiles<Contrast,
            DifferentialExpression,
            DifferentialExperiment,
            BulkDifferentialRequestContext,
            BulkDifferentialProfile> profileStreamFactory;


    @Inject
    public BulkDifferentialProfileStreamFactory(DataFileHub dataFileHub) {
        profileStreamFactory = new Impl(dataFileHub);
    }

    @Override
    public ObjectInputStream<BulkDifferentialProfile> create(DifferentialExperiment experiment,
                                                             BulkDifferentialRequestContext options,
                                                   Collection<String> keepGeneIds) {
        return profileStreamFactory.create(experiment, options, keepGeneIds);
    }

    static class Impl extends DifferentialProfileStreamFactory<DifferentialExpression,
            DifferentialExperiment, BulkDifferentialRequestContext, BulkDifferentialProfile> {


        protected Impl(DataFileHub dataFileHub) {
            super(dataFileHub);
        }

        @Override
        protected Function<String[], Function<String[], BulkDifferentialProfile>>
                  howToReadLine(final DifferentialExperiment experiment,
                                final Predicate<DifferentialExpression> expressionFilter) {
            return strings ->
                    new DifferentialGoThroughTsvLineAndPickUpExpressionsByIndex(
                            strings, experiment, expressionFilter) {
                @Nullable
                @Override
                protected DifferentialExpression nextExpression(Integer index,
                                                                Contrast correspondingColumn,
                                                                String[] currentLine) {
                    Preconditions.checkState(
                            currentLine.length > index + 1,
                            "Expecting row of the format ... <pvalue_i> <foldchange_i> ...");
                    String pValueString = currentLine[index];
                    String foldChangeString = currentLine[index + 1];
                    if (notAllDoubles(pValueString, foldChangeString)) {
                        return null;
                    } else {
                        return new DifferentialExpression(parseDouble(pValueString), parseDouble(foldChangeString));
                    }
                }

                @Override
                protected BulkDifferentialProfile newProfile(String[] currentLine) {
                    return new BulkDifferentialProfile(currentLine[0], currentLine[1]);
                }
            };
        }

        @Override
        protected Collection<ObjectInputStream<String[]>> getDataFiles(DifferentialExperiment experiment,
                                                                       BulkDifferentialRequestContext options) {
            return ImmutableList.of(
                    dataFileHub.getBulkDifferentialExperimentFiles(experiment.getAccession()).analytics.get());
        }
    }

}
