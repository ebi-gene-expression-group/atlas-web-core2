package uk.ac.ebi.atlas.search.suggester;

import java.util.Map;
import java.util.stream.Stream;

public interface AnalyticsSuggesterService {
    Stream<Map<String, String>> fetchMetadataSuggestions(String query, String... species);
}
