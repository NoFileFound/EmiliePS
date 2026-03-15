package org.genshinimpact.webserver.models.combo.guard;

// Imports
import org.genshinimpact.webserver.enums.AppId;
import org.genshinimpact.webserver.enums.ChannelType;
import org.genshinimpact.webserver.enums.ClientType;

@SuppressWarnings("unused")
public class GuardPing1Model {
    public AppId app_id;
    public ChannelType channel_id;
    public String open_id;
    public String combo_token;
    public String device_id;
    public ClientType client_type;
    public Role role;
    public Integer time;

    public static class Role {
        public String region;
        public String uid;
    }
}