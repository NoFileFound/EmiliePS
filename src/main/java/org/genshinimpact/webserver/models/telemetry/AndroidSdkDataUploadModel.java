package org.genshinimpact.webserver.models.telemetry;

@SuppressWarnings("unused")
public class AndroidSdkDataUploadModel {
    public Integer applicationId;
    public String applicationName = "adsdk";
    public String msgId;
    public Integer eventTime;
    public Integer eventId;
    public String eventName;
    public UploadContent uploadContent;

    public static class UploadContent {
        public AttributionInfo attributionInfo;

        public static class AttributionInfo {
            public String launch_trace_id;
            public String pkgname;
            public String androidid;
            public String event_name;
        }
    }
}