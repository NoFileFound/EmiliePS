package org.emilieps.library;

// Imports
import static dev.morphia.query.experimental.filters.Filters.eq;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import lombok.Getter;
import org.bson.Document;
import org.emilieps.Application;
import org.emilieps.database.*;

public final class MongodbLib {
    @Getter private static final Map<String, Account> cachedAccountDevices = new HashMap<>();
    private static final ExecutorService eventExecutor = new ThreadPoolExecutor(6, 6, 60, TimeUnit.SECONDS,new LinkedBlockingDeque<>(), new ThreadPoolExecutor.AbortPolicy());
    private static Datastore dataStore;

    /**
     * Starts the mongodb database.
     */
    public static void initializeDatabase() {
        String dbUrl = Application.getApplicationConfig().database_url;
        String dbName = Application.getApplicationConfig().collection_name;

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
        Counter document = dataStore.find(Counter.class).filter(eq("_id", counterId)).first();
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
        Future<?> future = eventExecutor.submit(() -> dataStore.delete(object));
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves an object into the database.
     * @param object The object to save.
     */
    public static void saveInstance(Object object) {
        Future<?> future = eventExecutor.submit(() -> dataStore.save(object));
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the counters collection if needed.
     */
    private static void createCounters() {
        String[] counters = {"lastAccountId", "lastSanctionId", "lastMailId"};
        for (String counter : counters) {
            Counter document = dataStore.find(Counter.class).filter(eq("_id", counter)).first();
            if(document != null) continue;

            Counter newCounter = new Counter(counter, 0L);
            saveInstance(newCounter);
        }
    }

    /**
     * Searches for account by given email address in the database.
     * @param emailAddress The given email address.
     * @return An account if exist or else null.
     */
    public static Account findAccountByEmail(String emailAddress) {
        return dataStore.find(Account.class).filter(eq("emailAddress", emailAddress)).first();
    }

    /**
     * Searches for account by given id in the database.
     * @param id The given account id.
     * @return An account if exist or else null.
     */
    public static Account findAccountById(Long id) {
        return dataStore.find(Account.class).filter(eq("_id", id)).first();
    }

    /**
     * Searches for account by mobile number.
     * @param mobile_number The given mobile number.
     * @param area The given mobile area.
     * @return An account if exists or else null.
     */
    public static Account findAccountByMobile(String mobile_number, String area) {
        return dataStore.find(Account.class).filter(eq("mobileNumber", mobile_number), eq("mobileNumberArea", area)).first();
    }

    /**
     * Searches for account by given name in the database.
     * @param name The given name.
     * @return An account if exist or else null.
     */
    public static Account findAccountByName(String name) {
        return dataStore.find(Account.class).filter(eq("name", name)).first();
    }

    /**
     * Searches for account by given thirdparty registration.
     * @param displayName The display name in the thirdparty app.
     * @param type The thirdparty app.
     * @return An account if exist or else null.
     */
    public static Account findAccountByThirdParty(String displayName, String type) {
        switch (type) {
            case "Twitter" -> dataStore.find(Account.class).filter(eq("twitterName", displayName)).first();
            case "Facebook" -> dataStore.find(Account.class).filter(eq("facebookName", displayName)).first();
            case "Apple" -> dataStore.find(Account.class).filter(eq("appleName", displayName)).first();
            case "Google" -> dataStore.find(Account.class).filter(eq("googleName", displayName)).first();
            case "Sony" -> dataStore.find(Account.class).filter(eq("sonyName", displayName)).first();
            case "Steam" -> dataStore.find(Account.class).filter(eq("steamName", displayName)).first();
            case "TapTap" -> dataStore.find(Account.class).filter(eq("tapName", displayName)).first();
            case "GameCenter" -> dataStore.find(Account.class).filter(eq("gameCenterName", displayName)).first();
        }
        return dataStore.find(Account.class).filter(eq("cxName", displayName)).first();
    }

    /**
     * Searches for account by given game token in the database.
     * @param gameToken The given game token.
     * @return An account if exist or else null.
     */
    public static Account findAccountByToken(String gameToken) {
        return dataStore.find(Account.class).filter(eq("gameToken", gameToken)).first();
    }

    /**
     * Searches for guest account by given device id in the database.
     * @param deviceId The given device id.
     * @return A guest account if exist or else null.
     */
    public static Account findGuestById(String deviceId) {
        return dataStore.find(Account.class).filter(eq("approvedDevices", new ArrayList<>(List.of(deviceId))), eq("isGuest", true)).first();
    }

    /**
     * Searches for last active sanction by given name.
     * @param accountId The given player name.
     * @return A sanction object if exist or else null.
     */
    public static Sanction findLatestSanction(Long accountId) {
        return dataStore.find(Sanction.class).filter(eq("accountId", accountId), eq("state", "Active")).iterator(new FindOptions().sort(Sort.ascending("createdDate"))).tryNext();
    }

    /**
     * Searches for ticket by account id.
     * @param accountId The given account id.
     * @param action_type The given action type.
     * @return The ticket instance if exist or else null.
     */
    public static Ticket findTicketByAccountId(Long accountId, String action_type) {
        return dataStore.find(Ticket.class).filter(eq("accountId", accountId), eq("type", action_type)).first();
    }

    /**
     * Searches for ticket by id and type.
     * @param id The given ticket id.
     * @return The ticket instance if exist or else null.
     */
    public static Ticket findTicketById(String id) {
        return dataStore.find(Ticket.class).filter(eq("id", id)).first();
    }

    /**
     * Gets the total documents in a given collection.
     * @param collectionName The collection name.
     * @return A total documents in collection.
     */
    public static long getDocumentsCount(String collectionName) {
        return dataStore.getDatabase().getCollection(collectionName).countDocuments();
    }

    /**
     * Searches for all experiments by given list of scenes.
     * @param scenes The scene list.
     * @return List of experiments.
     */
    public static List<Experiment> getSceneExperiments(List<String> scenes) {
        List<Experiment> experimentList = new ArrayList<>();
        for(String scene : scenes) {
            List<Experiment> tmp = dataStore.find(Experiment.class).filter(eq("config_id", scene)).stream().toList();
            experimentList.addAll(tmp);
        }

        return experimentList;
    }

    /**
     * Saves the data logs into a given collection.
     * @param jsonString The data as json string.
     * @param collectionName The collection name.
     */
    public static void saveTelemetryLog(String jsonString, String collectionName) {
        JsonNode root = JsonLib.parseJsonSafe(jsonString);
        if(root == null) {
            Application.getLogger().error(Application.getTranslations().get("console", "cantsavelogging", collectionName));
            return;
        }

        Document doc = null;
        if(root.isObject()) {
            doc = Document.parse(root.toString());
        } else if(root.isArray() && !root.isEmpty()) {
            doc = Document.parse(root.get(0).toString());
        }

        if(doc != null) {
            dataStore.getDatabase().getCollection(collectionName).insertOne(doc);
        }
    }
}