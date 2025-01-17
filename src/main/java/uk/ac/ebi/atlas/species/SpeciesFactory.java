package uk.ac.ebi.atlas.species;

import org.springframework.stereotype.Component;

@Component
public class SpeciesFactory {
    private final SpeciesPropertiesTrader speciesPropertiesTrader;

    public SpeciesFactory(SpeciesPropertiesTrader speciesPropertiesTrader) {
        this.speciesPropertiesTrader = speciesPropertiesTrader;
    }

    public Species create(String name) {
        return new Species(name, speciesPropertiesTrader.get(name));
    }

    public Species createUnknownSpecies() {
        return new Species("", SpeciesProperties.UNKNOWN);
    }
}
