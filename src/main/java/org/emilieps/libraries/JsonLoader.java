package org.emilieps.libraries;

// Imports
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class JsonLoader {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Loads JSON file and deserializes into a given class.
     * @param fileName The file to load.
     * @param clazz The class.
     * @return Class object deserialized from given JSON file or null if that JSON file does not exist.
     */
    public static <T> T loadJson(String fileName, Class<T> clazz) {
        try {
            return objectMapper.readValue(Files.readAllBytes(Paths.get("config/" + fileName)), clazz);
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Loads JSON file and deserializes into a given type.
     * @param fileName The file to load.
     * @param type The Type.
     * @return Class object deserialized from given JSON file or null if that JSON file does not exist.
     */
    public static <T> T loadJson(String fileName, Type type) {
        try {
            return objectMapper.readValue(Files.readAllBytes(Paths.get("config/" + fileName)), objectMapper.constructType(type));
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Parses a string json into json object.
     * @param json The string json.
     * @return The parsed json.
     */
    public static JsonNode parseJsonSafe(String json)  {
        if(json == null) return null;

        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    /**
     * Parses a string json into json object by given class.
     * @param json The string json.
     * @return The parsed json.
     * @param clazz The given class.
     * @throws JsonProcessingException Unable to parse the string. (not a valid json)
     */
    public static <T> T parseValue(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Saves Java object as JSON file.
     * @param fileName The file to save.
     * @param object The object to save.
     */
    public static <T> void saveJson(String fileName, T object) throws IOException {
        objectMapper.writeValue(new File("config/" + fileName), object);
    }

    /**
     * Converts an object to json.
     * @param object The given object.
     * @return A json object.
     * @throws JsonProcessingException Unable to parse the string. (not a valid json)
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}