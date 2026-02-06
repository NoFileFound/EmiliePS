package org.genshinimpact.webserver.models.telemetry;

@SuppressWarnings("unused")
public class ApmDataUploadModel {
    public Integer applicationId;
    public String applicationName = "apm";
    public Integer eventId;
    public String eventName;
    public String eventTime;
    public String msgId;
    public UploadContent uploadContent;

    public static class UploadContent {
        public _APMInfo APMInfo;
        public _AppInfo AppInfo;
        public _DeviceInfo DeviceInfo;
        public String LocalTimestamp;
        public String OccurTimestamp;
        public String ReportId;

        public static class _APMInfo {
            public Object PluginsInfo; // ignored
            public String SDKVersion;
        }

        public static class _AppInfo {
            public String Aid;
            public String AppId;
            public String AppVersion;
            public String Area;
            public String Channel;
            public String CompileType;
            public String LifecycleId;
            public String PackageName;
            public String Region;
            public String UserDeviceId;
            public String UserId;
        }

        public static class _DeviceInfo {
            public String Brand;
            public String CPUArchitecture;
            public String DeviceId;
            public String DeviceModel;
            public String DeviceName;
            public Boolean IsRoot;
            public String Platform;
            public String Rom;
            public String SystemVersion;
        }
    }
}