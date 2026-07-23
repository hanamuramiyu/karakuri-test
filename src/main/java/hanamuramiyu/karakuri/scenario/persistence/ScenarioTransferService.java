package hanamuramiyu.karakuri.scenario.persistence;

import hanamuramiyu.karakuri.Karakuri;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class ScenarioTransferService {
    private final Path configDirectory;
    private final Path importDirectory;
    private final Path exportDirectory;
    private final ScenarioJsonCodec jsonCodec;

    private ScenarioTransferService(
        Path configDirectory,
        ScenarioJsonCodec jsonCodec
    ) {
        this.configDirectory = Objects.requireNonNull(
            configDirectory,
            "Configuration directory must not be null"
        );
        this.importDirectory = configDirectory.resolve("import");
        this.exportDirectory = configDirectory.resolve("export");
        this.jsonCodec = Objects.requireNonNull(
            jsonCodec,
            "Scenario JSON codec must not be null"
        );
    }

    public static ScenarioTransferService createDefault() {
        Path configDirectory = FabricLoader
            .getInstance()
            .getConfigDir()
            .resolve(Karakuri.MOD_ID);

        return new ScenarioTransferService(
            configDirectory,
            ScenarioJsonCodec.createDefault()
        );
    }

    public Path configDirectory() {
        return configDirectory;
    }

    public Path importDirectory() {
        return importDirectory;
    }

    public Path exportDirectory() {
        return exportDirectory;
    }

    public void prepareDirectories() throws IOException {
        Files.createDirectories(configDirectory.resolve("scenarios"));
        Files.createDirectories(importDirectory);
        Files.createDirectories(exportDirectory);
    }

    public Path directoryFor(ImportSource source) {
        Objects.requireNonNull(source, "Import source must not be null");

        return switch (source) {
            case IMPORT -> importDirectory;
            case EXPORT -> exportDirectory;
        };
    }

    public List<ImportCandidate> scanImports(
        ImportSource source
    ) throws IOException {
        Objects.requireNonNull(source, "Import source must not be null");
        prepareDirectories();

        return scanDirectory(
            directoryFor(source),
            source
        )
            .stream()
            .sorted(
                Comparator.comparing(
                    ImportCandidate::fileName,
                    String.CASE_INSENSITIVE_ORDER
                )
            )
            .toList();
    }

    public Path exportScenario(Scenario scenario) throws IOException {
        Objects.requireNonNull(
            scenario,
            "Scenario must not be null"
        );

        prepareDirectories();

        Set<String> usedFileNames = new HashSet<>();

        try (Stream<Path> paths = Files.list(exportDirectory)) {
            paths
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .map(name -> name.toLowerCase(Locale.ROOT))
                .forEach(usedFileNames::add);
        }

        String fileName = ScenarioFileName.createUnique(
            scenario.name(),
            usedFileNames
        );
        Path destination = exportDirectory.resolve(fileName);
        writeScenarioFile(destination, scenario);
        return destination;
    }

    private List<ImportCandidate> scanDirectory(
        Path directory,
        ImportSource source
    ) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(this::isScenarioFile)
                .map(path -> readImportCandidate(path, source))
                .toList();
        }
    }

    private ImportCandidate readImportCandidate(
        Path path,
        ImportSource source
    ) {
        try (Reader reader = Files.newBufferedReader(
            path,
            StandardCharsets.UTF_8
        )) {
            return new ImportCandidate(
                path,
                source,
                jsonCodec.readScenario(reader),
                null
            );
        } catch (IOException | RuntimeException exception) {
            Karakuri.LOGGER.error(
                "Failed to inspect scenario import {}",
                path,
                exception
            );

            return new ImportCandidate(
                path,
                source,
                null,
                conciseMessage(exception)
            );
        }
    }

    private void writeScenarioFile(
        Path destination,
        Scenario scenario
    ) throws IOException {
        Path temporaryPath = destination.resolveSibling(
            destination.getFileName() + ".tmp"
        );

        try {
            try (Writer writer = Files.newBufferedWriter(
                temporaryPath,
                StandardCharsets.UTF_8
            )) {
                jsonCodec.writeScenario(writer, scenario);
            }

            moveTemporaryFile(temporaryPath, destination);
        } finally {
            Files.deleteIfExists(temporaryPath);
        }
    }

    private void moveTemporaryFile(
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

    private boolean isScenarioFile(Path path) {
        return path
            .getFileName()
            .toString()
            .toLowerCase(Locale.ROOT)
            .endsWith(ScenarioFileName.EXTENSION);
    }

    private String conciseMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        String singleLine = message
            .replace('\n', ' ')
            .replace('\r', ' ')
            .trim();

        return singleLine.length() <= 120
            ? singleLine
            : singleLine.substring(0, 117) + "...";
    }

    public enum ImportSource {
        IMPORT("import", "Import Folder"),
        EXPORT("export", "Export Folder");

        private final String directoryName;
        private final String label;

        ImportSource(
            String directoryName,
            String label
        ) {
            this.directoryName = directoryName;
            this.label = label;
        }

        public String directoryName() {
            return directoryName;
        }

        public String label() {
            return label;
        }
    }

    public record ImportCandidate(
        Path path,
        ImportSource source,
        Scenario scenario,
        String errorMessage
    ) {
        public ImportCandidate {
            Objects.requireNonNull(path, "Import path must not be null");
            Objects.requireNonNull(source, "Import source must not be null");

            if ((scenario == null) == (errorMessage == null)) {
                throw new IllegalArgumentException(
                    "Import candidate must contain either a scenario or an error"
                );
            }
        }

        public String fileName() {
            return path.getFileName().toString();
        }

        public String locationLabel() {
            return source.directoryName() + "/" + fileName();
        }

        public boolean valid() {
            return scenario != null;
        }
    }
}