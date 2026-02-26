package org.genshinimpact.webserver.stores;

// Imports
import lombok.Getter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.genshinimpact.utils.CryptoUtils;
import org.genshinimpact.webserver.SpringBootApp;
import org.genshinimpact.webserver.utils.JsonUtils;

public class GeetestStore {
    private final Map<String, GeetestClient> storage = new ConcurrentHashMap<>();

    /**
     * Returns the current captcha status for the given ip address or creates a new one if it doesn't exist.
     * @param ipAddress The given ip address.
     * @return The captcha (geetest) object.
     */
    public GeetestClient getOrGenerateCaptcha(String ipAddress) {
        GeetestClient client = this.storage.get(ipAddress);
        if(client != null) {
            if(client.timestamp > System.currentTimeMillis()) {
                return client;
            } else {
                this.storage.remove(ipAddress);
            }
        }

        String riskId = CryptoUtils.generateStringKey(32);
        GeetestModel model = this.generateGeetest();
        if(model == null) {
            model = new GeetestModel("", "");
        }

        client = new GeetestClient(riskId, model);
        this.storage.put(ipAddress, client);
        return client;
    }

    /**
     * Deletes the captcha (geetest) cache on the given ip address.
     * @param ipAddress The given ip address.
     */
    public void deleteCaptcha(String ipAddress) {
        if(this.storage.get(ipAddress) != null) {
            this.storage.remove(ipAddress);
        }
    }

    /**
     * Checks if the client passed the captcha (geetest) successfully.
     * @param riskId The captcha information. (c -> challenge, s -> seccode, v -> validate).
     * @return True if passed or False.
     */
    public synchronized boolean checkCaptchaStatus(String riskId) {
        if(riskId == null || riskId.isBlank()) {
            return true;
        }

        String challenge = null;
        String seccode = null;
        String validate = null;
        for(String part : riskId.split(";")) {
            int idx = part.indexOf('=');
            if(idx <= 0) continue;
            String value = part.substring(idx + 1).trim();
            switch(part.substring(0, idx).trim()) {
                case "c" -> challenge = value;
                case "s" -> seccode = value;
                case "v" -> validate = value;
            }
        }

        if(challenge == null || seccode == null || validate == null) {
            return true;
        }

        try {
            String jsonBody = "{\"seccode\":\"%s\",\"challenge\":\"%s\",\"validate\":\"%s\",\"json_format\":\"1\"}".formatted(seccode, challenge, validate);
            HttpURLConnection conn = (HttpURLConnection)new URL("https://api.geetest.com/validate.php").openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try(OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return true;
            }

            try(InputStream in = conn.getInputStream()) {
                String res = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                return !res.contains("seccode");
            }

        } catch(Exception ignored) {
            return true;
        }
    }

    /**
     * Generates a new Geetest captcha challenge.
     * @return a {@link GeetestModel} containing the geetest or {@code null} if generation fails.
     */
    private GeetestModel generateGeetest() {
        try {
            String gt = SpringBootApp.getWebConfig().geetestConfig.gt;
            URL url = new URL("https://api.geetest.com/register.php?gt=" + URLEncoder.encode(gt, StandardCharsets.UTF_8) + "&json_format=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            try(InputStream in = conn.getInputStream()) {
                String response = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                if("fail".equals(response)) {
                    return null;
                }

                var json = JsonUtils.read(response);
                if(json == null || !json.has("challenge")) {
                    return null;
                }

                String challenge = json.get("challenge").asText();
                if(challenge.length() != 32) {
                    return null;
                }

                return new GeetestModel(gt, CryptoUtils.getMd5((challenge + SpringBootApp.getWebConfig().geetestConfig.gtPrivateKey).getBytes()));
            }
        } catch(Exception ignored) {
            return null;
        }
    }


    public static class GeetestClient {
        @Getter private final String riskId;
        @Getter private final GeetestModel model;
        private final long timestamp;

        public GeetestClient(String riskId, GeetestModel model) {
            this.riskId = riskId;
            this.model = model;
            this.timestamp = System.currentTimeMillis() + 300000;
        }
    }

    public static class GeetestModel {
        public Integer success;
        public String gt;
        public String challenge;
        public Integer is_new_captcha;

        public GeetestModel(String gt, String challenge) {
            this.success = (challenge.isEmpty() ? 0 : 1);
            this.gt = gt;
            this.challenge = challenge;
            this.is_new_captcha = 1;
        }
    }
}