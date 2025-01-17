package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.io.Tuple;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.DummyTupleStreamBuilder;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.TupleStreamBuilder;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;

class CartesianProductStreamBuilderTest {
    private static final String FIELD_NAME_1 = randomAlphanumeric(3, 15);
    private static final String FIELD_NAME_2 = randomAlphanumeric(3, 15);

    private static final ImmutableList<String> FIELD_NAME_1_VALUE =
            IntStream.range(0, ThreadLocalRandom.current().nextInt(2, 100)).boxed()
                    .map(__ -> randomAlphanumeric(1, 20))
                    .collect(toImmutableList());
    private static final ImmutableList<String> FIELD_NAME_2_VALUE =
            IntStream.range(0, ThreadLocalRandom.current().nextInt(2, 100)).boxed()
                    .map(__ -> randomAlphanumeric(1, 20))
                    .collect(toImmutableList());

    private final TupleStreamBuilder tupleStreamBuilder =
            DummyTupleStreamBuilder.create(
                    ImmutableSet.of(new Tuple(ImmutableMap.of(
                            FIELD_NAME_1, FIELD_NAME_1_VALUE,
                            FIELD_NAME_2, FIELD_NAME_2_VALUE))),
                    FIELD_NAME_1, true);

    @Test
    void cartesianProductOverSingleField() {
        var subject = new CartesianProductStreamBuilder(tupleStreamBuilder, ImmutableSet.of(FIELD_NAME_2));
        try (var tupleStreamer = TupleStreamer.of(subject.build())) {
            var tuples = tupleStreamer.get().collect(toImmutableList());
            assertThat(tuples).hasSameSizeAs(FIELD_NAME_2_VALUE);
        }
    }

    @Test
    void cartesianProductOverMultipleFields() {
        var subject =
                new CartesianProductStreamBuilder(tupleStreamBuilder, ImmutableSet.of(FIELD_NAME_1, FIELD_NAME_2));
        try (var tupleStreamer = TupleStreamer.of(subject.build())) {
            var tuples = tupleStreamer.get().collect(toImmutableList());
            assertThat(tuples).hasSize(FIELD_NAME_1_VALUE.size() * FIELD_NAME_2_VALUE.size());
        }
    }

    @Test
    void cartesianProductOverNothingReturnsTheOriginalStream() {
        var subject = new CartesianProductStreamBuilder(tupleStreamBuilder, ImmutableSet.of());
        try (var tupleStreamer = TupleStreamer.of(subject.build())) {
            var tuples = tupleStreamer.get().collect(toImmutableList());
            assertThat(tuples).hasSize(1);
        }
    }
}
