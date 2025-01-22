package uk.ac.ebi.atlas.model.card;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.home.species.SpeciesSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsFilteredBySpeciesAndExperimentType;
import static uk.ac.ebi.atlas.utils.UrlHelpers.getExperimentsFilteredBySpeciesUrl;

@Component
public class CardModelFactory {
    public CardModel create(SpeciesSummary speciesSummary) {
        List<Pair<String, Optional<String>>> content = new ArrayList<>();
        content.add(
                Pair.of(speciesSummary.getTotalExperiments() + " experiment" +
                                (speciesSummary.getTotalExperiments() > 1 ? "s" : ""),
                        Optional.empty()));

        if (speciesSummary.getBaselineExperiments() > 0) {
            content.add(
                    Pair.of("Baseline: " + speciesSummary.getBaselineExperiments(),
                            Optional.of(
                                    getExperimentsFilteredBySpeciesAndExperimentType(
                                            speciesSummary.getSpecies(),
                                            "baseline"))));
        }
        if (speciesSummary.getDifferentialExperiments() > 0) {
            content.add(
                    Pair.of("Differential: " + speciesSummary.getDifferentialExperiments(),
                            Optional.of(
                                    getExperimentsFilteredBySpeciesAndExperimentType(
                                            speciesSummary.getSpecies(),
                                            "differential"))));
        }

        return CardModel.create(
                CardIconType.SPECIES,
                speciesSummary.getSpecies(),
                Pair.of(
                        Optional.of(StringUtils.capitalize(speciesSummary.getSpecies())),
                        Optional.of(getExperimentsFilteredBySpeciesUrl(speciesSummary.getSpecies()))),
                content);
    }
}
