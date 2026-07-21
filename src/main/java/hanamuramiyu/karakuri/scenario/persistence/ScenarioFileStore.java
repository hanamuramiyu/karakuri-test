package hanamuramiyu.karakuri.scenario.persistence;

import hanamuramiyu.karakuri.Karakuri;
import hanamuramiyu.karakuri.scenario.Scenario;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class ScenarioFileStore {
    private final Path configDirectory;
    private final Path scenarioDirectory;
    private final Path legacyFilePath;
    private final ScenarioJsonCodec jsonCodec;

    private ScenarioFileStore(
        Path configDirectory,
        ScenarioJsonCodec jsonCodec
    ) {
        if (configDirectory == null) {
            throw new IllegalArgumentException(
                "Configuration directory must not be null"
            );
        }

        if (jsonCodec == null) {
            throw new IllegalArgumentException(
                "Scenario JSON codec must not be null"
            );
        }

        this.configDirectory =
            configDirectory;

        this.scenarioDirectory =
            configDirectory.resolve(
                "scenarios"
            );

        this.legacyFilePath =
            configDirectory.resolve(
                "scenarios.json"
            );

        this.jsonCodec = jsonCodec;
    }

    public static ScenarioFileStore createDefault() {
        Path configDirectory =
            FabricLoader.getInstance()
                .getConfigDir()
                .resolve(Karakuri.MOD_ID);

        return new ScenarioFileStore(
            configDirectory,
            ScenarioJsonCodec.createDefault()
        );
    }

    public List<Scenario> load() {
        try {
            Files.createDirectories(
                scenarioDirectory
            );

            migrateLegacyFile();

            List<Path> scenarioFiles =
                listScenarioFiles();

            if (scenarioFiles.isEmpty()) {
                return List.of();
            }

            List<Scenario> scenarios =
                new ArrayList<>();

            for (
                Path scenarioFile :
                scenarioFiles
            ) {
                loadScenarioFile(
                    scenarioFile,
                    scenarios
                );
            }

            return List.copyOf(scenarios);
        } catch (IOException exception) {
            Karakuri.LOGGER.error(
                "Failed to access scenario directory {}",
                scenarioDirectory,
                exception
            );

            return List.of();
        }
    }

    public void save(
        List<Scenario> scenarios
    ) {
        if (scenarios == null) {
            throw new IllegalArgumentException(
                "Scenario list must not be null"
            );
        }

        writeScenarioFiles(scenarios);
    }

    private void loadScenarioFile(
        Path scenarioFile,
        List<Scenario> scenarios
    ) {
        try {
            scenarios.add(
                readScenarioFile(
                    scenarioFile
                )
            );
        } catch (
            IOException
                | RuntimeException exception
        ) {
            Karakuri.LOGGER.error(
                "Failed to load scenario from {}",
                scenarioFile,
                exception
            );
        }
    }

    private void migrateLegacyFile()
        throws IOException {
        if (
            Files.notExists(legacyFilePath)
                || !listScenarioFiles().isEmpty()
        ) {
            return;
        }

        List<Scenario> migratedScenarios =
            loadLegacyScenarios();

        if (migratedScenarios == null) {
            return;
        }

        if (
            writeScenarioFiles(
                migratedScenarios
            )
        ) {
            backupLegacyFile();
        }
    }

    private boolean writeScenarioFiles(
        List<Scenario> scenarios
    ) {
        try {
            Files.createDirectories(
                scenarioDirectory
            );

            Set<String> usedFileNames =
                new HashSet<>();

            Set<Path> expectedFiles =
                new HashSet<>();

            for (Scenario scenario : scenarios) {
                Path scenarioFile =
                    resolveScenarioFile(
                        scenario,
                        usedFileNames
                    );

                writeScenarioFile(
                    scenarioFile,
                    scenario
                );

                expectedFiles.add(
                    normalizePath(
                        scenarioFile
                    )
                );
            }

            deleteUnexpectedFiles(
                expectedFiles
            );

            return true;
        } catch (
            IOException
                | RuntimeException exception
        ) {
            Karakuri.LOGGER.error(
                "Failed to save scenarios to {}",
                scenarioDirectory,
                exception
            );

            return false;
        }
    }

    private Path resolveScenarioFile(
        Scenario scenario,
        Set<String> usedFileNames
    ) {
        String fileName =
            ScenarioFileName.createUnique(
                scenario.name(),
                usedFileNames
            );

        return scenarioDirectory.resolve(
            fileName
        );
    }

    private void deleteUnexpectedFiles(
        Set<Path> expectedFiles
    ) throws IOException {
        for (
            Path existingFile :
            listScenarioFiles()
        ) {
            Path normalizedFile =
                normalizePath(existingFile);

            if (
                !expectedFiles.contains(
                    normalizedFile
                )
            ) {
                Files.deleteIfExists(
                    existingFile
                );
            }
        }
    }

    private Scenario readScenarioFile(
        Path path
    ) throws IOException {
        try (
            Reader reader =
                Files.newBufferedReader(
                    path,
                    StandardCharsets.UTF_8
                )
        ) {
            return jsonCodec.readScenario(
                reader
            );
        }
    }

    private void writeScenarioFile(
        Path path,
        Scenario scenario
    ) throws IOException {
        Path temporaryPath =
            createTemporaryPath(path);

        try {
            try (
                Writer writer =
                    Files.newBufferedWriter(
                        temporaryPath,
                        StandardCharsets.UTF_8
                    )
            ) {
                jsonCodec.writeScenario(
                    writer,
                    scenario
                );
            }

            moveTemporaryFile(
                temporaryPath,
                path
            );
        } finally {
            Files.deleteIfExists(
                temporaryPath
            );
        }
    }

    private List<Scenario>
    loadLegacyScenarios() {
        try (
            Reader reader =
                Files.newBufferedReader(
                    legacyFilePath,
                    StandardCharsets.UTF_8
                )
        ) {
            return jsonCodec
                .readLegacyScenarios(reader);
        } catch (
            IOException
                | RuntimeException exception
        ) {
            Karakuri.LOGGER.error(
                "Failed to migrate legacy scenarios from {}",
                legacyFilePath,
                exception
            );

            return null;
        }
    }

    private void backupLegacyFile() {
        try {
            Path backupPath =
                findAvailableBackupPath();

            Files.move(
                legacyFilePath,
                backupPath,
                StandardCopyOption
                    .REPLACE_EXISTING
            );

            Karakuri.LOGGER.info(
                "Migrated legacy scenario file to {}",
                backupPath
            );
        } catch (IOException exception) {
            Karakuri.LOGGER.error(
                "Failed to back up legacy scenario file {}",
                legacyFilePath,
                exception
            );
        }
    }

    private Path findAvailableBackupPath() {
        Path backupPath =
            configDirectory.resolve(
                "scenarios.json.bak"
            );

        int suffix = 2;

        while (Files.exists(backupPath)) {
            backupPath =
                configDirectory.resolve(
                    "scenarios.json.bak."
                        + suffix
                );

            suffix++;
        }

        return backupPath;
    }

    private List<Path> listScenarioFiles()
        throws IOException {
        if (
            Files.notExists(
                scenarioDirectory
            )
        ) {
            return List.of();
        }

        try (
            Stream<Path> paths =
                Files.list(
                    scenarioDirectory
                )
        ) {
            return paths
                .filter(Files::isRegularFile)
                .filter(
                    this::isScenarioFile
                )
                .sorted(
                    Comparator.comparing(
                        path -> path
                            .getFileName()
                            .toString()
                    )
                )
                .toList();
        }
    }

    private boolean isScenarioFile(
        Path path
    ) {
        return path
            .getFileName()
            .toString()
            .endsWith(
                ScenarioFileName.EXTENSION
            );
    }

    private Path createTemporaryPath(
        Path destinationPath
    ) {
        return destinationPath.resolveSibling(
            destinationPath.getFileName()
                + ".tmp"
        );
    }

    private Path normalizePath(
        Path path
    ) {
        return path
            .toAbsolutePath()
            .normalize();
    }

    private void moveTemporaryFile(
        Path temporaryPath,
        Path destinationPath
    ) throws IOException {
        try {
            Files.move(
                temporaryPath,
                destinationPath,
                StandardCopyOption
                    .REPLACE_EXISTING,
                StandardCopyOption
                    .ATOMIC_MOVE
            );
        } catch (
            AtomicMoveNotSupportedException exception
        ) {
            Files.move(
                temporaryPath,
                destinationPath,
                StandardCopyOption
                    .REPLACE_EXISTING
            );
        }
    }
}