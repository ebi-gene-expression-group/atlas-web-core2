package uk.ac.ebi.atlas.home.species;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.ac.ebi.atlas.species.Species;

import java.util.Comparator;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;

@Component
public class SpeciesSummaryService {
    private final static Comparator<String> KINGDOM_COMPARATOR =
            arbitraryStringComparator("animals", "plants", "fungi", "protists");

    private final SpeciesSummaryDao speciesSummaryDao;

    public SpeciesSummaryService(SpeciesSummaryDao speciesSummaryDao) {
        this.speciesSummaryDao = speciesSummaryDao;
    }

    @Cacheable("speciesSummary")
    public ImmutableSortedMap<String, ImmutableList<SpeciesSummary>> getReferenceSpeciesSummariesGroupedByKingdom() {
        var kingdom2SpeciesSummary =
                speciesSummaryDao.getExperimentCountBySpeciesAndExperimentType().stream()
                        // Aggregate by reference name
                        .collect(groupingBy(triplet -> triplet.getLeft().getReferenceName()))
                        .entrySet().stream()
                        // Transform aggregates by species into a species summary
                        .map(entry ->
                                SpeciesSummary.create(
                                        entry.getKey(),
                                        entry.getValue().get(0).getLeft().getKingdom(),
                                        entry.getValue().stream()
                                                .filter(triplet -> triplet.getMiddle().isBaseline())
                                                .map(Triple::getRight)
                                                .reduce(Long::sum)
                                                .orElse(0L),
                                        entry.getValue().stream()
                                                .filter(triplet -> triplet.getMiddle().isDifferential())
                                                .map(Triple::getRight)
                                                .reduce(Long::sum)
                                                .orElse(0L)))
                        // Group species summaries by kingdom
                        .collect(toImmutableListMultimap(
                                SpeciesSummary::getKingdom,
                                identity()));

        // Reorder keys by kingdoms and values by number of experiments, descending
        return kingdom2SpeciesSummary.keySet().stream()
                .collect(toImmutableSortedMap(
                        KINGDOM_COMPARATOR,
                        identity(),
                        kingdom ->
                                kingdom2SpeciesSummary.get(kingdom).stream()
                                        .sorted(SpeciesSummary.BY_SIZE_DESCENDING)
                                        .collect(toImmutableList())));
    }

    public ImmutableSet<String> getSpecies() {
        return speciesSummaryDao.getExperimentCountBySpeciesAndExperimentType().stream()
                .map(Triple::getLeft)
                .map(Species::getName)
                .collect(toImmutableSet());
    }

    private static Comparator<String> arbitraryStringComparator(String... stringsInOrder) {
        // The list and the comparator are both reversed to get the argument strings first, and then the other elements
        var arbitraryComparator = comparing((String s) -> ImmutableList.copyOf(stringsInOrder).reverse().indexOf(s));
        return arbitraryComparator.reversed().thenComparing(naturalOrder());
    }
}
