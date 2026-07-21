package hanamuramiyu.karakuri.scenario;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record Scenario(String name, List<Step> steps) {
    public Scenario {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Scenario name must not be blank"
            );
        }

        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException(
                "Scenario must contain at least one step"
            );
        }

        name = name.trim();
        steps = List.copyOf(steps);

        for (int index = 0; index < steps.size() - 1; index++) {
            if (steps.get(index).isInfinite()) {
                throw new IllegalArgumentException(
                    "An infinite step must be the final scenario step"
                );
            }
        }
    }

    public sealed interface Step permits MoveStep, MouseStep, WaitStep {
        int durationTicks();

        String label();

        default boolean isInfinite() {
            return false;
        }
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

            return directions[
                (ordinal() + 1) % directions.length
            ];
        }

        public static MoveDirection fromId(String id) {
            for (MoveDirection direction : values()) {
                if (direction.id.equals(id)) {
                    return direction;
                }
            }

            throw new IllegalArgumentException(
                "Unknown movement direction: " + id
            );
        }
    }

    public enum MouseAction {
        LEFT_CLICK("left_click", "Left Click"),
        RIGHT_CLICK("right_click", "Right Click");

        private final String id;
        private final String label;

        MouseAction(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static MouseAction fromId(String id) {
            for (MouseAction action : values()) {
                if (action.id.equals(id)) {
                    return action;
                }
            }

            throw new IllegalArgumentException(
                "Unknown mouse action: " + id
            );
        }
    }

    public enum MouseInputMode {
        HOLD("hold", "Hold"),
        CLICK("click", "Click");

        private final String id;
        private final String label;

        MouseInputMode(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static MouseInputMode fromId(String id) {
            for (MouseInputMode mode : values()) {
                if (mode.id.equals(id)) {
                    return mode;
                }
            }

            throw new IllegalArgumentException(
                "Unknown mouse input mode: " + id
            );
        }
    }

    public enum MouseStopMode {
        DURATION("duration", "Time"),
        CLICK_COUNT("click_count", "Clicks"),
        MANUAL("manual", "Manual");

        private final String id;
        private final String label;

        MouseStopMode(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static MouseStopMode fromId(String id) {
            for (MouseStopMode mode : values()) {
                if (mode.id.equals(id)) {
                    return mode;
                }
            }

            throw new IllegalArgumentException(
                "Unknown mouse stop mode: " + id
            );
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

    public record MouseStep(
        MouseAction action,
        MouseInputMode inputMode,
        MouseStopMode stopMode,
        int durationTicks,
        int clicksPerSecondHalfSteps,
        int clickCount
    ) implements Step {
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

            validateDuration(durationTicks);

            if (
                clicksPerSecondHalfSteps < MIN_CPS_HALF_STEPS
                    || clicksPerSecondHalfSteps
                        > MAX_CPS_HALF_STEPS
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
            String actionName =
                action.label().toLowerCase(Locale.ROOT);

            if (inputMode == MouseInputMode.HOLD) {
                if (stopMode == MouseStopMode.MANUAL) {
                    return "Hold "
                        + actionName
                        + " until stopped";
                }

                return "Hold "
                    + actionName
                    + " for "
                    + formatDuration(durationTicks);
            }

            String rate =
                formatClicksPerSecondLabel(
                    clicksPerSecondHalfSteps
                );

            return switch (stopMode) {
                case DURATION ->
                    action.label()
                        + " at "
                        + rate
                        + " for "
                        + formatDuration(durationTicks);
                case CLICK_COUNT ->
                    action.label()
                        + " "
                        + clickCount
                        + " times at "
                        + rate;
                case MANUAL ->
                    action.label()
                        + " at "
                        + rate
                        + " until stopped";
            };
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
                        durationTicks
                            * clicksPerSecondHalfSteps,
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

        public MouseStep withInputMode(
            MouseInputMode updatedInputMode
        ) {
            MouseStopMode updatedStopMode = stopMode;

            if (
                updatedInputMode == MouseInputMode.HOLD
                    && updatedStopMode
                        == MouseStopMode.CLICK_COUNT
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

        public MouseStep withStopMode(
            MouseStopMode updatedStopMode
        ) {
            if (
                inputMode == MouseInputMode.HOLD
                    && updatedStopMode
                        == MouseStopMode.CLICK_COUNT
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

        public MouseStep withDurationTicks(
            int updatedDurationTicks
        ) {
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

        public MouseStep withClickCount(
            int updatedClickCount
        ) {
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

    public static String formatClicksPerSecond(
        int halfSteps
    ) {
        if (halfSteps % 2 == 0) {
            return Integer.toString(halfSteps / 2);
        }

        return String.format(
            Locale.ROOT,
            "%.1f",
            halfSteps / 2.0
        );
    }

    public static String formatClicksPerSecondLabel(
        int halfSteps
    ) {
        return formatClicksPerSecond(halfSteps) + " CPS";
    }

    private static void validateDuration(int durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException(
                "Duration must be greater than zero"
            );
        }
    }
}