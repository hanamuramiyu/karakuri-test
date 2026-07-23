package hanamuramiyu.karakuri.task;

import java.util.List;
import java.util.Set;

public record ScenarioStartResult(
    Status status,
    TaskSessionSnapshot session,
    List<TaskSessionSnapshot> conflicts,
    Set<TaskChannel> conflictingChannels
) {
    public ScenarioStartResult {
        if (status == null) {
            throw new IllegalArgumentException(
                "Start result status must not be null"
            );
        }

        conflicts = List.copyOf(conflicts);
        conflictingChannels =
            Set.copyOf(conflictingChannels);
    }

    public static ScenarioStartResult started(
        TaskSessionSnapshot session
    ) {
        return new ScenarioStartResult(
            Status.STARTED,
            session,
            List.of(),
            Set.of()
        );
    }

    public static ScenarioStartResult conflict(
        List<TaskSessionSnapshot> conflicts,
        Set<TaskChannel> channels
    ) {
        return new ScenarioStartResult(
            Status.CONFLICT,
            null,
            conflicts,
            channels
        );
    }

    public static ScenarioStartResult alreadyRunning(
        TaskSessionSnapshot session
    ) {
        return new ScenarioStartResult(
            Status.ALREADY_RUNNING,
            session,
            List.of(),
            Set.of()
        );
    }

    public static ScenarioStartResult unavailable() {
        return new ScenarioStartResult(
            Status.UNAVAILABLE,
            null,
            List.of(),
            Set.of()
        );
    }

    public enum Status {
        STARTED,
        CONFLICT,
        ALREADY_RUNNING,
        UNAVAILABLE
    }
}