package org.emilieps.database;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import java.util.Map;
import lombok.Getter;
import org.bson.types.ObjectId;

@Entity(value = "experiments", useDiscriminator = false)
@Getter
public final class Experiment {
    @Id private ObjectId id; // default
    private Integer code;
    private Integer type;
    private String config_id;
    private String period_id;
    private String version;
    private Map<String, String> configs;
    private Boolean sceneWhiteList;
    private Boolean experimentWhiteList;
}