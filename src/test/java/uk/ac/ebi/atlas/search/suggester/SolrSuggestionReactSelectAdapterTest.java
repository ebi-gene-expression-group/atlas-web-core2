package uk.ac.ebi.atlas.search.suggester;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.ac.ebi.atlas.solr.analytics.AnalyticsPropertyName;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.atlas.search.suggester.SolrSuggestionReactSelectAdapter.ANALYTIC_SUGGESTER_LABELS;
import static uk.ac.ebi.atlas.solr.cloud.collections.BioentitiesCollectionProxy.BIOENTITY_PROPERTY_NAMES;

class SolrSuggestionReactSelectAdapterTest {
    private static final Gson GSON = new Gson();

    @Test
    void emptyStreamReturnsEmptyJsonArray() {
        assertThat(SolrSuggestionReactSelectAdapter.serialize(Stream.empty()))
                .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("bioentityPropertyNameProvider")
    void suggestionsAreSerializedToJsonInAnOptionsArray(List<BioentityPropertyName> bioentityPropertyNames) {
        var suggestionA = ImmutableMap.of("term", "term a", "category", bioentityPropertyNames.get(0).name);

        // This is just ridiculous...
        var json = SolrSuggestionReactSelectAdapter
                                .serialize(Stream.of(suggestionA))
                                .get(0)
                                .getAsJsonObject()
                                .getAsJsonArray("options")
                                .get(0)
                                .getAsJsonObject()
                                .get("value")
                                .getAsString();

        assertThat(GSON.fromJson(json, JsonObject.class))
                .matches(jsonObject -> jsonObject.get("term").getAsString().equals(suggestionA.get("term")))
                .matches(jsonObject -> jsonObject.get("category").getAsString().equals(suggestionA.get("category")));
    }

    @ParameterizedTest
    @MethodSource("bioentityPropertyNameProvider")
    void groupsAreSortedByIdPrecedence(List<BioentityPropertyName> bioentityPropertyNames) {
        ImmutableList<Map<String, String>> suggestions =
                bioentityPropertyNames.stream()
                        .map(idPropertyName ->
                                ImmutableMap.of("term", randomAlphanumeric(30), "category", idPropertyName.name))
                .collect(toImmutableList());

        var results = SolrSuggestionReactSelectAdapter.serialize(suggestions.stream());
        var resultsPropertyNameLabels = ImmutableList.builder();
        results.forEach(result -> resultsPropertyNameLabels.add(result.getAsJsonObject().get("label").getAsString()));

        assertThat(resultsPropertyNameLabels.build())
                .containsExactlyElementsOf(
                        BIOENTITY_PROPERTY_NAMES.stream()
                                .map(idPropertyName -> idPropertyName.label)
                                .collect(toList()));
    }

    @ParameterizedTest
    @MethodSource("bioentityPropertyNameProvider")
    void suggestionsWithinACategoryAreSortedAlphabetically(List<BioentityPropertyName> bioentityPropertyNames) {
        var suggestionA = ImmutableMap.of("term", "term a", "category", bioentityPropertyNames.get(0).name);
        var suggestionB = ImmutableMap.of("term", "term b", "category", bioentityPropertyNames.get(0).name);
        var suggestionC = ImmutableMap.of("term", "term c", "category", bioentityPropertyNames.get(0).name);

        var results = GSON.toJsonTree(
                ImmutableMap.of(
                        "label", bioentityPropertyNames.get(0).label,
                        "options",
                        ImmutableList.of(
                                ImmutableMap.of("label", suggestionA.get("term"), "value", GSON.toJson(suggestionA)),
                                ImmutableMap.of("label", suggestionB.get("term"), "value", GSON.toJson(suggestionB)),
                                ImmutableMap.of("label", suggestionC.get("term"), "value", GSON.toJson(suggestionC)))));

        var suggestions = Lists.<Map<String, String>>newArrayList(suggestionA, suggestionB, suggestionC);
        Collections.shuffle(suggestions);

        assertThat(SolrSuggestionReactSelectAdapter.serialize(suggestions.stream()))
                .containsExactly(results);
    }

    @ParameterizedTest
    @MethodSource("analyticsPropertyNameProvider")
    void metadataSuggestionsAreGroupedByCategory(List<AnalyticsPropertyName> analyticsPropertyNames) {
        var suggestionA = ImmutableMap.of("term", "term a", "category", analyticsPropertyNames.get(0).name);
        var suggestionB = ImmutableMap.of("term", "term b", "category", analyticsPropertyNames.get(0).name);
        var suggestionC = ImmutableMap.of("term", "term c", "category", analyticsPropertyNames.get(0).name);

        var results = GSON.toJsonTree(
                ImmutableMap.of(
                        "label", analyticsPropertyNames.get(0).label,
                        "options",
                        ImmutableList.of(
                                ImmutableMap.of("label", suggestionA.get("term"), "value", GSON.toJson(suggestionA)),
                                ImmutableMap.of("label", suggestionB.get("term"), "value", GSON.toJson(suggestionB)),
                                ImmutableMap.of("label", suggestionC.get("term"), "value", GSON.toJson(suggestionC)))));

        var suggestions = Lists.<Map<String, String>>newArrayList(suggestionA, suggestionB, suggestionC);
        Collections.shuffle(suggestions);

        assertThat(SolrSuggestionReactSelectAdapter.serialize(suggestions.stream()))
                .containsExactly(results);
    }

    @ParameterizedTest
    @MethodSource("bioentityPropertyNameProvider")
    void metadataAndBioentitySuggestionsAreCombined(List<BioentityPropertyName> bioentityPropertyNames) {
        var suggestionA = ImmutableMap.of("term", "term a", "category", "metadata");
        var suggestionB = ImmutableMap.of("term", "term b", "category", "metadata");
        var suggestionC = ImmutableMap.of("term", "term c", "category", bioentityPropertyNames.get(0).name);
        var suggestionD = ImmutableMap.of("term", "term d", "category", bioentityPropertyNames.get(0).name);

        var metadataSuggestions =
                GSON.toJsonTree(
                        ImmutableMap.of(

                                "label", "Metadata",
                                "options",
                                ImmutableList.of(
                                        ImmutableMap.of("label", suggestionA.get("term"), "value", GSON.toJson(suggestionA)),
                                        ImmutableMap.of("label", suggestionB.get("term"), "value", GSON.toJson(suggestionB))))
                );

        var bioentitySuggestions = GSON.toJsonTree(
                ImmutableMap.of(
                        "label", bioentityPropertyNames.get(0).label,
                        "options",
                        ImmutableList.of(
                                ImmutableMap.of("label", suggestionB.get("term"), "value", GSON.toJson(suggestionC)),
                                ImmutableMap.of("label", suggestionC.get("term"), "value", GSON.toJson(suggestionD))))

        );

        var mergedInputSuggestions = new JsonArray();
        mergedInputSuggestions.add(metadataSuggestions);
        mergedInputSuggestions.add(bioentitySuggestions);

        var suggestions = Lists.<Map<String, String>>newArrayList(suggestionA, suggestionB, suggestionC, suggestionD);
        Collections.shuffle(suggestions);

        var combinedSuggestions = SolrSuggestionReactSelectAdapter.serialize(suggestions.stream());

        assertThat(combinedSuggestions.get(0).getAsJsonObject().get("label").getAsString())
                .isEqualTo("Metadata");

        assertThat(combinedSuggestions.get(1).getAsJsonObject().get("label"))
                .isEqualTo(bioentitySuggestions.getAsJsonObject().get("label"));
    }

    private static Stream<Arguments> bioentityPropertyNameProvider() {
        var bioentityPropertyNames = new ArrayList<>(BIOENTITY_PROPERTY_NAMES);
        Collections.shuffle(bioentityPropertyNames);
        return Stream.of(Arguments.of(bioentityPropertyNames));
    }

    private static Stream<Arguments> analyticsPropertyNameProvider() {
        var analyticsPropertyNames = new ArrayList<>(ANALYTIC_SUGGESTER_LABELS);
        Collections.shuffle(analyticsPropertyNames);
        return Stream.of(Arguments.of(analyticsPropertyNames));
    }
}
