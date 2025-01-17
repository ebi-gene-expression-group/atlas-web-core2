package uk.ac.ebi.atlas.solr.cloud.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryBuilder.SOLR_MAX_ROWS;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

// Correctness of query syntax tested in SolrQueryUtilsTest
class SolrQueryBuilderTest {
    private static final class DummySchemaField extends SchemaField<CollectionProxy<?>> {
        private DummySchemaField(String fieldName) {
            super(fieldName);
        }
    }

    private static final DummySchemaField FIELD1 = new DummySchemaField("field1");
    private static final DummySchemaField FIELD2 = new DummySchemaField("field2");

    @Test
    void byDefaultQueryAllAndRetrieveAllFields() {
        SolrQuery solrQuery = new SolrQueryBuilder<>().build();

        assertThat(solrQuery.getFilterQueries()).isEmpty();
        assertThat(solrQuery.getQuery()).isEqualTo("*:*");
        assertThat(solrQuery.getFields()).isEqualTo("*");
    }

    @Test
    void multipleQueryClausesAreJoinedWithAnd() {
        SolrQuery solrQuery =
                new SolrQueryBuilder<>()
                        .addQueryFieldByTerm(FIELD1, ImmutableSet.of("value1"))
                        .addQueryFieldByTerm(FIELD2, ImmutableSet.of("value21", "value22"))
                        .build();

        assertThat(solrQuery.getQuery().split(" AND ")).hasSize(2);
    }

    @Test
    void multipleQueriesAreJoinedWithOr() {
        Map<DummySchemaField, Collection<String>> fieldsAndValues = ImmutableMap.of(
                FIELD1, ImmutableList.of("value1"),
                FIELD2, ImmutableList.of("value1", "value2")
        );

        SolrQuery solrQuery =
                new SolrQueryBuilder<>()
                        .addQueryFieldByTerm(fieldsAndValues)
                        .build();

        assertThat(solrQuery.getQuery().split(" OR ")).hasSize(3);
    }

    @Test
    void testFilterQueries() {
        SolrQuery solrQuery =
                new SolrQueryBuilder<>()
                        .addFilterFieldByTerm(FIELD1, ImmutableSet.of("value1"))
                        .addFilterFieldByRangeMin(FIELD2, 0.0)
                        .addFilterFieldByRangeMax(FIELD2, 0.0)
                        .addFilterFieldByRangeMinMax(FIELD2, 0.0, 0.0)
                        .build();

        assertThat(solrQuery.getFilterQueries())
                .hasSize(4)
                .allMatch(str -> str.matches("field[12]:(.+)"));
    }

    @Test
    void testSetFieldList() {
        assertThat(
                new SolrQueryBuilder<>().setFieldList(ImmutableSet.of(FIELD1, FIELD2)).build().getFields().split(","))
                .containsExactlyInAnyOrder(FIELD1.name(), FIELD2.name());
    }

    @Test
    @DisplayName("Default number of rows is “big enough”, whatever that means")
    void defaultNumberOfRows() {
        assertThat(new SolrQueryBuilder<>().build().getRows()).isGreaterThanOrEqualTo(1000);
    }

    @Test
    void testSetRows() {
        int randomRows = ThreadLocalRandom.current().nextInt(1, SOLR_MAX_ROWS + 1);
        assertThat(new SolrQueryBuilder<>().setRows(randomRows).build().getRows()).isEqualTo(randomRows);
    }

    @Test
    void searchValuesAreDeduped() {
        SolrQuery solrQuery =
                new SolrQueryBuilder<>()
                        .addQueryFieldByTerm(
                                FIELD1, ImmutableSet.of("value1", "value2", "value2", "value3", "value3", "value3"))
                        .build();

        assertThat(solrQuery.getQuery())
                .containsOnlyOnce("value1")
                .containsOnlyOnce("value2")
                .containsOnlyOnce("value3");
    }

    @Test
    void sortsOrderIsPreserved() {
        SolrQuery solrQuery =
                new SolrQueryBuilder<>()
                    .sortBy(FIELD1, ORDER.asc)
                    .sortBy(FIELD2, ORDER.desc)
                    .build();

        assertThat(solrQuery.getSorts())
            .containsExactly(new SortClause(FIELD1.name(), ORDER.asc), new SortClause(FIELD2.name(), ORDER.desc));
    }

    @Test
    void noJsonFacetAreAddedIfFacetQueryIsEmpty() {
        SolrQuery solrQuery = new SolrQueryBuilder<>()
                .addQueryFieldByTerm(FIELD1, "value1")
                .build();

        Map<?, ?> result = GSON.fromJson(solrQuery.get("json.facet"), Map.class);

        assertThat(result).isEmpty();
    }

    @Test
    void jsonFacetsAreBuilt() {
        var jsonFacetBuilder = new SolrJsonFacetBuilder<>()
                .setFacetField(FIELD1);

        var solrQuery = new SolrQueryBuilder<>()
                .addFacet(FIELD1.name(), jsonFacetBuilder)
                .build();

        var result = GSON.<Map<?, ?>>fromJson(solrQuery.get("json.facet"), Map.class);

        assertThat(result).isNotEmpty();
    }

    @Test
    void queryIsNotNormalizedWhenFlagSetToFalse() {
        var fieldValue = "*" + randomAlphabetic(10);
        SolrQuery solrQuery =
                new SolrQueryBuilder<>()
                        .setNormalize(false)
                        .addQueryFieldByTerm(FIELD1, fieldValue)
                        .build();

        assertThat(solrQuery.getQuery()).isEqualTo(FIELD1.name() + ":(" + fieldValue + ")");
    }

    @Test
    void queryIsNormalizedByDefault() {
        var fieldValue = "*" + randomAlphabetic(10);
        SolrQuery solrQuery =
                new SolrQueryBuilder<>()
                        .addQueryFieldByTerm(FIELD1, fieldValue)
                        .build();

        assertThat(solrQuery.getQuery()).isEqualTo(FIELD1.name() + ":(\"\\" + fieldValue + "\")");
    }

    @Test
    void whenAddedFieldForExistence_thenBuiltQueryContainsFieldForExistence() {
        var fieldNameToExist = new DummySchemaField("fieldToExists");
        var expectedQuery = fieldNameToExist.name() + ":*";

        SolrQuery solrQuery =
            new SolrQueryBuilder<>()
                .exists(fieldNameToExist)
                .build();

        assertThat(solrQuery.getQuery()).isEqualTo(expectedQuery);
    }

    @Test
    void whenAddedFieldForNonExistence_thenBuiltQueryContainsFieldForNonExistence() {
        var fieldNameToExist = new DummySchemaField("fieldToNotExists");
        var expectedQuery = "!" + fieldNameToExist.name() + ":*";

        SolrQuery solrQuery =
            new SolrQueryBuilder<>()
                .notExists(fieldNameToExist)
                .build();

        assertThat(solrQuery.getQuery()).isEqualTo(expectedQuery);
    }
}
