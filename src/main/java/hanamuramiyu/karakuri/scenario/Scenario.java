package hanamuramiyu.karakuri.scenario;

import java.util.List;

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

    public sealed interface Step permits WalkForwardStep, WaitStep {
        String label();
    }

    public record WalkForwardStep(int durationTicks) implements Step {
        public WalkForwardStep {
            validateDuration(durationTicks);
        }

        @Override
        public String label() {
            return "Walk forward for " + formatDuration(durationTicks);
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

    private static void validateDuration(int durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }
    }

    private static String formatDuration(int durationTicks) {
        if (durationTicks % 20 == 0) {
            return durationTicks / 20 + "s";
        }

        return durationTicks + " ticks";
    }
}