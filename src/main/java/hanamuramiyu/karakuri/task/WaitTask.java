package hanamuramiyu.karakuri.task;

import net.minecraft.client.Minecraft;

public final class WaitTask implements ClientTask {
    private int remainingTicks;
    private boolean finished;

    public WaitTask(int durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        remainingTicks = durationTicks;
    }

    @Override
    public void start(Minecraft client) {
    }

    @Override
    public void tick(Minecraft client) {
        if (finished) {
            return;
        }

        remainingTicks--;

        if (remainingTicks <= 0) {
            finished = true;
        }
    }

    @Override
    public void pause(Minecraft client) {
    }

    @Override
    public void resume(Minecraft client) {
    }

    @Override
    public void stop(Minecraft client) {
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}