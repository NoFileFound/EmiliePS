package org.genshinhttpsrv.properties.configs;

// Imports
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.libraries.JsonLoader;
import org.genshinhttpsrv.properties.Property;

public final class PlatformExtensionsConfig implements Property {
    private Map<Integer, PlatformExtensionsClass> platformExtensionsInstance;

    @Override
    public Object getInstance() {
        return this.platformExtensionsInstance;
    }

    @Override
    public void loadFile() {
        this.platformExtensionsInstance = JsonLoader.loadJson("server/device_ext.json", new TypeReference<Map<Integer, PlatformExtensionsClass>>() {}.getType());
        if(this.platformExtensionsInstance == null) {
            Application.getLogger().warn(Application.getTranslationManager().get("console", "failedtoinitfile", "server/device_ext.json"));
            this.platformExtensionsInstance = new HashMap<>();
        }
    }

    @Override
    public void saveFile() {

    }

    public static class PlatformExtensionsClass {
        public ArrayList<String> ext_list = new ArrayList<>();
        public ArrayList<String> pkg_list = new ArrayList<>();
        public String pkg_str = "";
    }
}