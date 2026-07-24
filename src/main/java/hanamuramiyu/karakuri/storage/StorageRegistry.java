package hanamuramiyu.karakuri.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hanamuramiyu.karakuri.Karakuri;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class StorageRegistry {
    private static final int SCHEMA_VERSION = 3;
    private static final int PREVIOUS_SCHEMA_VERSION = 2;
    private static final int LEGACY_SCHEMA_VERSION = 1;
    private static final Gson GSON =
        new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static List<StorageGroup> groups = List.of();
    private static List<StorageMarker> markers = List.of();
    private static boolean initialized;

    private StorageRegistry() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        LoadedRegistry loaded = load();
        groups = loaded.groups();
        markers = loaded.markers();
        initialized = true;

        if (loaded.migrated()) {
            write(groups, markers);
        }
    }

    public static synchronized List<StorageGroup> groups() {
        initialize();
        return groups;
    }

    public static synchronized List<StorageMarker> markers() {
        initialize();
        return markers;
    }

    public static synchronized StorageGroup addGroup(
        String name,
        StorageColor color
    ) {
        initialize();

        if (containsGroupName(name, null)) {
            throw new IllegalArgumentException(
                "Storage group name already exists: " + name
            );
        }

        StorageGroup group = new StorageGroup(name, color);
        List<StorageGroup> updatedGroups =
            new ArrayList<>(groups);
        updatedGroups.add(group);
        save(updatedGroups, markers);
        return group;
    }

    public static synchronized void replaceGroup(
        StorageGroup group
    ) {
        initialize();

        if (group == null) {
            throw new IllegalArgumentException(
                "Storage group must not be null"
            );
        }

        if (containsGroupName(group.name(), group.id())) {
            throw new IllegalArgumentException(
                "Storage group name already exists: "
                    + group.name()
            );
        }

        List<StorageGroup> updatedGroups =
            new ArrayList<>(groups);
        updatedGroups.set(groupIndex(group.id()), group);
        save(updatedGroups, markers);
    }

    public static synchronized void deleteGroup(
        String groupId
    ) {
        initialize();
        requireGroup(groupId);

        List<StorageGroup> updatedGroups =
            new ArrayList<>(groups);
        updatedGroups.remove(groupIndex(groupId));

        List<StorageMarker> updatedMarkers =
            new ArrayList<>();

        for (StorageMarker marker : markers) {
            if (!marker.belongsTo(groupId)) {
                updatedMarkers.add(marker);
                continue;
            }

            if (marker.groupIds().size() > 1) {
                updatedMarkers.add(
                    marker.withRemovedGroup(groupId)
                );
            }
        }

        save(updatedGroups, updatedMarkers);
    }

    public static synchronized StorageGroup findGroup(
        String groupId
    ) {
        initialize();

        for (StorageGroup group : groups) {
            if (group.id().equals(groupId)) {
                return group;
            }
        }

        return null;
    }

    public static synchronized StorageMarker findMarker(
        String markerId
    ) {
        initialize();

        for (StorageMarker marker : markers) {
            if (marker.id().equals(markerId)) {
                return marker;
            }
        }

        return null;
    }

    public static synchronized List<StorageGroup>
    groupsForMarker(
        String markerId
    ) {
        StorageMarker marker = findMarker(markerId);

        if (marker == null) {
            return List.of();
        }

        List<StorageGroup> result = new ArrayList<>();

        for (String groupId : marker.groupIds()) {
            StorageGroup group = findGroup(groupId);

            if (group != null) {
                result.add(group);
            }
        }

        return List.copyOf(result);
    }

    public static synchronized StorageGroup groupForMarker(
        String markerId
    ) {
        List<StorageGroup> markerGroups =
            groupsForMarker(markerId);
        return markerGroups.isEmpty()
            ? null
            : markerGroups.getFirst();
    }

    public static synchronized StorageMarker addMarker(
        String groupId,
        StorageMarker marker
    ) {
        initialize();
        requireGroup(groupId);

        if (marker == null) {
            throw new IllegalArgumentException(
                "Storage marker must not be null"
            );
        }

        if (!marker.belongsTo(groupId)) {
            throw new IllegalArgumentException(
                "Storage marker does not belong to the target group"
            );
        }

        if (findMarker(marker.id()) != null) {
            throw new IllegalArgumentException(
                "Storage marker ID already exists: " + marker.id()
            );
        }

        if (
            findMarkerAt(
                marker.worldId(),
                marker.dimensionId(),
                marker.position()
            ) != null
        ) {
            throw new IllegalArgumentException(
                "This storage is already registered"
            );
        }

        List<StorageMarker> updatedMarkers =
            new ArrayList<>(markers);
        updatedMarkers.add(marker);
        save(groups, updatedMarkers);
        return marker;
    }

    public static synchronized StorageMarker assignMarkerToGroup(
        String markerId,
        String groupId
    ) {
        initialize();
        requireGroup(groupId);
        StorageMarker marker = requireMarker(markerId);

        if (marker.belongsTo(groupId)) {
            return marker;
        }

        StorageMarker updated = marker.withAddedGroup(groupId);
        replaceMarker(updated);
        return updated;
    }

    public static synchronized void removeMarkerFromGroup(
        String markerId,
        String groupId
    ) {
        initialize();
        requireGroup(groupId);
        StorageMarker marker = requireMarker(markerId);

        if (!marker.belongsTo(groupId)) {
            throw new IllegalArgumentException(
                "Storage marker is not assigned to this group"
            );
        }

        if (marker.groupIds().size() == 1) {
            deleteMarker(markerId);
            return;
        }

        replaceMarker(marker.withRemovedGroup(groupId));
    }

    public static synchronized void replaceMarker(
        StorageMarker marker
    ) {
        initialize();

        if (marker == null) {
            throw new IllegalArgumentException(
                "Storage marker must not be null"
            );
        }

        List<StorageMarker> updatedMarkers =
            new ArrayList<>(markers);
        updatedMarkers.set(markerIndex(marker.id()), marker);
        save(groups, updatedMarkers);
    }

    public static synchronized void deleteMarker(
        String markerId
    ) {
        initialize();

        List<StorageMarker> updatedMarkers =
            new ArrayList<>(markers);
        updatedMarkers.remove(markerIndex(markerId));
        save(groups, updatedMarkers);
    }

    public static synchronized StorageMarker findMarkerAt(
        String worldId,
        String dimensionId,
        BlockPos position
    ) {
        initialize();

        for (StorageMarker marker : markers) {
            if (
                marker.worldId().equals(worldId)
                    && marker.dimensionId()
                        .equals(dimensionId)
                    && marker.position().equals(position)
            ) {
                return marker;
            }
        }

        return null;
    }

    public static synchronized List<StorageMarker>
    markersForGroup(
        StorageGroup group
    ) {
        if (group == null) {
            return List.of();
        }

        return markers.stream()
            .filter(marker -> marker.belongsTo(group.id()))
            .toList();
    }

    public static synchronized List<StorageMarker>
    markersForWorld(
        StorageGroup group,
        String worldId
    ) {
        if (group == null) {
            return List.of();
        }

        return markersForGroup(group)
            .stream()
            .filter(marker -> marker.worldId().equals(worldId))
            .toList();
    }

    public static synchronized List<StorageGroup>
    overlappingGroups(
        String groupId
    ) {
        StorageGroup group = findGroup(groupId);

        if (
            group == null
                || group.itemFilter().emptyFilter()
        ) {
            return List.of();
        }

        Set<String> sharedGroupIds =
            new HashSet<>();

        for (StorageMarker marker : markersForGroup(group)) {
            sharedGroupIds.addAll(marker.groupIds());
        }

        sharedGroupIds.remove(group.id());

        List<StorageGroup> overlapping =
            new ArrayList<>();

        for (String sharedGroupId : sharedGroupIds) {
            StorageGroup sharedGroup =
                findGroup(sharedGroupId);

            if (
                sharedGroup != null
                    && group.itemFilter().overlaps(
                        sharedGroup.itemFilter()
                    )
            ) {
                overlapping.add(sharedGroup);
            }
        }

        overlapping.sort(
            java.util.Comparator.comparing(
                StorageGroup::name,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        return List.copyOf(overlapping);
    }

    public static synchronized List<StorageGroup>
    overlappingGroups(
        String groupId,
        StorageItemFilter itemFilter
    ) {
        StorageGroup group = findGroup(groupId);

        if (
            group == null
                || itemFilter == null
                || itemFilter.emptyFilter()
        ) {
            return List.of();
        }

        Set<String> sharedGroupIds =
            new HashSet<>();

        for (StorageMarker marker : markersForGroup(group)) {
            sharedGroupIds.addAll(marker.groupIds());
        }

        sharedGroupIds.remove(group.id());

        List<StorageGroup> overlapping =
            new ArrayList<>();

        for (String sharedGroupId : sharedGroupIds) {
            StorageGroup sharedGroup =
                findGroup(sharedGroupId);

            if (
                sharedGroup != null
                    && itemFilter.overlaps(
                        sharedGroup.itemFilter()
                    )
            ) {
                overlapping.add(sharedGroup);
            }
        }

        overlapping.sort(
            java.util.Comparator.comparing(
                StorageGroup::name,
                String.CASE_INSENSITIVE_ORDER
            )
        );

        return List.copyOf(overlapping);
    }

    private static void save(
        List<StorageGroup> updatedGroups,
        List<StorageMarker> updatedMarkers
    ) {
        validate(updatedGroups, updatedMarkers);
        groups = List.copyOf(updatedGroups);
        markers = List.copyOf(updatedMarkers);
        write(groups, markers);
    }

    private static LoadedRegistry load() {
        Path path = configPath();

        if (Files.notExists(path)) {
            return new LoadedRegistry(
                List.of(),
                List.of(),
                false
            );
        }

        try (
            Reader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
            )
        ) {
            JsonObject root =
                JsonParser.parseReader(reader)
                    .getAsJsonObject();
            int schemaVersion = requiredInt(
                root,
                "schemaVersion"
            );

            LoadedRegistry loaded = switch (schemaVersion) {
                case SCHEMA_VERSION -> readCurrent(root);
                case PREVIOUS_SCHEMA_VERSION ->
                    readPrevious(root);
                case LEGACY_SCHEMA_VERSION -> readLegacy(root);
                default -> throw new IllegalArgumentException(
                    "Unsupported storage schema version: "
                        + schemaVersion
                );
            };

            validate(loaded.groups(), loaded.markers());
            return loaded;
        } catch (
            IOException
                | RuntimeException exception
        ) {
            Karakuri.LOGGER.error(
                "Failed to load storage registry {}",
                path,
                exception
            );
            return new LoadedRegistry(
                List.of(),
                List.of(),
                false
            );
        }
    }

    private static LoadedRegistry readCurrent(
        JsonObject root
    ) {
        List<StorageGroup> loadedGroups =
            new ArrayList<>();
        List<StorageMarker> loadedMarkers =
            new ArrayList<>();

        for (
            JsonElement element :
            requiredArray(root, "groups")
        ) {
            loadedGroups.add(
                readCurrentGroup(element.getAsJsonObject())
            );
        }

        for (
            JsonElement element :
            requiredArray(root, "markers")
        ) {
            loadedMarkers.add(
                readCurrentMarker(element.getAsJsonObject())
            );
        }

        return new LoadedRegistry(
            List.copyOf(loadedGroups),
            List.copyOf(loadedMarkers),
            false
        );
    }

    private static LoadedRegistry readPrevious(
        JsonObject root
    ) {
        List<StorageGroup> loadedGroups =
            new ArrayList<>();
        List<StorageMarker> loadedMarkers =
            new ArrayList<>();

        for (
            JsonElement element :
            requiredArray(root, "groups")
        ) {
            loadedGroups.add(
                readPreviousGroup(element.getAsJsonObject())
            );
        }

        for (
            JsonElement element :
            requiredArray(root, "markers")
        ) {
            loadedMarkers.add(
                readCurrentMarker(element.getAsJsonObject())
            );
        }

        return new LoadedRegistry(
            List.copyOf(loadedGroups),
            List.copyOf(loadedMarkers),
            true
        );
    }

    private static LoadedRegistry readLegacy(
        JsonObject root
    ) {
        List<StorageGroup> loadedGroups =
            new ArrayList<>();
        List<StorageMarker> loadedMarkers =
            new ArrayList<>();

        for (
            JsonElement groupElement :
            requiredArray(root, "groups")
        ) {
            JsonObject groupObject =
                groupElement.getAsJsonObject();
            String groupId = requiredString(
                groupObject,
                "id"
            );

            loadedGroups.add(
                new StorageGroup(
                    groupId,
                    requiredString(groupObject, "name"),
                    StorageColor.fromId(
                        requiredString(groupObject, "color")
                    ),
                    optionalBoolean(
                        groupObject,
                        "enabled",
                        true
                    ),
                    StorageItemFilter.empty()
                )
            );

            for (
                JsonElement markerElement :
                requiredArray(groupObject, "markers")
            ) {
                loadedMarkers.add(
                    readLegacyMarker(
                        groupId,
                        markerElement.getAsJsonObject()
                    )
                );
            }
        }

        return new LoadedRegistry(
            List.copyOf(loadedGroups),
            List.copyOf(loadedMarkers),
            true
        );
    }

    private static void write(
        List<StorageGroup> groups,
        List<StorageMarker> markers
    ) {
        Path path = configPath();
        Path temporary = path.resolveSibling(
            path.getFileName() + ".tmp"
        );

        try {
            Files.createDirectories(path.getParent());

            JsonObject root = new JsonObject();
            root.addProperty(
                "schemaVersion",
                SCHEMA_VERSION
            );

            JsonArray groupArray = new JsonArray();

            for (StorageGroup group : groups) {
                groupArray.add(writeGroup(group));
            }

            JsonArray markerArray = new JsonArray();

            for (StorageMarker marker : markers) {
                markerArray.add(writeMarker(marker));
            }

            root.add("groups", groupArray);
            root.add("markers", markerArray);

            try (
                Writer writer = Files.newBufferedWriter(
                    temporary,
                    StandardCharsets.UTF_8
                )
            ) {
                GSON.toJson(root, writer);
            }

            moveTemporary(temporary, path);
        } catch (
            IOException
                | RuntimeException exception
        ) {
            Karakuri.LOGGER.error(
                "Failed to save storage registry {}",
                path,
                exception
            );
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
            }
        }
    }

    private static StorageGroup readCurrentGroup(
        JsonObject object
    ) {
        return new StorageGroup(
            requiredString(object, "id"),
            requiredString(object, "name"),
            StorageColor.fromId(
                requiredString(object, "color")
            ),
            optionalBoolean(object, "enabled", true),
            readItemFilter(
                requiredObject(object, "itemFilter")
            )
        );
    }

    private static StorageGroup readPreviousGroup(
        JsonObject object
    ) {
        return new StorageGroup(
            requiredString(object, "id"),
            requiredString(object, "name"),
            StorageColor.fromId(
                requiredString(object, "color")
            ),
            optionalBoolean(object, "enabled", true),
            StorageItemFilter.empty()
        );
    }

    private static StorageItemFilter readItemFilter(
        JsonObject object
    ) {
        List<String> itemIds = new ArrayList<>();

        for (
            JsonElement element :
            requiredArray(object, "itemIds")
        ) {
            if (
                !element.isJsonPrimitive()
                    || !element
                        .getAsJsonPrimitive()
                        .isString()
            ) {
                throw new IllegalArgumentException(
                    "Storage item filter ID must be a string"
                );
            }

            itemIds.add(element.getAsString());
        }

        return new StorageItemFilter(itemIds);
    }

    private static StorageMarker readCurrentMarker(
        JsonObject object
    ) {
        JsonArray groupIdArray =
            requiredArray(object, "groupIds");
        List<String> groupIds = new ArrayList<>();

        for (JsonElement element : groupIdArray) {
            if (
                !element.isJsonPrimitive()
                    || !element
                        .getAsJsonPrimitive()
                        .isString()
            ) {
                throw new IllegalArgumentException(
                    "Storage marker group ID must be a string"
                );
            }

            groupIds.add(element.getAsString());
        }

        return new StorageMarker(
            requiredString(object, "id"),
            groupIds,
            requiredString(object, "name"),
            requiredString(object, "worldId"),
            requiredString(object, "dimensionId"),
            requiredInt(object, "x"),
            requiredInt(object, "y"),
            requiredInt(object, "z"),
            requiredString(object, "blockId"),
            optionalBoolean(object, "enabled", true),
            optionalInt(
                object,
                "priority",
                StorageMarker.DEFAULT_PRIORITY
            )
        );
    }

    private static StorageMarker readLegacyMarker(
        String groupId,
        JsonObject object
    ) {
        return new StorageMarker(
            requiredString(object, "id"),
            List.of(groupId),
            requiredString(object, "name"),
            requiredString(object, "worldId"),
            requiredString(object, "dimensionId"),
            requiredInt(object, "x"),
            requiredInt(object, "y"),
            requiredInt(object, "z"),
            requiredString(object, "blockId"),
            optionalBoolean(object, "enabled", true),
            optionalInt(
                object,
                "priority",
                StorageMarker.DEFAULT_PRIORITY
            )
        );
    }

    private static JsonObject writeGroup(
        StorageGroup group
    ) {
        JsonObject object = new JsonObject();
        object.addProperty("id", group.id());
        object.addProperty("name", group.name());
        object.addProperty("color", group.color().id());
        object.addProperty("enabled", group.enabled());

        JsonObject itemFilterObject = new JsonObject();
        JsonArray itemIdArray = new JsonArray();

        for (String itemId : group.itemFilter().itemIds()) {
            itemIdArray.add(itemId);
        }

        itemFilterObject.add("itemIds", itemIdArray);
        object.add("itemFilter", itemFilterObject);
        return object;
    }

    private static JsonObject writeMarker(
        StorageMarker marker
    ) {
        JsonObject object = new JsonObject();
        object.addProperty("id", marker.id());

        JsonArray groupIdArray = new JsonArray();

        for (String groupId : marker.groupIds()) {
            groupIdArray.add(groupId);
        }

        object.add("groupIds", groupIdArray);
        object.addProperty("name", marker.name());
        object.addProperty("worldId", marker.worldId());
        object.addProperty(
            "dimensionId",
            marker.dimensionId()
        );
        object.addProperty("x", marker.x());
        object.addProperty("y", marker.y());
        object.addProperty("z", marker.z());
        object.addProperty("blockId", marker.blockId());
        object.addProperty("enabled", marker.enabled());
        object.addProperty("priority", marker.priority());
        return object;
    }

    private static void validate(
        List<StorageGroup> groups,
        List<StorageMarker> markers
    ) {
        if (groups == null) {
            throw new IllegalArgumentException(
                "Storage groups must not be null"
            );
        }

        if (markers == null) {
            throw new IllegalArgumentException(
                "Storage markers must not be null"
            );
        }

        Set<String> groupIds = new HashSet<>();
        Set<String> groupNames = new HashSet<>();

        for (StorageGroup group : groups) {
            if (group == null) {
                throw new IllegalArgumentException(
                    "Storage groups must not contain null"
                );
            }

            if (!groupIds.add(group.id())) {
                throw new IllegalArgumentException(
                    "Duplicate storage group ID: " + group.id()
                );
            }

            String normalizedName =
                group.name().toLowerCase(Locale.ROOT);

            if (!groupNames.add(normalizedName)) {
                throw new IllegalArgumentException(
                    "Duplicate storage group name: "
                        + group.name()
                );
            }
        }

        Set<String> markerIds = new HashSet<>();
        Set<String> markerPositions = new HashSet<>();

        for (StorageMarker marker : markers) {
            if (marker == null) {
                throw new IllegalArgumentException(
                    "Storage markers must not contain null"
                );
            }

            if (!markerIds.add(marker.id())) {
                throw new IllegalArgumentException(
                    "Duplicate storage marker ID: "
                        + marker.id()
                );
            }

            for (String groupId : marker.groupIds()) {
                if (!groupIds.contains(groupId)) {
                    throw new IllegalArgumentException(
                        "Storage marker references an unknown group: "
                            + groupId
                    );
                }
            }

            String positionKey = marker.worldId()
                + "\n"
                + marker.dimensionId()
                + "\n"
                + marker.x()
                + "\n"
                + marker.y()
                + "\n"
                + marker.z();

            if (!markerPositions.add(positionKey)) {
                throw new IllegalArgumentException(
                    "Duplicate storage marker position: "
                        + marker.position()
                );
            }
        }
    }

    private static StorageGroup requireGroup(
        String groupId
    ) {
        StorageGroup group = findGroup(groupId);

        if (group == null) {
            throw new IllegalArgumentException(
                "Unknown storage group ID: " + groupId
            );
        }

        return group;
    }

    private static StorageMarker requireMarker(
        String markerId
    ) {
        StorageMarker marker = findMarker(markerId);

        if (marker == null) {
            throw new IllegalArgumentException(
                "Unknown storage marker ID: " + markerId
            );
        }

        return marker;
    }

    private static int groupIndex(
        String groupId
    ) {
        for (int index = 0; index < groups.size(); index++) {
            if (groups.get(index).id().equals(groupId)) {
                return index;
            }
        }

        throw new IllegalArgumentException(
            "Unknown storage group ID: " + groupId
        );
    }

    private static int markerIndex(
        String markerId
    ) {
        for (int index = 0; index < markers.size(); index++) {
            if (markers.get(index).id().equals(markerId)) {
                return index;
            }
        }

        throw new IllegalArgumentException(
            "Unknown storage marker ID: " + markerId
        );
    }

    private static boolean containsGroupName(
        String name,
        String excludedId
    ) {
        if (name == null || name.isBlank()) {
            return false;
        }

        String normalized = name.trim();

        for (StorageGroup group : groups) {
            if (
                !group.id().equals(excludedId)
                    && group.name()
                        .equalsIgnoreCase(normalized)
            ) {
                return true;
            }
        }

        return false;
    }

    private static String requiredString(
        JsonObject object,
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
            throw new IllegalArgumentException(
                "Missing string property: " + key
            );
        }

        return element.getAsString();
    }

    private static int requiredInt(
        JsonObject object,
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
            throw new IllegalArgumentException(
                "Missing integer property: " + key
            );
        }

        return element.getAsInt();
    }

    private static int optionalInt(
        JsonObject object,
        String key,
        int fallback
    ) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull()
            ? fallback
            : element.getAsInt();
    }

    private static boolean optionalBoolean(
        JsonObject object,
        String key,
        boolean fallback
    ) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull()
            ? fallback
            : element.getAsBoolean();
    }

    private static JsonObject requiredObject(
        JsonObject object,
        String key
    ) {
        JsonElement element = object.get(key);

        if (element == null || !element.isJsonObject()) {
            throw new IllegalArgumentException(
                "Missing object property: " + key
            );
        }

        return element.getAsJsonObject();
    }

    private static JsonArray requiredArray(
        JsonObject object,
        String key
    ) {
        JsonElement element = object.get(key);

        if (element == null || !element.isJsonArray()) {
            throw new IllegalArgumentException(
                "Missing array property: " + key
            );
        }

        return element.getAsJsonArray();
    }

    private static Path configPath() {
        return FabricLoader
            .getInstance()
            .getConfigDir()
            .resolve(Karakuri.MOD_ID)
            .resolve("storage.json");
    }

    private static void moveTemporary(
        Path temporary,
        Path destination
    ) throws IOException {
        try {
            Files.move(
                temporary,
                destination,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (
            AtomicMoveNotSupportedException exception
        ) {
            Files.move(
                temporary,
                destination,
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private record LoadedRegistry(
        List<StorageGroup> groups,
        List<StorageMarker> markers,
        boolean migrated
    ) {
    }
}