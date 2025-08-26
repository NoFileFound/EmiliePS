package org.genshinhttpsrv.database;

// Imports
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.experimental.filters.Filters;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.database.collections.Counter;

public final class DBManager {
    private static final ExecutorService eventExecutor = new ThreadPoolExecutor(6, 6, 60, TimeUnit.SECONDS,new LinkedBlockingDeque<>(), new ThreadPoolExecutor.AbortPolicy());
    @Getter private static Datastore dataStore;

    /**
     * Starts the mongodb database.
     */
    public static void initializeDatabase() {
        String dbUrl = Application.getPropertiesInfo().database_url;
        String dbName = Application.getPropertiesInfo().collection_name;

        MapperOptions mapperOptions = MapperOptions.builder().storeEmpties(true).storeNulls(false).build();
        dataStore = Morphia.createDatastore(MongoClients.create(dbUrl), dbName, mapperOptions);
        dataStore.ensureIndexes();

        createCounters();
    }

    /**
     * Get the value of a specific counter by its ID and updates it.
     *
     * @param counterId The ID of the counter.
     * @return The value of the counter if its found or else null.
     */
    public static long getCounterValue(String counterId) {
        Counter document = getDataStore().find(Counter.class).filter(Filters.eq("_id", counterId)).first();
        if (document != null) {
            document.setValue(document.getValue() + 1);
            saveInstance(document);
            return document.getValue();
        }
        return 0;
    }

    /**
     * Deletes an object into the database.
     * @param object The object to delete.
     */
    public static void deleteInstance(Object object) {
        if(Application.getPropertiesInfo().save_data) {
            Future<?> future = eventExecutor.submit(() -> getDataStore().delete(object));
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Saves an object into the database.
     * @param object The object to save.
     */
    public static void saveInstance(Object object) {
        if(Application.getPropertiesInfo().save_data) {
            Future<?> future = eventExecutor.submit(() -> getDataStore().save(object));
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates the counters collection if needed.
     */
    private static void createCounters() {
        String[] counters = {"lastAccountId", "lastGuestId", "lastPlayerId", "lastMailId"};
        for (String counter : counters) {
            Counter document = getDataStore().find(Counter.class).filter(Filters.eq("_id", counter)).first();
            if(document != null) continue;

            Counter newCounter = new Counter(counter, 0L);
            saveInstance(newCounter);
        }
    }
}