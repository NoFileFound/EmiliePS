package org.genshinimpact.webserver.models.telemetry;

@SuppressWarnings("unused")
public class AndroidSdkDataUploadModel {
    public Integer applicationId;
    public String applicationName;
    public String msgId;
    public Integer eventTime;
    public Integer eventId;
    public String eventName;
    public UploadContent uploadContent;

    public static class UploadContent {
        public AttributionInfo attributionInfo;

        public static class AttributionInfo {
            ///  TODO: [Unfinished #1] Check more about android sdk.
        }
    }
}