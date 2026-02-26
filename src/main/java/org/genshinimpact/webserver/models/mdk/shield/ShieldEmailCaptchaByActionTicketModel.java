package org.genshinimpact.webserver.models.mdk.shield;

// Imports
import org.genshinimpact.webserver.enums.ClientApiActionType;

@SuppressWarnings("unused")
public class ShieldEmailCaptchaByActionTicketModel {
    public String action_ticket;
    public ClientApiActionType action_type;
}