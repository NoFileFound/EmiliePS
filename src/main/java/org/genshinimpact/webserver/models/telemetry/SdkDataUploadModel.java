package org.genshinimpact.webserver.models.telemetry;

// Imports
import org.genshinimpact.webserver.enums.ClientType;

@SuppressWarnings("unused")
public class SdkDataUploadModel {
    public Integer applicationId;
    public String applicationName;
    public String msgId;
    public Integer eventTime;
    public Integer eventId;
    public String eventName;
    public UploadContent uploadContent;

    public static class UploadContent {
        public DeviceInfo deviceInfo;
        public UserInfo userInfo;
        public VersionInfo versionInfo;
        public LogInfo logInfo;

        public static class DeviceInfo {
            public ClientType platform;
            public String systemInfo;
            public String deviceId;
            public String deviceName;
            public String deviceModel;
            public String bundleId;
            public String device_fp;
            public Integer device_sciX;
            public Integer device_sciY;
            public Integer soft_sciX;
            public Integer soft_sciY;
            public Double romCapacity;
            public Double romRemain;
            public Double ramCapacity;
            public Double ramRemain;
            public String addressMac;
            public String gpuName;
            public Integer gpuMemSize;
            public Integer processorCount;
            public Double processorFrequency;
            public String processorType;
        }

        public static class UserInfo {
            public String userId;
            public String accountType;
            public String accountId;
            public String channelId;
        }

        public static class VersionInfo {
            public String clientVersion;
            public String logVersion;
        }

        public static class LogInfo {
            public String logTime;
            public Integer actionId;
            public String actionName;
            public String cBody;
        }
    }
}