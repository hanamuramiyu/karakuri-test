package hanamuramiyu.karakuri.scenario.model;

import java.util.List;
import java.util.Objects;

public record RepeatStep(
    RepeatMode mode,
    int repeatCount,
    List<ScenarioStep> steps
) implements ScenarioStep {
    public static final int MIN_REPEAT_COUNT = 1;
    public static final int MAX_REPEAT_COUNT = 100000;
    public static final int DEFAULT_REPEAT_COUNT = 2;

    public RepeatStep {
        Objects.requireNonNull(
            mode,
            "Repeat mode must not be null"
        );

        if (
            repeatCount < MIN_REPEAT_COUNT
                || repeatCount > MAX_REPEAT_COUNT
        ) {
            throw new IllegalArgumentException(
                "Repeat count must be between 1 and 100000"
            );
        }

        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException(
                "Repeat group must contain at least one step"
            );
        }

        steps = List.copyOf(steps);

        if (steps.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                "Repeat group steps must not contain null"
            );
        }
    }

    @Override
    public int durationTicks() {
        long cycleDuration = 0;

        for (ScenarioStep step : steps) {
            cycleDuration += step.durationTicks();
        }

        long totalDuration =
            mode == RepeatMode.COUNT
                ? cycleDuration * repeatCount
                : cycleDuration;

        return (int) Math.min(
            Integer.MAX_VALUE,
            totalDuration
        );
    }

    @Override
    public String label() {
        return mode == RepeatMode.FOREVER
            ? "Repeat forever"
            : "Repeat " + repeatCount + "x";
    }

    @Override
    public <T> T accept(
        ScenarioStepVisitor<T> visitor
    ) {
        return visitor.visit(this);
    }

    @Override
    public boolean isInfinite() {
        return mode == RepeatMode.FOREVER
            || steps.getLast().isInfinite();
    }

    public RepeatStep withMode(
        RepeatMode updatedMode
    ) {
        return new RepeatStep(
            updatedMode,
            repeatCount,
            steps
        );
    }

    public RepeatStep withRepeatCount(
        int updatedRepeatCount
    ) {
        return new RepeatStep(
            mode,
            updatedRepeatCount,
            steps
        );
    }

    public RepeatStep withSteps(
        List<ScenarioStep> updatedSteps
    ) {
        return new RepeatStep(
            mode,
            repeatCount,
            updatedSteps
        );
    }
}