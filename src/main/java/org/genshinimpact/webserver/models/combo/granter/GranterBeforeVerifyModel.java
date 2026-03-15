package org.genshinimpact.webserver.models.combo.granter;

// Imports
import org.genshinimpact.webserver.enums.AppId;
import org.genshinimpact.webserver.enums.ChannelType;

@SuppressWarnings("unused")
public class GranterBeforeVerifyModel {
    public AppId app_id;
    public ChannelType channel_id;
    public String open_id;
    public String combo_token;
    public Role role;
    public Integer time;

    public static class Role {
        public String region;
        public String uid;
    }
}