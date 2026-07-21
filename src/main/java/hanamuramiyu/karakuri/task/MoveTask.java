package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.Scenario;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class MoveTask implements ClientTask {
    private final Scenario.MoveDirection direction;

    private int remainingTicks;
    private boolean finished;

    public MoveTask(
        Scenario.MoveDirection direction,
        int durationTicks
    ) {
        if (direction == null) {
            throw new IllegalArgumentException(
                "Movement direction must not be null"
            );
        }

        if (durationTicks <= 0) {
            throw new IllegalArgumentException(
                "Duration must be greater than zero"
            );
        }

        this.direction = direction;
        this.remainingTicks = durationTicks;
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
        getMovementKey(client).setDown(moving);
    }

    private KeyMapping getMovementKey(Minecraft client) {
        return switch (direction) {
            case FORWARD -> client.options.keyUp;
            case BACKWARD -> client.options.keyDown;
            case LEFT -> client.options.keyLeft;
            case RIGHT -> client.options.keyRight;
        };
    }
}