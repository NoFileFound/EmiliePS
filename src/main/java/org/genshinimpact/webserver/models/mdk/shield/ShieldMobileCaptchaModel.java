package org.genshinimpact.webserver.models.mdk.shield;

// Imports
import org.genshinimpact.webserver.enums.ClientApiActionType;

public class ShieldMobileCaptchaModel {
    public ClientApiActionType action_type;
    public String action_ticket;
    public String mobile;
    public Boolean safe_mobile;
}