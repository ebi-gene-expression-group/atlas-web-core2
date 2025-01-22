package uk.ac.ebi.atlas.species;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class Species {
    private final String name;
    private final SpeciesProperties mappedProperties;

    public Species(String name, SpeciesProperties mappedProperties) {
        this.name = name;
        this.mappedProperties = mappedProperties;
    }

    //the string that came to us
    public String getName() {
        return name;
    }

    //a nice looking version of getEnsemblName
    public String getReferenceName() {
        return mappedProperties.referenceName();
    }

    public String getEnsemblName() {
        return mappedProperties.ensemblName();
    }

    public String getKingdom() {
        return mappedProperties.kingdom();
    }

    public String getDefaultQueryFactorType() {
        return mappedProperties.defaultQueryFactorType();
    }

    public boolean isPlant() {
        return "plants".equalsIgnoreCase(mappedProperties.kingdom());
    }

    public boolean isUnknown() {
        return mappedProperties == SpeciesProperties.UNKNOWN;
    }

    public ImmutableList<ImmutableMap<String, String>> getGenomeBrowsers() {
        return mappedProperties.getResourcesOfType(SpeciesProperties.GENOME_BROWSER_TYPE);
    }

    public ImmutableMap<String, String> getAttributes() {
        return ImmutableMap.of("species", name, "speciesReferenceName", getReferenceName());
    }

    public boolean isUs() {
        /*
        Is this where the falling angel meets the rising ape?
         */
        return getName().toLowerCase().contains("sapiens");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof Species) {
            var other = (Species) o;
            return other.name.equalsIgnoreCase(this.name) && other.mappedProperties.equals(this.mappedProperties);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.mappedProperties.hashCode();
    }
}
