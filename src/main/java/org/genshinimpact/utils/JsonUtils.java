package org.genshinimpact.utils;

// Imports
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Parses the given JSON string into a {@link JsonNode} or null if fails.
     * @param json The given string.
     * @return The parsed {@link JsonNode} or {@code null} if the input is invalid JSON.
     */
    public static JsonNode read(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }

    /**
     * Reads JSON data from the given input stream and deserializes it into a class.
     *
     * @param input The input stream containing JSON data.
     * @param type  The target class to deserialize the JSON into.
     * @throws IOException if the input cannot be read or the JSON is invalid.
     */
    public static <T> T read(InputStream input, Class<T> type) throws IOException {
        return MAPPER.readValue(input, type);
    }

    /**
     * Reads JSON data from the given input stream and deserializes it into a class.
     *
     * @param json The UTF-8 encoded JSON string to deserialize.
     * @param type  The target class to deserialize the JSON into.
     * @throws IOException if the input cannot be read or the JSON is invalid.
     */
    public static <T> T read(String json, Class<T> type) throws IOException {
        try(InputStream is = new ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            return read(is, type);
        }
    }

    /**
     * Reads JSON data from the given file and deserializes it into a class.
     *
     * @param filePath The path to the JSON file.
     * @param type  The target class to deserialize the JSON into.
     * @throws IOException if the input cannot be read or the JSON is invalid.
     */
    public static <T> T readFile(String filePath, Class<T> type) throws IOException {
        return MAPPER.readValue(new File(filePath), type);
    }

    /**
     * Reads JSON data from the given input stream and deserializes it into a list.
     *
     * @param input The input stream containing JSON data.
     * @param type  The target class to deserialize the JSON into.
     * @throws IOException if the input cannot be read or the JSON is invalid.
     */
    public static <T> List<T> readList(InputStream input, Class<T> type) throws IOException {
        return MAPPER.readValue(input, MAPPER.getTypeFactory().constructCollectionType(List.class, type));
    }

    /**
     * Serializes the given object into its JSON string representation.
     * @param obj The given object.
     * @return The JSON string representation of the given object.
     */
    public static String toJsonString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}