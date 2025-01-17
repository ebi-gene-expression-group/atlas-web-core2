package uk.ac.ebi.atlas.solr.cloud.search;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.atlas.solr.cloud.CollectionProxy;
import uk.ac.ebi.atlas.solr.cloud.SchemaField;

import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

enum SolrFacetType {
    TERMS("terms"),
    RANGE("range"),
    QUERY("query");

    public final String name;

    SolrFacetType(String name) {
        this.name = name;
    }
}

public class SolrJsonFacetBuilder<T extends CollectionProxy<?>> {
    private static final int DEFAULT_LIMIT = -1;
    private static final String DOMAIN_FILTER_TEMPLATE = "%s:%s";

    // Support "terms" facets by default
    private String facetType = SolrFacetType.TERMS.name;
    private String facetField;

    private final ImmutableSet.Builder<String> domainFiltersBuilder = ImmutableSet.builder();
    private final ImmutableMap.Builder<String, SolrJsonFacetBuilder<?>> nestedFacetsBuilder = ImmutableMap.builder();

    private int limit = DEFAULT_LIMIT;

    public final <U extends SchemaField<T>> SolrJsonFacetBuilder<T> setFacetField(U field) {
        facetField = field.name();
        return this;
    }

    public final SolrJsonFacetBuilder<T> setFacetType(SolrFacetType type) {
        facetType = type.name;
        return this;
    }

    public final SolrJsonFacetBuilder<T> addNestedFacet(String name, SolrJsonFacetBuilder<T> solrJsonFacetBuilder) {
        nestedFacetsBuilder.put(name, solrJsonFacetBuilder);
        return this;
    }

    public final SolrJsonFacetBuilder<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public final <U extends SchemaField<T>> SolrJsonFacetBuilder<T> addDomainFilter(U field, String value) {
        domainFiltersBuilder.add(
                String.format(
                        DOMAIN_FILTER_TEMPLATE,
                        field.name(),
                        value));
        return this;
    }

    public JsonObject build() {
        if (StringUtils.isBlank(facetField)) {
            throw new IllegalArgumentException("A facet field must be set.");
        }

        var facetWrapper = new JsonObject();
        facetWrapper.addProperty("type", facetType);
        facetWrapper.addProperty("field", facetField);
        facetWrapper.addProperty("limit", limit);
        // https://lucene.apache.org/solr/guide/7_2/json-facet-api.html#TermsFacet
        // > This makes stats for returned buckets exact.
        facetWrapper.addProperty("refine", true);

        var domainFilters = domainFiltersBuilder.build();
        if (!domainFilters.isEmpty()) {
            var filters = new JsonObject();
            filters.add("filter", GSON.toJsonTree(domainFilters));
            facetWrapper.add("domain", filters);
        }

        facetWrapper.add(
                "facet",
                GSON.toJsonTree(
                        Maps.transformValues(nestedFacetsBuilder.build(), SolrJsonFacetBuilder::build)));

        return facetWrapper;
    }
}
