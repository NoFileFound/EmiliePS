package org.emilieps.library;

// Imports
import com.maxmind.geoip2.DatabaseReader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import org.emilieps.Application;

public final class GeoIPLib {
    private static DatabaseReader reader;

    /**
     * Initializes the geoip database.
     */
    public static void loadGeoDatabase() {
        try {
            File dbFile = new File("config/GeoIP.dat");
            if (!dbFile.exists()) {
                throw new IOException("GeoIP.dat not found at " + dbFile.getAbsolutePath());
            }

            reader = new DatabaseReader.Builder(dbFile).build();
            Application.getLogger().info("GeoIP database loaded");
        } catch (Exception ex) {
            Application.getLogger().error("GeoIP database could not be loaded", ex);
            System.exit(1);
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