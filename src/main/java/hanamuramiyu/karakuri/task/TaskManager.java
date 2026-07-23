package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.composite.RepeatTask;
import hanamuramiyu.karakuri.task.factory.ScenarioTaskFactory;
import hanamuramiyu.karakuri.task.input.InputOwnershipManager;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TaskManager {
    private static final List<Session> SESSIONS =
        new ArrayList<>();

    private static final List<Session> RENDERING_SESSIONS =
        new ArrayList<>();

    private static long nextSessionId = 1L;
    private static long nextGroupId = 1L;
    private static long lastStartedGroupId;
    private static boolean rendering;

    private TaskManager() {
    }

    public static void start(
        ClientTask task,
        Minecraft client
    ) {
        if (task == null) {
            throw new IllegalArgumentException(
                "Task must not be null"
            );
        }

        stop(client);

        if (!isClientReady(client)) {
            return;
        }

        long groupId = nextGroupId++;
        lastStartedGroupId = groupId;

        startSession(
            "editor-preview",
            "Editor Preview",
            groupId,
            "Editor Preview",
            task,
            EnumSet.allOf(TaskChannel.class),
            1,
            true,
            client
        );
    }

    public static ScenarioStartResult startScenario(
        Scenario scenario,
        int repeatCount,
        Minecraft client
    ) {
        if (scenario == null) {
            throw new IllegalArgumentException(
                "Scenario must not be null"
            );
        }

        TaskSessionSnapshot existing =
            findScenarioSessionById(
                scenario.id()
            );

        if (existing != null) {
            return ScenarioStartResult
                .alreadyRunning(existing);
        }

        ScenarioGroupStartResult result =
            startScenarioGroup(
                scenario.name(),
                List.of(scenario),
                repeatCount,
                client
            );

        return switch (result.status()) {
            case STARTED ->
                ScenarioStartResult.started(
                    result.sessions().getFirst()
                );
            case CONFLICT ->
                ScenarioStartResult.conflict(
                    result.conflicts(),
                    result.conflictingChannels()
                );
            case INTERNAL_CONFLICT,
                 UNAVAILABLE ->
                ScenarioStartResult.unavailable();
        };
    }

    public static ScenarioGroupStartResult
    startScenarioGroup(
        String groupName,
        List<Scenario> scenarios,
        int repeatCount,
        Minecraft client
    ) {
        validateGroupName(groupName);
        validateScenarios(scenarios);
        validateRepeatCount(repeatCount);

        if (!isClientReady(client)) {
            return ScenarioGroupStartResult
                .unavailable(groupName);
        }

        EnumSet<TaskChannel> groupChannels =
            EnumSet.noneOf(TaskChannel.class);

        EnumSet<TaskChannel> internalConflicts =
            EnumSet.noneOf(TaskChannel.class);

        Set<String> scenarioIds =
            new HashSet<>();

        boolean duplicateScenarioIds = false;

        for (Scenario scenario : scenarios) {
            Set<TaskChannel> channels =
                ScenarioConflictAnalyzer.channels(
                    scenario
                );

            Set<TaskChannel> overlap =
                ScenarioConflictAnalyzer.conflicts(
                    groupChannels,
                    channels
                );

            internalConflicts.addAll(overlap);
            groupChannels.addAll(channels);

            if (!scenarioIds.add(scenario.id())) {
                duplicateScenarioIds = true;
                internalConflicts.addAll(channels);
            }
        }

        if (
            duplicateScenarioIds
                || !internalConflicts.isEmpty()
        ) {
            return ScenarioGroupStartResult
                .internalConflict(
                    groupName,
                    internalConflicts
                );
        }

        List<TaskSessionSnapshot> conflicts =
            new ArrayList<>();

        EnumSet<TaskChannel> conflictingChannels =
            EnumSet.noneOf(TaskChannel.class);

        for (Session session : SESSIONS) {
            Set<TaskChannel> overlap =
                ScenarioConflictAnalyzer.conflicts(
                    groupChannels,
                    session.channels
                );

            boolean duplicateScenario =
                scenarioIds.contains(
                    session.scenarioId
                );

            if (
                overlap.isEmpty()
                    && !duplicateScenario
            ) {
                continue;
            }

            conflicts.add(session.snapshot());
            conflictingChannels.addAll(overlap);

            if (
                duplicateScenario
                    && overlap.isEmpty()
            ) {
                conflictingChannels.addAll(
                    session.channels
                );
            }
        }

        if (!conflicts.isEmpty()) {
            return ScenarioGroupStartResult.conflict(
                groupName,
                conflicts,
                conflictingChannels
            );
        }

        long groupId = nextGroupId++;
        lastStartedGroupId = groupId;

        List<Session> startedSessions =
            new ArrayList<>();

        List<TaskSessionSnapshot> snapshots =
            new ArrayList<>();

        try {
            for (Scenario scenario : scenarios) {
                Session session = startSession(
                    scenario.id(),
                    scenario.name(),
                    groupId,
                    groupName,
                    new RepeatTask(
                        () -> ScenarioTaskFactory.create(
                            scenario
                        ),
                        repeatCount
                    ),
                    ScenarioConflictAnalyzer.channels(
                        scenario
                    ),
                    repeatCount,
                    false,
                    client
                );

                startedSessions.add(session);
                snapshots.add(session.snapshot());
            }
        } catch (RuntimeException exception) {
            for (Session session : startedSessions) {
                finishSession(session, client);
            }

            throw exception;
        }

        return ScenarioGroupStartResult.started(
            groupId,
            groupName,
            snapshots
        );
    }

    public static void tick(
        Minecraft client
    ) {
        if (SESSIONS.isEmpty()) {
            return;
        }

        if (!isClientReady(client)) {
            stop(client);
            return;
        }

        for (
            Session session :
            List.copyOf(SESSIONS)
        ) {
            if (
                session.status
                    != TaskStatus.RUNNING
            ) {
                continue;
            }

            runForSession(
                session,
                () -> session.task.tick(client)
            );

            if (session.task.isFinished()) {
                finishSession(session, client);
            }
        }
    }

    public static void beginRender(
        Minecraft client,
        float tickProgress
    ) {
        if (
            rendering
                || !isClientReady(client)
                || SESSIONS.isEmpty()
        ) {
            return;
        }

        rendering = true;
        RENDERING_SESSIONS.clear();

        float clampedProgress = Math.clamp(
            tickProgress,
            0.0f,
            1.0f
        );

        try {
            for (Session session : SESSIONS) {
                if (
                    session.status
                        != TaskStatus.RUNNING
                ) {
                    continue;
                }

                runForSession(
                    session,
                    () -> session.task.beginRender(
                        client,
                        clampedProgress
                    )
                );

                RENDERING_SESSIONS.add(session);
            }
        } catch (RuntimeException exception) {
            finishRenderIfNeeded(client);
            throw exception;
        }
    }

    public static void endRender(
        Minecraft client
    ) {
        finishRenderIfNeeded(client);
    }

    public static void pause(
        Minecraft client
    ) {
        pauseSessions(
            SESSIONS.stream()
                .filter(
                    session -> session.status
                        == TaskStatus.RUNNING
                )
                .map(session -> session.id)
                .toList(),
            client
        );
    }

    public static void resume(
        Minecraft client
    ) {
        resumeSessions(
            SESSIONS.stream()
                .filter(
                    session -> session.status
                        == TaskStatus.PAUSED
                )
                .map(session -> session.id)
                .toList(),
            client
        );
    }

    public static void stop(
        Minecraft client
    ) {
        stopSessions(
            SESSIONS.stream()
                .map(session -> session.id)
                .toList(),
            client
        );

        InputOwnershipManager
            .releaseEverything(client);
    }

    public static void pauseSession(
        long sessionId,
        Minecraft client
    ) {
        Session session =
            findSession(sessionId);

        if (
            session == null
                || session.status
                    != TaskStatus.RUNNING
        ) {
            return;
        }

        finishRenderIfNeeded(client);

        runForSession(
            session,
            () -> session.task.pause(client)
        );

        InputOwnershipManager.releaseAll(
            session.id,
            client
        );

        session.status = TaskStatus.PAUSED;
    }

    public static void resumeSession(
        long sessionId,
        Minecraft client
    ) {
        Session session =
            findSession(sessionId);

        if (
            session == null
                || session.status
                    != TaskStatus.PAUSED
                || !isClientReady(client)
        ) {
            return;
        }

        runForSession(
            session,
            () -> session.task.resume(client)
        );

        session.status = TaskStatus.RUNNING;
    }

    public static void stopSession(
        long sessionId,
        Minecraft client
    ) {
        Session session =
            findSession(sessionId);

        if (session == null) {
            return;
        }

        finishRenderIfNeeded(client);
        finishSession(session, client);
    }

    public static void pauseSessions(
        Collection<Long> sessionIds,
        Minecraft client
    ) {
        for (long sessionId : List.copyOf(sessionIds)) {
            pauseSession(sessionId, client);
        }
    }

    public static void resumeSessions(
        Collection<Long> sessionIds,
        Minecraft client
    ) {
        for (long sessionId : List.copyOf(sessionIds)) {
            resumeSession(sessionId, client);
        }
    }

    public static void stopSessions(
        Collection<Long> sessionIds,
        Minecraft client
    ) {
        for (long sessionId : List.copyOf(sessionIds)) {
            stopSession(sessionId, client);
        }
    }

    public static void pauseGroup(
        long groupId,
        Minecraft client
    ) {
        pauseSessions(
            SESSIONS.stream()
                .filter(
                    session -> session.groupId
                        == groupId
                )
                .map(session -> session.id)
                .toList(),
            client
        );
    }

    public static void resumeGroup(
        long groupId,
        Minecraft client
    ) {
        resumeSessions(
            SESSIONS.stream()
                .filter(
                    session -> session.groupId
                        == groupId
                )
                .map(session -> session.id)
                .toList(),
            client
        );
    }

    public static void stopGroup(
        long groupId,
        Minecraft client
    ) {
        stopSessions(
            SESSIONS.stream()
                .filter(
                    session -> session.groupId
                        == groupId
                )
                .map(session -> session.id)
                .toList(),
            client
        );
    }

    public static TaskGroupControlResult
    togglePauseLastGroup(
        Minecraft client
    ) {
        long groupId = lastActiveGroupId();

        if (groupId == 0L) {
            return TaskGroupControlResult.NO_ACTIVE;
        }

        boolean hasRunning =
            SESSIONS.stream().anyMatch(
                session -> session.groupId == groupId
                    && session.status
                        == TaskStatus.RUNNING
            );

        if (hasRunning) {
            pauseGroup(groupId, client);
            return TaskGroupControlResult.PAUSED;
        }

        resumeGroup(groupId, client);
        return TaskGroupControlResult.RESUMED;
    }

    public static TaskGroupControlResult
    stopLastGroup(
        Minecraft client
    ) {
        long groupId = lastActiveGroupId();

        if (groupId == 0L) {
            return TaskGroupControlResult.NO_ACTIVE;
        }

        stopGroup(groupId, client);
        return TaskGroupControlResult.STOPPED;
    }

    public static long lastActiveGroupId() {
        if (
            lastStartedGroupId != 0L
                && SESSIONS.stream().anyMatch(
                    session -> session.groupId
                        == lastStartedGroupId
                )
        ) {
            return lastStartedGroupId;
        }

        return SESSIONS.stream()
            .mapToLong(session -> session.groupId)
            .max()
            .orElse(0L);
    }

    public static List<TaskSessionSnapshot>
    sessionsForGroup(
        long groupId
    ) {
        return SESSIONS.stream()
            .filter(
                session -> session.groupId
                    == groupId
            )
            .map(Session::snapshot)
            .toList();
    }

    public static TaskStatus getStatus() {
        if (SESSIONS.isEmpty()) {
            return TaskStatus.IDLE;
        }

        if (
            SESSIONS.stream().anyMatch(
                session -> session.status
                    == TaskStatus.RUNNING
            )
        ) {
            return TaskStatus.RUNNING;
        }

        return TaskStatus.PAUSED;
    }

    public static List<TaskSessionSnapshot> sessions() {
        return SESSIONS.stream()
            .map(Session::snapshot)
            .toList();
    }

    public static int activeCount() {
        return SESSIONS.size();
    }

    public static int activeGroupCount() {
        return (int) SESSIONS.stream()
            .map(session -> session.groupId)
            .distinct()
            .count();
    }

    public static int runningCount() {
        return (int) SESSIONS.stream()
            .filter(
                session -> session.status
                    == TaskStatus.RUNNING
            )
            .count();
    }

    public static int pausedCount() {
        return (int) SESSIONS.stream()
            .filter(
                session -> session.status
                    == TaskStatus.PAUSED
            )
            .count();
    }

    public static Set<TaskChannel> activeChannels() {
        EnumSet<TaskChannel> channels =
            EnumSet.noneOf(TaskChannel.class);

        for (Session session : SESSIONS) {
            channels.addAll(session.channels);
        }

        return Set.copyOf(channels);
    }

    public static TaskSessionSnapshot findScenarioSession(
        String scenarioName
    ) {
        if (
            scenarioName == null
                || scenarioName.isBlank()
        ) {
            return null;
        }

        for (Session session : SESSIONS) {
            if (
                !session.preview
                    && session.name.equalsIgnoreCase(
                        scenarioName
                    )
            ) {
                return session.snapshot();
            }
        }

        return null;
    }

    public static TaskSessionSnapshot
    findScenarioSessionById(
        String scenarioId
    ) {
        if (
            scenarioId == null
                || scenarioId.isBlank()
        ) {
            return null;
        }

        for (Session session : SESSIONS) {
            if (
                !session.preview
                    && session.scenarioId.equals(
                        scenarioId
                    )
            ) {
                return session.snapshot();
            }
        }

        return null;
    }

    public static boolean isScenarioActive(
        String scenarioId
    ) {
        return findScenarioSessionById(
            scenarioId
        ) != null;
    }

    public static boolean hasActiveSessions() {
        return !SESSIONS.isEmpty();
    }

    private static Session startSession(
        String scenarioId,
        String name,
        long groupId,
        String groupName,
        ClientTask task,
        Set<TaskChannel> channels,
        int repeatCount,
        boolean preview,
        Minecraft client
    ) {
        Session session = new Session(
            nextSessionId++,
            groupId,
            groupName,
            scenarioId,
            name,
            task,
            channels,
            repeatCount,
            preview
        );

        SESSIONS.add(session);

        try {
            runForSession(
                session,
                () -> session.task.start(client)
            );

            if (session.task.isFinished()) {
                finishSession(session, client);
            }
        } catch (RuntimeException exception) {
            finishSession(session, client);
            throw exception;
        }

        return session;
    }

    private static void finishSession(
        Session session,
        Minecraft client
    ) {
        if (!SESSIONS.contains(session)) {
            return;
        }

        try {
            runForSession(
                session,
                () -> session.task.stop(client)
            );
        } finally {
            InputOwnershipManager.releaseAll(
                session.id,
                client
            );

            SESSIONS.remove(session);
        }
    }

    private static void finishRenderIfNeeded(
        Minecraft client
    ) {
        if (!rendering) {
            return;
        }

        try {
            for (
                int index =
                    RENDERING_SESSIONS.size() - 1;
                index >= 0;
                index--
            ) {
                Session session =
                    RENDERING_SESSIONS.get(index);

                runForSession(
                    session,
                    () -> session.task.endRender(
                        client
                    )
                );
            }
        } finally {
            RENDERING_SESSIONS.clear();
            rendering = false;
        }
    }

    private static void runForSession(
        Session session,
        Runnable action
    ) {
        TaskExecutionContext.run(
            session.id,
            action
        );
    }

    private static Session findSession(
        long sessionId
    ) {
        for (Session session : SESSIONS) {
            if (session.id == sessionId) {
                return session;
            }
        }

        return null;
    }

    private static void validateGroupName(
        String groupName
    ) {
        if (
            groupName == null
                || groupName.isBlank()
        ) {
            throw new IllegalArgumentException(
                "Scenario group name must not be blank"
            );
        }
    }

    private static void validateScenarios(
        List<Scenario> scenarios
    ) {
        if (
            scenarios == null
                || scenarios.isEmpty()
        ) {
            throw new IllegalArgumentException(
                "Scenario group must not be empty"
            );
        }

        if (scenarios.stream().anyMatch(
            scenario -> scenario == null
        )) {
            throw new IllegalArgumentException(
                "Scenario group must not contain null"
            );
        }
    }

    private static void validateRepeatCount(
        int repeatCount
    ) {
        if (
            repeatCount == 0
                || repeatCount < RepeatTask.INFINITE
        ) {
            throw new IllegalArgumentException(
                "Repeat count must be positive or infinite"
            );
        }
    }

    private static boolean isClientReady(
        Minecraft client
    ) {
        return client != null
            && client.player != null
            && client.level != null;
    }

    private static final class Session {
        private final long id;
        private final long groupId;
        private final String groupName;
        private final String scenarioId;
        private final String name;
        private final ClientTask task;
        private final Set<TaskChannel> channels;
        private final int repeatCount;
        private final boolean preview;

        private TaskStatus status =
            TaskStatus.RUNNING;

        private Session(
            long id,
            long groupId,
            String groupName,
            String scenarioId,
            String name,
            ClientTask task,
            Set<TaskChannel> channels,
            int repeatCount,
            boolean preview
        ) {
            this.id = id;
            this.groupId = groupId;
            this.groupName = groupName;
            this.scenarioId = scenarioId;
            this.name = name;
            this.task = task;
            this.channels = Set.copyOf(channels);
            this.repeatCount = repeatCount;
            this.preview = preview;
        }

        private TaskSessionSnapshot snapshot() {
            return new TaskSessionSnapshot(
                id,
                groupId,
                groupName,
                scenarioId,
                name,
                status,
                channels,
                repeatCount,
                preview
            );
        }
    }
}