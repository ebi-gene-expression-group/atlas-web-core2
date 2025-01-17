package uk.ac.ebi.atlas.experimentimport.analyticsindex.conditions;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.atlas.configuration.TestConfig;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class EfoLookupServiceIT {
    private static final String BTO_0002690 = "BTO_0002690";
    private static final String GO_0023014 = "GO_0023014";
    private static final String EFO_0009025 = "EFO_0009025";

    // All terms have an additional top-level ancestor, namely owl#Thing which is omitted in OLS
    private static final int BTO_0002690_PARENTS = 3;
    private static final int GO_0023014_PARENTS = 16;
    private static final int COMMON_PARENTS = 2;

    @Inject
    private EfoLookupService subject;

    @Test
    void allParents() {
        assertThat(subject.getAllParents(ImmutableSet.of(BTO_0002690))).hasSize(BTO_0002690_PARENTS);
    }

    @Test
    void onlyIsARelationsAreIncluded() {
        assertThat(subject.getAllParents(ImmutableSet.of(GO_0023014))).hasSize(GO_0023014_PARENTS);
    }

    @Test
    void parentNodesAreUnique() {
        assertThat(subject.getAllParents(ImmutableSet.of(GO_0023014, BTO_0002690)))
                .hasSize(BTO_0002690_PARENTS + GO_0023014_PARENTS - COMMON_PARENTS);
    }

    @Test
    void nonExistentIdsHaveNoParents() {
        assertThat(subject.getAllParents(ImmutableSet.of("Blah"))).isEmpty();
    }

    @Test
    void nullTermsAreOmitted() {
        assertThat(subject.getLabels(subject.getAllParents(ImmutableSet.of(EFO_0009025))))
                .doesNotContainNull();
    }
}
