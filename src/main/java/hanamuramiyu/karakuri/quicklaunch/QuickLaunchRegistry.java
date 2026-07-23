package hanamuramiyu.karakuri.quicklaunch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hanamuramiyu.karakuri.Karakuri;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public final class QuickLaunchRegistry {
    public static final int SLOT_COUNT = 6;

    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON =
        new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static List<QuickLaunchSlot> slots =
        emptySlots();

    private static boolean initialized;

    private QuickLaunchRegistry() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        slots = load();
        initialized = true;
    }

    public static synchronized QuickLaunchSlot slot(
        int number
    ) {
        initialize();
        validateNumber(number);
        return slots.get(number - 1);
    }

    public static synchronized List<QuickLaunchSlot> slots() {
        initialize();
        return slots;
    }

    public static synchronized void saveSlot(
        int number,
        Collection<String> scenarioIds
    ) {
        initialize();
        validateNumber(number);

        if (scenarioIds == null) {
            throw new IllegalArgumentException(
                "Quick slot scenario IDs must not be null"
            );
        }

        List<QuickLaunchSlot> updated =
            new ArrayList<>(slots);

        updated.set(
            number - 1,
            new QuickLaunchSlot(
                number,
                List.copyOf(scenarioIds)
            )
        );

        slots = List.copyOf(updated);
        write(slots);
    }

    public static synchronized void clearSlot(
        int number
    ) {
        saveSlot(number, List.of());
    }

    public static List<Scenario> resolveScenarios(
        QuickLaunchSlot slot
    ) {
        if (slot == null) {
            throw new IllegalArgumentException(
                "Quick slot must not be null"
            );
        }

        List<Scenario> resolved =
            new ArrayList<>();

        for (String scenarioId : slot.scenarioIds()) {
            Scenario scenario =
                ScenarioLibrary.findById(
                    scenarioId
                );

            if (scenario != null) {
                resolved.add(scenario);
            }
        }

        return List.copyOf(resolved);
    }

    public static List<String> missingScenarioIds(
        QuickLaunchSlot slot
    ) {
        if (slot == null) {
            throw new IllegalArgumentException(
                "Quick slot must not be null"
            );
        }

        List<String> missing =
            new ArrayList<>();

        for (String scenarioId : slot.scenarioIds()) {
            if (
                ScenarioLibrary.findById(
                    scenarioId
                ) == null
            ) {
                missing.add(scenarioId);
            }
        }

        return List.copyOf(missing);
    }

    public static String summary(
        QuickLaunchSlot slot
    ) {
        List<Scenario> scenarios =
            resolveScenarios(slot);

        int missingCount =
            missingScenarioIds(slot).size();

        if (
            scenarios.isEmpty()
                && missingCount == 0
        ) {
            return "Empty";
        }

        if (
            scenarios.size() == 1
                && missingCount == 0
        ) {
            return scenarios.getFirst().name();
        }

        String summary = scenarios.size()
            + (scenarios.size() == 1
                ? " scenario"
                : " scenarios");

        if (missingCount > 0) {
            summary += " · "
                + missingCount
                + " missing";
        }

        return summary;
    }

    private static List<QuickLaunchSlot> load() {
        Path path = configPath();

        if (Files.notExists(path)) {
            return emptySlots();
        }

        try (
            Reader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
            )
        ) {
            JsonElement rootElement =
                JsonParser.parseReader(reader);

            if (!rootElement.isJsonObject()) {
                throw new IllegalArgumentException(
                    "Quick launch config root must be an object"
                );
            }

            JsonObject root =
                rootElement.getAsJsonObject();

            int schemaVersion =
                requiredInt(
                    root,
                    "schemaVersion"
                );

            if (schemaVersion != SCHEMA_VERSION) {
                throw new IllegalArgumentException(
                    "Unsupported quick launch schema version: "
                        + schemaVersion
                );
            }

            List<QuickLaunchSlot> loaded =
                new ArrayList<>(emptySlots());

            JsonElement slotsElement =
                root.get("slots");

            if (
                slotsElement == null
                    || !slotsElement.isJsonArray()
            ) {
                throw new IllegalArgumentException(
                    "Quick launch slots must be an array"
                );
            }

            for (
                JsonElement slotElement :
                slotsElement.getAsJsonArray()
            ) {
                if (!slotElement.isJsonObject()) {
                    continue;
                }

                JsonObject slotObject =
                    slotElement.getAsJsonObject();

                int number =
                    requiredInt(
                        slotObject,
                        "number"
                    );

                if (
                    number < 1
                        || number > SLOT_COUNT
                ) {
                    continue;
                }

                JsonElement idsElement =
                    slotObject.get("scenarioIds");

                if (
                    idsElement == null
                        || !idsElement.isJsonArray()
                ) {
                    continue;
                }

                LinkedHashSet<String> ids =
                    new LinkedHashSet<>();

                for (
                    JsonElement idElement :
                    idsElement.getAsJsonArray()
                ) {
                    if (
                        idElement.isJsonPrimitive()
                            && idElement
                                .getAsJsonPrimitive()
                                .isString()
                    ) {
                        String id =
                            idElement.getAsString();

                        if (!id.isBlank()) {
                            ids.add(id.trim());
                        }
                    }
                }

                loaded.set(
                    number - 1,
                    new QuickLaunchSlot(
                        number,
                        List.copyOf(ids)
                    )
                );
            }

            return List.copyOf(loaded);
        } catch (
            IOException
                | RuntimeException exception
        ) {
            Karakuri.LOGGER.error(
                "Failed to load quick launch config {}",
                path,
                exception
            );

            return emptySlots();
        }
    }

    private static void write(
        List<QuickLaunchSlot> slots
    ) {
        Path path = configPath();
        Path temporary = path.resolveSibling(
            path.getFileName() + ".tmp"
        );

        try {
            Files.createDirectories(
                path.getParent()
            );

            JsonObject root =
                new JsonObject();

            root.addProperty(
                "schemaVersion",
                SCHEMA_VERSION
            );

            JsonArray slotArray =
                new JsonArray();

            for (QuickLaunchSlot slot : slots) {
                JsonObject slotObject =
                    new JsonObject();

                slotObject.addProperty(
                    "number",
                    slot.number()
                );

                JsonArray ids =
                    new JsonArray();

                for (
                    String scenarioId :
                    slot.scenarioIds()
                ) {
                    ids.add(scenarioId);
                }

                slotObject.add(
                    "scenarioIds",
                    ids
                );

                slotArray.add(slotObject);
            }

            root.add("slots", slotArray);

            try (
                Writer writer =
                    Files.newBufferedWriter(
                        temporary,
                        StandardCharsets.UTF_8
                    )
            ) {
                GSON.toJson(root, writer);
            }

            moveTemporary(
                temporary,
                path
            );
        } catch (
            IOException
                | RuntimeException exception
        ) {
            Karakuri.LOGGER.error(
                "Failed to save quick launch config {}",
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

    private static Path configPath() {
        return FabricLoader
            .getInstance()
            .getConfigDir()
            .resolve(Karakuri.MOD_ID)
            .resolve("quick-launch.json");
    }

    private static List<QuickLaunchSlot> emptySlots() {
        List<QuickLaunchSlot> empty =
            new ArrayList<>();

        for (
            int number = 1;
            number <= SLOT_COUNT;
            number++
        ) {
            empty.add(
                new QuickLaunchSlot(
                    number,
                    List.of()
                )
            );
        }

        return List.copyOf(empty);
    }

    private static void validateNumber(
        int number
    ) {
        if (
            number < 1
                || number > SLOT_COUNT
        ) {
            throw new IllegalArgumentException(
                "Quick slot number is out of bounds: "
                    + number
            );
        }
    }
}