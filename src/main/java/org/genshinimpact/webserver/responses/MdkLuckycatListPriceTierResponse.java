package org.genshinimpact.webserver.responses;

// Imports
import java.util.List;

public class MdkLuckycatListPriceTierResponse {
    public String suggest_currency;
    public List<Tier> tiers;

    public MdkLuckycatListPriceTierResponse(String suggest_currency) {
        this.suggest_currency = suggest_currency;
        this.tiers = List.of();
    }

    public static class Tier {
        public String tier_id;
        public List<TierPrice> t_price;

        public static class TierPrice {
            public Boolean enable;
            public String country;
            public String currency;
            public String price;
            public String symbol;
            public String amount_display;
        }
    }
}