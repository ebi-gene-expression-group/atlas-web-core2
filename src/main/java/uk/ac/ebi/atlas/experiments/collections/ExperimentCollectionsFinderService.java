package uk.ac.ebi.atlas.experiments.collections;

import com.google.common.collect.ImmutableSet;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

@Service
public class ExperimentCollectionsFinderService {
    private final ExperimentCollectionsFinderDao experimentCollectionFinderDao;
    private final ExperimentCollectionDao experimentCollectionDao;

    public ExperimentCollectionsFinderService(ExperimentCollectionsFinderDao experimentCollectionFinderDao,
                                              ExperimentCollectionDao experimentCollectionDao) {
        this.experimentCollectionFinderDao = experimentCollectionFinderDao;
        this.experimentCollectionDao = experimentCollectionDao;
    }

    @Cacheable("experiment2Collections")
    public ImmutableSet<ExperimentCollection> getExperimentCollections(String experimentAccession) {
        return experimentCollectionFinderDao.findExperimentCollectionIds(experimentAccession)
                .stream()
                .map(experimentCollectionDao::findCollection)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toImmutableSet());
    }
}
