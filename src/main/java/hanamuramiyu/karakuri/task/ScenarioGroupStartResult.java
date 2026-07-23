package hanamuramiyu.karakuri.task;

import java.util.List;
import java.util.Set;

public record ScenarioGroupStartResult(
    Status status,
    long groupId,
    String groupName,
    List<TaskSessionSnapshot> sessions,
    List<TaskSessionSnapshot> conflicts,
    Set<TaskChannel> conflictingChannels
) {
    public ScenarioGroupStartResult {
        if (status == null) {
            throw new IllegalArgumentException(
                "Group start status must not be null"
            );
        }

        groupName = groupName == null
            ? ""
            : groupName;

        sessions = List.copyOf(sessions);
        conflicts = List.copyOf(conflicts);
        conflictingChannels =
            Set.copyOf(conflictingChannels);
    }

    public static ScenarioGroupStartResult started(
        long groupId,
        String groupName,
        List<TaskSessionSnapshot> sessions
    ) {
        return new ScenarioGroupStartResult(
            Status.STARTED,
            groupId,
            groupName,
            sessions,
            List.of(),
            Set.of()
        );
    }

    public static ScenarioGroupStartResult conflict(
        String groupName,
        List<TaskSessionSnapshot> conflicts,
        Set<TaskChannel> channels
    ) {
        return new ScenarioGroupStartResult(
            Status.CONFLICT,
            0L,
            groupName,
            List.of(),
            conflicts,
            channels
        );
    }

    public static ScenarioGroupStartResult internalConflict(
        String groupName,
        Set<TaskChannel> channels
    ) {
        return new ScenarioGroupStartResult(
            Status.INTERNAL_CONFLICT,
            0L,
            groupName,
            List.of(),
            List.of(),
            channels
        );
    }

    public static ScenarioGroupStartResult unavailable(
        String groupName
    ) {
        return new ScenarioGroupStartResult(
            Status.UNAVAILABLE,
            0L,
            groupName,
            List.of(),
            List.of(),
            Set.of()
        );
    }

    public enum Status {
        STARTED,
        CONFLICT,
        INTERNAL_CONFLICT,
        UNAVAILABLE
    }
}