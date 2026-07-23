package hanamuramiyu.karakuri.task;

import java.util.Set;

public record TaskSessionSnapshot(
    long id,
    long groupId,
    String groupName,
    String scenarioId,
    String name,
    TaskStatus status,
    Set<TaskChannel> channels,
    int repeatCount,
    boolean preview
) {
    public TaskSessionSnapshot {
        if (groupId <= 0) {
            throw new IllegalArgumentException(
                "Session group ID must be positive"
            );
        }

        if (
            groupName == null
                || groupName.isBlank()
        ) {
            throw new IllegalArgumentException(
                "Session group name must not be blank"
            );
        }

        if (
            scenarioId == null
                || scenarioId.isBlank()
        ) {
            throw new IllegalArgumentException(
                "Session scenario ID must not be blank"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Session name must not be blank"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                "Session status must not be null"
            );
        }

        channels = Set.copyOf(channels);
    }
}