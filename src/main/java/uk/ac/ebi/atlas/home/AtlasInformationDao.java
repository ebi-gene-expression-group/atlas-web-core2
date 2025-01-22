package uk.ac.ebi.atlas.home;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
import io.atlassian.util.concurrent.LazyReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

import static java.util.stream.Collectors.toMap;
import static uk.ac.ebi.atlas.utils.GsonProvider.GSON;

@Component
public class AtlasInformationDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtlasInformationDao.class);

    private Path atlasInformationFilePath;
    private HashMap<String, String> releaseMetadata =
            Maps.newHashMap(
                    Arrays.stream(AtlasInformationDataType.values())
                            .map(AtlasInformationDataType::getId)
                            .collect(toMap(dataType -> dataType, __ -> "unknown")));

    public final LazyReference<ImmutableMap<String, String>> atlasInformation =
            new LazyReference<>() {
                @Override
                protected ImmutableMap<String, String> create() {
                    try {
                        var jsonReader =
                                new JsonReader(
                                        Files.newBufferedReader(atlasInformationFilePath, StandardCharsets.UTF_8));
                        LOGGER.info("Release metadata file found {}:", atlasInformationFilePath.toString());

                        releaseMetadata.putAll(GSON.<HashMap<String, String>>fromJson(jsonReader, HashMap.class));
                        LOGGER.info("{}", releaseMetadata.toString());

                        jsonReader.close();
                    } catch (Exception e) {
                        LOGGER.error("Error reading release metadata file: {}", e.getMessage());
                    }

                    return ImmutableMap.copyOf(releaseMetadata);
                }
            };

    public AtlasInformationDao(Path atlasInformationFilePath) {
        this.atlasInformationFilePath = atlasInformationFilePath;
    }
}
