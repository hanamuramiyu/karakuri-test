package hanamuramiyu.karakuri.task;

import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public final class RepeatTask implements ClientTask {
    public static final int INFINITE = -1;

    private final Supplier<ClientTask> taskFactory;
    private final int repeatCount;

    private int completedRepeats;
    private ClientTask currentTask;
    private boolean started;
    private boolean finished;

    public RepeatTask(Supplier<ClientTask> taskFactory, int repeatCount) {
        if (taskFactory == null) {
            throw new IllegalArgumentException("Task factory must not be null");
        }

        if (repeatCount == 0 || repeatCount < INFINITE) {
            throw new IllegalArgumentException("Repeat count must be positive or infinite");
        }

        this.taskFactory = taskFactory;
        this.repeatCount = repeatCount;
    }

    @Override
    public void start(Minecraft client) {
        if (started || finished) {
            return;
        }

        started = true;
        startNextTask(client);
    }

    @Override
    public void tick(Minecraft client) {
        if (!started || finished || currentTask == null) {
            return;
        }

        currentTask.tick(client);

        if (!currentTask.isFinished()) {
            return;
        }

        currentTask.stop(client);
        currentTask = null;
        completedRepeats++;
        startNextTask(client);
    }

    @Override
    public void pause(Minecraft client) {
        if (currentTask != null && !finished) {
            currentTask.pause(client);
        }
    }

    @Override
    public void resume(Minecraft client) {
        if (currentTask != null && !finished) {
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

    private void startNextTask(Minecraft client) {
        while (hasNextRepeat()) {
            currentTask = taskFactory.get();

            if (currentTask == null) {
                throw new IllegalStateException("Task factory returned null");
            }

            currentTask.start(client);

            if (!currentTask.isFinished()) {
                return;
            }

            currentTask.stop(client);
            currentTask = null;
            completedRepeats++;
        }

        finished = true;
    }

    private boolean hasNextRepeat() {
        return repeatCount == INFINITE || completedRepeats < repeatCount;
    }
}