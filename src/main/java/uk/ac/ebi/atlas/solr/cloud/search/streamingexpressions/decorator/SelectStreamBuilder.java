package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.solr.client.solrj.io.stream.SelectStream;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

public class SelectStreamBuilder extends TupleStreamBuilder {
    private final TupleStreamBuilder tupleStreamBuilder;
    private final ImmutableList.Builder<String> selectedFieldsBuilder = ImmutableList.builder();
    private final ImmutableMap.Builder<String, String> fieldMapsBuilder = ImmutableMap.builder();

    public SelectStreamBuilder(TupleStreamBuilder tupleStreamBuilder) {
        this.tupleStreamBuilder = tupleStreamBuilder;
    }

    public SelectStreamBuilder(TupleStreamBuilder tupleStreamBuilder, ImmutableList<String> selectedFieldsBuilder) {
        this.tupleStreamBuilder = tupleStreamBuilder;
        this.selectedFieldsBuilder.addAll(selectedFieldsBuilder);
    }

    // public SelectStreamBuilder<T> addFieldMap(String originalfieldName, String newFieldName) {
    //     fieldMapsBuilder.put(originalfieldName, newFieldName);
    //     return this;
    // }

    public SelectStreamBuilder addFieldMapping(Map<String, String> fieldNamesMap) {
        fieldMapsBuilder.putAll(fieldNamesMap);
        return this;
    }

    @Override
    protected TupleStream getRawTupleStream() {
        try {
            return selectedFieldsBuilder.build().isEmpty() ?
                    new SelectStream(tupleStreamBuilder.build(), fieldMapsBuilder.build()) :
                    new SelectStream(tupleStreamBuilder.build(), selectedFieldsBuilder.build());
        } catch (IOException e) {
            // The truth is, this constructor can’t throw an IOException, but the class declaration does... lame
            throw new UncheckedIOException(e);
        }
    }
}
