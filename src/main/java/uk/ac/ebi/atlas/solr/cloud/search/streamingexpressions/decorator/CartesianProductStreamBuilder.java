package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.util.Collection;

public class CartesianProductStreamBuilder extends TupleStreamBuilder {
    private final TupleStreamBuilder tupleStreamBuilder;
    private final ImmutableSet.Builder<String> fieldNamesBuilder = ImmutableSet.builder();

    public CartesianProductStreamBuilder(TupleStreamBuilder tupleStreamBuilder, Collection<String> fieldNames) {
        this.tupleStreamBuilder = tupleStreamBuilder;
        this.fieldNamesBuilder.addAll(fieldNames);
    }

    @Override
    protected TupleStream getRawTupleStream() {
        return new _CartesianProductStream(tupleStreamBuilder.build(), fieldNamesBuilder.build(), null);
    }
}
