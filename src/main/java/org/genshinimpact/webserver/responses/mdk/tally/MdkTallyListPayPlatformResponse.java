package org.genshinimpact.webserver.responses.mdk.tally;

// Imports
import java.util.List;

public class MdkTallyListPayPlatformResponse {
    public List<PayType> pay_types;

    public MdkTallyListPayPlatformResponse() {
        this.pay_types = List.of();
    }

    public static class PayType {
        public String pay_type;
        public String display_name;
        public String icon_url;
        public List<PayVendor> pay_vendors;
    }

    public static class PayVendor {
        public String game;
        public String pay_plat;
        public String pay_type;
        public String pay_vendor;
        public String icon;
        public String display_name;
        public List<String> currency;
    }
}