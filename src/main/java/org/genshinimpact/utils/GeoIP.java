package org.genshinimpact.utils;

// Imports
import com.maxmind.geoip2.DatabaseReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Currency;
import java.util.Locale;
import org.genshinimpact.bootstrap.AppBootstrap;

public final class GeoIP {
    private static DatabaseReader reader;
    private static boolean initialized = false;

    /**
     * Initializes the geo ip database.
     */
    public static void loadGeoDatabase() {
        if(initialized) {
            return;
        }

        try(InputStream dbStream = AppBootstrap.class.getClassLoader().getResourceAsStream("GeoIP.dat")) {
            if (dbStream == null) {
                initialized = false;
                throw new IOException("GeoIP.dat not found in resources");
            }

            reader = new DatabaseReader.Builder(dbStream).build();
            initialized = true;
            AppBootstrap.getLogger().info("GeoIP database loaded.");
        } catch (Exception e) {
            AppBootstrap.getLogger().error("GeoIP database could not be loaded: ", e);
        }
    }

    /**
     * Gets the country information of given ip address.
     * @param ipAddress The given ip address.
     * @return Country object.
     */
    public static String getCountryCode(String ipAddress) {
        if(!initialized) {
            return "JP";
        }

        try {
            return reader.country(InetAddress.getByName(ipAddress)).getCountry().getIsoCode();
        } catch (Exception ignored) {
            return "JP";
        }
    }

    /**
     * Gets the currency of given ip address.
     * @param ipAddress The given ip address.
     * @return The currency.
     */
    public static String getCountryCurrency(String ipAddress) {
        if(!initialized) {
            return "JPY";
        }

        try {
            Currency currency = Currency.getInstance(new Locale("", getCountryCode(ipAddress)));
            return currency != null ? currency.getCurrencyCode() : "JPY";
        } catch(Exception e) {
            return "JPY";
        }
    }
}