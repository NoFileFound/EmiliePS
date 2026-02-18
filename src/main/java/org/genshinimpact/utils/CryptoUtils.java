package org.genshinimpact.utils;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtils {
    private static final String h5logKey = "F#ju0q8I9HbmH8PMpJzzBee&p0b5h@Yb";
    @Getter private static byte[] dispatchSeed;
    @Getter private static byte[] dispatchKey;
    @Getter private static final Map<Integer, PublicKey> dispatchEncryptionKeys = new HashMap<>();
    @Getter private static PrivateKey dispatchSignatureKey;

    /**
     * Encodes the given string into a Base64-encoded string.
     *
     * @param input The string to encode (as bytes).
     * @return The Base64-encoded representation of the input string.
     */
    public static String encodeBase64(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
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
     * Performs a xor encryption on the given data with the given key.
     * @param data The given data.
     * @param key The given key.
     * @return Xor-ed array.
     */
    public static byte[] getXor(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for(int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }

        return result;
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

    /**
     * Loads dispatch-related cryptographic resources.
     */
    public static void loadDispatchFiles() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try(InputStream seedStream = CryptoUtils.class.getClassLoader().getResourceAsStream("webserver/dispatch/dispatchSeed.bin");
            InputStream keyStream = CryptoUtils.class.getClassLoader().getResourceAsStream("webserver/dispatch/dispatchKey.bin");
            InputStream signingStream = CryptoUtils.class.getClassLoader().getResourceAsStream("webserver/dispatch/dispatchSignatureKey.der"))
        {
            if(seedStream == null || keyStream == null || signingStream == null) {
                throw new FileNotFoundException("One or more dispatch resources could not be found.");
            }

            dispatchSeed = seedStream.readAllBytes();
            dispatchKey = keyStream.readAllBytes();
            dispatchSignatureKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(signingStream.readAllBytes()));
        }

        for(int i = 1; i <= 5; i++) {
            String resourcePath = "webserver/dispatch/key_id_" + i + ".der";
            try(InputStream is = CryptoUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if(is == null) {
                    throw new FileNotFoundException("Missing key file: " + resourcePath);
                }

                dispatchEncryptionKeys.put(i, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(is.readAllBytes())));
            } catch(Exception ignored) {}
        }
    }
}