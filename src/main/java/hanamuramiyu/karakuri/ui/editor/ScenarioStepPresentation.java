package hanamuramiyu.karakuri.ui.editor;

import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.JumpMode;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.JumpStopMode;
import hanamuramiyu.karakuri.scenario.model.MouseInputMode;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MouseStopMode;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.RepeatMode;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioFormat;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;

public final class ScenarioStepPresentation {
    private ScenarioStepPresentation() {
    }

    public static int inspectorAccentColor(
        ScenarioStep step
    ) {
        return switch (step) {
            case CameraStep cameraStep ->
                switch (cameraStep.direction()) {
                    case LEFT -> 0xFF67B6E8;
                    case RIGHT -> 0xFFB38AE8;
                    case UP -> 0xFF61D394;
                    case DOWN -> 0xFFF0A765;
                };
            case HotbarStep hotbarStep ->
                0xFFE8D26A;
            case JumpStep jumpStep ->
                0xFF78D6C6;
            case MoveStep moveStep ->
                switch (moveStep.direction()) {
                    case FORWARD -> 0xFF61D394;
                    case BACKWARD -> 0xFFF0A765;
                    case LEFT -> 0xFF67B6E8;
                    case RIGHT -> 0xFFB38AE8;
                };
            case MouseStep mouseStep ->
                switch (mouseStep.action()) {
                    case LEFT_CLICK -> 0xFFE66777;
                    case RIGHT_CLICK -> 0xFF67C7E8;
                };
            case RepeatStep repeatStep ->
                0xFFE58AC8;
            case WaitStep waitStep ->
                0xFFA49BAD;
        };
    }

    public static int workflowAccentColor(
        ScenarioStep step
    ) {
        if (step instanceof MoveStep moveStep) {
            return switch (moveStep.direction()) {
                case FORWARD -> 0xFF64D69B;
                case BACKWARD -> 0xFFF0A765;
                case LEFT -> 0xFF67B6E8;
                case RIGHT -> 0xFFB38AE8;
            };
        }

        return inspectorAccentColor(step);
    }

    public static String inspectorTitle(
        ScenarioStep step
    ) {
        return switch (step) {
            case CameraStep cameraStep ->
                cameraStep.direction().label();
            case HotbarStep hotbarStep ->
                "Select Hotbar Slot "
                    + (hotbarStep.slot() + 1);
            case JumpStep jumpStep ->
                switch (jumpStep.mode()) {
                    case SINGLE -> "Single Jump";
                    case HOLD -> "Hold Jump";
                    case REPEAT -> "Repeat Jumps";
                };
            case MoveStep moveStep -> {
                String movement =
                    moveStep.mode().label()
                        + " "
                        + moveStep.direction().label();

                yield moveStep.jumping()
                    ? "Jump + " + movement
                    : movement;
            }
            case MouseStep mouseStep ->
                mouseStep.inputMode()
                    == MouseInputMode.HOLD
                        ? "Hold "
                            + mouseStep.action().label()
                        : mouseStep.action().label();
            case RepeatStep repeatStep ->
                "Repeat Group";
            case WaitStep waitStep ->
                "Wait";
        };
    }

    public static String compactInspectorLabel(
        ScenarioStep step
    ) {
        return switch (step) {
            case CameraStep cameraStep ->
                "Direction / motion";
            case HotbarStep hotbarStep ->
                "Select active hotbar slot";
            case JumpStep jumpStep ->
                "Mode / stop condition";
            case MoveStep moveStep ->
                "Direction / style / jumping";
            case MouseStep mouseStep ->
                "Button / input / stop";
            case RepeatStep repeatStep ->
                "Mode / count / nested blocks";
            case WaitStep waitStep ->
                "Timing action";
        };
    }

    public static String workflowIcon(
        ScenarioStep step
    ) {
        return switch (step) {
            case CameraStep cameraStep ->
                switch (cameraStep.direction()) {
                    case LEFT -> "<";
                    case RIGHT -> ">";
                    case UP -> "^";
                    case DOWN -> "v";
                };
            case HotbarStep hotbarStep ->
                Integer.toString(
                    hotbarStep.slot() + 1
                );
            case JumpStep jumpStep ->
                "J";
            case MoveStep moveStep ->
                switch (moveStep.direction()) {
                    case FORWARD -> "F";
                    case BACKWARD -> "B";
                    case LEFT -> "L";
                    case RIGHT -> "R";
                };
            case MouseStep mouseStep ->
                switch (mouseStep.action()) {
                    case LEFT_CLICK -> "1";
                    case RIGHT_CLICK -> "2";
                };
            case RepeatStep repeatStep ->
                "R";
            case WaitStep waitStep ->
                "W";
        };
    }

    public static String workflowTitle(
        ScenarioStep step
    ) {
        return switch (step) {
            case CameraStep cameraStep ->
                cameraStep.direction().label();
            case HotbarStep hotbarStep ->
                "Select Slot";
            case JumpStep jumpStep ->
                switch (jumpStep.mode()) {
                    case SINGLE -> "Jump";
                    case HOLD -> "Hold Jump";
                    case REPEAT -> "Repeat Jump";
                };
            case MoveStep moveStep ->
                moveStep.mode().label()
                    + " "
                    + moveStep.direction().label();
            case MouseStep mouseStep ->
                mouseStep.action().label();
            case RepeatStep repeatStep ->
                "Repeat Group";
            case WaitStep waitStep ->
                "Wait";
        };
    }

    public static String workflowSubtitle(
        ScenarioStep step
    ) {
        return switch (step) {
            case CameraStep cameraStep ->
                cameraStep.motion()
                    == CameraMotion.INSTANT
                        ? cameraStep.angleDegrees()
                            + "° · Instant"
                        : cameraStep.angleDegrees()
                            + "° · "
                            + ScenarioFormat.formatDuration(
                                cameraStep.durationTicks()
                            );
            case HotbarStep hotbarStep ->
                "Hotbar "
                    + (hotbarStep.slot() + 1);
            case JumpStep jumpStep ->
                jumpSubtitle(jumpStep);
            case MoveStep moveStep ->
                (moveStep.jumping()
                    ? "Jump · "
                    : "")
                    + ScenarioFormat.formatDuration(
                        moveStep.durationTicks()
                    );
            case MouseStep mouseStep ->
                mouseSubtitle(mouseStep);
            case RepeatStep repeatStep ->
                repeatSubtitle(repeatStep);
            case WaitStep waitStep ->
                ScenarioFormat.formatDuration(
                    waitStep.durationTicks()
                );
        };
    }

    public static String jumpPrimaryValueLabel(
        JumpStep step
    ) {
        if (step.mode() == JumpMode.SINGLE) {
            return null;
        }

        return switch (step.stopMode()) {
            case DURATION ->
                "Duration in seconds";
            case JUMP_COUNT ->
                "Jump count";
            case MANUAL ->
                null;
        };
    }

    public static String jumpDescription(
        JumpStep step
    ) {
        return switch (step.mode()) {
            case SINGLE ->
                "Presses Jump once";
            case HOLD ->
                step.stopMode()
                    == JumpStopMode.MANUAL
                        ? "Keeps Jump held until Stop"
                        : "Keeps Jump held for the selected time";
            case REPEAT ->
                step.stopMode()
                    == JumpStopMode.MANUAL
                        ? "Jumps after every landing until Stop"
                        : "Jumps again after every landing";
        };
    }

    public static String mousePrimaryValueLabel(
        MouseStep step
    ) {
        return switch (step.stopMode()) {
            case DURATION ->
                "Duration in seconds";
            case CLICK_COUNT ->
                "Click count";
            case MANUAL ->
                null;
        };
    }

    public static String mouseEstimate(
        MouseStep step
    ) {
        if (step.inputMode() == MouseInputMode.HOLD) {
            return step.stopMode()
                == MouseStopMode.MANUAL
                    ? "Runs until Stop"
                    : "Held for "
                        + ScenarioValueFormatter.durationValue(
                            step.durationTicks()
                        );
        }

        return switch (step.stopMode()) {
            case DURATION ->
                "Estimated clicks: "
                    + step.estimatedClickCount();
            case CLICK_COUNT ->
                "Estimated time: "
                    + ScenarioValueFormatter.durationValue(
                        step.estimatedDurationTicks()
                    );
            case MANUAL ->
                "Clicks until Stop";
        };
    }

    public static String countDisplayValue(
        ScenarioStep step
    ) {
        if (step instanceof JumpStep jumpStep) {
            return jumpStep.jumpCount()
                + (jumpStep.jumpCount() == 1
                    ? " jump"
                    : " jumps");
        }

        if (step instanceof MouseStep mouseStep) {
            return mouseStep.clickCount()
                + (mouseStep.clickCount() == 1
                    ? " click"
                    : " clicks");
        }

        if (step instanceof RepeatStep repeatStep) {
            return repeatStep.repeatCount()
                + (repeatStep.repeatCount() == 1
                    ? " repeat"
                    : " repeats");
        }

        throw new IllegalArgumentException(
            "Step does not use a count value"
        );
    }


    public static String repeatDescription(
        RepeatStep step
    ) {
        String blocks =
            step.steps().size() == 1
                ? "1 block"
                : step.steps().size() + " blocks";

        return step.mode() == RepeatMode.FOREVER
            ? blocks + " · Runs until Stop"
            : blocks + " · Double-click to open";
    }

    private static String repeatSubtitle(
        RepeatStep step
    ) {
        String blocks =
            step.steps().size() == 1
                ? "1 block"
                : step.steps().size() + " blocks";

        return step.mode() == RepeatMode.FOREVER
            ? "Forever · " + blocks
            : step.repeatCount() + "x · " + blocks;
    }

    private static String jumpSubtitle(
        JumpStep step
    ) {
        return switch (step.mode()) {
            case SINGLE ->
                "Single";
            case HOLD ->
                step.stopMode()
                    == JumpStopMode.MANUAL
                        ? "Hold · Manual"
                        : "Hold · "
                            + ScenarioFormat.formatDuration(
                                step.durationTicks()
                            );
            case REPEAT ->
                switch (step.stopMode()) {
                    case DURATION ->
                        "Repeat · "
                            + ScenarioFormat.formatDuration(
                                step.durationTicks()
                            );
                    case JUMP_COUNT ->
                        step.jumpCount()
                            + (step.jumpCount() == 1
                                ? " jump"
                                : " jumps");
                    case MANUAL ->
                        "Repeat · Manual";
                };
        };
    }

    private static String mouseSubtitle(
        MouseStep step
    ) {
        if (step.inputMode() == MouseInputMode.HOLD) {
            return step.stopMode()
                == MouseStopMode.MANUAL
                    ? "Hold · Manual"
                    : "Hold · "
                        + ScenarioFormat.formatDuration(
                            step.durationTicks()
                        );
        }

        String rate =
            ScenarioFormat.formatClicksPerSecondLabel(
                step.clicksPerSecondHalfSteps()
            );

        return switch (step.stopMode()) {
            case DURATION ->
                rate
                    + " · "
                    + ScenarioFormat.formatDuration(
                        step.durationTicks()
                    );
            case CLICK_COUNT ->
                step.clickCount()
                    + "x · "
                    + rate;
            case MANUAL ->
                rate + " · Manual";
        };
    }
}