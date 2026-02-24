package org.genshinimpact.webserver.responses;

// Imports
import java.util.List;
import org.bson.json.JsonObject;

public class MdkLuckycatListPayPlatformResponse {
    public List<PayPlatform> pay_plats;

    public MdkLuckycatListPayPlatformResponse() {
        this.pay_plats = List.of();
    }

    public static class PayPlatform {
        public String currency;
        public String name;
        public String icon_urls;
        public String redirect_url;
        public Boolean enable;
        public List<String> pay_type;
        public JsonObject event;
    }
}