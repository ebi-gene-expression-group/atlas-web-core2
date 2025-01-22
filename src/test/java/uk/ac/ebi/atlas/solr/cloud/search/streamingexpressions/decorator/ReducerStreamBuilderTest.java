package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.solr.client.solrj.io.Tuple;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.DummyTupleStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.UncheckedIOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ReducerStreamBuilderTest {
    private static final String SORT_FIELD = "field1";

    private final List<Tuple> streamA = ImmutableList.of(
            new Tuple(ImmutableMap.of(SORT_FIELD, "a", "field2", "a")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "a", "field2", "b")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "a", "field2", "c")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "b", "field2", "d")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "b", "field2", "e")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "b", "field2", "f")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "c", "field2", "g")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "c", "field2", "h")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "c", "field2", "i")),
            new Tuple(ImmutableMap.of(SORT_FIELD, "c", "field2", "j")));

    private final TupleStreamBuilder tupleStreamBuilderA =
            DummyTupleStreamBuilder.create(streamA, SORT_FIELD, true);

    @Test
    void throwExceptionIfGivenFieldIsNotSortField() {
        var subject = new ReducerStreamBuilder(tupleStreamBuilderA, "field2", SORT_FIELD, 10);
        assertThatExceptionOfType(UncheckedIOException.class)
                        .isThrownBy(() -> TupleStreamer.of(subject.build()).get());
    }

    @Test
    void returnsReducedStreamForGivenSortField() {
        var subject = new ReducerStreamBuilder(tupleStreamBuilderA, SORT_FIELD, "field2",10);
        assertThat(TupleStreamer.of(subject.build()).get()).hasSize(3);
    }
}