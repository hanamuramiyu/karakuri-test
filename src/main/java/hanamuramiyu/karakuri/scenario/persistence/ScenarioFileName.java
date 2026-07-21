package hanamuramiyu.karakuri.scenario.persistence;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

final class ScenarioFileName {
    static final String EXTENSION =
        ".karakuri";

    private ScenarioFileName() {
    }

    static String createUnique(
        String scenarioName,
        Set<String> usedFileNames
    ) {
        if (usedFileNames == null) {
            throw new IllegalArgumentException(
                "Used file names must not be null"
            );
        }

        String baseName =
            createBaseName(scenarioName);

        String fileName =
            baseName + EXTENSION;

        int suffix = 2;

        while (!usedFileNames.add(fileName)) {
            fileName =
                baseName
                    + "-"
                    + suffix
                    + EXTENSION;

            suffix++;
        }

        return fileName;
    }

    private static String createBaseName(
        String scenarioName
    ) {
        if (scenarioName == null) {
            throw new IllegalArgumentException(
                "Scenario name must not be null"
            );
        }

        String normalizedName =
            Normalizer.normalize(
                scenarioName,
                Normalizer.Form.NFKC
            );

        String fileName =
            normalizedName
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                    "[^\\p{L}\\p{N}]+",
                    "-"
                )
                .replaceAll(
                    "^-+|-+$",
                    ""
                );

        return fileName.isBlank()
            ? "scenario"
            : fileName;
    }
}