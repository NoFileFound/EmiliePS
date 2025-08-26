package org.genshinhttpsrv.libraries;

// Imports
import lombok.Getter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.genshinhttpsrv.Application;

public final class EncryptionManager {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String h5logKey = "F#ju0q8I9HbmH8PMpJzzBee&p0b5h@Yb";
    private static final String overseasComboKey = "6a4c78fe0356ba4673b8071127b28123"; // 7320c0c14ad8ce0e82fd8483f7d98435 - sandbox,
    private static final String chinaComboKey = "d0d3a7342df2026a70f650b907800111"; // 71f35d945cf97f8f202cef35ae6aa7ed - sandbox
    private static byte[] passwordDecryptionKey;
    private static byte[] identityDecryptionKey;
    @Getter private static final Map<Integer, String> abTestKeys = new HashMap<>(Map.of(60, "d10ff485-06ec-4b9d-8977-14716c0a1dda", 31, "5f876baa-c4c4-43df-880a-c026184fd01c", 28, "df7f6400-ae6e-4850-8ff1-63e1f3f960d6", 45, "2902c529-499a-4a9b-a7b3-3b675632b8c3", 47, "b437f6e3-7e48-445a-8d8b-c8ebb5bd2b3e"));
    @Getter private static byte[] dispatchSeed;
    @Getter private static byte[] dispatchKey;
    @Getter private static PrivateKey dispatchSignatureKey;
    @Getter private static final Map<Integer, PublicKey> EncryptionKeys = new HashMap<>();

    /**
     * Decodes the h5log's data content. (RC4 Stream)
     * @param input The input data.
     @return The resulting byte array after applying RC4.
     */
    public static byte[] decodeH5Log(byte[] input) {
        byte[] result = new byte[input.length];
        byte[] s = new byte[256];
        int j = 0;

        for (int i = 0; i < 256; i++) {
            s[i] = (byte)i;
        }
        for (int i = 0; i < 256; i++) {
            j = (j + s[i] + h5logKey.getBytes()[i % h5logKey.length()]) & 0xFF;
            byte temp = s[i];
            s[i] = s[j];
            s[j] = temp;
        }

        int i = 0;
        j = 0;
        for (int y = 0; y < input.length; y++) {
            i = (i + 1) & 0xFF;
            j = (j + s[i]) & 0xFF;
            byte temp = s[i];
            s[i] = s[j];
            s[j] = temp;
            byte k = s[(s[i] + s[j]) & 0xFF];
            result[y] = (byte) (input[y] ^ k);
        }

        return result;
    }

    /**
     * Decrypts the player's realname or identity card information.
     * @param identity The identity detail.
     * @return The decrypted identity detail.
     */
    public static String decryptIdentity(String identity) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(identityDecryptionKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey private_key = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, private_key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(identity)), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Decrypts the AES-256 password in the login screen.
     * @param encryptedPassword The encrypted password.
     * @return The decrypted password if decryption was successfully or else empty string.
     */
    public static String decryptPassword(String encryptedPassword) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(passwordDecryptionKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey private_key = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, private_key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedPassword)), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "6969696969a";
        }
    }

    /**
     * Creates a new HMAC hash by given content and key.
     * @param content The given data.
     * @return A HMAC hash.
     */
    public static String generateHMAC(String content, Boolean isOverseas) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(isOverseas ? overseasComboKey.getBytes() : chinaComboKey.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);

            byte[] result = mac.doFinal(content.getBytes());
            return java.util.stream.IntStream.range(0, result.length).mapToObj(i -> String.format("%02x", result[i])).reduce("", String::concat);
        } catch(Exception ex) {
            return null;
        }
    }

    /**
     * Generates a verification code (6 digit code).
     * @return A verification code.
     */
    public static String generateVerificationCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Creates a random string by given length.
     * @param length The given string length.
     * @return A generated string.
     */
    public static String generateRandomKey(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);

        return java.util.stream.IntStream.range(0, bytes.length).mapToObj(i -> String.format("%02x", bytes[i])).reduce("", String::concat);
    }

    /**
     * Initialize the encryption.
     */
    public static void loadEncryptionKeys() {
        try {
            dispatchSeed = Files.readAllBytes(Paths.get("resources/dispatch/dispatchSeed.bin"));
            dispatchKey = Files.readAllBytes(Paths.get("resources/dispatch/dispatchKey.bin"));
            passwordDecryptionKey = Files.readAllBytes(Paths.get("resources/dispatch/passwordPriv.bin"));
            identityDecryptionKey = Files.readAllBytes(Paths.get("resources/dispatch/identityPriv.bin"));
            dispatchSignatureKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get("resources/dispatch/SigningKey.der"))));
            Path keysDir = Paths.get("resources/dispatch/keys/");
            if (Files.isDirectory(keysDir)) {
                try (var paths = Files.list(keysDir)) {
                    paths.filter(Files::isRegularFile)
                            .forEach(path -> {
                                try {
                                    String fileName = path.getFileName().toString();
                                    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Files.readAllBytes(keysDir.resolve(fileName))));
                                    EncryptionKeys.put(Integer.parseInt(fileName.replaceAll("\\D+", "")), publicKey);
                                } catch (Exception ex) {
                                    Application.getLogger().error(ex.getMessage());
                                    System.exit(1);
                                }
                            });
                }
            } else {
                throw new Exception("The directory does not exist.");
            }

            Application.getLogger().info(Application.getTranslationManager().get("console", "rsaloadedkeys"));
        } catch (Exception ignored) {
            Application.getLogger().error(Application.getTranslationManager().get("console", "rsaloadedkeysfailed"));
            System.exit(1);
        }
    }

    /**
     * Encodes a string into md5.
     * @param plainText The given text.
     * @return An md5 string.
     */
    public static String md5Encode(String plainText) {
        String re_md5;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte[] b = md.digest();
            int i;
            StringBuilder buf = new StringBuilder();
            for (byte value : b) {
                i = value;
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();
        } catch (NoSuchAlgorithmException ignored) {
            re_md5 = "";
        }
        return re_md5;
    }

    /**
     * Performs XOR encryption on the given array.
     * @param arr The given array.
     * @param key The given key to xor.
     * @return A new array that is xored.
     */
    public static byte[] performXor(byte[] arr, byte[] key) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] ^= key[i % key.length];
        }
        return arr;
    }
}