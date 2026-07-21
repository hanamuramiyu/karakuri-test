package hanamuramiyu.karakuri.scenario;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import hanamuramiyu.karakuri.Karakuri;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public final class ScenarioRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final String FILE_EXTENSION = ".karakuri";

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static final Path CONFIG_DIRECTORY = FabricLoader.getInstance()
        .getConfigDir()
        .resolve(Karakuri.MOD_ID);

    private static final Path SCENARIO_DIRECTORY = CONFIG_DIRECTORY
        .resolve("scenarios");

    private static final Path LEGACY_FILE_PATH = CONFIG_DIRECTORY
        .resolve("scenarios.json");

    private ScenarioRepository() {
    }

    public static List<Scenario> load() {
        try {
            Files.createDirectories(SCENARIO_DIRECTORY);

            List<Path> scenarioFiles = listScenarioFiles();

            if (scenarioFiles.isEmpty() && Files.exists(LEGACY_FILE_PATH)) {
                List<Scenario> migratedScenarios = loadLegacyScenarios();

                if (
                    !migratedScenarios.isEmpty()
                        && writeScenarioFiles(migratedScenarios)
                ) {
                    backupLegacyFile();
                    scenarioFiles = listScenarioFiles();
                }
            }

            if (scenarioFiles.isEmpty()) {
                List<Scenario> defaultScenarios = createDefaultScenarios();
                writeScenarioFiles(defaultScenarios);
                return defaultScenarios;
            }

            List<Scenario> scenarios = new ArrayList<>();

            for (Path scenarioFile : scenarioFiles) {
                try {
                    scenarios.add(readScenarioFile(scenarioFile));
                } catch (IOException | RuntimeException exception) {
                    Karakuri.LOGGER.error(
                        "Failed to load scenario from {}",
                        scenarioFile,
                        exception
                    );
                }
            }

            if (scenarios.isEmpty()) {
                Karakuri.LOGGER.error(
                    "No valid Karakuri scenario files were found in {}",
                    SCENARIO_DIRECTORY
                );

                return createDefaultScenarios();
            }

            return List.copyOf(scenarios);
        } catch (IOException exception) {
            Karakuri.LOGGER.error(
                "Failed to access scenario directory {}",
                SCENARIO_DIRECTORY,
                exception
            );

            return createDefaultScenarios();
        }
    }

    public static void save(List<Scenario> scenarios) {
        if (scenarios == null || scenarios.isEmpty()) {
            throw new IllegalArgumentException(
                "Scenario list must not be empty"
            );
        }

        writeScenarioFiles(scenarios);
    }

    private static boolean writeScenarioFiles(
        List<Scenario> scenarios
    ) {
        try {
            Files.createDirectories(SCENARIO_DIRECTORY);

            Set<String> usedFileNames = new HashSet<>();
            Set<Path> expectedFiles = new HashSet<>();

            for (Scenario scenario : scenarios) {
                String fileName = createUniqueFileName(
                    scenario.name(),
                    usedFileNames
                );

                Path scenarioFile = SCENARIO_DIRECTORY.resolve(fileName);
                writeScenarioFile(scenarioFile, scenario);
                expectedFiles.add(
                    scenarioFile.toAbsolutePath().normalize()
                );
            }

            for (Path existingFile : listScenarioFiles()) {
                Path normalizedFile = existingFile
                    .toAbsolutePath()
                    .normalize();

                if (!expectedFiles.contains(normalizedFile)) {
                    Files.deleteIfExists(existingFile);
                }
            }

            return true;
        } catch (IOException | RuntimeException exception) {
            Karakuri.LOGGER.error(
                "Failed to save scenarios to {}",
                SCENARIO_DIRECTORY,
                exception
            );

            return false;
        }
    }

    private static Scenario readScenarioFile(
        Path path
    ) throws IOException {
        try (
            Reader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
            )
        ) {
            JsonElement rootElement = JsonParser.parseReader(reader);

            if (!rootElement.isJsonObject()) {
                throw new JsonParseException(
                    "Scenario file root must be an object"
                );
            }

            JsonObject root = rootElement.getAsJsonObject();
            int schemaVersion = getRequiredInt(
                root,
                "schemaVersion"
            );

            if (schemaVersion != SCHEMA_VERSION) {
                throw new JsonParseException(
                    "Unsupported scenario schema version: "
                        + schemaVersion
                );
            }

            return parseScenario(root);
        }
    }

    private static void writeScenarioFile(
        Path path,
        Scenario scenario
    ) throws IOException {
        JsonObject root = serializeScenario(scenario);
        Path temporaryPath = path.resolveSibling(
            path.getFileName() + ".tmp"
        );

        try {
            try (
                Writer writer = Files.newBufferedWriter(
                    temporaryPath,
                    StandardCharsets.UTF_8
                )
            ) {
                GSON.toJson(root, writer);
            }

            moveTemporaryFile(temporaryPath, path);
        } finally {
            Files.deleteIfExists(temporaryPath);
        }
    }

    private static List<Scenario> loadLegacyScenarios() {
        try (
            Reader reader = Files.newBufferedReader(
                LEGACY_FILE_PATH,
                StandardCharsets.UTF_8
            )
        ) {
            JsonElement rootElement = JsonParser.parseReader(reader);

            if (!rootElement.isJsonObject()) {
                throw new JsonParseException(
                    "Legacy scenario file root must be an object"
                );
            }

            JsonObject root = rootElement.getAsJsonObject();
            int schemaVersion = getRequiredInt(
                root,
                "schemaVersion"
            );

            if (schemaVersion != SCHEMA_VERSION) {
                throw new JsonParseException(
                    "Unsupported legacy scenario schema version: "
                        + schemaVersion
                );
            }

            JsonArray scenarioArray = getRequiredArray(
                root,
                "scenarios"
            );

            List<Scenario> scenarios = new ArrayList<>();

            for (JsonElement scenarioElement : scenarioArray) {
                scenarios.add(parseScenario(scenarioElement));
            }

            return List.copyOf(scenarios);
        } catch (IOException | RuntimeException exception) {
            Karakuri.LOGGER.error(
                "Failed to migrate legacy scenarios from {}",
                LEGACY_FILE_PATH,
                exception
            );

            return List.of();
        }
    }

    private static void backupLegacyFile() {
        try {
            Path backupPath = findAvailableBackupPath();

            Files.move(
                LEGACY_FILE_PATH,
                backupPath,
                StandardCopyOption.REPLACE_EXISTING
            );

            Karakuri.LOGGER.info(
                "Migrated legacy scenario file to {}",
                backupPath
            );
        } catch (IOException exception) {
            Karakuri.LOGGER.error(
                "Failed to back up legacy scenario file {}",
                LEGACY_FILE_PATH,
                exception
            );
        }
    }

    private static Path findAvailableBackupPath() {
        Path backupPath = CONFIG_DIRECTORY.resolve(
            "scenarios.json.bak"
        );

        int suffix = 2;

        while (Files.exists(backupPath)) {
            backupPath = CONFIG_DIRECTORY.resolve(
                "scenarios.json.bak." + suffix
            );
            suffix++;
        }

        return backupPath;
    }

    private static List<Path> listScenarioFiles()
        throws IOException {
        if (Files.notExists(SCENARIO_DIRECTORY)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(SCENARIO_DIRECTORY)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(
                    path -> path
                        .getFileName()
                        .toString()
                        .endsWith(FILE_EXTENSION)
                )
                .sorted(
                    Comparator.comparing(
                        path -> path.getFileName().toString()
                    )
                )
                .toList();
        }
    }

    private static Scenario parseScenario(JsonElement element) {
        if (!element.isJsonObject()) {
            throw new JsonParseException(
                "Scenario must be an object"
            );
        }

        JsonObject object = element.getAsJsonObject();
        String name = getRequiredString(object, "name");
        JsonArray stepArray = getRequiredArray(object, "steps");
        List<Scenario.Step> steps = new ArrayList<>();

        for (JsonElement stepElement : stepArray) {
            steps.add(parseStep(stepElement));
        }

        return new Scenario(name, steps);
    }

    private static Scenario.Step parseStep(JsonElement element) {
        if (!element.isJsonObject()) {
            throw new JsonParseException(
                "Scenario step must be an object"
            );
        }

        JsonObject object = element.getAsJsonObject();
        String type = getRequiredString(object, "type");
        int durationTicks = getRequiredInt(
            object,
            "durationTicks"
        );

        return switch (type) {
            case "move" -> new Scenario.MoveStep(
                Scenario.MoveDirection.fromId(
                    getRequiredString(object, "direction")
                ),
                durationTicks
            );
            case "walk_forward" -> new Scenario.MoveStep(
                Scenario.MoveDirection.FORWARD,
                durationTicks
            );
            case "wait" -> new Scenario.WaitStep(durationTicks);
            default -> throw new JsonParseException(
                "Unknown scenario step type: " + type
            );
        };
    }

    private static JsonObject serializeScenario(
        Scenario scenario
    ) {
        JsonObject object = new JsonObject();
        object.addProperty("schemaVersion", SCHEMA_VERSION);
        object.addProperty("name", scenario.name());

        JsonArray stepArray = new JsonArray();

        for (Scenario.Step step : scenario.steps()) {
            stepArray.add(serializeStep(step));
        }

        object.add("steps", stepArray);
        return object;
    }

    private static JsonObject serializeStep(
        Scenario.Step step
    ) {
        JsonObject object = new JsonObject();

        switch (step) {
            case Scenario.MoveStep moveStep -> {
                object.addProperty("type", "move");
                object.addProperty(
                    "direction",
                    moveStep.direction().id()
                );
                object.addProperty(
                    "durationTicks",
                    moveStep.durationTicks()
                );
            }
            case Scenario.WaitStep waitStep -> {
                object.addProperty("type", "wait");
                object.addProperty(
                    "durationTicks",
                    waitStep.durationTicks()
                );
            }
        }

        return object;
    }

    private static String createUniqueFileName(
        String scenarioName,
        Set<String> usedFileNames
    ) {
        String baseName = createFileBaseName(scenarioName);
        String fileName = baseName + FILE_EXTENSION;
        int suffix = 2;

        while (!usedFileNames.add(fileName)) {
            fileName = baseName
                + "-"
                + suffix
                + FILE_EXTENSION;
            suffix++;
        }

        return fileName;
    }

    private static String createFileBaseName(
        String scenarioName
    ) {
        String normalizedName = Normalizer.normalize(
            scenarioName,
            Normalizer.Form.NFKC
        );

        String fileName = normalizedName
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^\\p{L}\\p{N}]+", "-")
            .replaceAll("^-+|-+$", "");

        if (fileName.isBlank()) {
            return "scenario";
        }

        return fileName;
    }

    private static String getRequiredString(
        JsonObject object,
        String key
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isString()
        ) {
            throw new JsonParseException(
                "Missing string property: " + key
            );
        }

        return element.getAsString();
    }

    private static int getRequiredInt(
        JsonObject object,
        String key
    ) {
        JsonElement element = object.get(key);

        if (
            element == null
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isNumber()
        ) {
            throw new JsonParseException(
                "Missing integer property: " + key
            );
        }

        return element.getAsInt();
    }

    private static JsonArray getRequiredArray(
        JsonObject object,
        String key
    ) {
        JsonElement element = object.get(key);

        if (element == null || !element.isJsonArray()) {
            throw new JsonParseException(
                "Missing array property: " + key
            );
        }

        return element.getAsJsonArray();
    }

    private static List<Scenario> createDefaultScenarios() {
        return List.of(
            new Scenario(
                "Basic Movement",
                List.of(
                    new Scenario.MoveStep(
                        Scenario.MoveDirection.FORWARD,
                        40
                    ),
                    new Scenario.WaitStep(20),
                    new Scenario.MoveStep(
                        Scenario.MoveDirection.FORWARD,
                        40
                    )
                )
            )
        );
    }

    private static void moveTemporaryFile(
        Path temporaryPath,
        Path destinationPath
    ) throws IOException {
        try {
            Files.move(
                temporaryPath,
                destinationPath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                temporaryPath,
                destinationPath,
                StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}