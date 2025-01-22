package uk.ac.ebi.atlas.testutils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;
import uk.ac.ebi.atlas.model.experiment.sample.AssayGroup;
import uk.ac.ebi.atlas.model.experiment.sample.BiologicalReplicate;
import uk.ac.ebi.atlas.model.experiment.sample.Contrast;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;
import uk.ac.ebi.atlas.experimentpage.tsne.TSnePoint;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.species.SpeciesProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.StringUtils.capitalize;

public class RandomDataTestUtils {
    private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

    private static final int ENSEMBLE_GENE_ID_NUM_LENGTH = 12;
    private static final int ENA_SEQ_RUN_NUM_LENGTH = 7;
    private static final int MAX_ASSAY_GROUP_ID = 1350; // The highest so far is PCAWG by individual, 1350
    private static final int MAX_TECHNICAL_REPLICATE_ID = 9233; // The highest so far is GTEx by individual, 9233
    private static final int MAX_TECHNICAL_REPLICATE_GROUP_SIZE = 10;

    protected RandomDataTestUtils() {
        throw new UnsupportedOperationException();
    }

    public static String generateRandomExperimentAccession() {
        // n / 456,975,543,024 chance of clashing for n experiments in the test database, let’s roll!
        return "E-" + randomAlphabetic(4).toUpperCase() + "-" + randomNumeric(1, 6);
    }

    public static String generateRandomCellId() {
        return "SRR" + randomNumeric(7) + '-' + getRandomSequence(12);
    }

    public static String generateRandomExperimentAccession(String accessionPattern) {
        return "E-" + accessionPattern.toUpperCase() + "-" + randomNumeric(1, 6);
    }

    public static String generateRandomPrideExperimentAccession() {
        return "PXD" + randomNumeric(6);
    }

    public static String generateRandomEnsemblGeneId() {
        return "ENS" + randomAlphabetic(4).toUpperCase() + randomNumeric(ENSEMBLE_GENE_ID_NUM_LENGTH);
    }

    public static List<String> generateRandomEnsemblGeneIds(int numberOfEnsemblGeneIds) {
        return Stream.generate(RandomDataTestUtils::generateRandomEnsemblGeneId)
                .limit(numberOfEnsemblGeneIds)
                .collect(Collectors.toUnmodifiableList());
    }


    public static String generateRandomOntologyId() {
        return "UBERON_" + randomNumeric(1,7);
    }

    public static String generateRandomGeneSymbol() {
        return randomAlphabetic(3, 10);
    }

    public static List<String> generateRandomGeneSymbols(int numberOfGeneSymbols) {
        return Stream.generate(RandomDataTestUtils::generateRandomGeneSymbol)
                .limit(numberOfGeneSymbols)
                .collect(Collectors.toUnmodifiableList());
    }

    public static String generateRandomEfoAccession() {
        // https://www.ebi.ac.uk/ols/ontologies/efo
        // Version: 2.99
        // Number of terms: 22023
        return "EFO_" +  String.format("%07d", RNG.nextInt(1, 22023));
    }

    public static List<String[]> generateRandomClusters(int fromK, int toK, int numberOfCells) {
        ImmutableList.Builder<String[]> clustersTsvBuilder = ImmutableList.builder();

        clustersTsvBuilder.add(
                Stream.concat(
                        Stream.of("sel.K", "K"),
                        generateRandomSingleCellRnaSeqRunIds(numberOfCells).stream())
                .toArray(String[]::new));

        // It’s a bit convoluted, but randomClustersLine will be invoked with true only once, the first iteration in
        // which thisClusterSelK becomes true
        boolean selKSet = false;
        for (int k = fromK; k <= toK; k++) {
            boolean thisClusterSelK = RNG.nextBoolean();
            clustersTsvBuilder.add(randomClustersLine(!selKSet && thisClusterSelK, k, numberOfCells));
            selKSet = selKSet || thisClusterSelK;
        }

        return clustersTsvBuilder.build();
    }

    private static Set<String> generateRandomSingleCellRnaSeqRunIds(int n) {
        Set<String> runIds = new HashSet<>(n);
        while (runIds.size() < n) {
            runIds.add(generateRandomRnaSeqRunId());
        }
        return runIds;
    }
    /**
     * The chances of a duplicate universally unique identifier (UUID)(128 bit value) number being
     * produced is billions of billions to one against.
     * The chance of these being acquired by the same application are a million to one.
     * The chance of this causing a fault might be 1 in 100.
     * Added randomNumeric() to reduce the chance of causing a fault- I guess it might be 1 in 500.
     */
    public static String generateRandomRnaSeqRunId() {
        return "ERR" + randomNumeric(1, ENA_SEQ_RUN_NUM_LENGTH) + UUID.randomUUID().toString();
    }

    private static String[] randomClustersLine(boolean selK, int k, int n) {
        List<String> clusterIds = new ArrayList<>(n);
        clusterIds.add(Boolean.toString(selK).toUpperCase());
        clusterIds.add(Integer.toString(k));
        while (clusterIds.size() < n + 2) {
            clusterIds.add(Integer.toString(RNG.nextInt(1, k + 1)));
        }
        return clusterIds.toArray(new String[0]);
    }

    public static Set<TSnePoint.Dto> generateRandomTSnePointDtos(int n) {
        Set<String> runIds = generateRandomSingleCellRnaSeqRunIds(n);

        return runIds
                .stream()
                .map(id -> TSnePoint.Dto.create(RNG.nextDouble(), RNG.nextDouble(), id))
                .collect(Collectors.toSet());
    }

    public static Set<TSnePoint.Dto> generateRandomTSnePointDtosWithExpression(int n) {
        Set<TSnePoint.Dto> tSnePointDtos = new HashSet<>(n);
        while (tSnePointDtos.size() < n) {
            tSnePointDtos.add(
                    TSnePoint.Dto.create(
                            RNG.nextDouble(), RNG.nextDouble(), RNG.nextDouble(), generateRandomRnaSeqRunId()));
        }

        return tSnePointDtos;
    }

    public static Set<TSnePoint.Dto> generateRandomTSnePointDtosWithClusters(int n, int k) {
        Set<TSnePoint.Dto> tSnePointDtos = new HashSet<>(n);

        var coveredKs = new HashSet<Integer>(k);

        while (tSnePointDtos.size() < n) {
            var nextK = RNG.nextInt(1, k + 1);
            while (coveredKs.size() < k && coveredKs.contains(nextK)) {
                nextK = RNG.nextInt(1, k + 1);
            }
            coveredKs.add(nextK);

            tSnePointDtos.add(
                    TSnePoint.Dto.create(
                            RNG.nextDouble(),
                            RNG.nextDouble(),
                            Integer.toString(nextK),
                            generateRandomRnaSeqRunId()));
        }

        return tSnePointDtos;
    }

    public static BioentityPropertyName generateRandomKnownBioentityPropertyName() {
        return BioentityPropertyName.values()[
                RNG.nextInt(0, BioentityPropertyName.values().length)];
    }

    private static final ImmutableList<String> FACTOR_TYPES =
            ImmutableList.of("ORGANISM_PART", "DISEASE", "DEVELOPMENTAL_STAGE", "SEX", "AGE");
    private static final ImmutableList<String> KINGDOMS =
            ImmutableList.of(
                    "North", "Mountain and the Vale", "Isles and Rivers", "Rock", "Stormlands", "Reach", "Dorne");
    public static Species generateRandomSpecies() {
        String first = capitalize(randomAlphabetic(1, 10).toLowerCase());
        String second = randomAlphabetic(6, 10).toLowerCase();

        return new Species(
                (first.length() == 1 ? first + "." : first) + " " + second,
                SpeciesProperties.create(
                        first + "_" + second,
                        FACTOR_TYPES.get(RNG.nextInt(FACTOR_TYPES.size())),
                        KINGDOMS.get(RNG.nextInt(KINGDOMS.size())),
                        ImmutableList.of(
                                ImmutableMap.of(
                                        "type", "genome_browser",
                                        "name", "Ensembl Dorne",
                                        "url", "https://dorne.ensembl.org/" + first + "_" + second))));
    }

    public static String generateRandomCellType() {
        return randomAlphabetic(4, 10).toLowerCase();
    }

    public static String generateRandomOrganismPart() {
        return randomAlphabetic(4, 10).toLowerCase();
    }

    public static String generateRandomTechnicalReplicateGroupId() {
        return "t" + RNG.nextInt(1, MAX_TECHNICAL_REPLICATE_ID);
    }

    public static String generateRandomAssayGroupId() {
        return "g" + RNG.nextInt(1, MAX_ASSAY_GROUP_ID);
    }

    public static AssayGroup generateRandomAssayGroup() {
        return new AssayGroup(generateRandomAssayGroupId(), generateRandomBiologicalReplicates(1, 10));
    }

    public static ImmutableList<AssayGroup> generateRandomAssayGroups(int count) {
        List<AssayGroup> assayGroups = new ArrayList<>();

        while (assayGroups.size() < count) {
            AssayGroup newAssayGroup = generateRandomAssayGroup();

            ImmutableSet<String> allIdsGeneratedSoFar =
                    assayGroups.stream()
                            .flatMap(assayGroup ->
                                    Streams.concat(
                                            // ID of assays, either a tech. replicate ID or a biological replicate ID
                                            assayGroup.getAssays().stream().map(BiologicalReplicate::getId),
                                            // If the assay is a technical replicate, there will be multiple assay IDs
                                            assayGroup.getAssayIds().stream(),
                                            // And finally the assay group ID
                                            Stream.of(assayGroup.getId())))
                            .collect(toImmutableSet());

            ImmutableSet<String> newAssayGroupIds =
                    Streams.concat(
                            newAssayGroup.getAssays().stream().map(BiologicalReplicate::getId),
                            newAssayGroup.getAssayIds().stream(),
                            Stream.of(newAssayGroup.getId()))
                    .collect(toImmutableSet());

            if (Sets.intersection(allIdsGeneratedSoFar, newAssayGroupIds).isEmpty()) {
                assayGroups.add(newAssayGroup);
            }
        }

        return ImmutableList.copyOf(assayGroups);
    }

    private static BiologicalReplicate generateRandomBiologicalReplicate() {
        // Heads: assay
        if (RNG.nextInt(0, 2) == 0) {
            return BiologicalReplicate.create(generateRandomRnaSeqRunId());
        }

        // Tails: technical replicate group
        int technicalReplicateGroupSize = RNG.nextInt(2, MAX_TECHNICAL_REPLICATE_GROUP_SIZE);
        Set<String> technicalReplicateIds = new HashSet<>(technicalReplicateGroupSize);

        while(technicalReplicateIds.size() < technicalReplicateGroupSize) {
            technicalReplicateIds.add(generateRandomRnaSeqRunId());
        }

        return BiologicalReplicate.create(
                "t" + RNG.nextInt(1, MAX_TECHNICAL_REPLICATE_ID),
                technicalReplicateIds);
    }

    public static ImmutableSet<BiologicalReplicate> generateRandomBiologicalReplicates(int lowerBound,
                                                                                       int upperBound) {
        int size = RNG.nextInt(lowerBound, upperBound);
        Set<BiologicalReplicate> replicates = new HashSet<>(size);
        while (replicates.size() < size) {
            BiologicalReplicate newReplicate = generateRandomBiologicalReplicate();

            // If the new replicate has an ID or assay IDs that have already been taken, skip and try a new one
            if (
                    replicates.stream()
                            .anyMatch(replicate -> replicate.getId().equals(newReplicate.getId())) ||
                    replicates.stream()
                            .flatMap(replicate -> replicate.getAssayIds().stream())
                            .anyMatch(assayId -> newReplicate.getAssayIds().contains(assayId))) {
                continue;
            }

            replicates.add(newReplicate);
        }

        return ImmutableSet.copyOf(replicates);
    }

    public static String generateBlankString() {
        // Heads: string composed of space characters
        if (RNG.nextInt(0, 2) == 0) {
            return random(RNG.nextInt(1, 10), " \t\n\r");
        }

        // Tails: empty string
        return "";
    }

    public static ImmutableSet<String> generateRandomAssayIds() {
        return IntStream.rangeClosed(0, RNG.nextInt(1, MAX_TECHNICAL_REPLICATE_GROUP_SIZE)).boxed()
                .map(__ -> generateRandomRnaSeqRunId())
                .collect(toImmutableSet());
    }

    public static Contrast generateRandomContrast(boolean microarray) {
        AssayGroup referenceAssayGroup = generateRandomAssayGroup();
        Set<String> referenceTechnicalReplicateGroupIds =
                referenceAssayGroup.getAssays().stream()
                        .filter(replicate -> replicate.getAssayIds().size() > 1)
                        .map(BiologicalReplicate::getId)
                        .collect(toImmutableSet());

        AssayGroup testAssayGroup;
        Set<String> testTechnicalReplicateGroupIds;
        // If assay group IDs match or assays in one group match any assay in the other group, roll dice again
        do {
            testAssayGroup = generateRandomAssayGroup();
            testTechnicalReplicateGroupIds =
                    testAssayGroup.getAssays().stream()
                            .filter(replicate -> replicate.getAssayIds().size() > 1)
                            .map(BiologicalReplicate::getId)
                            .collect(toImmutableSet());
        }  while (testAssayGroup.equals(referenceAssayGroup) || //
                  !Sets.intersection(testAssayGroup.getAssayIds(), referenceAssayGroup.getAssayIds()).isEmpty() ||
                  !Sets.intersection(testTechnicalReplicateGroupIds, referenceTechnicalReplicateGroupIds).isEmpty());

        return new Contrast(
                String.join("_", referenceAssayGroup.getId(), testAssayGroup.getId()),
                "'" + randomAlphabetic(20) + "'" + " vs " + "'" + randomAlphabetic(20) + "'",
                referenceAssayGroup,
                testAssayGroup,
                microarray ? generateRandomArrayDesignAccession() : null);
    }

    public static ImmutableList<Contrast> generateRandomContrasts(int count, boolean isMicroarray) {
        // Generate a pool of assay groups: two contrasts require at least three assay groups
        List<AssayGroup> assayGroups = generateRandomAssayGroups(Math.max(count, 3));
        List<String> arrayDesigns =
                assayGroups.stream()
                        .map(__ -> generateRandomArrayDesignAccession())
                        .collect(toImmutableList());
        List<String> assayGroupDisplayNames =
                assayGroups.stream()
                        .map(__ -> randomAlphabetic(6, 20))
                        .collect(toImmutableList());

        // Generate reference/test pairs, we keep an extra set, analysedAssayGroupIndexes, to ensure that we don’t
        // create the same contrast with reference and test assay groups reversed
        Set<Contrast> contrasts = new HashSet<>();
        Set<Set<Integer>> analysedAssayGroupIndexes = new HashSet<>();
        while (contrasts.size() < count) {
            int referenceIndex;
            int testIndex;
            do {
                referenceIndex = RNG.nextInt(assayGroups.size());
                testIndex = RNG.nextInt(assayGroups.size());
                while (testIndex == referenceIndex) {
                    testIndex = RNG.nextInt(assayGroups.size());
                }
            } while (analysedAssayGroupIndexes.contains(ImmutableSet.of(referenceIndex, testIndex)));
            analysedAssayGroupIndexes.add(ImmutableSet.of(referenceIndex, testIndex));

            AssayGroup referenceAssayGroup = assayGroups.get(referenceIndex);
            AssayGroup testAssayGroup = assayGroups.get(testIndex);

            Contrast contrast =
                    new Contrast(
                            referenceAssayGroup.getId() + "_" + testAssayGroup.getId(),
                            "‘" + assayGroupDisplayNames.get(referenceIndex) + "’ vs " +
                            "‘" + assayGroupDisplayNames.get(testIndex) + "’",
                            referenceAssayGroup,
                            testAssayGroup,
                            isMicroarray ? arrayDesigns.get(referenceIndex) : null);
            contrasts.add(contrast);
        }

        return ImmutableList.copyOf(contrasts);
    }

    public static String generateRandomArrayDesignAccession() {
        ArrayList<String> arrayDesigns = Lists.newArrayList("AFFY", "AGIL", "GEOD", "MEXP");
        Collections.shuffle(arrayDesigns);
        return "A-" + arrayDesigns.get(0) + "-" + RNG.nextInt(1, 17409);    // A-AFFY-1, A-GEOD-17408

    }

    // Generates a set of factor types (middle element) of size factorTypeCount, from which a random subset of size
    // factorFilterCount are assigned factor values (right element). The left element is any factor type which isn’t
    // been used as a factor value. Used to generate a random valid baselien configuration with which to build
    // ExperimentDisplayDefaults objects.
    @NotNull
    public static ImmutableTriple<String, ImmutableSet<String>, ImmutableSet<Factor>>
    generateFilterFactors(int factorTypeCount, int factorFilterCount) {
        ImmutableSet<String> factorTypes = IntStream.range(0, factorTypeCount).boxed()
                .map(__ -> randomAlphabetic(5, 10).toUpperCase())
                .collect(toImmutableSet());

        ArrayList<Factor> factorTypesWithValues =
                    factorTypes.stream()
                            .map(factorType -> new Factor(factorType, randomAlphabetic(10, 20)))
                            .collect(toCollection(ArrayList::new));
        while (factorTypesWithValues.size() != factorFilterCount) {
            factorTypesWithValues.remove(RNG.nextInt(factorTypesWithValues.size()));
        }

        String defaultQueryFactorType =
                factorTypes.stream()
                        .filter(factorType ->
                                factorTypesWithValues.stream()
                                        .noneMatch(factor -> factor.getType().equals(factorType)))
                        .findAny()
                        .orElseThrow(RuntimeException::new);

        return ImmutableTriple.of(defaultQueryFactorType, factorTypes, ImmutableSet.copyOf(factorTypesWithValues));
    }

    public static String generateRandomUrl() throws Exception {
        var uriBuilder = new URIBuilder()
                .setScheme(RNG.nextBoolean() ? "https" : "http")
                .setHost(
                        IntStream.range(1, RNG.nextInt(2, 4))
                                .boxed()
                                .map(__ -> randomAlphabetic(3, 10).toLowerCase())
                                .collect(joining(".")) +
                                "." + randomAlphabetic(3, 5).toLowerCase())
                .setPath(RNG.nextBoolean() ? randomAlphabetic(5, 20).toLowerCase() : "");

        if (RNG.nextBoolean()) {
            uriBuilder.setFragment(randomAlphabetic(5, 20).toLowerCase());
        }

        return uriBuilder.build().toString();
    }

    public static String generateRandomPubmedId() {
        return randomNumeric(8);
    }

    public static String generateRandomDoi() {
        return "10." + randomNumeric(4) + "/" + randomAlphanumeric(2, 9) + randomNumeric(2, 5);
    }

    private static String getRandomSequence(int length) {
        var sb = new StringBuilder();
        for (var i = 0; i < length; i++) {
            sb.append(getRandomBase());
        }

        return sb.toString();
    }

    private static String getRandomBase() {
        var bases = "ACGT";
        var randomInt = new Random();
        return String.valueOf(bases.charAt(randomInt.nextInt(bases.length())));
    }

    public static String generateRandomGeneOntologyAccession() {
        return String.format("GO:%07d", Math.abs(RNG.nextInt(999999)));
    }
}
