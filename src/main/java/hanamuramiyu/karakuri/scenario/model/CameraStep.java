package hanamuramiyu.karakuri.scenario.model;

import java.util.Objects;

public record CameraStep(
    CameraDirection direction,
    CameraMotion motion,
    int angleDegrees,
    int durationTicks
) implements ScenarioStep {
    public static final int MIN_ANGLE_DEGREES = 1;
    public static final int MAX_ANGLE_DEGREES = 180;
    public static final int DEFAULT_ANGLE_DEGREES = 90;
    public static final int DEFAULT_DURATION_TICKS = 20;

    public CameraStep {
        direction = Objects.requireNonNull(
            direction,
            "Camera direction must not be null"
        );

        motion = Objects.requireNonNull(
            motion,
            "Camera motion must not be null"
        );

        if (
            angleDegrees < MIN_ANGLE_DEGREES
                || angleDegrees > MAX_ANGLE_DEGREES
        ) {
            throw new IllegalArgumentException(
                "Camera angle must be between 1 and 180 degrees"
            );
        }

        ScenarioStep.validateDuration(durationTicks);
    }

    @Override
    public String label() {
        String angle = angleDegrees + "°";

        if (motion == CameraMotion.INSTANT) {
            return direction.label()
                + " "
                + angle
                + " instantly";
        }

        return direction.label()
            + " "
            + angle
            + " over "
            + ScenarioFormat.formatDuration(durationTicks);
    }

    public CameraStep withDirection(CameraDirection updatedDirection) {
        return new CameraStep(
            updatedDirection,
            motion,
            angleDegrees,
            durationTicks
        );
    }

    public CameraStep withMotion(CameraMotion updatedMotion) {
        return new CameraStep(
            direction,
            updatedMotion,
            angleDegrees,
            durationTicks
        );
    }

    public CameraStep withAngleDegrees(int updatedAngleDegrees) {
        return new CameraStep(
            direction,
            motion,
            updatedAngleDegrees,
            durationTicks
        );
    }

    public CameraStep withDurationTicks(int updatedDurationTicks) {
        return new CameraStep(
            direction,
            motion,
            angleDegrees,
            updatedDurationTicks
        );
    }
}
