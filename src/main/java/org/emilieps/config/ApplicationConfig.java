package org.emilieps.config;

// Imports
import java.util.ArrayList;
import org.emilieps.Application;
import org.emilieps.data.ApplicationType;
import org.emilieps.library.JsonLib;

public final class ApplicationConfig implements Property {
    private ApplicationConfigClass applicationConfigInstance;

    @Override
    public Object getInstance() {
        return this.applicationConfigInstance;
    }

    @Override
    public void loadFile() {
        this.applicationConfigInstance = JsonLib.loadJson("app.json", ApplicationConfigClass.class);
        if(this.applicationConfigInstance == null) {
            Application.getLogger().warn(Application.getTranslations().get("console", "failedtoinitfile", "app.json"));
            System.exit(1);
        }
    }

    @Override
    public void saveFile() {

    }

    public static class ApplicationConfigClass {
        public String database_url = "mongodb://localhost:27017";
        public String collection_name = "genshin69";
        public ApplicationType applicationType = ApplicationType.BOTH;
        public ArrayList<String> blacklist_ips = new ArrayList<>();
        public ArrayList<String> whitelist_ips = new ArrayList<>();
        public boolean enable_encryption = true;
        public boolean is_debug = true;
        public boolean is_debug_packets = false;
    }
}