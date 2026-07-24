package hanamuramiyu.karakuri.ui.editor.workflow;

import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.DepositItemsStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
import hanamuramiyu.karakuri.scenario.model.JumpMode;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.JumpStopMode;
import hanamuramiyu.karakuri.scenario.model.MouseInputMode;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MouseStopMode;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.RepeatMode;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;

final class ScenarioStepValueAdjuster {
    private static final int DURATION_STEP_TICKS = 10;
    private static final int CAMERA_ANGLE_STEP = 5;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS = 72000;

    private ScenarioStepValueAdjuster() {
    }

    static ScenarioStep adjustPrimaryValue(
        ScenarioStep step,
        int direction
    ) {
        return switch (step) {
            case CameraStep cameraStep ->
                cameraStep.withAngleDegrees(
                    Math.clamp(
                        cameraStep.angleDegrees()
                            + direction
                            * CAMERA_ANGLE_STEP,
                        CameraStep.MIN_ANGLE_DEGREES,
                        CameraStep.MAX_ANGLE_DEGREES
                    )
                );

            case DepositItemsStep depositItemsStep ->
                depositItemsStep;

            case HotbarStep hotbarStep ->
                hotbarStep.withSlot(
                    Math.clamp(
                        hotbarStep.slot() + direction,
                        HotbarStep.MIN_SLOT,
                        HotbarStep.MAX_SLOT
                    )
                );

            case InventorySlotStep inventorySlotStep ->
                inventorySlotStep;

            case JumpStep jumpStep ->
                adjustJumpStep(
                    jumpStep,
                    direction
                );

            case MoveStep moveStep ->
                moveStep.withDurationTicks(
                    adjustDuration(
                        moveStep.durationTicks(),
                        direction
                    )
                );

            case MouseStep mouseStep ->
                adjustMouseStep(
                    mouseStep,
                    direction
                );

            case RepeatStep repeatStep ->
                adjustRepeatStep(
                    repeatStep,
                    direction
                );

            case WaitStep waitStep ->
                new WaitStep(
                    adjustDuration(
                        waitStep.durationTicks(),
                        direction
                    )
                );
        };
    }

    private static JumpStep adjustJumpStep(
        JumpStep step,
        int direction
    ) {
        if (
            step.mode() == JumpMode.SINGLE
                || step.stopMode()
                    == JumpStopMode.MANUAL
        ) {
            return step;
        }

        if (
            step.mode() == JumpMode.REPEAT
                && step.stopMode()
                    == JumpStopMode.JUMP_COUNT
        ) {
            return step.withJumpCount(
                Math.clamp(
                    step.jumpCount() + direction,
                    JumpStep.MIN_JUMP_COUNT,
                    JumpStep.MAX_JUMP_COUNT
                )
            );
        }

        return step.withDurationTicks(
            adjustDuration(
                step.durationTicks(),
                direction
            )
        );
    }

    private static RepeatStep adjustRepeatStep(
        RepeatStep step,
        int direction
    ) {
        if (step.mode() == RepeatMode.FOREVER) {
            return step;
        }

        return step.withRepeatCount(
            Math.clamp(
                step.repeatCount() + direction,
                RepeatStep.MIN_REPEAT_COUNT,
                RepeatStep.MAX_REPEAT_COUNT
            )
        );
    }

    private static MouseStep adjustMouseStep(
        MouseStep step,
        int direction
    ) {
        if (
            step.stopMode()
                == MouseStopMode.MANUAL
        ) {
            return step;
        }

        if (
            step.inputMode()
                == MouseInputMode.CLICK
                && step.stopMode()
                    == MouseStopMode.CLICK_COUNT
        ) {
            return step.withClickCount(
                Math.clamp(
                    step.clickCount() + direction,
                    MouseStep.MIN_CLICK_COUNT,
                    MouseStep.MAX_CLICK_COUNT
                )
            );
        }

        return step.withDurationTicks(
            adjustDuration(
                step.durationTicks(),
                direction
            )
        );
    }

    private static int adjustDuration(
        int durationTicks,
        int direction
    ) {
        return Math.clamp(
            durationTicks
                + direction
                * DURATION_STEP_TICKS,
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );
    }
}