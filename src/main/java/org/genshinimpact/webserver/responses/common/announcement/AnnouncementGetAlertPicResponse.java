package org.genshinimpact.webserver.responses.common.announcement;

// Imports
import java.util.List;

public class AnnouncementGetAlertPicResponse {
    public Integer total;
    public List<Object> list;

    public AnnouncementGetAlertPicResponse() {
        this.total = 0;
        this.list = List.of();
    }
}