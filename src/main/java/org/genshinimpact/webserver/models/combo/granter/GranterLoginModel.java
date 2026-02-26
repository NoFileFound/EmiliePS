package org.genshinimpact.webserver.models.combo.granter;

// Imports
import org.genshinimpact.webserver.enums.AppId;
import org.genshinimpact.webserver.enums.ChannelType;

@SuppressWarnings("unused")
public class GranterLoginModel {
    public AppId app_id;
    public ChannelType channel_id;
    public String data;
    public String device;
    public String sign;
}