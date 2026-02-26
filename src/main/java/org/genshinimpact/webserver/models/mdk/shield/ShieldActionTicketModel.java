package org.genshinimpact.webserver.models.mdk.shield;

// Imports
import org.genshinimpact.webserver.enums.ClientApiActionType;

@SuppressWarnings("unused")
public class ShieldActionTicketModel {
    public String account_id;
    public ClientApiActionType action_type;
    public String game_token;
}