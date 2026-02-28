package org.genshinimpact.webserver.utils;

// Imports
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.utils.CryptoUtils;

public final class Utils {
    /**
     * Filters the given string.
     * @param input The given string.
     * @return A censored version of the string.
     */
    public static String filterString(String input) {
        if(input.isEmpty()) return "";
        int length = input.length();
        if(input.contains("@")) {
            String[] parts = input.split("@", 2);
            String local = parts[0];
            String domain = parts[1];
            if(local.length() <= 2) {
                local = local.charAt(0) + "*";
            } else {
                int visibleStart = 2;
                int visibleEnd = 1;
                int starsCount = local.length() - visibleStart - visibleEnd;
                local = local.substring(0, visibleStart) + "*".repeat(starsCount) + local.substring(local.length() - visibleEnd);
            }
            return local + "@" + domain;
        }

        if(length == 2) {
            return input.charAt(0) + "*";
        } else if(length == 3) {
            return input.substring(0, 2) + "*";
        }

        int visibleStart = Math.max(1, length / 3);
        int visibleEnd = Math.max(1, length / 3);
        if(visibleStart + visibleEnd >= length) {
            visibleStart = 1;
            visibleEnd = 1;
        }

        String start = input.substring(0, visibleStart);
        String end = input.substring(length - visibleEnd);
        int starsCount = length - visibleStart - visibleEnd;
        return start + "*".repeat(Math.max(0, starsCount)) + end;
    }

    /**
     * Gets the environment type.
     * @param isOverseas Is the client located outside of China.
     * @return The environment id based on the server type and client.
     */
    public static Integer getDispatchEnvType(Boolean isOverseas) {
        switch(AppBootstrap.getMainConfig().serverType) {
            case SERVER_TYPE_DEV -> {
                return (isOverseas ? 3 : 1);
            }
            case SERVER_TYPE_BETA -> {
                return (isOverseas ? 11 : 9);
            }
            default -> {
                return (isOverseas ? 2 : 0);
            }
        }
    }

    /**
     * Gets the sdk key.
     * @param isOverseas Is the client located outside of China.
     * @param isCombo Is the key about combo or mdk.
     * @return The sdk key based on the server type, client and key request type.
     */
    public static String getSDKey(Boolean isOverseas, Boolean isCombo) {
        switch(AppBootstrap.getMainConfig().serverType) {
            case SERVER_TYPE_DEV -> {
                if(isCombo) {
                    return isOverseas ? CryptoUtils.getComboKeys().get(3) : CryptoUtils.getComboKeys().get(1);
                }

                return isOverseas ? CryptoUtils.getMdkKeys().get(3) : CryptoUtils.getMdkKeys().get(1);
            }

            case SERVER_TYPE_BETA -> {
                if(isCombo) {
                    return isOverseas ? CryptoUtils.getComboKeys().get(11) : CryptoUtils.getComboKeys().get(9);
                }

                return isOverseas ? CryptoUtils.getMdkKeys().get(11) : CryptoUtils.getMdkKeys().get(9);
            }

            default -> {
                if(isCombo) {
                    return isOverseas ? CryptoUtils.getComboKeys().get(2) : CryptoUtils.getComboKeys().get(0);
                }

                return isOverseas ? CryptoUtils.getMdkKeys().get(2) : CryptoUtils.getMdkKeys().get(0);
            }
        }
    }
}