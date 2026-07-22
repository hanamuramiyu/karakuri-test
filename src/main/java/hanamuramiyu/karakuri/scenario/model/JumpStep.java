package hanamuramiyu.karakuri.scenario.model;

import java.util.Objects;

public record JumpStep(
    JumpMode mode,
    JumpStopMode stopMode,
    int durationTicks,
    int jumpCount
) implements ScenarioStep {
    public static final int MIN_JUMP_COUNT = 1;
    public static final int MAX_JUMP_COUNT = 100000;
    public static final int DEFAULT_JUMP_COUNT = 10;
    public static final int DEFAULT_DURATION_TICKS = 20;

    public JumpStep {
        mode = Objects.requireNonNull(
            mode,
            "Jump mode must not be null"
        );

        stopMode = Objects.requireNonNull(
            stopMode,
            "Jump stop mode must not be null"
        );

        if (mode == JumpMode.SINGLE) {
            stopMode = JumpStopMode.DURATION;
        }

        if (
            mode == JumpMode.HOLD
                && stopMode == JumpStopMode.JUMP_COUNT
        ) {
            stopMode = JumpStopMode.DURATION;
        }

        ScenarioStep.validateDuration(durationTicks);

        if (
            jumpCount < MIN_JUMP_COUNT
                || jumpCount > MAX_JUMP_COUNT
        ) {
            throw new IllegalArgumentException(
                "Jump count must be between 1 and 100000"
            );
        }
    }

    @Override
    public boolean isInfinite() {
        return mode != JumpMode.SINGLE
            && stopMode == JumpStopMode.MANUAL;
    }

    @Override
    public String label() {
        return switch (mode) {
            case SINGLE -> "Jump once";
            case HOLD -> stopMode == JumpStopMode.MANUAL
                ? "Hold jump until stopped"
                : "Hold jump for "
                    + ScenarioFormat.formatDuration(durationTicks);
            case REPEAT -> switch (stopMode) {
                case DURATION -> "Repeat jumps for "
                    + ScenarioFormat.formatDuration(durationTicks);
                case JUMP_COUNT -> "Repeat "
                    + jumpCount
                    + (jumpCount == 1 ? " jump" : " jumps");
                case MANUAL -> "Repeat jumps until stopped";
            };
        };
    }

    @Override
    public <T> T accept(
        ScenarioStepVisitor<T> visitor
    ) {
        return visitor.visit(this);
    }

    public JumpStep withMode(JumpMode updatedMode) {
        JumpStopMode updatedStopMode = stopMode;

        if (updatedMode == JumpMode.SINGLE) {
            updatedStopMode = JumpStopMode.DURATION;
        }

        if (
            updatedMode == JumpMode.HOLD
                && updatedStopMode == JumpStopMode.JUMP_COUNT
        ) {
            updatedStopMode = JumpStopMode.DURATION;
        }

        return new JumpStep(
            updatedMode,
            updatedStopMode,
            durationTicks,
            jumpCount
        );
    }

    public JumpStep withStopMode(JumpStopMode updatedStopMode) {
        if (mode == JumpMode.SINGLE) {
            updatedStopMode = JumpStopMode.DURATION;
        }

        if (
            mode == JumpMode.HOLD
                && updatedStopMode == JumpStopMode.JUMP_COUNT
        ) {
            updatedStopMode = JumpStopMode.DURATION;
        }

        return new JumpStep(
            mode,
            updatedStopMode,
            durationTicks,
            jumpCount
        );
    }

    public JumpStep withDurationTicks(int updatedDurationTicks) {
        return new JumpStep(
            mode,
            stopMode,
            updatedDurationTicks,
            jumpCount
        );
    }

    public JumpStep withJumpCount(int updatedJumpCount) {
        return new JumpStep(
            mode,
            stopMode,
            durationTicks,
            updatedJumpCount
        );
    }
}