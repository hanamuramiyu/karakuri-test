package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.CameraStep;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class CameraTask implements ClientTask {
    private static final float MIN_PITCH = -90.0f;
    private static final float MAX_PITCH = 90.0f;

    private final CameraStep step;

    private float startYaw;
    private float startPitch;
    private float targetYaw;
    private float targetPitch;

    private float previousTickYaw;
    private float previousTickPitch;
    private float currentTickYaw;
    private float currentTickPitch;

    private float savedYaw;
    private float savedPitch;
    private float savedPreviousYaw;
    private float savedPreviousPitch;

    private LocalPlayer renderedPlayer;

    private int elapsedTicks;

    private boolean started;
    private boolean finished;
    private boolean renderOverrideActive;

    public CameraTask(
        CameraStep step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Camera step must not be null"
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

        LocalPlayer player =
            client.player;

        if (player == null) {
            finished = true;
            return;
        }

        started = true;

        startYaw = player.getYRot();
        startPitch = player.getXRot();

        targetYaw = startYaw;
        targetPitch = startPitch;

        switch (step.direction()) {
            case LEFT ->
                targetYaw =
                    startYaw - step.angleDegrees();

            case RIGHT ->
                targetYaw =
                    startYaw + step.angleDegrees();

            case UP ->
                targetPitch = clampPitch(
                    startPitch - step.angleDegrees()
                );

            case DOWN ->
                targetPitch = clampPitch(
                    startPitch + step.angleDegrees()
                );
        }

        previousTickYaw = startYaw;
        previousTickPitch = startPitch;
        currentTickYaw = startYaw;
        currentTickPitch = startPitch;

        if (
            step.motion()
                == CameraMotion.INSTANT
        ) {
            applyInstantRotation(
                player,
                targetYaw,
                targetPitch
            );

            finished = true;
        }
    }

    @Override
    public void tick(
        Minecraft client
    ) {
        if (
            !started
                || finished
                || step.motion()
                    == CameraMotion.INSTANT
        ) {
            return;
        }

        LocalPlayer player =
            client.player;

        if (player == null) {
            finished = true;
            return;
        }

        previousTickYaw =
            currentTickYaw;

        previousTickPitch =
            currentTickPitch;

        elapsedTicks++;

        float progress = Math.min(
            1.0f,
            elapsedTicks
                / (float) step.durationTicks()
        );

        float easedProgress =
            smoothStepSeventhOrder(progress);

        currentTickYaw = interpolate(
            startYaw,
            targetYaw,
            easedProgress
        );

        currentTickPitch = interpolate(
            startPitch,
            targetPitch,
            easedProgress
        );

        applyTickRotation(player);

        if (progress >= 1.0f) {
            previousTickYaw =
                currentTickYaw;

            previousTickPitch =
                currentTickPitch;

            currentTickYaw =
                targetYaw;

            currentTickPitch =
                targetPitch;

            applyTickRotation(player);
            finished = true;
        }
    }

    @Override
    public void pause(
        Minecraft client
    ) {
        restoreRenderOverride();

        LocalPlayer player =
            client.player;

        if (player == null) {
            return;
        }

        previousTickYaw =
            player.getYRot();

        previousTickPitch =
            player.getXRot();

        currentTickYaw =
            player.getYRot();

        currentTickPitch =
            player.getXRot();

        synchronizeRotation(player);
    }

    @Override
    public void resume(
        Minecraft client
    ) {
        LocalPlayer player =
            client.player;

        if (player == null || finished) {
            return;
        }

        synchronizeRotation(player);
    }

    @Override
    public void stop(
        Minecraft client
    ) {
        restoreRenderOverride();

        LocalPlayer player =
            client.player;

        if (player != null) {
            synchronizeRotation(player);
        }

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
            !started
                || finished
                || renderOverrideActive
                || step.motion()
                    != CameraMotion.SMOOTH
        ) {
            return;
        }

        LocalPlayer player =
            client.player;

        if (player == null) {
            return;
        }

        renderedPlayer = player;

        savedYaw =
            player.getYRot();

        savedPitch =
            player.getXRot();

        savedPreviousYaw =
            player.yRotO;

        savedPreviousPitch =
            player.xRotO;

        float renderedYaw = interpolate(
            previousTickYaw,
            currentTickYaw,
            tickProgress
        );

        float renderedPitch = interpolate(
            previousTickPitch,
            currentTickPitch,
            tickProgress
        );

        player.yRotO =
            renderedYaw;

        player.xRotO =
            renderedPitch;

        player.setYRot(
            renderedYaw
        );

        player.setXRot(
            clampPitch(renderedPitch)
        );

        renderOverrideActive = true;
    }

    @Override
    public void endRender(
        Minecraft client
    ) {
        restoreRenderOverride();
    }

    private void applyTickRotation(
        LocalPlayer player
    ) {
        player.yRotO =
            previousTickYaw;

        player.xRotO =
            previousTickPitch;

        player.setYRot(
            currentTickYaw
        );

        player.setXRot(
            clampPitch(currentTickPitch)
        );
    }

    private void applyInstantRotation(
        LocalPlayer player,
        float yaw,
        float pitch
    ) {
        float clampedPitch =
            clampPitch(pitch);

        player.yRotO = yaw;
        player.xRotO = clampedPitch;

        player.setYRot(yaw);
        player.setXRot(clampedPitch);

        previousTickYaw = yaw;
        previousTickPitch = clampedPitch;
        currentTickYaw = yaw;
        currentTickPitch = clampedPitch;
    }

    private void restoreRenderOverride() {
        if (!renderOverrideActive) {
            return;
        }

        LocalPlayer player =
            renderedPlayer;

        if (player != null) {
            player.yRotO =
                savedPreviousYaw;

            player.xRotO =
                savedPreviousPitch;

            player.setYRot(
                savedYaw
            );

            player.setXRot(
                savedPitch
            );
        }

        renderedPlayer = null;
        renderOverrideActive = false;
    }

    private void synchronizeRotation(
        LocalPlayer player
    ) {
        player.yRotO =
            player.getYRot();

        player.xRotO =
            player.getXRot();
    }

    private float smoothStepSeventhOrder(
        float progress
    ) {
        float value = Math.clamp(
            progress,
            0.0f,
            1.0f
        );

        float valueSquared =
            value * value;

        float valueFourth =
            valueSquared * valueSquared;

        return valueFourth
            * (
                35.0f
                    - 84.0f * value
                    + 70.0f * valueSquared
                    - 20.0f
                        * valueSquared
                        * value
            );
    }

    private float interpolate(
        float start,
        float end,
        float progress
    ) {
        return start
            + (end - start) * progress;
    }

    private float clampPitch(
        float pitch
    ) {
        return Math.clamp(
            pitch,
            MIN_PITCH,
            MAX_PITCH
        );
    }
}
