package org.genshinimpact.webserver.models;

// Imports
import org.genshinimpact.webserver.enums.AppId;
import org.genshinimpact.webserver.enums.ChannelType;

@SuppressWarnings("unused")
public class GetProtocolModel {
    public AppId app_id;
    public ChannelType channel_id;
    public String language;
    public Integer major;
    public Integer minimum;
}