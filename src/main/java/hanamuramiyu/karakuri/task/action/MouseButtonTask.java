package hanamuramiyu.karakuri.task.action;

import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MouseInputMode;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MouseStopMode;
import hanamuramiyu.karakuri.task.ClientTask;
import hanamuramiyu.karakuri.task.input.InputControl;
import hanamuramiyu.karakuri.task.input.InputOwnershipManager;
import net.minecraft.client.Minecraft;

public final class MouseButtonTask implements ClientTask {
    private final MouseStep step;

    private int elapsedTicks;
    private int completedClicks;
    private boolean pressed;
    private boolean finished;

    public MouseButtonTask(MouseStep step) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Mouse step must not be null"
            );
        }

        this.step = step;
    }

    @Override
    public void start(Minecraft client) {
        if (
            step.inputMode()
                == MouseInputMode.HOLD
        ) {
            setPressed(client, true);
            return;
        }

        press(client);
    }

    @Override
    public void tick(Minecraft client) {
        if (finished) {
            return;
        }

        elapsedTicks++;

        if (
            step.inputMode()
                == MouseInputMode.HOLD
        ) {
            tickHold(client);
            return;
        }

        tickClick(client);
    }

    @Override
    public void pause(Minecraft client) {
        setPressed(client, false);
    }

    @Override
    public void resume(Minecraft client) {
        if (
            !finished
                && step.inputMode()
                    == MouseInputMode.HOLD
        ) {
            setPressed(client, true);
        }
    }

    @Override
    public void stop(Minecraft client) {
        setPressed(client, false);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private void tickHold(Minecraft client) {
        if (
            step.stopMode()
                == MouseStopMode.MANUAL
        ) {
            setPressed(client, true);
            return;
        }

        if (elapsedTicks >= step.durationTicks()) {
            setPressed(client, false);
            finished = true;
            return;
        }

        setPressed(client, true);
    }

    private void tickClick(Minecraft client) {
        if (pressed) {
            setPressed(client, false);

            if (shouldFinishClicking()) {
                finished = true;
            }

            return;
        }

        if (shouldFinishClicking()) {
            finished = true;
            return;
        }

        int nextPressTick = Math.ceilDiv(
            completedClicks * 40,
            step.clicksPerSecondHalfSteps()
        );

        if (elapsedTicks >= nextPressTick) {
            press(client);
        }
    }

    private boolean shouldFinishClicking() {
        return switch (step.stopMode()) {
            case DURATION ->
                elapsedTicks >= step.durationTicks();
            case CLICK_COUNT ->
                completedClicks >= step.clickCount();
            case MANUAL -> false;
        };
    }

    private void press(Minecraft client) {
        setPressed(client, true);
        completedClicks++;
    }

    private void setPressed(
        Minecraft client,
        boolean value
    ) {
        pressed = value;

        InputOwnershipManager.setDown(
            step.action()
                == MouseAction.LEFT_CLICK
                    ? InputControl.ATTACK
                    : InputControl.USE,
            client,
            value
        );
    }
}