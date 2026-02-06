package org.genshinimpact.webserver.enums;

// Imports
import lombok.Getter;

@SuppressWarnings("unused")
public enum ReportEventType {
    DownloadReport(100001),
    GameQAReport(100002),
    WarningAlarm(100003);

    @Getter
    private final int value;
    ReportEventType(int value) {
        this.value = value;
    }
}