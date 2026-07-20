package hanamuramiyu.karakuri.task;

import net.minecraft.client.Minecraft;

public final class TaskManager {
    private static ClientTask currentTask;
    private static TaskStatus status = TaskStatus.IDLE;

    private TaskManager() {
    }

    public static void start(ClientTask task, Minecraft client) {
        stop(client);

        if (client.player == null || client.level == null) {
            return;
        }

        currentTask = task;
        status = TaskStatus.RUNNING;
        currentTask.start(client);

        if (currentTask.isFinished()) {
            finish(client);
        }
    }

    public static void tick(Minecraft client) {
        if (currentTask == null) {
            return;
        }

        if (client.player == null || client.level == null) {
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

    public static void pause(Minecraft client) {
        if (currentTask == null || status != TaskStatus.RUNNING) {
            return;
        }

        currentTask.pause(client);
        status = TaskStatus.PAUSED;
    }

    public static void resume(Minecraft client) {
        if (currentTask == null || status != TaskStatus.PAUSED) {
            return;
        }

        currentTask.resume(client);
        status = TaskStatus.RUNNING;
    }

    public static void stop(Minecraft client) {
        if (currentTask != null) {
            currentTask.stop(client);
        }

        currentTask = null;
        status = TaskStatus.IDLE;
    }

    public static TaskStatus getStatus() {
        return status;
    }

    private static void finish(Minecraft client) {
        currentTask.stop(client);
        currentTask = null;
        status = TaskStatus.IDLE;
    }
}