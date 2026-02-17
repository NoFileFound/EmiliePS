package org.genshinimpact.database;

// Imports
import com.mongodb.client.*;
import dev.morphia.*;
import dev.morphia.mapping.MapperOptions;
import lombok.Getter;
import org.genshinimpact.Application;

import java.util.List;

public final class Database {
    @Getter
    private static Datastore dataStore;
    private static MongoClient instance;

    /**
     * Initializes to the database.
     */
    public static void initialize() {
        instance = MongoClients.create("mongodb://localhost:27017");

        // Set mapper options.
        MapperOptions mapperOptions = MapperOptions.builder().storeEmpties(true).storeNulls(false).build();
        dataStore = Morphia.createDatastore(instance, "genshin11", mapperOptions);
        dataStore.ensureIndexes();

        Application.getLogger().info("The database was initialized successfully.");
    }


    public static void saveLog(Object log, String dbName) {

    }

    public static Object getSceneExperiments(List<String> list) {
        return "";
    }
}