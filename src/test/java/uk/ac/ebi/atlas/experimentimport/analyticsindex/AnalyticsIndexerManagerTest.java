package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ebi.atlas.model.experiment.ExperimentBuilder.TestExperimentBuilder;
import uk.ac.ebi.atlas.model.experiment.ExperimentTest.TestExperiment;
import uk.ac.ebi.atlas.trader.ExperimentTrader;
import uk.ac.ebi.atlas.utils.BioentityIdentifiersReader;
import uk.ac.ebi.atlas.utils.ExperimentSorter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.experimentimport.analyticsindex.AnalyticsIndexerManager.DEFAULT_SOLR_BATCH_SIZE;

@ExtendWith(MockitoExtension.class)
class AnalyticsIndexerManagerTest {
    @Mock
    private ExperimentSorter experimentSorterMock;

    @Mock
    private AnalyticsIndexerMonitor analyticsIndexerMonitorMock;

    @Mock
    private BioentityIdentifiersReader bioentityIdentifiersReaderMock;

    @Mock
    private ExperimentTrader experimentTraderMock;

    @Mock
    private BioentityPropertiesDao bioentityPropertiesDaoMock;

    @Mock
    private AnalyticsIndexerService analyticsIndexerServiceMock;

    private AnalyticsIndexerManager subject;

    @BeforeEach
    void setUp() {
        subject = new AnalyticsIndexerManager(
                experimentSorterMock,
                analyticsIndexerMonitorMock,
                bioentityIdentifiersReaderMock,
                analyticsIndexerServiceMock,
                experimentTraderMock,
                bioentityPropertiesDaoMock);
    }

    // This is quite a weak test, since AnalyticsIndexerManager is oblivious to the fact that the experiment has the
    // private flag set to true. However, without @MockitoSettings(strictness = Strictness.LENIENT), we guarantee that
    // the stubbings and mocks used are strictly the only ones necessary, which means that AnalyticsIndexerManager is
    // using ExperimentTrader::getExperimentForAnalyticsIndex (i.e. without an access key) and that the experiment is
    // properly retrieved and passed to AnalyticsIndexerService.
    @Test
    void privateExperimentsCanBeIndexed() {
        TestExperiment experiment = new TestExperimentBuilder().withPrivate(true).build();
        when(experimentTraderMock.getExperimentForAnalyticsIndex(experiment.getAccession()))
                .thenReturn(experiment);
        when(bioentityPropertiesDaoMock.getMap(any()))
                .thenReturn(ImmutableMap.of());
        when(analyticsIndexerServiceMock.index(
                experiment,
                ImmutableMap.of(),
                Integer.valueOf(DEFAULT_SOLR_BATCH_SIZE)))
                .thenReturn(0);

        subject.addToAnalyticsIndex(experiment.getAccession());
        verify(analyticsIndexerServiceMock).index(
                experiment,
                ImmutableMap.of(),
                Integer.valueOf(DEFAULT_SOLR_BATCH_SIZE));
    }
}
