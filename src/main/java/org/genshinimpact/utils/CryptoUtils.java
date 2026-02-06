package org.genshinimpact.utils;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtils {
    private static final String h5logKey = "F#ju0q8I9HbmH8PMpJzzBee&p0b5h@Yb";

    /**
     * Encodes the given string into a Base64-encoded string.
     *
     * @param input The string to encode.
     * @return The Base64-encoded representation of the input string.
     */
    public static String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Decodes the given Base64-encoded string into string.
     *
     * @param output The string to decode.
     * @return The decoded string into bytes;
     */
    public static byte[] decodeBase64(String output) {
        return Base64.getDecoder().decode(output.replaceAll("\\r\\n|\\r|\\n", "").getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Decrypts the h5log (RC4 Stream).
     *
     * @param data The provided encoded h5log data.
     * @return The JSON object of the decrypted h5log data.
     */
    public static JsonNode decodeH5Log(String data) {
        byte[] encoded = decodeBase64(data);
        byte[] result = new byte[encoded.length];
        byte[] s = new byte[256];
        int j = 0;
        for (int i = 0; i < 256; i++) {
            s[i] = (byte) i;
        }
        for (int i = 0; i < 256; i++) {
            j = (j + s[i] + h5logKey.getBytes()[i % h5logKey.length()]) & 0xFF;
            byte temp = s[i];
            s[i] = s[j];
            s[j] = temp;
        }

        int i = 0;
        j = 0;
        for (int y = 0; y < encoded.length; y++) {
            i = (i + 1) & 0xFF;
            j = (j + s[i]) & 0xFF;
            byte temp = s[i];
            s[i] = s[j];
            s[j] = temp;
            byte k = s[(s[i] + s[j]) & 0xFF];
            result[y] = (byte) (encoded[y] ^ k);
        }

        return JsonUtils.read(new String(result));
    }

    /**
     * Computes the HMAC-SHA1 of the given data using the specified key.
     * @param data The given string to compute.
     * @param key The given key.
     * @return The HMAC-SHA1 as a lowercase hex string.
     */
    public static String getHMAC1(String data, String key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(byte b : rawHmac) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch(Exception ignored) {
            return "";
        }
    }

    /**
     * Computes the MD5 hash of the given byte array.
     * @param data The given byte array.
     * @return The MD5 hash as a lowercase hex string.
     */
    public static String getMd5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for(byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch(Exception e) {
            return "";
        }
    }

    /**
     * Generates a random string of the specified length.
     *
     * @param len The length of the string to generate.
     * @return A generated string.
     */
    public static String generateStringKey(int len) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }

        return sb.toString();
    }
}