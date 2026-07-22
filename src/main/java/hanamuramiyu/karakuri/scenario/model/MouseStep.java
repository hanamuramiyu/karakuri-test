package hanamuramiyu.karakuri.scenario.model;

import java.util.Locale;
import java.util.Objects;

public record MouseStep(
    MouseAction action,
    MouseInputMode inputMode,
    MouseStopMode stopMode,
    int durationTicks,
    int clicksPerSecondHalfSteps,
    int clickCount
) implements ScenarioStep {
    public static final int MIN_CPS_HALF_STEPS = 1;
    public static final int MAX_CPS_HALF_STEPS = 20;
    public static final int DEFAULT_CPS_HALF_STEPS = 10;
    public static final int MIN_CLICK_COUNT = 1;
    public static final int MAX_CLICK_COUNT = 100000;
    public static final int DEFAULT_CLICK_COUNT = 20;

    public MouseStep {
        action = Objects.requireNonNull(
            action,
            "Mouse action must not be null"
        );

        inputMode = Objects.requireNonNull(
            inputMode,
            "Mouse input mode must not be null"
        );

        stopMode = Objects.requireNonNull(
            stopMode,
            "Mouse stop mode must not be null"
        );

        if (
            inputMode == MouseInputMode.HOLD
                && stopMode == MouseStopMode.CLICK_COUNT
        ) {
            throw new IllegalArgumentException(
                "Hold mode cannot stop after a click count"
            );
        }

        ScenarioStep.validateDuration(durationTicks);

        if (
            clicksPerSecondHalfSteps < MIN_CPS_HALF_STEPS
                || clicksPerSecondHalfSteps > MAX_CPS_HALF_STEPS
        ) {
            throw new IllegalArgumentException(
                "Mouse CPS must be between 0.5 and 10"
            );
        }

        if (
            clickCount < MIN_CLICK_COUNT
                || clickCount > MAX_CLICK_COUNT
        ) {
            throw new IllegalArgumentException(
                "Mouse click count must be between 1 and 100000"
            );
        }
    }

    @Override
    public boolean isInfinite() {
        return stopMode == MouseStopMode.MANUAL;
    }

    @Override
    public String label() {
        String actionName = action.label().toLowerCase(Locale.ROOT);

        if (inputMode == MouseInputMode.HOLD) {
            if (stopMode == MouseStopMode.MANUAL) {
                return "Hold " + actionName + " until stopped";
            }

            return "Hold "
                + actionName
                + " for "
                + ScenarioFormat.formatDuration(durationTicks);
        }

        String rate = ScenarioFormat.formatClicksPerSecondLabel(
            clicksPerSecondHalfSteps
        );

        return switch (stopMode) {
            case DURATION -> action.label()
                + " at "
                + rate
                + " for "
                + ScenarioFormat.formatDuration(durationTicks);
            case CLICK_COUNT -> action.label()
                + " "
                + clickCount
                + " times at "
                + rate;
            case MANUAL -> action.label()
                + " at "
                + rate
                + " until stopped";
        };
    }

    @Override
    public <T> T accept(
        ScenarioStepVisitor<T> visitor
    ) {
        return visitor.visit(this);
    }

    public double clicksPerSecond() {
        return clicksPerSecondHalfSteps / 2.0;
    }

    public int estimatedClickCount() {
        if (inputMode != MouseInputMode.CLICK) {
            return 0;
        }

        return switch (stopMode) {
            case DURATION -> Math.max(
                1,
                Math.ceilDiv(
                    durationTicks * clicksPerSecondHalfSteps,
                    40
                )
            );
            case CLICK_COUNT -> clickCount;
            case MANUAL -> -1;
        };
    }

    public int estimatedDurationTicks() {
        if (
            inputMode != MouseInputMode.CLICK
                || stopMode != MouseStopMode.CLICK_COUNT
        ) {
            return durationTicks;
        }

        return Math.max(
            1,
            Math.ceilDiv(
                clickCount * 40,
                clicksPerSecondHalfSteps
            )
        );
    }

    public MouseStep withAction(MouseAction updatedAction) {
        return new MouseStep(
            updatedAction,
            inputMode,
            stopMode,
            durationTicks,
            clicksPerSecondHalfSteps,
            clickCount
        );
    }

    public MouseStep withInputMode(MouseInputMode updatedInputMode) {
        MouseStopMode updatedStopMode = stopMode;

        if (
            updatedInputMode == MouseInputMode.HOLD
                && updatedStopMode == MouseStopMode.CLICK_COUNT
        ) {
            updatedStopMode = MouseStopMode.DURATION;
        }

        return new MouseStep(
            action,
            updatedInputMode,
            updatedStopMode,
            durationTicks,
            clicksPerSecondHalfSteps,
            clickCount
        );
    }

    public MouseStep withStopMode(MouseStopMode updatedStopMode) {
        if (
            inputMode == MouseInputMode.HOLD
                && updatedStopMode == MouseStopMode.CLICK_COUNT
        ) {
            updatedStopMode = MouseStopMode.DURATION;
        }

        return new MouseStep(
            action,
            inputMode,
            updatedStopMode,
            durationTicks,
            clicksPerSecondHalfSteps,
            clickCount
        );
    }

    public MouseStep withDurationTicks(int updatedDurationTicks) {
        return new MouseStep(
            action,
            inputMode,
            stopMode,
            updatedDurationTicks,
            clicksPerSecondHalfSteps,
            clickCount
        );
    }

    public MouseStep withClicksPerSecondHalfSteps(
        int updatedClicksPerSecondHalfSteps
    ) {
        return new MouseStep(
            action,
            inputMode,
            stopMode,
            durationTicks,
            updatedClicksPerSecondHalfSteps,
            clickCount
        );
    }

    public MouseStep withClickCount(int updatedClickCount) {
        return new MouseStep(
            action,
            inputMode,
            stopMode,
            durationTicks,
            clicksPerSecondHalfSteps,
            updatedClickCount
        );
    }
}