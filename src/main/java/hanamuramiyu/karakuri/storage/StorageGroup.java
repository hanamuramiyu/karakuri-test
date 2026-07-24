package hanamuramiyu.karakuri.storage;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record StorageGroup(
    String id,
    String name,
    StorageColor color,
    boolean enabled
) {
    public StorageGroup {
        id = normalizeUuid(id);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Storage group name must not be blank"
            );
        }

        name = name.trim();
        color = Objects.requireNonNull(
            color,
            "Storage group color must not be null"
        );
    }

    public StorageGroup(
        String name,
        StorageColor color
    ) {
        this(
            UUID.randomUUID().toString(),
            name,
            color,
            true
        );
    }

    public StorageGroup withName(
        String updatedName
    ) {
        return new StorageGroup(
            id,
            updatedName,
            color,
            enabled
        );
    }

    public StorageGroup withColor(
        StorageColor updatedColor
    ) {
        return new StorageGroup(
            id,
            name,
            updatedColor,
            enabled
        );
    }

    public StorageGroup withEnabled(
        boolean updatedEnabled
    ) {
        return new StorageGroup(
            id,
            name,
            color,
            updatedEnabled
        );
    }

    private static String normalizeUuid(
        String value
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                "Storage group ID must not be blank"
            );
        }

        try {
            return UUID.fromString(value.trim())
                .toString()
                .toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "Storage group ID must be a UUID: " + value,
                exception
            );
        }
    }
}