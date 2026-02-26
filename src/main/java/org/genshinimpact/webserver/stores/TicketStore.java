package org.genshinimpact.webserver.stores;

// Imports
import org.genshinimpact.database.DBManager;
import org.genshinimpact.database.DBUtils;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Ticket;

public class TicketStore {
    /**
     * Creates a new ticket on the given account.
     *
     * @param myAccount The given account.
     * @param type      The ticket type.
     */
    public synchronized Ticket getOrCreateTicket(Account myAccount, Ticket.TicketType type) {
        Ticket myTicket = DBUtils.getTicketByAccountId(myAccount.getId(), type);
        if(myTicket != null) {
            if(!myTicket.isExpired()) {
                return myTicket;
            }

            this.removeTicket(myTicket, myAccount);
        }

        myTicket = new Ticket(myAccount.getId(), type);
        myTicket.save();
        switch(type) {
            case TICKET_REACTIVATE_ACCOUNT:
                myAccount.setRequireActivation(true, myTicket.getId());
                myAccount.save(true);
                break;
            case TICKET_DEVICE_GRANT:
                myAccount.setDeviceGrant(true, myTicket.getId());
                myAccount.save(true);
                break;
        }

        return myTicket;
    }

    /**
     * Removes the ticket from the given account.
     * @param myTicket The given ticket to remove.
     * @param myAccount The given account to remove the ticket from.
     */
    public synchronized void removeTicket(Ticket myTicket, Account myAccount) {
        switch(myTicket.getType()) {
            case TICKET_REACTIVATE_ACCOUNT:
                myAccount.setRequireActivation(false, myTicket.getId());
                break;
            case TICKET_DEVICE_GRANT:
                myAccount.setDeviceGrant(false, myTicket.getId());
                break;
        }

        DBManager.getCachedTickets().remove(myTicket.getId());
        myTicket.delete();
    }
}