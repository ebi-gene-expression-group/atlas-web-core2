package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import org.apache.solr.client.solrj.io.eq.FieldEqualitor;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.io.stream.UniqueStream;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;

public class UniqueStreamBuilder extends TupleStreamBuilder {
    private final TupleStreamBuilder tupleStreamBuilder;
    private final String overField;

    public UniqueStreamBuilder(TupleStreamBuilder tupleStreamBuilder, String fieldName) {
        this.tupleStreamBuilder = tupleStreamBuilder;
        this.overField = fieldName;
    }

    @Override
    protected TupleStream getRawTupleStream() {
        try {
            return new UniqueStream(
                    tupleStreamBuilder.build(),
                    new FieldEqualitor(overField));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}