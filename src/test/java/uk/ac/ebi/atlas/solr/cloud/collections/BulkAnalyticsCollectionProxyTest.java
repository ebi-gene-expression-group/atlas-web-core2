package uk.ac.ebi.atlas.solr.cloud.collections;

import org.junit.jupiter.api.Test;
import uk.ac.ebi.atlas.model.ExpressionUnit;

import static org.assertj.core.api.Assertions.assertThat;

class BulkAnalyticsCollectionProxyTest {
    @Test
    void tpmAndFpkmFieldsAreDifferent() {
        assertThat(BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Rna.TPM))
                .isNotEqualTo(BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Rna.FPKM));
    }

    @Test
    void proteomicUnitsAndTpmsAreTheSameField() {
        assertThat(BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Protein.PPB))
                .isEqualTo(BulkAnalyticsCollectionProxy.getExpressionLevelFieldNames(ExpressionUnit.Absolute.Rna.TPM));
    }

    // TODO add tests for asAnalyticsGeneQuery
}
