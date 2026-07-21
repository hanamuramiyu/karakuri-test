package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.Scenario;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class MouseButtonTask implements ClientTask {
    private final Scenario.MouseAction action;

    private int remainingTicks;
    private boolean finished;

    public MouseButtonTask(
        Scenario.MouseAction action,
        int durationTicks
    ) {
        if (action == null) {
            throw new IllegalArgumentException(
                "Mouse action must not be null"
            );
        }

        if (durationTicks <= 0) {
            throw new IllegalArgumentException(
                "Duration must be greater than zero"
            );
        }

        this.action = action;
        this.remainingTicks = durationTicks;
    }

    @Override
    public void start(Minecraft client) {
        setPressed(client, true);
    }

    @Override
    public void tick(Minecraft client) {
        if (finished) {
            return;
        }

        setPressed(client, true);
        remainingTicks--;

        if (remainingTicks <= 0) {
            finished = true;
            setPressed(client, false);
        }
    }

    @Override
    public void pause(Minecraft client) {
        setPressed(client, false);
    }

    @Override
    public void resume(Minecraft client) {
        if (!finished) {
            setPressed(client, true);
        }
    }

    @Override
    public void stop(Minecraft client) {
        finished = true;
        setPressed(client, false);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private void setPressed(
        Minecraft client,
        boolean pressed
    ) {
        getKey(client).setDown(pressed);
    }

    private KeyMapping getKey(Minecraft client) {
        return switch (action) {
            case LEFT_CLICK -> client.options.keyAttack;
            case RIGHT_CLICK -> client.options.keyUse;
        };
    }
}