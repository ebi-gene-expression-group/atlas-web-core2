package uk.ac.ebi.atlas.search.suggester;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class SuggesterDaoIT {
    @Inject
    private SuggesterDao subject;

    @Test
    void propertySuggestionsHighlightMatchedRegionInBold() {
        assertThat(subject.fetchBioentityProperties("asp", 10, true))
                .isNotEmpty()
                .allMatch(suggestion -> suggestion.getTerm().matches(".*<b>(?i)(asp)</b>.*"));
    }
}
