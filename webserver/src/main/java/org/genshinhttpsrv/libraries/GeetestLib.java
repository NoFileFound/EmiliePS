package org.genshinhttpsrv.libraries;

// Imports
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.genshinhttpsrv.Application;

public final class GeetestLib {
    /**
     * Creates a new geetest challenge.
     * @return The challenge hash.
     */
    public static String generateChallenge() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://api.geetest.com/register.php?" + "gt=" + URLEncoder.encode(Application.getPropertiesInfo().geetest.gt, StandardCharsets.UTF_8) + "&json_format=1").openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            if (conn.getResponseCode() != 200)
                return "";
            try (InputStream in = conn.getInputStream()) {
                StringBuilder sb = new StringBuilder();
                byte[] buf = new byte[1024]; int r;
                while((r = in.read(buf)) != -1)
                    sb.append(new String(buf, 0, r, StandardCharsets.UTF_8));
                String res = sb.toString();
                if ("fail".equals(res)) return "";

                String challenge = Objects.requireNonNull(JsonLoader.parseJsonSafe(res)).get("challenge").asText();
                if (challenge.length() == 32) {
                    return EncryptionManager.md5Encode(challenge + Application.getPropertiesInfo().geetest.private_key);
                }
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Validates the geetest challenge.
     * @param risky The challenge information.
     * @return True if the challenge is successfully beaten or false.
     */
    public static Boolean checkVerifiedChallenge(String risky) {
        if(Application.getPropertiesInfo().disable_mmt) return true;

        Map<String, String> map = new HashMap<>();
        String[] parts = risky.split(";");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }

        String challenge = map.getOrDefault("c", "");
        String seccode = map.getOrDefault("s", "");
        String validate = map.getOrDefault("v", "");

        try {
            String jsonBody = """
            {
                "seccode": "%s",
                "challenge": "%s",
                "validate": "%s",
                "json_format": "1"
            }
            """.formatted(seccode, challenge, validate);

            HttpURLConnection conn = (HttpURLConnection) new URL("http://api.geetest.com/validate.php").openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            if (conn.getResponseCode() != 200) return false;
            try (InputStream in = conn.getInputStream()) {
                StringBuilder sb = new StringBuilder();
                byte[] buf = new byte[1024];
                int r;
                while ((r = in.read(buf)) != -1) {
                    sb.append(new String(buf, 0, r, StandardCharsets.UTF_8));
                }

                String res = sb.toString();
                if (res.isEmpty()) return false;

                return res.contains("seccode");
            }

        } catch (Exception e) {
            return false;
        }
    }
}