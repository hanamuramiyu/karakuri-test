package hanamuramiyu.karakuri.scenario.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

final class JsonObjectReader {
    private final JsonObject object;

    private JsonObjectReader(
        JsonObject object
    ) {
        this.object = object;
    }

    static JsonObjectReader from(
        JsonElement element,
        String subject
    ) {
        if (
            element == null
                || !element.isJsonObject()
        ) {
            throw new JsonParseException(
                subject + " must be an object"
            );
        }

        return new JsonObjectReader(
            element.getAsJsonObject()
        );
    }

    static JsonObjectReader from(
        JsonObject object
    ) {
        if (object == null) {
            throw new IllegalArgumentException(
                "JSON object must not be null"
            );
        }

        return new JsonObjectReader(object);
    }

    JsonObject object() {
        return object;
    }

    String requiredString(
        String key
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || !element.isJsonPrimitive()
                || !element
                    .getAsJsonPrimitive()
                    .isString()
        ) {
            throw new JsonParseException(
                "Missing string property: "
                    + key
            );
        }

        return element.getAsString();
    }

    String optionalString(
        String key,
        String defaultValue
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || element.isJsonNull()
        ) {
            return defaultValue;
        }

        if (
            !element.isJsonPrimitive()
                || !element
                    .getAsJsonPrimitive()
                    .isString()
        ) {
            throw new JsonParseException(
                "Invalid string property: "
                    + key
            );
        }

        return element.getAsString();
    }

    int requiredInt(
        String key
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || !element.isJsonPrimitive()
                || !element
                    .getAsJsonPrimitive()
                    .isNumber()
        ) {
            throw new JsonParseException(
                "Missing integer property: "
                    + key
            );
        }

        try {
            return element.getAsInt();
        } catch (RuntimeException exception) {
            throw new JsonParseException(
                "Invalid integer property: "
                    + key,
                exception
            );
        }
    }

    int optionalInt(
        String key,
        int defaultValue
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || element.isJsonNull()
        ) {
            return defaultValue;
        }

        if (
            !element.isJsonPrimitive()
                || !element
                    .getAsJsonPrimitive()
                    .isNumber()
        ) {
            throw new JsonParseException(
                "Invalid integer property: "
                    + key
            );
        }

        try {
            return element.getAsInt();
        } catch (RuntimeException exception) {
            throw new JsonParseException(
                "Invalid integer property: "
                    + key,
                exception
            );
        }
    }

    boolean optionalBoolean(
        String key,
        boolean defaultValue
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || element.isJsonNull()
        ) {
            return defaultValue;
        }

        if (
            !element.isJsonPrimitive()
                || !element
                    .getAsJsonPrimitive()
                    .isBoolean()
        ) {
            throw new JsonParseException(
                "Invalid boolean property: "
                    + key
            );
        }

        return element.getAsBoolean();
    }

    JsonArray optionalArray(
        String key
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || element.isJsonNull()
        ) {
            return null;
        }

        if (!element.isJsonArray()) {
            throw new JsonParseException(
                "Invalid array property: " + key
            );
        }

        return element.getAsJsonArray();
    }

    JsonArray requiredArray(
        String key
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || !element.isJsonArray()
        ) {
            throw new JsonParseException(
                "Missing array property: "
                    + key
            );
        }

        return element.getAsJsonArray();
    }
}