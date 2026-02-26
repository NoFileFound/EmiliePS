package org.genshinimpact.webserver.responses.combo.reddot;

// Imports
import java.util.List;

public class RedDotListResponse {
    public List<RedDot> infos;

    public RedDotListResponse() {
        this.infos = List.of();
    }

    public RedDotListResponse(List<RedDot> infos) {
        this.infos = infos;
    }

    public static class RedDot {
        public Integer red_point_type;
        public Integer content_id;
        public Boolean display;
    }
}