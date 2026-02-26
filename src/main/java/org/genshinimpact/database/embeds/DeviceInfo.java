package org.genshinimpact.database.embeds;

// Imports
import dev.morphia.annotations.Embedded;
import lombok.Getter;
import lombok.Setter;
import org.genshinimpact.webserver.enums.ClientType;
import org.genshinimpact.webserver.models.account.device.DeviceInfoModel;

@Getter
@Embedded
public final class DeviceInfo {
    private final String deviceModel;
    private final String deviceId;
    private final ClientType clientType;
    private final String deviceName;
    private final Long timestamp;
    @Setter private Boolean confirmed;

    public DeviceInfo(DeviceInfoModel data) {
        this.deviceModel = data.device_model;
        this.deviceId = data.device_id;
        this.clientType = data.client;
        this.deviceName = data.device_name;
        this.timestamp = System.currentTimeMillis();
        this.confirmed = true;
    }
}