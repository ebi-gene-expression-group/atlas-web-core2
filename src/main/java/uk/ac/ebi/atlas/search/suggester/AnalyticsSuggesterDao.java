package uk.ac.ebi.atlas.search.suggester;

import org.apache.solr.client.solrj.response.Suggestion;
import uk.ac.ebi.atlas.species.Species;
import java.util.stream.Stream;

public interface AnalyticsSuggesterDao {
    Stream<Suggestion> fetchMetadataSuggestions(String query, int limit);
}
