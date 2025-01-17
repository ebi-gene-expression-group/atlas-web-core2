package uk.ac.ebi.atlas.bioentity.properties;

import org.springframework.stereotype.Service;

@Service
public interface ExpressedBioentityFinder {
    boolean bioentityIsExpressedInAtLeastOneExperiment(String bioentityIdentifier);
}
