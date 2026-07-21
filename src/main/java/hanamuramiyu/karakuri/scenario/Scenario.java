package hanamuramiyu.karakuri.scenario;

import java.math.BigDecimal;
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

    public sealed interface Step permits
        CameraStep,
        HotbarStep,
        JumpStep,
        MoveStep,
        MouseStep,
        WaitStep {
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

    public enum MoveMode {
        WALK("walk", "Walk"),
        SPRINT("sprint", "Sprint"),
        SNEAK("sneak", "Sneak");

        private final String id;
        private final String label;

        MoveMode(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static MoveMode fromId(String id) {
            for (MoveMode mode : values()) {
                if (mode.id.equals(id)) {
                    return mode;
                }
            }

            throw new IllegalArgumentException(
                "Unknown movement mode: " + id
            );
        }
    }

    public enum CameraDirection {
        LEFT("left", "Turn Left"),
        RIGHT("right", "Turn Right"),
        UP("up", "Look Up"),
        DOWN("down", "Look Down");

        private final String id;
        private final String label;

        CameraDirection(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static CameraDirection fromId(String id) {
            for (CameraDirection direction : values()) {
                if (direction.id.equals(id)) {
                    return direction;
                }
            }

            throw new IllegalArgumentException(
                "Unknown camera direction: " + id
            );
        }
    }

    public enum CameraMotion {
        INSTANT("instant", "Instant"),
        SMOOTH("smooth", "Smooth");

        private final String id;
        private final String label;

        CameraMotion(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static CameraMotion fromId(String id) {
            for (CameraMotion motion : values()) {
                if (motion.id.equals(id)) {
                    return motion;
                }
            }

            throw new IllegalArgumentException(
                "Unknown camera motion: " + id
            );
        }
    }

    public enum JumpMode {
        SINGLE("single", "Single"),
        HOLD("hold", "Hold"),
        REPEAT("repeat", "Repeat");

        private final String id;
        private final String label;

        JumpMode(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static JumpMode fromId(String id) {
            for (JumpMode mode : values()) {
                if (mode.id.equals(id)) {
                    return mode;
                }
            }

            throw new IllegalArgumentException(
                "Unknown jump mode: " + id
            );
        }
    }

    public enum JumpStopMode {
        DURATION("duration", "Time"),
        JUMP_COUNT("jump_count", "Jumps"),
        MANUAL("manual", "Manual");

        private final String id;
        private final String label;

        JumpStopMode(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public String label() {
            return label;
        }

        public static JumpStopMode fromId(String id) {
            for (JumpStopMode mode : values()) {
                if (mode.id.equals(id)) {
                    return mode;
                }
            }

            throw new IllegalArgumentException(
                "Unknown jump stop mode: " + id
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

    public record CameraStep(
        CameraDirection direction,
        CameraMotion motion,
        int angleDegrees,
        int durationTicks
    ) implements Step {
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

            validateDuration(durationTicks);
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
                + formatDuration(durationTicks);
        }

        public CameraStep withDirection(
            CameraDirection updatedDirection
        ) {
            return new CameraStep(
                updatedDirection,
                motion,
                angleDegrees,
                durationTicks
            );
        }

        public CameraStep withMotion(
            CameraMotion updatedMotion
        ) {
            return new CameraStep(
                direction,
                updatedMotion,
                angleDegrees,
                durationTicks
            );
        }

        public CameraStep withAngleDegrees(
            int updatedAngleDegrees
        ) {
            return new CameraStep(
                direction,
                motion,
                updatedAngleDegrees,
                durationTicks
            );
        }

        public CameraStep withDurationTicks(
            int updatedDurationTicks
        ) {
            return new CameraStep(
                direction,
                motion,
                angleDegrees,
                updatedDurationTicks
            );
        }
    }

    public record HotbarStep(int slot) implements Step {
        public static final int MIN_SLOT = 0;
        public static final int MAX_SLOT = 8;
        public static final int DEFAULT_SLOT = 0;

        public HotbarStep {
            if (slot < MIN_SLOT || slot > MAX_SLOT) {
                throw new IllegalArgumentException(
                    "Hotbar slot must be between 0 and 8"
                );
            }
        }

        @Override
        public int durationTicks() {
            return 1;
        }

        @Override
        public String label() {
            return "Select hotbar slot " + (slot + 1);
        }

        public HotbarStep withSlot(int updatedSlot) {
            return new HotbarStep(updatedSlot);
        }
    }

    public record JumpStep(
        JumpMode mode,
        JumpStopMode stopMode,
        int durationTicks,
        int jumpCount
    ) implements Step {
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

            validateDuration(durationTicks);

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
                case SINGLE ->
                    "Jump once";

                case HOLD ->
                    stopMode == JumpStopMode.MANUAL
                        ? "Hold jump until stopped"
                        : "Hold jump for "
                            + formatDuration(durationTicks);

                case REPEAT ->
                    switch (stopMode) {
                        case DURATION ->
                            "Repeat jumps for "
                                + formatDuration(durationTicks);

                        case JUMP_COUNT ->
                            "Repeat "
                                + jumpCount
                                + (
                                    jumpCount == 1
                                        ? " jump"
                                        : " jumps"
                                );

                        case MANUAL ->
                            "Repeat jumps until stopped";
                    };
            };
        }

        public JumpStep withMode(JumpMode updatedMode) {
            JumpStopMode updatedStopMode = stopMode;

            if (updatedMode == JumpMode.SINGLE) {
                updatedStopMode = JumpStopMode.DURATION;
            }

            if (
                updatedMode == JumpMode.HOLD
                    && updatedStopMode
                        == JumpStopMode.JUMP_COUNT
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

        public JumpStep withStopMode(
            JumpStopMode updatedStopMode
        ) {
            if (mode == JumpMode.SINGLE) {
                updatedStopMode = JumpStopMode.DURATION;
            }

            if (
                mode == JumpMode.HOLD
                    && updatedStopMode
                        == JumpStopMode.JUMP_COUNT
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

        public JumpStep withDurationTicks(
            int updatedDurationTicks
        ) {
            return new JumpStep(
                mode,
                stopMode,
                updatedDurationTicks,
                jumpCount
            );
        }

        public JumpStep withJumpCount(
            int updatedJumpCount
        ) {
            return new JumpStep(
                mode,
                stopMode,
                durationTicks,
                updatedJumpCount
            );
        }
    }

    public record MoveStep(
        MoveDirection direction,
        MoveMode mode,
        boolean jumping,
        int durationTicks
    ) implements Step {
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

            validateDuration(durationTicks);
        }

        @Override
        public String label() {
            String movement = mode.label()
                + " "
                + direction.label()
                    .toLowerCase(Locale.ROOT);

            if (jumping) {
                movement = "Jump while "
                    + movement.toLowerCase(Locale.ROOT);
            }

            return movement
                + " for "
                + formatDuration(durationTicks);
        }

        public MoveStep withDirection(
            MoveDirection updatedDirection
        ) {
            return new MoveStep(
                updatedDirection,
                mode,
                jumping,
                durationTicks
            );
        }

        public MoveStep withMode(
            MoveMode updatedMode
        ) {
            return new MoveStep(
                direction,
                updatedMode,
                jumping,
                durationTicks
            );
        }

        public MoveStep withJumping(
            boolean updatedJumping
        ) {
            return new MoveStep(
                direction,
                mode,
                updatedJumping,
                durationTicks
            );
        }

        public MoveStep withDurationTicks(
            int updatedDurationTicks
        ) {
            return new MoveStep(
                direction,
                mode,
                jumping,
                updatedDurationTicks
            );
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

            String rate = formatClicksPerSecondLabel(
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

        public MouseStep withAction(
            MouseAction updatedAction
        ) {
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

    public record WaitStep(
        int durationTicks
    ) implements Step {
        public WaitStep {
            validateDuration(durationTicks);
        }

        @Override
        public String label() {
            return "Wait for "
                + formatDuration(durationTicks);
        }
    }

    public static String formatDuration(
        int durationTicks
    ) {
        return BigDecimal
            .valueOf(durationTicks)
            .divide(BigDecimal.valueOf(20))
            .stripTrailingZeros()
            .toPlainString()
            + " s";
    }

    public static String formatClicksPerSecond(
        int halfSteps
    ) {
        if (halfSteps % 2 == 0) {
            return Integer.toString(
                halfSteps / 2
            );
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
        return formatClicksPerSecond(halfSteps)
            + " CPS";
    }

    private static void validateDuration(
        int durationTicks
    ) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException(
                "Duration must be greater than zero"
            );
        }
    }
}