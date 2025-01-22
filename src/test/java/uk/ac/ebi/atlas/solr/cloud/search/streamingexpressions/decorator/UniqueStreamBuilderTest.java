package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.io.Tuple;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.DummyTupleStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UniqueStreamBuilderTest {
    private static final String SORT_FIELD = "id";

    private final List<Map<String, String>> streamA = ImmutableList.of(
            ImmutableMap.of(SORT_FIELD, "a", "fieldA", "x"),
            ImmutableMap.of(SORT_FIELD, "a", "fieldA", "x"),
            ImmutableMap.of(SORT_FIELD, "b", "fieldA", "y"),
            ImmutableMap.of(SORT_FIELD, "b", "fieldA", "y"),
            ImmutableMap.of(SORT_FIELD, "c", "fieldA", "z"),
            ImmutableMap.of(SORT_FIELD, "c", "fieldA", "z"));

    private final TupleStreamBuilder tupleStreamBuilderA =
            DummyTupleStreamBuilder.create(streamA.stream().map(Tuple::new).collect(toList()), SORT_FIELD, true);

    @Test
    void returnsUniqueStreamForGivenField() {
        var subject = new UniqueStreamBuilder(tupleStreamBuilderA, SORT_FIELD);
        assertThat(TupleStreamer.of(subject.build()).get().collect(toList()))
                .hasSize(3);
    }

    @Test
    void throwExceptionIfGivenFieldIsNotSortField() {
        var subject = new UniqueStreamBuilder(tupleStreamBuilderA, "fieldB");
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> TupleStreamer.of(subject.build()).get());
    }
}