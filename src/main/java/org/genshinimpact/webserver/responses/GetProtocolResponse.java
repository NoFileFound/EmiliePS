package org.genshinimpact.webserver.responses;

// Imports
import org.genshinimpact.webserver.enums.AppId;

public class GetProtocolResponse {
    public boolean modified;
    public Protocol protocol;

    public GetProtocolResponse() {
        this.modified = false;
        this.protocol = null;
    }

    public GetProtocolResponse(Boolean modified, Integer id, AppId app_id, String language, Integer major, Integer minimum) {
        this.modified = modified;
        this.protocol = new Protocol(id, app_id, language, major, minimum);
    }

    public static class Protocol {
        public Integer id;
        public AppId app_id;
        public String language;
        public String user_proto;
        public String priv_proto;
        public Integer major;
        public Integer minimum;
        public String create_time;
        public String teenager_proto;
        public String third_proto;
        public String full_priv_proto;

        public Protocol(Integer id, AppId app_id, String language, Integer major, Integer minimum) {
            this.id = id;
            this.app_id = app_id;
            this.language = language;
            this.user_proto = "";
            this.priv_proto = "";
            this.major = major;
            this.minimum = minimum;
            this.create_time = "0";
            this.teenager_proto = "";
            this.third_proto = "";
            this.full_priv_proto = "";
        }
    }
}