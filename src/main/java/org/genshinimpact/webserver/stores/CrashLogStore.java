package org.genshinimpact.webserver.stores;

// Imports
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.genshinimpact.webserver.models.telemetry.CrashDataUploadModel;
import org.springframework.stereotype.Component;

@Component
public class CrashLogStore {
    private final List<CrashDataUploadModel> storage = new CopyOnWriteArrayList<>();

    /**
     * Adds a crash log to the store.
     *
     * @param data The crash report.
     */
    public void insert(CrashDataUploadModel data) {
        if(data == null) {
            return;
        }

        this.storage.add(data);
    }

    /**
     * Removes crash logs by given message identifier.
     * @param msgId The message identifier of the crash log to remove.
     */
    public boolean delete(String msgId) {
        if(msgId == null || msgId.isEmpty()) {
            return false;
        }

        return this.storage.removeIf(log -> msgId.equals(log.msgID));
    }

    /**
     * Returns the number of the crash logs.
     * @return The crash logs count.
     */
    public int size() {
        return this.storage.size();
    }

    /**
     * Clears the crash logs.
     */
    public void clear() {
        this.storage.clear();
    }

    /**
     * Returns all crash logs sorted by time.
     * @return The list of crash logs.
     */
    public List<CrashDataUploadModel> getStorage() {
        List<CrashDataUploadModel> result = new ArrayList<>(this.storage);
        result.sort(Comparator.comparingLong((CrashDataUploadModel c) -> c.uploadContent.time).reversed());
        return result;
    }
}