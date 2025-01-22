package uk.ac.ebi.atlas.solr.cloud.search;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SolrJsonFacetBuilderTest {
    private static final class DummySchemaField extends SchemaField<CollectionProxy<?>> {
        private DummySchemaField(String fieldName) {
            super(fieldName);
        }
    }

    private static final int MAX_LIMIT = 1000000;

    @Test
    void throwsIfNoFieldIsSpecified() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SolrJsonFacetBuilder<>().build());
    }

    @Test
    void buildsFlatTermsFacetByDefault() {
        var field = new DummySchemaField(randomAlphanumeric(1, 10));

        var subject =
                new SolrJsonFacetBuilder<>()
                        .setFacetField(field)
                        .build();
        assertThat(subject.get("type").getAsString()).isEqualTo("terms");
        assertThat(subject.get("limit").getAsInt()).isEqualTo(-1);
        assertThat(subject.get("field").getAsString()).isEqualTo(field.name());
        assertThat(subject.get("facet").getAsJsonObject().entrySet()).isEmpty();
    }

    @Test
    void canBuildNestedFacets() {
        var field = new DummySchemaField(randomAlphanumeric(1, 10));
        var nestedField = new DummySchemaField(randomAlphanumeric(1, 10));

        var subject =
                new SolrJsonFacetBuilder<>()
                        .setFacetField(field)
                        .addNestedFacet(
                                "foobar",
                                new SolrJsonFacetBuilder<>().setFacetField(nestedField))
                        .build();
        assertThat(subject.get("type").getAsString()).isEqualTo("terms");
        assertThat(subject.get("limit").getAsInt()).isEqualTo(-1);
        assertThat(subject.get("field").getAsString()).isEqualTo(field.name());

        var nestedFacet = subject.get("facet").getAsJsonObject().get("foobar").getAsJsonObject();
        assertThat(nestedFacet.get("type").getAsString()).isEqualTo("terms");
        assertThat(nestedFacet.get("limit").getAsInt()).isEqualTo(-1);
        assertThat(nestedFacet.get("field").getAsString()).isEqualTo(nestedField.name());
    }

    @Test
    void canSetLimit() {
        var limit = ThreadLocalRandom.current().nextInt(MAX_LIMIT);
        var subject =
                new SolrJsonFacetBuilder<>()
                        .setFacetField(new DummySchemaField(randomAlphanumeric(1, 10)))
                        .setLimit(limit)
                        .build();
        assertThat(subject.get("limit").getAsInt()).isEqualTo(limit);
    }

    @Test
    void canSetFacetType() {
        var type = SolrFacetType.values()[ThreadLocalRandom.current().nextInt(SolrFacetType.values().length)];
        var subject =
                new SolrJsonFacetBuilder<>()
                        .setFacetField(new DummySchemaField(randomAlphanumeric(1, 10)))
                        .setFacetType(type)
                        .build();
        assertThat(subject.get("type").getAsString()).isEqualTo(type.name);
    }

    @Test
    void canSetDomainFilter() {
        var domainField = new DummySchemaField(randomAlphanumeric(1, 10));
        var domainValue = randomAlphanumeric(5);
        var subject =
                new SolrJsonFacetBuilder<>()
                        .setFacetField(new DummySchemaField(randomAlphanumeric(1, 10)))
                        .addDomainFilter(domainField, domainValue)
                        .build();
        assertThat(subject.get("domain").getAsJsonObject().get("filter").getAsString())
                .isEqualTo(domainField.name() + ":" + domainValue);
    }
}
