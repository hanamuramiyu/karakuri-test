package hanamuramiyu.karakuri.task;

import net.minecraft.client.Minecraft;

import java.util.List;

public final class SequenceTask implements ClientTask {
    private final List<ClientTask> tasks;

    private int currentIndex;
    private ClientTask currentTask;
    private boolean started;
    private boolean finished;

    public SequenceTask(List<ClientTask> tasks) {
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException(
                "Task sequence must not be empty"
            );
        }

        this.tasks = List.copyOf(tasks);
    }

    @Override
    public void start(Minecraft client) {
        if (started || finished) {
            return;
        }

        started = true;
        startCurrentTask(client);
    }

    @Override
    public void tick(Minecraft client) {
        if (
            !started
                || finished
                || currentTask == null
        ) {
            return;
        }

        currentTask.tick(client);

        if (!currentTask.isFinished()) {
            return;
        }

        currentTask.stop(client);
        currentIndex++;
        startCurrentTask(client);
    }

    @Override
    public void pause(Minecraft client) {
        if (
            currentTask != null
                && !finished
        ) {
            currentTask.pause(client);
        }
    }

    @Override
    public void resume(Minecraft client) {
        if (
            currentTask != null
                && !finished
        ) {
            currentTask.resume(client);
        }
    }

    @Override
    public void stop(Minecraft client) {
        if (currentTask != null) {
            currentTask.stop(client);
        }

        currentTask = null;
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void beginRender(
        Minecraft client,
        float tickProgress
    ) {
        if (
            currentTask != null
                && !finished
        ) {
            currentTask.beginRender(
                client,
                tickProgress
            );
        }
    }

    @Override
    public void endRender(
        Minecraft client
    ) {
        if (
            currentTask != null
                && !finished
        ) {
            currentTask.endRender(client);
        }
    }

    private void startCurrentTask(
        Minecraft client
    ) {
        while (currentIndex < tasks.size()) {
            currentTask =
                tasks.get(currentIndex);

            currentTask.start(client);

            if (!currentTask.isFinished()) {
                return;
            }

            currentTask.stop(client);
            currentIndex++;
        }

        currentTask = null;
        finished = true;
    }
}