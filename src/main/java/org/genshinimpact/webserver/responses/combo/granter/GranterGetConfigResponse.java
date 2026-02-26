package org.genshinimpact.webserver.responses.combo.granter;

// Imports
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GranterGetConfigResponse {
    private boolean protocol;
    private boolean qrEnabled;
    private String logLevel;
    private String announceUrl;
    private Integer pushAliasType;
    private boolean disableYsdkGuard;
    private boolean enableAnnouncePicPopup;
    private String appName;
    private QrApps qrEnabledApps;
    private QrAppIcons qrAppIcons;
    private String qrCloudDisplayName;
    private String qrAppDisplayName;
    private String qrBbsDisplayName;
    private boolean enableUserCenter;
    private FunctionalSwitchConfigs functionalSwitchConfigs;
    private boolean ugcProtocol;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QrApps {
        private boolean bbs;
        private boolean cloud;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QrAppIcons {
        private String app;
        private String bbs;
        private String cloud;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionalSwitchConfigs {
        private boolean jpush;
        private boolean initializeAppsflyer;
        private boolean allowNotification;
    }
}