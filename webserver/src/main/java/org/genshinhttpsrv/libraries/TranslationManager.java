package org.genshinhttpsrv.libraries;

// Imports
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.genshinhttpsrv.Application;

public final class TranslationManager {
    private final Map<String, Map<String, String>> translations;

    /**
     * Initializes the translation manager by loading translations from file.
     */
    public TranslationManager() {
        Map<String, Map<String, String>> loadedTranslations = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(new File("./config/cnsl_translation.json"));
            root.fieldNames().forEachRemaining(lang -> {
                JsonNode langNode = root.get(lang);
                Map<String, String> langMap = objectMapper.convertValue(langNode, new TypeReference<>() {});
                loadedTranslations.put(lang.toLowerCase(), langMap);
            });
        } catch (IOException e) {
            System.err.println("The translations file could not be found or accessed or there is an error in the file. The program will now close.");
            System.exit(1);
        }

        this.translations = loadedTranslations;
    }

    /**
     * Retrieves the translated text for the given key and language code.
     *
     * @param lang The language code.
     * @param key  The translation key.
     * @param args Optional arguments for formatting.
     * @return The formatted translated string or the key itself if not found.
     */
    public String get(String lang, String key, Object... args) {
        if (lang == null) {
            lang = "en";
        }
        lang = lang.toLowerCase();
        Map<String, String> langTranslations = translations.get(lang);
        if ("console".equals(lang) && langTranslations == null) {
            Application.getLogger().warn("Unable to find the translation for: {}", key);
            return null;
        }

        if (langTranslations == null) {
            langTranslations = translations.get("en");
        }

        String template = (langTranslations != null) ? langTranslations.getOrDefault(key, key) : key;
        try {
            return String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }
}