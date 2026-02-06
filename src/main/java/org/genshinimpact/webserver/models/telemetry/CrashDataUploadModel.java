package org.genshinimpact.webserver.models.telemetry;

// Imports
import org.genshinimpact.webserver.enums.ReportErrorCode;
import org.genshinimpact.webserver.enums.ReportEventType;

@SuppressWarnings("unused")
public class CrashDataUploadModel {
    public Integer applicationId;
    public String applicationName;
    public String msgID;
    public String eventTime;
    public Integer eventId;
    public ReportEventType eventName;
    public UploadContent uploadContent;

    public static class UploadContent {
        public ReportErrorCode error_code;
        public String message;
        public Integer user_id;
        public String auid;
        public Integer time;
        public String stackTrace;
        public Integer exceptionSerialNum;
        public String frame;
        public String deviceModel;
        public String deviceName;
        public String operatingSystem;
        public String userName;
        public String version;
        public String guid;
        public String errorCode;
        public Boolean isRelease;
        public String serverName;
        public String projectNick;
        public String userNick;
        public String logType;
        public String subErrorCode;
        public String cpuInfo;
        public String gpuInfo;
        public String memoryInfo;
        public String clientIp;
        public String errorLevel;
        public String errorCategory;
        public String notifyUser;
    }
}