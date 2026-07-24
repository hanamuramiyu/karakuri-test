package hanamuramiyu.karakuri.storage;

import net.minecraft.core.BlockPos;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public record StorageMarker(
    String id,
    List<String> groupIds,
    String name,
    String worldId,
    String dimensionId,
    int x,
    int y,
    int z,
    String blockId,
    boolean enabled,
    int priority
) {
    public static final int DEFAULT_PRIORITY = 100;

    public StorageMarker {
        id = normalizeUuid(id, "Storage marker ID");
        groupIds = normalizeGroupIds(groupIds);
        name = normalizeText(name, "Storage marker name");
        worldId = normalizeText(worldId, "Storage world ID");
        dimensionId = normalizeText(
            dimensionId,
            "Storage dimension ID"
        );
        blockId = normalizeText(blockId, "Storage block ID");

        if (priority < 0) {
            throw new IllegalArgumentException(
                "Storage marker priority must not be negative"
            );
        }
    }

    public StorageMarker(
        String groupId,
        String name,
        String worldId,
        String dimensionId,
        BlockPos position,
        String blockId
    ) {
        this(
            UUID.randomUUID().toString(),
            List.of(groupId),
            name,
            worldId,
            dimensionId,
            position.getX(),
            position.getY(),
            position.getZ(),
            blockId,
            true,
            DEFAULT_PRIORITY
        );
    }

    public BlockPos position() {
        return new BlockPos(x, y, z);
    }

    public boolean belongsTo(
        String groupId
    ) {
        return groupId != null
            && groupIds.contains(groupId);
    }

    public StorageMarker withAddedGroup(
        String groupId
    ) {
        String normalized = normalizeUuid(
            groupId,
            "Storage group ID"
        );
        LinkedHashSet<String> updated =
            new LinkedHashSet<>(groupIds);
        updated.add(normalized);
        return withGroupIds(List.copyOf(updated));
    }

    public StorageMarker withRemovedGroup(
        String groupId
    ) {
        LinkedHashSet<String> updated =
            new LinkedHashSet<>(groupIds);
        updated.remove(groupId);

        if (updated.isEmpty()) {
            throw new IllegalArgumentException(
                "Storage marker must belong to at least one group"
            );
        }

        return withGroupIds(List.copyOf(updated));
    }

    public StorageMarker withGroupIds(
        List<String> updatedGroupIds
    ) {
        return new StorageMarker(
            id,
            updatedGroupIds,
            name,
            worldId,
            dimensionId,
            x,
            y,
            z,
            blockId,
            enabled,
            priority
        );
    }

    public StorageMarker withName(
        String updatedName
    ) {
        return new StorageMarker(
            id,
            groupIds,
            updatedName,
            worldId,
            dimensionId,
            x,
            y,
            z,
            blockId,
            enabled,
            priority
        );
    }

    public StorageMarker withEnabled(
        boolean updatedEnabled
    ) {
        return new StorageMarker(
            id,
            groupIds,
            name,
            worldId,
            dimensionId,
            x,
            y,
            z,
            blockId,
            updatedEnabled,
            priority
        );
    }

    public StorageMarker withPriority(
        int updatedPriority
    ) {
        return new StorageMarker(
            id,
            groupIds,
            name,
            worldId,
            dimensionId,
            x,
            y,
            z,
            blockId,
            enabled,
            updatedPriority
        );
    }

    private static List<String> normalizeGroupIds(
        List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(
                "Storage marker group IDs must not be empty"
            );
        }

        LinkedHashSet<String> normalized =
            new LinkedHashSet<>();

        for (String value : values) {
            normalized.add(
                normalizeUuid(value, "Storage group ID")
            );
        }

        return List.copyOf(normalized);
    }

    private static String normalizeUuid(
        String value,
        String subject
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                subject + " must not be blank"
            );
        }

        try {
            return UUID.fromString(value.trim())
                .toString()
                .toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                subject + " must be a UUID: " + value,
                exception
            );
        }
    }

    private static String normalizeText(
        String value,
        String subject
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                subject + " must not be blank"
            );
        }

        return value.trim();
    }
}