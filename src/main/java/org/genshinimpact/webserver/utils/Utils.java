package org.genshinimpact.webserver.utils;

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
}