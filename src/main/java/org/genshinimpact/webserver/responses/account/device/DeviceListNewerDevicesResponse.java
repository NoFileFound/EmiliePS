package org.genshinimpact.webserver.responses.account.device;

// Imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.genshinimpact.database.embeds.DeviceInfo;
import org.genshinimpact.webserver.enums.ClientType;

public class DeviceListNewerDevicesResponse {
    public List<Device> devices;
    public String latest_id;

    public DeviceListNewerDevicesResponse(Map<String, DeviceInfo> deviceInfo) {
        this.latest_id = String.valueOf(deviceInfo.size());
        this.devices = new ArrayList<>(deviceInfo.size());
        for(DeviceInfo device : deviceInfo.values()) {
            this.devices.add(new Device(device.getClientType(), device.getDeviceName(), device.getDeviceModel(), device.getDeviceId(), device.getTimestamp()));
        }
    }

    public static class Device {
        public ClientType client_type;
        public String device_name;
        public String device_model;
        public String device_id;
        public Long time;

        public Device(ClientType client, String device_name, String device_model, String device_id, Long time) {
            this.client_type = client;
            this.device_name = device_name;
            this.device_model = device_model;
            this.device_id = device_id;
            this.time = time;
            ///  TODO: INVESTIGATE WHY TIME IS ALWAYS 683 MONTHS
        }
    }
}