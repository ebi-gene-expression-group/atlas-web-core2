package uk.ac.ebi.atlas.model.experiment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.experiment.sdrf.Factor;

import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateBlankString;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateFilterFactors;

class ExperimentDisplayDefaultsTest {
    private final static ThreadLocalRandom RNG = ThreadLocalRandom.current();
    private final static int FACTOR_TYPES_MAX = 5;

    @Test
    void throwIfdefaultQueryFactorTypeIsBlank() {
        int factorTypesCount = RNG.nextInt(1, FACTOR_TYPES_MAX);
        ImmutableTriple<String, ImmutableSet<String>, ImmutableSet<Factor>> filterFactors =
                generateFilterFactors(factorTypesCount, RNG.nextInt(0, factorTypesCount));

        assertThatIllegalArgumentException().isThrownBy(
                () -> ExperimentDisplayDefaults.create(
                        generateBlankString(),
                        filterFactors.getRight(),
                        filterFactors.getMiddle(),
                        RNG.nextBoolean()));
    }

    @Test
    void throwIfdefaultQueryFactorTypeIsNotInFilterTypes() {
        int factorTypesCount = RNG.nextInt(1, FACTOR_TYPES_MAX);
        ImmutableTriple<String, ImmutableSet<String>, ImmutableSet<Factor>> filterFactors =
                generateFilterFactors(factorTypesCount, RNG.nextInt(0, factorTypesCount));

        assertThatIllegalArgumentException().isThrownBy(
                () -> ExperimentDisplayDefaults.create(
                        // Change to factorTypes.get(_any_) to make test pass
                        randomAlphabetic(5, 10),
                        filterFactors.getRight(),
                        filterFactors.getMiddle(),
                        RNG.nextBoolean()));
    }

    @Test
    void throwIfDefaultFiltersHasDefaultQueryFactorType() {
        // We want at least one default filter factor, so we need at least two types (one for the filter factor, the
        // other would be the default query factor type in a correct set up)
        int factorTypesCount = RNG.nextInt(2, FACTOR_TYPES_MAX);
        ImmutableTriple<String, ImmutableSet<String>, ImmutableSet<Factor>> filterFactors =
                generateFilterFactors(factorTypesCount, RNG.nextInt(1, factorTypesCount));

        assertThatIllegalArgumentException().isThrownBy(
                () -> ExperimentDisplayDefaults.create(
                        // Change to filterFactors.getLeft() to make the test fail
                        filterFactors.getRight().asList().get(RNG.nextInt(filterFactors.getRight().size())).getType(),
                        filterFactors.getRight(),
                        filterFactors.getMiddle(),
                        RNG.nextBoolean()));
    }

    @Test
    void getters() {
        int factorTypesCount = RNG.nextInt(1, FACTOR_TYPES_MAX);
        ImmutableTriple<String, ImmutableSet<String>, ImmutableSet<Factor>> filterFactors =
                generateFilterFactors(factorTypesCount, RNG.nextInt(0, factorTypesCount));
        // If factorTypesCount == 1 finalFilterFactors will always be empty, otherwise it may or may not be

        boolean isOrderPreserved = RNG.nextBoolean();

        ExperimentDisplayDefaults subject =
                ExperimentDisplayDefaults.create(
                        filterFactors.getLeft(),
                        filterFactors.getRight(),
                        filterFactors.getMiddle(),
                        isOrderPreserved);

        assertThat(subject)
                .hasFieldOrPropertyWithValue("defaultQueryFactorType", filterFactors.getLeft())
                .hasFieldOrPropertyWithValue("columnOrderPreserved", isOrderPreserved);
        assertThat(subject.getFactorTypes())
                .hasSameElementsAs(filterFactors.getMiddle());
        assertThat(subject.getDefaultFilterValues().keySet())
                .hasSameElementsAs(filterFactors.getRight().stream().map(Factor::getType).collect(toImmutableSet()));
        assertThat(subject.getDefaultFilterValues().values())
                .hasSameElementsAs(filterFactors.getRight().stream().map(Factor::getValue).collect(toImmutableSet()));
    }

    @Test
    void filterFactorValuesCanBeRetrievedByHeaderOrType() {
        int factorTypesCount = RNG.nextInt(2, FACTOR_TYPES_MAX);
        ImmutableTriple<String, ImmutableSet<String>, ImmutableSet<Factor>> filterFactors =
                generateFilterFactors(factorTypesCount, RNG.nextInt(1, factorTypesCount));

        ExperimentDisplayDefaults subject =
                ExperimentDisplayDefaults.create(
                        filterFactors.getLeft(),
                        filterFactors.getRight(),
                        filterFactors.getMiddle(),
                        RNG.nextBoolean());

        Factor randomFilterFactor =
                filterFactors.getRight().asList().get(RNG.nextInt(filterFactors.getRight().size()));
        assertThat(subject.defaultFilterValuesForFactor(randomFilterFactor.getHeader()))
                .isEqualTo(subject.defaultFilterValuesForFactor(randomFilterFactor.getType()))
                .hasValue(randomFilterFactor.getValue());

        assertThat(subject.defaultFilterValuesForFactor(randomAlphabetic(10)))
                .isEqualTo(subject.defaultFilterValuesForFactor(generateBlankString()))
                .isEmpty();
    }

    @Test
    void simpleDefaultsHaveNoFilterValues() {
        assertThat(ExperimentDisplayDefaults.create())
                .hasFieldOrPropertyWithValue("defaultQueryFactorType", "")
                .hasFieldOrPropertyWithValue("defaultFilterValues", ImmutableMap.of())
                .hasFieldOrPropertyWithValue("factorTypes", ImmutableSet.of())
                .hasFieldOrPropertyWithValue("columnOrderPreserved", false);
    }
}