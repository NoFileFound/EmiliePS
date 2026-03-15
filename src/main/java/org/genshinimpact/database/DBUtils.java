package org.genshinimpact.database;

// Imports
import static dev.morphia.query.experimental.filters.Filters.eq;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Experiment;
import org.genshinimpact.database.collections.Guest;
import org.genshinimpact.database.collections.Sanction;
import org.genshinimpact.database.collections.Ticket;
import org.genshinimpact.webserver.utils.JsonUtils;

public final class DBUtils {
    /**
     * Searches for all experiments by given list of scenes.
     * @param scenes The scene list.
     * @return List of experiments.
     */
    public static List<Experiment> getSceneExperiments(List<String> scenes) {
        List<Experiment> experimentList = new ArrayList<>();
        for(String scene : scenes) {
            experimentList.addAll(DBManager.getDataStore().find(Experiment.class).filter(eq("config_id", scene)).stream().toList());
        }

        return experimentList;
    }

    /**
     * Gets the guest object or creates a guest by provided device id.
     * @param deviceId The provided device id.
     * @return A guest object.
     */
    public static Guest getOrCreateGuest(String deviceId) {
        Guest myGuest = DBManager.getDataStore().find(Guest.class).filter(eq("deviceId", deviceId)).first();
        if(myGuest != null) {
            myGuest.setIsNew(false);
            return myGuest;
        }

        Guest newGuest = new Guest(deviceId);
        newGuest.setIsNew(true);
        newGuest.save(true);
        return newGuest;
    }

    /**
     * Searches for ticket by account id and ticket type.
     * @param accountId The account id.
     * @param type The ticket type.
     * @return A ticket if exist or else null.
     */
    public static Ticket getTicketByAccountId(Long accountId, Ticket.TicketType type) {
        for(Ticket ticket : DBManager.getCachedTickets().values()) {
            if(accountId.equals(ticket.getAccountId()) && type.equals(ticket.getType())) {
                return ticket;
            }
        }

        return DBManager.getDataStore().find(Ticket.class).filter(eq("accountId", accountId), eq("type", type)).first();
    }

    /**
     * Searches for ticket by id.
     * @param ticketId The given ticket id.
     * @return A ticket object if exist or else null.
     */
    public static Ticket getTicketById(String ticketId) {
        if(DBManager.getCachedTickets().get(ticketId) != null) {
            return DBManager.getCachedTickets().get(ticketId);
        }

        return DBManager.getDataStore().find(Ticket.class).filter(eq("id", ticketId)).first();
    }

    /**
     * Searches for account by given email address in the database.
     * @param emailAddress The given email address.
     * @return An account if exist or else null.
     */
    public static Account findAccountByEmailAddress(String emailAddress) {
        return DBManager.getDataStore().find(Account.class).filter(eq("emailAddress", emailAddress)).first();
    }

    /**
     * Searches for account by given id in the database.
     * @param id The given id.
     * @return An account if exist or else null.
     */
    public static Account findAccountById(Long id) {
        if(DBManager.getCachedAccounts().get(id) != null) {
            return DBManager.getCachedAccounts().get(id);
        }

        return DBManager.getDataStore().find(Account.class).filter(eq("id", id)).first();
    }

    /**
     * Searches for account by given phone number.
     * @param mobile The phone number.
     * @return An account if exist or else null.
     */
    public static Account findAccountByMobile(String mobile) {
        return DBManager.getDataStore().find(Account.class).filter(eq("mobileNumber", mobile)).first();
    }

    /**
     * Searches for account by given username in the database.
     * @param username The given username.
     * @return An account if exist or else null.
     */
    public static Account findAccountByUsername(String username) {
        return DBManager.getDataStore().find(Account.class).filter(eq("username", username)).first();
    }

    /**
     * Searches for guest by id.
     * @param id The given id.
     * @return A guest if exist or else null.
     */
    public static Guest findGuestById(Long id) {
        if(DBManager.getCachedGuests().get(id) != null) {
            return DBManager.getCachedGuests().get(id);
        }

        return DBManager.getDataStore().find(Guest.class).filter(eq("id", id)).first();
    }

    /**
     * Searches for sanctions by account id.
     * @param id The given account id.
     * @return The List of sanctions that are currently active.
     */
    public static List<Sanction> findSanctionListByAccountId(Long id) {
        return DBManager.getDataStore().find(Sanction.class).filter(eq("accountId", id), eq("state", "Active")).stream().toList();
    }

    /**
     * Saves the log in the database.
     * @param cacheBody The log content.
     * @param cacheDbName The log's database name.
     */
    public static void saveLogCache(Object cacheBody, String cacheDbName) {
        DBManager.getDataStore().getDatabase().getCollection(cacheDbName).insertOne(Document.parse(JsonUtils.toJsonString(cacheBody)));
    }
}