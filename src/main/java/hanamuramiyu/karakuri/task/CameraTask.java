package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.Scenario;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class CameraTask implements ClientTask {
    private final Scenario.CameraStep step;

    private float startYaw;
    private float startPitch;
    private float targetYaw;
    private float targetPitch;

    private int elapsedTicks;
    private boolean started;
    private boolean finished;

    public CameraTask(Scenario.CameraStep step) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Camera step must not be null"
            );
        }

        this.step = step;
    }

    @Override
    public void start(Minecraft client) {
        if (started || finished) {
            return;
        }

        LocalPlayer player = client.player;

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
                targetYaw -= step.angleDegrees();
            case RIGHT ->
                targetYaw += step.angleDegrees();
            case UP ->
                targetPitch = clampPitch(
                    startPitch - step.angleDegrees()
                );
            case DOWN ->
                targetPitch = clampPitch(
                    startPitch + step.angleDegrees()
                );
        }

        if (step.motion() == Scenario.CameraMotion.INSTANT) {
            applyRotation(
                player,
                targetYaw,
                targetPitch
            );

            finished = true;
        }
    }

    @Override
    public void tick(Minecraft client) {
        if (!started || finished) {
            return;
        }

        LocalPlayer player = client.player;

        if (player == null) {
            finished = true;
            return;
        }

        elapsedTicks++;

        float progress = Math.min(
            1.0f,
            elapsedTicks
                / (float) step.durationTicks()
        );

        float easedProgress =
            progress
                * progress
                * (3.0f - 2.0f * progress);

        float yaw = lerp(
            startYaw,
            targetYaw,
            easedProgress
        );

        float pitch = lerp(
            startPitch,
            targetPitch,
            easedProgress
        );

        applyRotation(player, yaw, pitch);

        if (progress >= 1.0f) {
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

    private void applyRotation(
        LocalPlayer player,
        float yaw,
        float pitch
    ) {
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setXRot(clampPitch(pitch));
    }

    private float lerp(
        float start,
        float end,
        float progress
    ) {
        return start + (end - start) * progress;
    }

    private float clampPitch(float pitch) {
        return Math.max(
            -90.0f,
            Math.min(90.0f, pitch)
        );
    }
}