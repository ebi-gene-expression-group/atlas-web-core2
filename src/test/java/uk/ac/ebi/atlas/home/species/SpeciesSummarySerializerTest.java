package uk.ac.ebi.atlas.home.species;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.card.CardModel;
import uk.ac.ebi.atlas.model.card.CardModelFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.model.card.CardIconType.SPECIES;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomSpecies;

@ExtendWith(MockitoExtension.class)
class SpeciesSummarySerializerTest {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private static final int MAX_NUMBER_OF_SPECIES = 100;
    private static final int MAX_NUMBER_OF_BASELINE_EXPERIMENTS = 2000;
    private static final int MAX_NUMBER_OF_DIFFERENTIAL_EXPERIMENTS = 2000;

    @Mock
    private CardModelFactory cardModelFactoryMock;

    private SpeciesSummarySerializer subject;

    @BeforeEach
    void setUp() {
        subject = new SpeciesSummarySerializer(cardModelFactoryMock);
    }

    @Test
    void jsonContainsTheRightNumberOfEntries() {
        when(cardModelFactoryMock.create(any(SpeciesSummary.class)))
                // This is fine: we care about the structure of the JSON document, not about its specific contents
                .thenReturn(CardModel.create(SPECIES, "", ImmutableList.of()));

        var summaries =
                IntStream.range(0, RNG.nextInt(1, MAX_NUMBER_OF_SPECIES)).boxed()
                        .map(__ -> SpeciesSummary.create(
                                generateRandomSpecies().getReferenceName(),
                                generateRandomSpecies().getKingdom(),
                                RNG.nextInt(MAX_NUMBER_OF_BASELINE_EXPERIMENTS),
                                RNG.nextInt(MAX_NUMBER_OF_DIFFERENTIAL_EXPERIMENTS)))
                        .sorted(SpeciesSummary.BY_SIZE_DESCENDING)
                        .collect(groupingBy(SpeciesSummary::getKingdom))
                        .entrySet().stream()
                        .collect(toImmutableMap(
                                Map.Entry::getKey,
                                entry -> ImmutableList.copyOf(entry.getValue())));

        var ctx = JsonPath.parse(subject.serialize(summaries));
        List<Map<String, Object>> elements = ctx.read("$.speciesSummary[*]");
        List<String> kingdoms = ctx.read("$.speciesSummary..kingdom");

        assertThat(elements)
                .doesNotContainNull()
                .allSatisfy(jsonObjectAsMap ->
                        assertThat(jsonObjectAsMap)
                                .containsKeys("kingdom", "cards")
                                .doesNotContainValue(null));
        assertThat(kingdoms)
                .containsExactlyElementsOf(summaries.keySet());
    }

    @Test
    void serialiseEmptyMap() {
        ReadContext ctx = JsonPath.parse(subject.serialize(ImmutableMap.of()));

        assertThat(ctx.<List<Map<String, Object>>>read("$.speciesSummary"))
                .asList()
                .isEmpty();
    }
}