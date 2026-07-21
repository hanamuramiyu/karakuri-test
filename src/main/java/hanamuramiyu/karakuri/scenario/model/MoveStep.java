package hanamuramiyu.karakuri.scenario.model;

import java.util.Locale;
import java.util.Objects;

public record MoveStep(
    MoveDirection direction,
    MoveMode mode,
    boolean jumping,
    int durationTicks
) implements ScenarioStep {
    public MoveStep(
        MoveDirection direction,
        int durationTicks
    ) {
        this(
            direction,
            MoveMode.WALK,
            false,
            durationTicks
        );
    }

    public MoveStep {
        direction = Objects.requireNonNull(
            direction,
            "Movement direction must not be null"
        );

        mode = Objects.requireNonNull(
            mode,
            "Movement mode must not be null"
        );

        ScenarioStep.validateDuration(durationTicks);
    }

    @Override
    public String label() {
        String movement = mode.label()
            + " "
            + direction.label().toLowerCase(Locale.ROOT);

        if (jumping) {
            movement = "Jump while "
                + movement.toLowerCase(Locale.ROOT);
        }

        return movement
            + " for "
            + ScenarioFormat.formatDuration(durationTicks);
    }

    public MoveStep withDirection(MoveDirection updatedDirection) {
        return new MoveStep(
            updatedDirection,
            mode,
            jumping,
            durationTicks
        );
    }

    public MoveStep withMode(MoveMode updatedMode) {
        return new MoveStep(
            direction,
            updatedMode,
            jumping,
            durationTicks
        );
    }

    public MoveStep withJumping(boolean updatedJumping) {
        return new MoveStep(
            direction,
            mode,
            updatedJumping,
            durationTicks
        );
    }

    public MoveStep withDurationTicks(int updatedDurationTicks) {
        return new MoveStep(
            direction,
            mode,
            jumping,
            updatedDurationTicks
        );
    }
}
