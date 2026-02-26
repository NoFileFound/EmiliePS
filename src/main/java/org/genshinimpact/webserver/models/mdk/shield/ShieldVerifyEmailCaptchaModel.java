package org.genshinimpact.webserver.models.mdk.shield;

// Imports
import org.genshinimpact.webserver.enums.ClientApiActionType;

public class ShieldVerifyEmailCaptchaModel {
    public String action_ticket;
    public ClientApiActionType action_type;
    public String captcha;
}