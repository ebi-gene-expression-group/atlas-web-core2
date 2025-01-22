package uk.ac.ebi.atlas.profiles.writer;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import uk.ac.ebi.atlas.experimentpage.context.RequestContext;
import uk.ac.ebi.atlas.model.experiment.sample.ReportsGeneExpression;
import uk.ac.ebi.atlas.model.Expression;
import uk.ac.ebi.atlas.model.Profile;
import uk.ac.ebi.atlas.search.SearchDescription;

import java.io.Writer;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.wrap;

public abstract class ProfilesWriterFactory<R extends ReportsGeneExpression,
                                            E extends Expression,
                                            P extends Profile<R, E, P>,
                                            C extends RequestContext<R, ?, ?, ?>> {

    protected abstract String getTsvFileMasthead(C requestContext, String queryDescription);

    protected String[] getProfileIdColumnHeaders(C requestContext) {
        return new String[]{"Gene ID", "Gene Name"};
    }

    protected Stream<String> labelsForColumn(C requestContext, R dataColumnDescriptor) {
        return Stream.of(requestContext.displayNameForColumn(dataColumnDescriptor));
    }

    protected Stream<String> valuesFromColumn(C requestContext, @Nullable E expression) {
        return Stream.of(expression == null ? "" : Double.toString(expression.getLevel()));
    }

    public final ProfilesWriter<P> create(Writer responseWriter, final C requestContext) {
        final List<R> columns = requestContext.getDataColumnsToReturn();
        return new ProfilesWriter<>(
                responseWriter,
                getTsvFileMasthead(requestContext, wrap(SearchDescription.get(requestContext.getGeneQuery()), "'")),
                buildCsvHeaderLine(requestContext, columns),
                prof ->
                        buildCsvRow(
                                prof.identifiers(),
                                columns.stream()
                                       .flatMap(
                                               dataColumnDescriptor ->
                                                       valuesFromColumn(
                                                               requestContext,
                                                               prof.getExpression(dataColumnDescriptor)))
                                       .toArray(String[]::new))
        );
    }

    private String[] buildCsvHeaderLine(final C requestContext, final List<R> columns) {
        return buildCsvRow(
                getProfileIdColumnHeaders(requestContext),
                columns.stream()
                       .flatMap(dataColumnDescriptor -> labelsForColumn(requestContext, dataColumnDescriptor))
                       .toArray(String[]::new));
    }

    private String[] buildCsvRow(String[] rowHeaders, String[] values) {
        return ArrayUtils.addAll(rowHeaders, values);
    }

}
