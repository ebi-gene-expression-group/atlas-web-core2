package uk.ac.ebi.atlas.web;

public class ProteomicsDifferentialRequestPreferences extends DifferentialRequestPreferences {
    public ProteomicsDifferentialRequestPreferences() {
        super();
        setFoldChangeCutoff(0.5);
    }
}
