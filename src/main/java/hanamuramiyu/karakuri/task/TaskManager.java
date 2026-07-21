package hanamuramiyu.karakuri.task;

import net.minecraft.client.Minecraft;

public final class TaskManager {
    private static ClientTask currentTask;
    private static TaskStatus status =
        TaskStatus.IDLE;

    private static boolean rendering;

    private TaskManager() {
    }

    public static void start(
        ClientTask task,
        Minecraft client
    ) {
        stop(client);

        if (
            client.player == null
                || client.level == null
        ) {
            return;
        }

        currentTask = task;
        status = TaskStatus.RUNNING;

        currentTask.start(client);

        if (currentTask.isFinished()) {
            finish(client);
        }
    }

    public static void tick(
        Minecraft client
    ) {
        if (currentTask == null) {
            return;
        }

        if (
            client.player == null
                || client.level == null
        ) {
            stop(client);
            return;
        }

        if (status != TaskStatus.RUNNING) {
            return;
        }

        currentTask.tick(client);

        if (currentTask.isFinished()) {
            finish(client);
        }
    }

    public static void beginRender(
        Minecraft client,
        float tickProgress
    ) {
        if (
            currentTask == null
                || status != TaskStatus.RUNNING
                || rendering
                || client.player == null
                || client.level == null
        ) {
            return;
        }

        rendering = true;

        try {
            currentTask.beginRender(
                client,
                Math.clamp(
                    tickProgress,
                    0.0f,
                    1.0f
                )
            );
        } catch (RuntimeException exception) {
            rendering = false;
            throw exception;
        }
    }

    public static void endRender(
        Minecraft client
    ) {
        if (!rendering) {
            return;
        }

        try {
            if (currentTask != null) {
                currentTask.endRender(client);
            }
        } finally {
            rendering = false;
        }
    }

    public static void pause(
        Minecraft client
    ) {
        if (
            currentTask == null
                || status != TaskStatus.RUNNING
        ) {
            return;
        }

        finishRenderIfNeeded(client);

        currentTask.pause(client);
        status = TaskStatus.PAUSED;
    }

    public static void resume(
        Minecraft client
    ) {
        if (
            currentTask == null
                || status != TaskStatus.PAUSED
        ) {
            return;
        }

        currentTask.resume(client);
        status = TaskStatus.RUNNING;
    }

    public static void stop(
        Minecraft client
    ) {
        finishRenderIfNeeded(client);

        if (currentTask != null) {
            currentTask.stop(client);
        }

        currentTask = null;
        status = TaskStatus.IDLE;
    }

    public static TaskStatus getStatus() {
        return status;
    }

    private static void finish(
        Minecraft client
    ) {
        finishRenderIfNeeded(client);

        currentTask.stop(client);
        currentTask = null;
        status = TaskStatus.IDLE;
    }

    private static void finishRenderIfNeeded(
        Minecraft client
    ) {
        if (!rendering) {
            return;
        }

        try {
            if (currentTask != null) {
                currentTask.endRender(client);
            }
        } finally {
            rendering = false;
        }
    }
}