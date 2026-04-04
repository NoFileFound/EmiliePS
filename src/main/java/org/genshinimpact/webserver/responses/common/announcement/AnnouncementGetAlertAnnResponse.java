package org.genshinimpact.webserver.responses.common.announcement;

public class AnnouncementGetAlertAnnResponse {
    public Boolean alert;
    public Integer alert_id;
    public Boolean remind;
    public Boolean extra_remind;
    public String remind_text;

    public AnnouncementGetAlertAnnResponse() {
        this.alert = false;
        this.alert_id = 0;
        this.remind = false;
        this.extra_remind = false;
        this.remind_text = "";
    }
}