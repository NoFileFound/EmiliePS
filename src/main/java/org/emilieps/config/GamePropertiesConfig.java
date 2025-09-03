package org.emilieps.config;

// Imports
import org.emilieps.Application;
import org.emilieps.data.RegionClass;

// Libraries
import org.emilieps.library.JsonLib;

public final class GamePropertiesConfig implements Property {
    private GameConfigClass gameConfigInstance;

    @Override
    public Object getInstance() {
        return this.gameConfigInstance;
    }

    @Override
    public void loadFile() {
        this.gameConfigInstance = JsonLib.loadJson("game_properties.json", GameConfigClass.class);
        if(this.gameConfigInstance == null) {
            this.gameConfigInstance = new GameConfigClass();
            Application.getLogger().warn(Application.getTranslations().get("console", "failedtoinitfile", "game_properties.json"));
        }
    }

    @Override
    public void saveFile() {

    }

    public static class GameConfigClass {
        public RegionClass region = new RegionClass();
        public String security_md5_library = "f822bc67f41e4677e52e5099bb6a9c52";
    }
}