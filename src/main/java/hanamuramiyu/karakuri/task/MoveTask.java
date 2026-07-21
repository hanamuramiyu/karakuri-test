package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.Scenario;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class MoveTask implements ClientTask {
    private final Scenario.MoveStep step;

    private int remainingTicks;
    private boolean finished;

    public MoveTask(
        Scenario.MoveStep step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Movement step must not be null"
            );
        }

        this.step = step;
        remainingTicks = step.durationTicks();
    }

    @Override
    public void start(
        Minecraft client
    ) {
        applyKeys(client, true);
    }

    @Override
    public void tick(
        Minecraft client
    ) {
        if (finished) {
            return;
        }

        if (
            client.player == null
                || client.level == null
        ) {
            stop(client);
            return;
        }

        applyKeys(client, true);
        remainingTicks--;

        if (remainingTicks <= 0) {
            stop(client);
        }
    }

    @Override
    public void pause(
        Minecraft client
    ) {
        applyKeys(client, false);
    }

    @Override
    public void resume(
        Minecraft client
    ) {
        if (!finished) {
            applyKeys(client, true);
        }
    }

    @Override
    public void stop(
        Minecraft client
    ) {
        applyKeys(client, false);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private void applyKeys(
        Minecraft client,
        boolean pressed
    ) {
        getDirectionKey(client)
            .setDown(pressed);

        if (
            step.mode()
                == Scenario.MoveMode.SPRINT
        ) {
            client.options
                .keySprint
                .setDown(pressed);
        }

        if (
            step.mode()
                == Scenario.MoveMode.SNEAK
        ) {
            client.options
                .keyShift
                .setDown(pressed);
        }

        if (step.jumping()) {
            client.options
                .keyJump
                .setDown(pressed);
        }
    }

    private KeyMapping getDirectionKey(
        Minecraft client
    ) {
        return switch (step.direction()) {
            case FORWARD ->
                client.options.keyUp;

            case BACKWARD ->
                client.options.keyDown;

            case LEFT ->
                client.options.keyLeft;

            case RIGHT ->
                client.options.keyRight;
        };
    }
}