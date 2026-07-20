package hanamuramiyu.karakuri.task;

import net.minecraft.client.Minecraft;

public final class WalkForwardTask implements ClientTask {
    private int remainingTicks;
    private boolean finished;

    public WalkForwardTask(int durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        remainingTicks = durationTicks;
    }

    @Override
    public void start(Minecraft client) {
        setMoving(client, true);
    }

    @Override
    public void tick(Minecraft client) {
        if (finished) {
            return;
        }

        setMoving(client, true);
        remainingTicks--;

        if (remainingTicks <= 0) {
            finished = true;
            setMoving(client, false);
        }
    }

    @Override
    public void pause(Minecraft client) {
        setMoving(client, false);
    }

    @Override
    public void resume(Minecraft client) {
        if (!finished) {
            setMoving(client, true);
        }
    }

    @Override
    public void stop(Minecraft client) {
        finished = true;
        setMoving(client, false);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private void setMoving(Minecraft client, boolean moving) {
        client.options.keyUp.setDown(moving);
    }
}