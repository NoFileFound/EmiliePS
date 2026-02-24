package org.genshinimpact.webserver.responses;

// Imports
import java.util.List;

public class MdkShopWindowListPriceTierResponse {
    public String suggest_currency;
    public List<Tier> tiers;
    public String price_tier_version;

    public MdkShopWindowListPriceTierResponse(String suggest_currency) {
        this.suggest_currency = suggest_currency;
        this.tiers = List.of();
        this.price_tier_version = "0";
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