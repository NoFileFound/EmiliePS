package org.genshinimpact.database;

// Imports
import static dev.morphia.query.experimental.filters.Filters.eq;
import java.util.ArrayList;
import java.util.List;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Experiment;

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
     * Searches for account by given username in the database.
     * @param username The given username.
     * @return An account if exist or else null.
     */
    public static Account findAccountByUsername(String username) {
        return DBManager.getDataStore().find(Account.class).filter(eq("username", username)).first();
    }


    public static void saveLogCache(Object cacheBody, String cacheFile) {
        ///  TODO: IMPLEMENT
    }
}