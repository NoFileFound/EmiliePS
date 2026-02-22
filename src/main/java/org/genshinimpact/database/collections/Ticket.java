package org.genshinimpact.database.collections;

// Imports
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Getter;
import org.genshinimpact.database.DBManager;
import org.genshinimpact.utils.CryptoUtils;

@Getter
@Entity(value = "tickets", useDiscriminator = false)
public final class Ticket {
    @Id private final String id;
    private final Long accountId;
    private final TicketType type;
    private final Long timestamp;

    /**
     * Creates a new ticket.
     * @param accountId The account id/
     * @param ticketType The ticket type.
     */
    public Ticket(Long accountId, TicketType ticketType) {
        this.id = CryptoUtils.getMd5(CryptoUtils.generateStringKey(32).getBytes());
        this.accountId = accountId;
        this.type = ticketType;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Deletes the ticket instance.
     */
    public void delete() {
        DBManager.deleteInstance(this);
    }

    /**
     * Checks if the ticket is expired.
     * @return True if its expired or else False.
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() - this.timestamp) > 600069;
    }

    /**
     * Saves the ticket instance.
     */
    public void save() {
        DBManager.saveInstance(this);
        DBManager.getCachedTickets().put(this.id, this);
    }


    public enum TicketType {
        TICKET_REACTIVATE_ACCOUNT,
        TICKET_DEVICE_GRANT,
    }
}