package org.genshinhttpsrv.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.genshinhttpsrv.database.DBManager;
import org.genshinhttpsrv.libraries.EncryptionManager;

@Getter
@Entity(value = "tickets", useDiscriminator = false)
public final class Ticket {
    @Id private final String id;
    private final Long accountId;
    private final String type;
    private final Long createdAt;
    @Setter private String verificationCode;
    private final Map<String, Object> params;

    /**
     * Creates a new ticket.
     * @param accountId The account id (holder).
     * @param type The ticket type.
     */
    public Ticket(Long accountId, String type) {
        this.id = EncryptionManager.md5Encode(EncryptionManager.generateRandomKey(32));
        this.accountId = accountId;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.verificationCode = "";
        this.params = new HashMap<>();
    }

    /**
     * Deletes the database of ticket.
     */
    public void delete() {
        DBManager.deleteInstance(this);
    }

    /**
     * Updates the database of ticket.
     */
    public void save() {
        DBManager.saveInstance(this);
    }
}