package uk.ac.ebi.atlas.experiments.collections;

import com.google.auto.value.AutoValue;
import org.springframework.lang.Nullable;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.util.Optional;

import static java.util.Objects.isNull;

@AutoValue
public abstract class ExperimentCollection {
    public abstract String id();
    public abstract String name();
    public abstract String description();
    public abstract Optional<Image> icon();

    public static ExperimentCollection create(
            String id,
            String name,
            String description,
            @Nullable byte[] imageBytes) {
        if (isNull(imageBytes)) {
            return new AutoValue_ExperimentCollection(id, name, description, Optional.empty());
        }
        try (var imageByteArrayInputStream = new ByteArrayInputStream(imageBytes)) {
            return new AutoValue_ExperimentCollection(
                    id,
                    name,
                    description,
                    Optional.of(ImageIO.read(imageByteArrayInputStream)));
        } catch (Exception e) {
            // Maybe too defensive?
            return new AutoValue_ExperimentCollection(id, name, description, Optional.empty());
        }
    }
}
