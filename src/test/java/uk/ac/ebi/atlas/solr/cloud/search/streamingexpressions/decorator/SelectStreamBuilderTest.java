package uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.decorator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Condition;
import org.junit.Test;
import uk.ac.ebi.atlas.solr.cloud.TupleStreamer;
import uk.ac.ebi.atlas.solr.cloud.search.streamingexpressions.DummyTupleStreamBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectStreamBuilderTest {
    @Test
    public void fieldsAreRenamed() {
        var size = ThreadLocalRandom.current().nextInt(1, 1000);
        var tupleStreamBuilderMock = DummyTupleStreamBuilder.create(size);

        var subject =
                new SelectStreamBuilder(tupleStreamBuilderMock)
                        .addFieldMapping(ImmutableMap.of("field1", "fieldA", "field2", "fieldB"));

        assertThat(TupleStreamer.of(subject.build()).get().collect(toList()))
                .hasSize(size)
                .allMatch(tuple -> tuple.getMap().containsKey("fieldA") && tuple.getMap().containsKey("fieldB"))
                .areNot(
                        new Condition<>(
                                tuple -> tuple.getMap().containsKey("field1") || tuple.getMap().containsKey("field2"),
                                "Does not contain field1 or field2"));
    }

    @Test
    public void resultHasSelectedFields() {
        var size = ThreadLocalRandom.current().nextInt(1, 1000);
        var tupleStreamBuilderMock = DummyTupleStreamBuilder.create(size);

        var subject = new SelectStreamBuilder(tupleStreamBuilderMock, ImmutableList.of("field1"));

        assertThat(TupleStreamer.of(subject.build()).get().collect(toList()))
                .hasSize(size)
                .allMatch(tuple -> tuple.fields.containsKey("field1"))
                .areNot(
                        new Condition<>(
                                tuple -> tuple.fields.containsKey("field2"),
                                "Does not contain field2"));
    }

    @Test
    public void byDefaultNoFieldsArePreserved() {
        var size = ThreadLocalRandom.current().nextInt(1, 1000);
        var tupleStreamBuilderMock = DummyTupleStreamBuilder.create(size);

        var subject = new SelectStreamBuilder(tupleStreamBuilderMock);

        assertThat(TupleStreamer.of(subject.build()).get().collect(toList()))
                .hasSize(size)
                .allMatch(tuple -> tuple.getMap().keySet().size() == 0);
    }
}
