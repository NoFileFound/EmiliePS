package org.genshinimpact.database;

// Imports
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.query.experimental.filters.Filters;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Counter;
import org.genshinimpact.database.collections.Guest;
import org.genshinimpact.database.collections.Ticket;

public final class DBManager {
    @Getter private static Datastore dataStore;
    private static final ExecutorService eventExecutor = new ThreadPoolExecutor(6, 6, 60, TimeUnit.SECONDS,new LinkedBlockingDeque<>(), new ThreadPoolExecutor.AbortPolicy());
    private static final boolean applyChanges = true;
    private static boolean initialized = false;

    // Cache
    @Getter private static final ConcurrentHashMap<Long, Account> cachedAccounts = new ConcurrentHashMap<>();
    @Getter private static final ConcurrentHashMap<Long, Guest> cachedGuests = new ConcurrentHashMap<>();
    @Getter private static final ConcurrentHashMap<String, Ticket> cachedTickets = new ConcurrentHashMap<>();

    /**
     * Starts the mongodb database.
     */
    public static void initializeDatabase() {
        if(initialized) {
            return;
        }

        String dbUrl = AppBootstrap.getMainConfig().mongodbUrl;
        String dbName = AppBootstrap.getMainConfig().mongodbName;
        MapperOptions mapperOptions = MapperOptions.builder().storeEmpties(true).storeNulls(false).build();
        dataStore = Morphia.createDatastore(MongoClients.create(dbUrl), dbName, mapperOptions);
        dataStore.ensureIndexes();

        initialized = true;
        AppBootstrap.getLogger().info("The database was loaded successfully.");
        createCounters();
    }

    /**
     * Get the value of a specific counter by its ID and updates it.
     *
     * @param counterId The ID of the counter.
     * @return The value of the counter if its found or else null.
     */
    public static long getCounterValue(String counterId) {
        if(!initialized) {
            return -1;
        }

        Counter document = getDataStore().find(Counter.class).filter(Filters.eq("_id", counterId)).first();
        if(document != null) {
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
        if(!initialized) {
            return;
        }

        if(applyChanges) {
            eventExecutor.submit(() -> getDataStore().delete(object));
        }
    }

    /**
     * Saves an object into the database.
     * @param object The object to save.
     */
    public static void saveInstance(Object object) {
        if(!initialized) {
            return;
        }

        if(applyChanges) {
            eventExecutor.submit(() -> getDataStore().save(object));
        }
    }

    /**
     * Shutdowns the database.
     */
    public static void shutdownDatabase() {
        for(Account account : cachedAccounts.values()) {
            account.save(true);
        }

        for(Ticket ticket : cachedTickets.values()) {
            ticket.save();
        }

        for(Guest guest : cachedGuests.values()) {
            guest.save(true);
        }

        eventExecutor.shutdown();
        try {
            if(!eventExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                eventExecutor.shutdownNow();
            }
        } catch(InterruptedException e) {
            eventExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates the counters collection if needed.
     */
    private static void createCounters() {
        String[] counters = {"lastAccountId", "lastTicketId", "lastSanctionId"};
        for(String counter : counters) {
            Counter document = getDataStore().find(Counter.class).filter(Filters.eq("_id", counter)).first();
            if(document != null) continue;

            Counter newCounter = new Counter(counter, 0L);
            saveInstance(newCounter);
        }
    }
}