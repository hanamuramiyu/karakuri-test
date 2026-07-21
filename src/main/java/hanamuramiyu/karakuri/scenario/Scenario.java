package hanamuramiyu.karakuri.scenario;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record Scenario(String name, List<Step> steps) {
    public Scenario {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Scenario name must not be blank");
        }

        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Scenario must contain at least one step");
        }

        name = name.trim();
        steps = List.copyOf(steps);
    }

    public sealed interface Step permits MoveStep, WaitStep {
        int durationTicks();

        String label();
    }

    public enum MoveDirection {
        FORWARD("forward", "Forward"),
        BACKWARD("backward", "Backward"),
        LEFT("left", "Left"),
        RIGHT("right", "Right");

        private final String id;
        private final String label;

        MoveDirection(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public MoveDirection next() {
            MoveDirection[] directions = values();
            return directions[(ordinal() + 1) % directions.length];
        }

        public static MoveDirection fromId(String id) {
            for (MoveDirection direction : values()) {
                if (direction.id.equals(id)) {
                    return direction;
                }
            }

            throw new IllegalArgumentException("Unknown movement direction: " + id);
        }
    }

    public record MoveStep(
        MoveDirection direction,
        int durationTicks
    ) implements Step {
        public MoveStep {
            direction = Objects.requireNonNull(
                direction,
                "Movement direction must not be null"
            );
            validateDuration(durationTicks);
        }

        @Override
        public String label() {
            return "Move "
                + direction.label().toLowerCase(Locale.ROOT)
                + " for "
                + formatDuration(durationTicks);
        }
    }

    public record WaitStep(int durationTicks) implements Step {
        public WaitStep {
            validateDuration(durationTicks);
        }

        @Override
        public String label() {
            return "Wait for " + formatDuration(durationTicks);
        }
    }

    public static String formatDuration(int durationTicks) {
        if (durationTicks % 20 == 0) {
            return durationTicks / 20 + "s";
        }

        if (durationTicks % 10 == 0) {
            return String.format(
                Locale.ROOT,
                "%.1fs",
                durationTicks / 20.0
            );
        }

        return durationTicks + " ticks";
    }

    private static void validateDuration(int durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException(
                "Duration must be greater than zero"
            );
        }
    }
}