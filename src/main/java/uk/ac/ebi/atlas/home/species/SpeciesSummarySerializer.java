package uk.ac.ebi.atlas.home.species;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.controllers.JsonExceptionHandlingController;
import uk.ac.ebi.atlas.model.card.CardModelAdapter;
import uk.ac.ebi.atlas.model.card.CardModelFactory;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

@Component
public class SpeciesSummarySerializer extends JsonExceptionHandlingController {
    private final CardModelFactory cardModelFactory;

    public SpeciesSummarySerializer(CardModelFactory cardModelFactory) {
        this.cardModelFactory = cardModelFactory;
    }

    public String serialize(
            ImmutableMap<String, ? extends ImmutableCollection<SpeciesSummary>> kingdom2SpeciesSummaries) {
        var kingdom2SerialisedCards =
                kingdom2SpeciesSummaries.entrySet().stream()
                        .map(entry -> ImmutableMap.of(
                                "kingdom", entry.getKey(),
                                "cards", CardModelAdapter.serialize(
                                            entry.getValue().stream()
                                                    .map(cardModelFactory::create)
                                                    .collect(toImmutableList()))))
                        .collect(toImmutableList());

        return GSON.toJson(ImmutableMap.of("speciesSummary", kingdom2SerialisedCards));
    }
}
