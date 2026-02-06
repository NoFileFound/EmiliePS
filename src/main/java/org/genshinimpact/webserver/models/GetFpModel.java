package org.genshinimpact.webserver.models;

// Imports
import org.genshinimpact.webserver.enums.AppName;
import org.genshinimpact.webserver.enums.ClientType;

@SuppressWarnings("unused")
public class GetFpModel {
    public String device_id;
    public String seed_id;
    public String seed_time;
    public ClientType platform;
    public String device_fp;
    public AppName app_name;
    public String ext_fields;
}