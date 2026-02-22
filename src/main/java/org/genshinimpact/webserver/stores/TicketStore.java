package org.genshinimpact.webserver.stores;

// Imports
import org.genshinimpact.database.DBManager;
import org.genshinimpact.database.collections.Account;
import org.genshinimpact.database.collections.Ticket;

public class TicketStore {
    /**
     * Creates a new ticket on the given account.
     *
     * @param myAccount The given account.
     * @param type    The ticket type.
     */
    public void createTicket(Account myAccount, Ticket.TicketType type) {
        Ticket myTicket = getTicketByAccountId(myAccount.getId());
        if(myTicket != null) {
            if(!myTicket.isExpired()) {
                return;
            }

            this.removeTicket(myTicket, myAccount);
        }

        myTicket = new Ticket(myAccount.getId(), type);
        myTicket.save();
        switch(type) {
            case TICKET_REACTIVATE_ACCOUNT:
                myAccount.setRequireActivation(true, myTicket.getId());
                break;
            case TICKET_DEVICE_GRANT:
                myAccount.setDeviceGrant(true, myTicket.getId());
                break;
        }

        myAccount.save();
    }

    /**
     * Removes the ticket from the given account.
     * @param myTicket The given ticket to remove.
     * @param myAccount The given account to remove the ticket from.
     */
    public void removeTicket(Ticket myTicket, Account myAccount) {
        ///  TODO: IMPLEMENT THIS:
        /*Ticket myTicket = DBManager.getCachedTickets().get(ticketId);
        if(myTicket != null) {
            DBManager.deleteInstance(myTicket);
            DBManager.getCachedTickets().remove(ticketId);
            return;
        }*/


    }


    public Ticket getTicketByAccountId(Long accountId) {
        ///  TODO: IMPLEMENT THIS:
        return null;
    }
}