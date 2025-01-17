package uk.ac.ebi.atlas.experimentimport.analyticsindex;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.stream.ExperimentDataPoint;
import uk.ac.ebi.atlas.solr.bioentities.BioentityPropertyName;
import uk.ac.ebi.atlas.solr.bioentities.query.BioentitiesSolrClient;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;

@Controller
public class BioentityPropertiesDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(BioentityPropertiesDao.class);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private BioentitiesSolrClient gxaSolrClient;

    @Autowired
    public BioentityPropertiesDao(BioentitiesSolrClient gxaSolrClient) {
        this.gxaSolrClient = gxaSolrClient;
    }

    public ImmutableMap<String, Map<BioentityPropertyName, Set<String>>> getMap(Set<String> bioentityIdentifiers) {
        LOGGER.info(
                "Building bioentity properties map of {} IDs",
                NUMBER_FORMAT.format(bioentityIdentifiers.size()));
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ImmutableMap.Builder<String, Map<BioentityPropertyName, Set<String>>> mapBuilder = new ImmutableMap.Builder<>();
        for (String bioentityIdentifier : bioentityIdentifiers) {
            mapBuilder.put(
                    bioentityIdentifier,
                    gxaSolrClient.getMap(bioentityIdentifier, ExperimentDataPoint.BIOENTITY_PROPERTY_NAMES));
        }

        stopWatch.stop();
        LOGGER.info(
                "Bioentity properties of {} bioentities fetched in {} seconds",
                NUMBER_FORMAT.format(bioentityIdentifiers.size()),
                NUMBER_FORMAT.format(stopWatch.getTotalTimeSeconds()));

        return mapBuilder.build();
    }

}
