package org.genshinhttpsrv.database;

// Imports
import static dev.morphia.query.experimental.filters.Filters.eq;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bson.Document;
import org.genshinhttpsrv.Application;
import org.genshinhttpsrv.database.collections.Account;
import org.genshinhttpsrv.database.collections.Ticket;
import org.genshinhttpsrv.libraries.JsonLoader;

public final class DBUtils {
    // Cache
    @Getter private static final Map<String, Account> cachedAccountDevices = new HashMap<>();

    /**
     * Searches for account by given email address in the database.
     * @param emailAddress The given email address.
     * @return An account if exist or else null.
     */
    public static Account findAccountByEmail(String emailAddress) {
        return DBManager.getDataStore().find(Account.class).filter(eq("emailAddress", emailAddress)).first();
    }

    /**
     * Searches for account by given id in the database.
     * @param id The given account id.
     * @return An account if exist or else null.
     */
    public static Account findAccountById(Long id) {
        return DBManager.getDataStore().find(Account.class).filter(eq("_id", id)).first();
    }

    /**
     * Searches for account by mobile number.
     * @param mobile_number The given mobile number.
     * @param area The given mobile area.
     * @return An account if exists or else null.
     */
    public static Account findAccountByMobile(String mobile_number, String area) {
        return DBManager.getDataStore().find(Account.class).filter(eq("mobileNumber", mobile_number), eq("mobileNumberArea", area)).first();
    }

    /**
     * Searches for account by given name in the database.
     * @param name The given name.
     * @return An account if exist or else null.
     */
    public static Account findAccountByName(String name) {
        return DBManager.getDataStore().find(Account.class).filter(eq("name", name)).first();
    }

    /**
     * Searches for account by given thirdparty registration.
     * @param displayName The display name in the thirdparty app.
     * @param type The thirdparty app.
     * @return An account if exist or else null.
     */
    public static Account findAccountByThirdParty(String displayName, String type) {
        switch (type) {
            case "Twitter" -> DBManager.getDataStore().find(Account.class).filter(eq("twitterName", displayName)).first();
            case "Facebook" -> DBManager.getDataStore().find(Account.class).filter(eq("facebookName", displayName)).first();
            case "Apple" -> DBManager.getDataStore().find(Account.class).filter(eq("appleName", displayName)).first();
            case "Google" -> DBManager.getDataStore().find(Account.class).filter(eq("googleName", displayName)).first();
            case "Sony" -> DBManager.getDataStore().find(Account.class).filter(eq("sonyName", displayName)).first();
            case "Steam" -> DBManager.getDataStore().find(Account.class).filter(eq("steamName", displayName)).first();
            case "TapTap" -> DBManager.getDataStore().find(Account.class).filter(eq("tapName", displayName)).first();
            case "GameCenter" -> DBManager.getDataStore().find(Account.class).filter(eq("gameCenterName", displayName)).first();
        }
        return DBManager.getDataStore().find(Account.class).filter(eq("cxName", displayName)).first();
    }

    /**
     * Searches for account by given game token in the database.
     * @param gameToken The given game token.
     * @return An account if exist or else null.
     */
    public static Account findAccountByToken(String gameToken) {
        return DBManager.getDataStore().find(Account.class).filter(eq("gameToken", gameToken)).first();
    }

    /**
     * Searches for guest account by given device id in the database.
     * @param deviceId The given device id.
     * @return A guest account if exist or else null.
     */
    public static Account findGuestById(String deviceId) {
        return DBManager.getDataStore().find(Account.class).filter(eq("approvedDevices", new ArrayList<>(List.of(deviceId))), eq("isGuest", true)).first();
    }

    /**
     * Searches for ticket by account id.
     * @param accountId The given account id.
     * @param action_type The given action type.
     * @return The ticket instance if exist or else null.
     */
    public static Ticket findTicketByAccountId(Long accountId, String action_type) {
        return DBManager.getDataStore().find(Ticket.class).filter(eq("accountId", accountId), eq("type", action_type)).first();
    }

    /**
     * Searches for ticket by id and type.
     * @param id The given ticket id.
     * @return The ticket instance if exist or else null.
     */
    public static Ticket findTicketById(String id) {
        return DBManager.getDataStore().find(Ticket.class).filter(eq("id", id)).first();
    }

    /**
     * Saves the data logs into a given collection.
     * @param jsonString The data as json string.
     * @param collectionName The collection name.
     */
    public static void saveTelemetryLog(String jsonString, String collectionName) {
        JsonNode root = JsonLoader.parseJsonSafe(jsonString);
        if(root == null) {
            Application.getLogger().error(Application.getTranslationManager().get("console", "cantsavelogging", collectionName));
            return;
        }

        Document doc = null;
        if(root.isObject()) {
            doc = Document.parse(root.toString());
        } else if(root.isArray() && !root.isEmpty()) {
            doc = Document.parse(root.get(0).toString());
        }

        if(doc != null) {
            DBManager.getDataStore().getDatabase().getCollection(collectionName).insertOne(doc);
        }
    }
}