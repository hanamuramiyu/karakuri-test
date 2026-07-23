package hanamuramiyu.karakuri.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

final class KarakuriPathOpener {
    private static final long QUICK_FAILURE_TIMEOUT_MILLIS = 350;

    private KarakuriPathOpener() {
    }

    static String open(Path path) {
        Objects.requireNonNull(path, "Path must not be null");

        Path normalizedPath = path
            .toAbsolutePath()
            .normalize();

        if (!Files.isDirectory(normalizedPath)) {
            return "Folder does not exist: " + normalizedPath;
        }

        String osName = System
            .getProperty("os.name", "")
            .toLowerCase(Locale.ROOT);

        for (List<String> command : commandsFor(osName, normalizedPath)) {
            if (start(command)) {
                return null;
            }
        }

        try {
            if (
                Desktop.isDesktopSupported()
                    && Desktop
                        .getDesktop()
                        .isSupported(Desktop.Action.OPEN)
            ) {
                Desktop.getDesktop().open(normalizedPath.toFile());
                return null;
            }
        } catch (
            IOException
                | SecurityException
                | UnsupportedOperationException exception
        ) {
            return failureMessage(normalizedPath, exception);
        }

        return "Could not open folder automatically: " + normalizedPath;
    }

    private static List<List<String>> commandsFor(
        String osName,
        Path path
    ) {
        String value = path.toString();

        if (osName.contains("win")) {
            return List.of(
                List.of("explorer.exe", value)
            );
        }

        if (osName.contains("mac")) {
            return List.of(
                List.of("open", value)
            );
        }

        return List.of(
            List.of("xdg-open", value),
            List.of("gio", "open", value),
            List.of("kde-open5", value),
            List.of("kde-open", value)
        );
    }

    private static boolean start(List<String> command) {
        try {
            Process process = new ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();

            if (
                process.waitFor(
                    QUICK_FAILURE_TIMEOUT_MILLIS,
                    TimeUnit.MILLISECONDS
                )
            ) {
                return process.exitValue() == 0;
            }

            return true;
        } catch (
            IOException
                | SecurityException
                | IllegalArgumentException exception
        ) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static String failureMessage(
        Path path,
        Exception exception
    ) {
        String message = exception.getMessage();

        return message == null || message.isBlank()
            ? "Failed to open folder: " + path
            : "Failed to open folder: " + message;
    }
}