package org.genshinimpact.webserver.responses.mdk.luckycat;

// Imports
import java.util.List;
import org.bson.json.JsonObject;

public class LuckycatListPayPlatformResponse {
    public List<PayPlatform> pay_plats;

    public LuckycatListPayPlatformResponse() {
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