package hanamuramiyu.karakuri.task.action;

import hanamuramiyu.karakuri.scenario.model.MoveMode;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.task.ClientTask;
import hanamuramiyu.karakuri.task.input.InputControl;
import hanamuramiyu.karakuri.task.input.InputOwnershipManager;
import net.minecraft.client.Minecraft;

public final class MoveTask implements ClientTask {
    private final MoveStep step;

    private int remainingTicks;
    private boolean finished;

    public MoveTask(
        MoveStep step
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
        InputOwnershipManager.setDown(
            directionControl(),
            client,
            pressed
        );

        if (
            step.mode()
                == MoveMode.SPRINT
        ) {
            InputOwnershipManager.setDown(
                InputControl.SPRINT,
                client,
                pressed
            );
        }

        if (
            step.mode()
                == MoveMode.SNEAK
        ) {
            InputOwnershipManager.setDown(
                InputControl.SNEAK,
                client,
                pressed
            );
        }

        if (step.jumping()) {
            InputOwnershipManager.setDown(
                InputControl.JUMP,
                client,
                pressed
            );
        }
    }

    private InputControl directionControl() {
        return switch (step.direction()) {
            case FORWARD -> InputControl.FORWARD;
            case BACKWARD -> InputControl.BACKWARD;
            case LEFT -> InputControl.LEFT;
            case RIGHT -> InputControl.RIGHT;
        };
    }
}