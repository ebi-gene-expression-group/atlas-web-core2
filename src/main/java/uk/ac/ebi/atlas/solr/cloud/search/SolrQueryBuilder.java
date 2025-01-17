package uk.ac.ebi.atlas.solr.cloud.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.joining;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryUtils.createDoubleBoundRangeQuery;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryUtils.createFieldExistQuery;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryUtils.createFieldNotExistQuery;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryUtils.createLowerBoundRangeQuery;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryUtils.createNegativeFilterQuery;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryUtils.createOrBooleanQuery;
import static uk.ac.ebi.atlas.solr.cloud.search.SolrQueryUtils.createUpperBoundRangeQuery;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

public class SolrQueryBuilder<T extends CollectionProxy<?>> {
    // Some magic Solr number, from the logs:
    // ERROR (qtp511707818-76) [   ] o.a.s.s.HttpSolrCall null:java.lang.IllegalArgumentException:
    // maxSize must be <= 2147483630; got: 2147483646
    public static final int SOLR_MAX_ROWS = 2147483630;
    public static final int DEFAULT_ROWS = 100000;

    private final ImmutableSet.Builder<String> fqClausesBuilder = ImmutableSet.builder();
    private final ImmutableSet.Builder<String> qClausesBuilder = ImmutableSet.builder();
    private final ImmutableSet.Builder<String> flBuilder = ImmutableSet.builder();
    private final ImmutableList.Builder<SortClause> sortBuilder = ImmutableList.builder();
    private final ImmutableMap.Builder<String, SolrJsonFacetBuilder<T>> facetsBuilder = ImmutableMap.builder();

    private int rows = DEFAULT_ROWS;
    private boolean normalize = true;

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addFilterFieldByTerm(U field, Collection<String> values) {
        fqClausesBuilder.add(createOrBooleanQuery(field, values, normalize));
        return this;
    }

    // Convenience method when filtering by a single value
    public <U extends SchemaField<T>> SolrQueryBuilder<T> addFilterFieldByTerm(U field, String value) {
        return addFilterFieldByTerm(field, ImmutableSet.of(value));
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addFilterFieldByRangeMin(U field, double min) {
        fqClausesBuilder.add(createLowerBoundRangeQuery(field, min));
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addFilterFieldByRangeMax(U field, double max) {
        fqClausesBuilder.add(createUpperBoundRangeQuery(field, max));
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addFilterFieldByRangeMinMax(U field,
                                                                                      double min,
                                                                                      double max) {
        fqClausesBuilder.add(createDoubleBoundRangeQuery(field, min, max));
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByTerm(U field, Collection<String> values) {
        qClausesBuilder.add(createOrBooleanQuery(field, values, normalize));
        return this;
    }

    // Allows OR-ing together of one or multiple schema fields, i.e. (fieldA: x OR fieldA: y OR fieldB: z)
    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByTerm(Map<U, Collection<String>> fieldsAndValues) {
        String clause = fieldsAndValues
                .entrySet()
                .stream()
                .map(fieldAndValue -> createOrBooleanQuery(fieldAndValue.getKey(), fieldAndValue.getValue(), normalize))
                .collect(joining(" OR "));

        qClausesBuilder.add("(" + clause + ")");
        return this;
    }

    // Convenience method when querying a single value
    public <U extends SchemaField<T>> SolrQueryBuilder<T> addQueryFieldByTerm(U field, String value) {
        return addQueryFieldByTerm(field, ImmutableSet.of(value));
    }

    public final <U extends SchemaField<T>> SolrQueryBuilder<T> setFieldList(Collection<U> fields) {
        for (SchemaField<T> field : fields) {
            flBuilder.add(field.name());
        }
        return this;
    }

    public final <U extends SchemaField<T>> SolrQueryBuilder<T> setFieldList(U field) {
        return setFieldList(ImmutableSet.of(field));
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> sortBy(U field, SolrQuery.ORDER order) {
        sortBuilder.add(new SortClause(field.name(), order));
        return this;
    }

    public SolrQueryBuilder<T> setRows(int rows) {
        this.rows = rows;
        return this;
    }

    public SolrQueryBuilder<T> addFacet(String facetName, SolrJsonFacetBuilder<T> solrJsonFacetBuilder) {
        facetsBuilder.put(facetName, solrJsonFacetBuilder);
        return this;
    }

    public SolrQueryBuilder<T> setNormalize(boolean normalize) {
        this.normalize = normalize;
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> addNegativeFilterFieldByTerm(U field, Collection<String> values) {
        fqClausesBuilder.add(createNegativeFilterQuery(field,values,normalize));
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> exists(U field) {
        qClausesBuilder.add(createFieldExistQuery(field));
        return this;
    }

    public <U extends SchemaField<T>> SolrQueryBuilder<T> notExists(U field) {
        qClausesBuilder.add(createFieldNotExistQuery(field));
        return this;
    }

    public SolrQuery build() {
        ImmutableSet<String> fqClauses = fqClausesBuilder.build();
        ImmutableSet<String> qClauses = qClausesBuilder.build();
        ImmutableSet<String> fl = flBuilder.build();
        ImmutableList<SortClause> sorts = sortBuilder.build();

        var facets =
                facetsBuilder.build().entrySet().stream()
                        .collect(toImmutableMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().build()));

        return new SolrQuery()
                .addFilterQuery(fqClauses.toArray(new String[0]))
                .setQuery(
                        qClauses.isEmpty() ?
                                "*:*" :
                                qClauses.stream().filter(StringUtils::isNotBlank).collect(joining(" AND ")))
                .setFields(
                        fl.isEmpty() ?
                                "*" :
                                fl.stream().filter(StringUtils::isNotBlank).collect(joining(",")))
                .setParam("json.facet", GSON.toJson(facets))
                .setSorts(sorts)
                .setRows(rows);
    }
}
