package uk.ac.ebi.atlas.experiments.collections;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.ac.ebi.atlas.testutils.RandomDataTestUtils.generateRandomExperimentAccession;

@ExtendWith(MockitoExtension.class)
class ExperimentCollectionsFinderServiceTest {
    @Mock
    private ExperimentCollectionsFinderDao experimentCollectionsFinderDao;

    @Mock
    private ExperimentCollectionDao experimentCollectionDao;

    private ExperimentCollectionsFinderService subject;

    @BeforeEach
    void setUp() {
        subject = new ExperimentCollectionsFinderService(experimentCollectionsFinderDao, experimentCollectionDao);
    }

    @Test
    void retrievesExperimentCollections() {
        var experimentCollection = ExperimentCollection.create(
                randomAlphanumeric(3,5).toUpperCase(),
                randomAlphanumeric(5, 10),
                randomAlphanumeric(10, 30),
                new byte[0]);
        when(experimentCollectionDao.findCollection(anyString()))
                .thenReturn(Optional.of(experimentCollection));
        when(experimentCollectionsFinderDao.findExperimentCollectionIds(anyString()))
                .thenReturn(ImmutableList.of(experimentCollection.id()));
        assertThat(subject.getExperimentCollections(generateRandomExperimentAccession()))
                .containsExactly(experimentCollection);
    }

    @Test
    void returnEmptyIfExperimentDoesNotBelongToAnyCollection() {
        when(experimentCollectionsFinderDao.findExperimentCollectionIds(anyString()))
                .thenReturn(ImmutableList.of());
        assertThat(subject.getExperimentCollections(generateRandomExperimentAccession()))
            .isEmpty();
    }
}