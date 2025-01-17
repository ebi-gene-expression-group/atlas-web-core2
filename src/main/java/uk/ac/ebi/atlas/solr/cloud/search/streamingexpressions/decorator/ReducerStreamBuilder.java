package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import org.apache.solr.client.solrj.io.comp.ComparatorOrder;
import org.apache.solr.client.solrj.io.comp.FieldComparator;
import org.apache.solr.client.solrj.io.eq.FieldEqualitor;
import org.apache.solr.client.solrj.io.ops.GroupOperation;
import org.apache.solr.client.solrj.io.stream.ReducerStream;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;

public class ReducerStreamBuilder extends TupleStreamBuilder {
    private final TupleStreamBuilder tupleStreamBuilder;
    private final String reduceByFieldName;
    private final String groupByFieldName;
    private final int groupBySize;

    public ReducerStreamBuilder(
            TupleStreamBuilder tupleStreamBuilder,
            String reduceByFieldName,
            String groupByFieldName,
            int groupBySize) {
        this.tupleStreamBuilder = tupleStreamBuilder;
        this.reduceByFieldName = reduceByFieldName;
        this.groupByFieldName = groupByFieldName;
        this.groupBySize = groupBySize;
    }

    @Override
    protected TupleStream getRawTupleStream() {
        try {
            var groupByFieldComparator = new FieldComparator(groupByFieldName, ComparatorOrder.ASCENDING);
            return new ReducerStream(
                    tupleStreamBuilder.build(),
                    new FieldEqualitor(reduceByFieldName),
                    new GroupOperation(groupByFieldComparator, groupBySize));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}