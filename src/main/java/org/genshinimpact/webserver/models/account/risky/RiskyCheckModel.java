package org.genshinimpact.webserver.models.account.risky;

// Imports
import org.genshinimpact.webserver.enums.ClientApiActionType;

@SuppressWarnings("unused")
public class RiskyCheckModel {
    public ClientApiActionType action_type;
    public String api_name;
    public String username = "";
    public String mobile = "";
    public String email = "";
}