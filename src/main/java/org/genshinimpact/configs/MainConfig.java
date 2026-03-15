package org.genshinimpact.configs;

// Imports
import java.util.List;

@SuppressWarnings("unused")
public class MainConfig {
    public String mongodbUrl = "mongodb://localhost:27017";
    public String mongodbName = "genshin11";
    public Integer maximumGuests = 100;
    public Integer maximumPlayers = 100;
    public ServerType serverType = ServerType.SERVER_TYPE_DEV;
    public List<String> badWords = List.of();
    public List<String> badIPS = List.of();

    public enum ServerType {
        SERVER_TYPE_DEV,
        SERVER_TYPE_BETA,
        SERVER_TYPE_REL
    }
}