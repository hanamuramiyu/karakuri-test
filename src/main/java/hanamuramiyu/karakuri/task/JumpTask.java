package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.JumpStopMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class JumpTask implements ClientTask {
    private static final int SINGLE_PRESS_TICKS = 2;
    private static final int REPEAT_PRESS_TICKS = 1;
    private static final int FAILED_PRESS_RETRY_TICKS = 4;

    private final JumpStep step;

    private int elapsedTicks;
    private int completedJumps;
    private int pressTicksRemaining;
    private int waitingForAirborneTicks;

    private boolean started;
    private boolean finished;
    private boolean jumpKeyDown;
    private boolean waitingForAirborne;
    private boolean waitingForLanding;

    public JumpTask(
        JumpStep step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Jump step must not be null"
            );
        }

        this.step = step;
    }

    @Override
    public void start(
        Minecraft client
    ) {
        if (started || finished) {
            return;
        }

        LocalPlayer player = client.player;

        if (
            player == null
                || client.level == null
        ) {
            finished = true;
            return;
        }

        started = true;

        switch (step.mode()) {
            case SINGLE -> {
                pressJump(
                    client,
                    SINGLE_PRESS_TICKS
                );
            }

            case HOLD -> {
                setJumpKeyDown(
                    client,
                    true
                );
            }

            case REPEAT -> {
                if (player.onGround()) {
                    beginRepeatedJump(client);
                } else {
                    waitingForLanding = true;
                }
            }
        }
    }

    @Override
    public void tick(
        Minecraft client
    ) {
        if (!started || finished) {
            return;
        }

        LocalPlayer player = client.player;

        if (
            player == null
                || client.level == null
        ) {
            stop(client);
            return;
        }

        switch (step.mode()) {
            case SINGLE ->
                tickSingle(client);

            case HOLD ->
                tickHold(client);

            case REPEAT ->
                tickRepeat(
                    client,
                    player
                );
        }
    }

    @Override
    public void pause(
        Minecraft client
    ) {
        setJumpKeyDown(
            client,
            false
        );
    }

    @Override
    public void resume(
        Minecraft client
    ) {
        if (finished) {
            return;
        }

        switch (step.mode()) {
            case SINGLE -> {
                pressTicksRemaining = Math.max(
                    1,
                    pressTicksRemaining
                );

                setJumpKeyDown(
                    client,
                    true
                );
            }

            case HOLD -> {
                setJumpKeyDown(
                    client,
                    true
                );
            }

            case REPEAT -> {
                setJumpKeyDown(
                    client,
                    false
                );
            }
        }
    }

    @Override
    public void stop(
        Minecraft client
    ) {
        setJumpKeyDown(
            client,
            false
        );

        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private void tickSingle(
        Minecraft client
    ) {
        elapsedTicks++;

        if (!jumpKeyDown) {
            finish(client);
            return;
        }

        pressTicksRemaining--;

        if (pressTicksRemaining <= 0) {
            finish(client);
        }
    }

    private void tickHold(
        Minecraft client
    ) {
        elapsedTicks++;

        setJumpKeyDown(
            client,
            true
        );

        if (
            step.stopMode()
                == JumpStopMode.DURATION
                && elapsedTicks
                    >= step.durationTicks()
        ) {
            finish(client);
        }
    }

    private void tickRepeat(
        Minecraft client,
        LocalPlayer player
    ) {
        elapsedTicks++;

        if (
            step.stopMode()
                == JumpStopMode.DURATION
                && elapsedTicks
                    >= step.durationTicks()
        ) {
            finish(client);
            return;
        }

        if (jumpKeyDown) {
            pressTicksRemaining--;

            if (pressTicksRemaining <= 0) {
                setJumpKeyDown(
                    client,
                    false
                );
            }

            return;
        }

        if (waitingForAirborne) {
            waitingForAirborneTicks++;

            if (!player.onGround()) {
                completedJumps++;
                waitingForAirborne = false;
                waitingForLanding = true;
                waitingForAirborneTicks = 0;

                if (hasReachedJumpCount()) {
                    finish(client);
                }

                return;
            }

            if (
                waitingForAirborneTicks
                    >= FAILED_PRESS_RETRY_TICKS
            ) {
                waitingForAirborne = false;
                waitingForAirborneTicks = 0;
            }

            return;
        }

        if (waitingForLanding) {
            if (!player.onGround()) {
                return;
            }

            waitingForLanding = false;
        }

        if (hasReachedJumpCount()) {
            finish(client);
            return;
        }

        if (player.onGround()) {
            beginRepeatedJump(client);
        }
    }

    private void beginRepeatedJump(
        Minecraft client
    ) {
        waitingForAirborne = true;
        waitingForLanding = false;
        waitingForAirborneTicks = 0;

        pressJump(
            client,
            REPEAT_PRESS_TICKS
        );
    }

    private void pressJump(
        Minecraft client,
        int pressTicks
    ) {
        pressTicksRemaining = pressTicks;

        setJumpKeyDown(
            client,
            true
        );
    }

    private boolean hasReachedJumpCount() {
        return step.stopMode()
            == JumpStopMode.JUMP_COUNT
            && completedJumps >= step.jumpCount();
    }

    private void finish(
        Minecraft client
    ) {
        setJumpKeyDown(
            client,
            false
        );

        finished = true;
    }

    private void setJumpKeyDown(
        Minecraft client,
        boolean down
    ) {
        client.options
            .keyJump
            .setDown(down);

        jumpKeyDown = down;
    }
}
