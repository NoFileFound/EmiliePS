package org.genshinimpact.utils;

// Imports
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.webserver.utils.JsonUtils;

public final class CryptoUtils {
    private static final String h5logKey = "F#ju0q8I9HbmH8PMpJzzBee&p0b5h@Yb";
    private static byte[] passwordDecryptionKey;
    @Getter private static byte[] dispatchSeed;
    @Getter private static byte[] dispatchKey;
    @Getter private static byte[] clientSecretKey;
    @Getter private static byte[] clientSecretKeyBuffer;
    @Getter private static final long clientSecretKeySeed = Long.parseUnsignedLong("11468049314633205968");
    @Getter private static final Map<Integer, PublicKey> dispatchEncryptionKeys = new HashMap<>();
    @Getter private static PrivateKey dispatchSignatureKey;
    @Getter private static final Map<Integer, String> mdkKeys = Map.ofEntries(
            Map.entry(0, "6bdc3982c25f3f3c38668a32d287d16b"),
            Map.entry(1, "54c88f02dedb5fdfe997b40d325788ba"),
            Map.entry(2, "5ba7ef2079a4f72815b8df3edb0b5156"),
            Map.entry(3, "8e8a13e081aec9619ef8647bbc37da59"),
            Map.entry(4, "6bdc3982c25f3f3c38668a32d287d16b"),
            Map.entry(5, "5ba7ef2079a4f72815b8df3edb0b5156"),
            Map.entry(6, "54c88f02dedb5fdfe997b40d325788ba"),
            Map.entry(7, "8e8a13e081aec9619ef8647bbc37da59"),
            Map.entry(8, "54c88f02dedb5fdfe997b40d325788ba"),
            Map.entry(9, "6bdc3982c25f3f3c38668a32d287d16b"),
            Map.entry(11, "5ba7ef2079a4f72815b8df3edb0b5156"),
            Map.entry(12, "8e8a13e081aec9619ef8647bbc37da59"),
            Map.entry(19, "6bdc3982c25f3f3c38668a32d287d16b"),
            Map.entry(20, "5ba7ef2079a4f72815b8df3edb0b5156"),
            Map.entry(21, "5ba7ef2079a4f72815b8df3edb0b5156"),
            Map.entry(22, "184e37403a5f5281193da53a6c35236d"));
    @Getter private static final Map<Integer, String> comboKeys = Map.ofEntries(
            Map.entry(0, "d0d3a7342df2026a70f650b907800111"),
            Map.entry(1, "71f35d945cf97f8f202cef35ae6aa7ed"),
            Map.entry(2, "6a4c78fe0356ba4673b8071127b28123"),
            Map.entry(3, "7320c0c14ad8ce0e82fd8483f7d98435"),
            Map.entry(4, "d0d3a7342df2026a70f650b907800111"),
            Map.entry(5, "6a4c78fe0356ba4673b8071127b28123"),
            Map.entry(6, "71f35d945cf97f8f202cef35ae6aa7ed"),
            Map.entry(7, "7320c0c14ad8ce0e82fd8483f7d98435"),
            Map.entry(8, "71f35d945cf97f8f202cef35ae6aa7ed"),
            Map.entry(9, "d0d3a7342df2026a70f650b907800111"),
            Map.entry(11, "6a4c78fe0356ba4673b8071127b28123"),
            Map.entry(12, "7320c0c14ad8ce0e82fd8483f7d98435"),
            Map.entry(19, "d0d3a7342df2026a70f650b907800111"),
            Map.entry(20, "6a4c78fe0356ba4673b8071127b28123"),
            Map.entry(21, "6a4c78fe0356ba4673b8071127b28123"),
            Map.entry(22, "5b79c89e2decf7309d21673652beb893"));

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
     * Decrypts the login password.
     * @param password The provided encrypted password.
     * @return The decrypted password if its successfully or else empty string.
     */
    public static String decryptPassword(String password) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(passwordDecryptionKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, (RSAPrivateKey)keyFactory.generatePrivate(keySpec));
            return new String(cipher.doFinal(decodeBase64(password)), java.nio.charset.StandardCharsets.UTF_8);
        } catch(Exception ignored) {
            return "";
        }
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
     * Computes the HMAC-SHA256 of the given data using the specified key.
     * @param data The given string to compute.
     * @param key The given key.
     * @return The HMAC-SHA1 as a lowercase hex string.
     */
    public static String getHMAC256(String data, String key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
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
            InputStream signingStream = CryptoUtils.class.getClassLoader().getResourceAsStream("webserver/dispatch/dispatchSignatureKey.der");
            InputStream passwordStream = CryptoUtils.class.getClassLoader().getResourceAsStream("webserver/dispatch/passwordKey.der")) {

            if(seedStream == null || keyStream == null || signingStream == null || passwordStream == null) {
                throw new FileNotFoundException("One or more dispatch resources could not be found.");
            }

            dispatchSeed = seedStream.readAllBytes();
            dispatchKey = keyStream.readAllBytes();
            dispatchSignatureKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(signingStream.readAllBytes()));
            passwordDecryptionKey = passwordStream.readAllBytes();
        }

        try(InputStream secretSeedStream = CryptoUtils.class.getClassLoader().getResourceAsStream("gameserver/clientSecretKey.bin")) {
            if(secretSeedStream == null) {
                throw new FileNotFoundException("One or more dispatch resources could not be found #2.");
            }

            clientSecretKey = secretSeedStream.readAllBytes();
        }

        try(InputStream secretSeedBufferStream = CryptoUtils.class.getClassLoader().getResourceAsStream("gameserver/clientSecretKeyBuffer.bin")) {
            if(secretSeedBufferStream == null) {
                clientSecretKeyBuffer = new byte[0];
            } else {
                clientSecretKeyBuffer = secretSeedBufferStream.readAllBytes();
            }
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

        AppBootstrap.getLogger().info("The dispatch encryption files were loaded successfully.");
    }
}