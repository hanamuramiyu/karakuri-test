package hanamuramiyu.karakuri.quicklaunch;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.ScenarioConflictAnalyzer;
import hanamuramiyu.karakuri.task.TaskChannel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record QuickLaunchValidation(
    List<Conflict> conflicts,
    Set<TaskChannel> channels
) {
    public QuickLaunchValidation {
        conflicts = List.copyOf(conflicts);
        channels = Set.copyOf(channels);
    }

    public boolean valid() {
        return conflicts.isEmpty();
    }

    public static QuickLaunchValidation analyze(
        List<Scenario> scenarios
    ) {
        if (scenarios == null) {
            throw new IllegalArgumentException(
                "Quick launch scenarios must not be null"
            );
        }

        List<Conflict> conflicts =
            new ArrayList<>();

        EnumSet<TaskChannel> allChannels =
            EnumSet.noneOf(TaskChannel.class);

        for (
            int firstIndex = 0;
            firstIndex < scenarios.size();
            firstIndex++
        ) {
            Scenario first =
                scenarios.get(firstIndex);

            Set<TaskChannel> firstChannels =
                ScenarioConflictAnalyzer.channels(first);

            allChannels.addAll(firstChannels);

            for (
                int secondIndex = firstIndex + 1;
                secondIndex < scenarios.size();
                secondIndex++
            ) {
                Scenario second =
                    scenarios.get(secondIndex);

                Set<TaskChannel> overlap =
                    ScenarioConflictAnalyzer.conflicts(
                        firstChannels,
                        ScenarioConflictAnalyzer.channels(
                            second
                        )
                    );

                if (!overlap.isEmpty()) {
                    conflicts.add(
                        new Conflict(
                            first,
                            second,
                            overlap
                        )
                    );
                }
            }
        }

        return new QuickLaunchValidation(
            conflicts,
            allChannels
        );
    }

    public record Conflict(
        Scenario first,
        Scenario second,
        Set<TaskChannel> channels
    ) {
        public Conflict {
            if (
                first == null
                    || second == null
            ) {
                throw new IllegalArgumentException(
                    "Conflicting scenarios must not be null"
                );
            }

            channels = Set.copyOf(channels);
        }
    }
}