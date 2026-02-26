package org.genshinimpact.webserver.responses.mdk.shopwindow;

public class ShopWindowGetCurrencyAndCountryByIpResponse {
    public String currency;
    public String country;
    public String price_tier_version;

    public ShopWindowGetCurrencyAndCountryByIpResponse(String currency, String country) {
        this.currency = currency;
        this.country = country;
        this.price_tier_version = String.valueOf(System.currentTimeMillis() / 1000);
    }
}