package org.genshinimpact.webserver.models.mdk.shield;

// Imports
import org.genshinimpact.webserver.enums.ClientApiActionType;

@SuppressWarnings("unused")
public class ShieldEmailCaptchaModel {
    public ClientApiActionType action_type;
    public String email;
    public Object mmt = null; // MMT V4
}