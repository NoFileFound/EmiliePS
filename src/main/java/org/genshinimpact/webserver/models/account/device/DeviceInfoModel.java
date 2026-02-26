package org.genshinimpact.webserver.models.account.device;

// Imports
import org.genshinimpact.webserver.enums.ClientType;

@SuppressWarnings("unused")
public class DeviceInfoModel {
    public String device_id;
    public String device_name;
    public ClientType client;
    public String device_model;
}