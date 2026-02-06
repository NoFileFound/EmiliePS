package org.genshinimpact.libraries;

// Imports
import com.maxmind.geoip2.DatabaseReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import org.genshinimpact.Application;

public class GeoIP {
    private static DatabaseReader reader;

    /**
     * Initializes the geo ip database.
     */
    public static void loadGeoDatabase() {
        try(InputStream dbStream = Application.class.getClassLoader().getResourceAsStream("GeoIP.dat")) {
            if (dbStream == null) {
                throw new IOException("GeoIP.dat not found in resources");
            }

            reader = new DatabaseReader.Builder(dbStream).build();
            Application.getLogger().info("GeoIP database loaded.");
        } catch (Exception e) {
            Application.getLogger().severe("GeoIP database could not be loaded: " + e.getMessage());
        }
    }

    /**
     * Gets the country information of given ip address.
     * @param ipAddress The given ip address.
     * @return Country object.
     */
    public static String getCountryCode(String ipAddress) {
        try {
            return reader.country(InetAddress.getByName(ipAddress)).getCountry().getIsoCode();
        } catch (Exception ignored) {
            return "JP";
        }
    }
}