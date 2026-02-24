package org.genshinimpact.webserver.models;

// Imports
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;

@SuppressWarnings("unused")
public class GuestLoginModel {
    public AppName game_key;
    public ClientType client;
    public String device;
    public String sign;
    public String g_version;
}