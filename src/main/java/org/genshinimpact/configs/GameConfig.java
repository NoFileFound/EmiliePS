package org.genshinimpact.configs;

@SuppressWarnings("unused")
public class GameConfig {
    public String regionName;
    public Limit inventoryLimit = new Limit();

    public static class Limit {
        public int itemLimit = 1000;
        public int weaponInventoryLimit = 100;
        public int reliquaryInventoryLimit = 100;
        public int materialInventoryLimit = 100;
        public int furnitureInventoryLimit = 100;
    }
}