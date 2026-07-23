package hanamuramiyu.karakuri.quicklaunch;

import java.util.LinkedHashSet;
import java.util.List;

public record QuickLaunchSlot(
    int number,
    List<String> scenarioIds
) {
    public QuickLaunchSlot {
        if (
            number < 1
                || number > QuickLaunchRegistry.SLOT_COUNT
        ) {
            throw new IllegalArgumentException(
                "Quick slot number is out of bounds: "
                    + number
            );
        }

        if (scenarioIds == null) {
            throw new IllegalArgumentException(
                "Quick slot scenario IDs must not be null"
            );
        }

        LinkedHashSet<String> normalized =
            new LinkedHashSet<>();

        for (String scenarioId : scenarioIds) {
            if (
                scenarioId != null
                    && !scenarioId.isBlank()
            ) {
                normalized.add(
                    scenarioId.trim()
                );
            }
        }

        scenarioIds = List.copyOf(normalized);
    }

    public boolean empty() {
        return scenarioIds.isEmpty();
    }

    public String label() {
        return "Quick Slot " + number;
    }
}