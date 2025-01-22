package uk.ac.ebi.atlas.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    protected StringUtil() {
        throw new UnsupportedOperationException();
    }

    public static String[] splitAtLastSlash(String uri) {
        int finalSlashIndex = uri.lastIndexOf('/');
        return finalSlashIndex == -1 ?
                new String[] {uri} :
                new String[] {uri.substring(0, finalSlashIndex + 1), uri.substring(finalSlashIndex + 1)};
    }

    public static String suffixAfterLastSlash(String uri) {
        int finalSlashIndex = uri.lastIndexOf('/');
        return finalSlashIndex == -1 ?
                uri : uri.substring(finalSlashIndex + 1);
    }

    public static String escapeDoubleQuotes(String s) {
        return s.replace("\"", "\\\"");
    }

    // Converts snakecase metadata values to human-friendly names
    // E.g. biopsy_site => Biopsy site, cell_type => Cell type)
    public static String snakeCaseToDisplayName(String s) {
        String displayName = s.trim().replace("_", " ");

        return StringUtils.capitalize(displayName);
    }

    // Converts space-separated words to snakecase
    // E.g. FACS marker => facs_marker)
    public static String wordsToSnakeCase(String words) {
        return words.trim().toLowerCase().replace(" ", "_");
    }
}
