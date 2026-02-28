package org.genshinimpact.configs;

@SuppressWarnings("unused")
public class MainConfig {
    public String mongodbUrl = "mongodb://localhost:27017";
    public String mongodbName = "genshin11";
    public Integer maximumGuests = 100;
    public ServerType serverType = ServerType.SERVER_TYPE_DEV;

    public enum ServerType {
        SERVER_TYPE_DEV,
        SERVER_TYPE_BETA,
        SERVER_TYPE_REL
    }
}