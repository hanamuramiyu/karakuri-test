package hanamuramiyu.karakuri.ui.editor;

import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.DepositItemsStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.RestockItemsStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;

import java.util.List;
import java.util.Objects;

public final class ScenarioEditorClipboard {
    private static ScenarioStep copiedStep;

    private ScenarioEditorClipboard() {
    }

    public static boolean hasContents() {
        return copiedStep != null;
    }

    public static void copy(
        ScenarioStep step
    ) {
        copiedStep = copyOf(step);
    }

    public static ScenarioStep createPaste() {
        if (copiedStep == null) {
            return null;
        }

        return copyOf(copiedStep);
    }

    public static ScenarioStep copyOf(
        ScenarioStep step
    ) {
        Objects.requireNonNull(
            step,
            "Scenario step must not be null"
        );

        return switch (step) {
            case CameraStep cameraStep ->
                new CameraStep(
                    cameraStep.direction(),
                    cameraStep.motion(),
                    cameraStep.angleDegrees(),
                    cameraStep.durationTicks()
                );
            case DepositItemsStep depositItemsStep ->
                new DepositItemsStep(
                    depositItemsStep.options()
                );
            case HotbarStep hotbarStep ->
                new HotbarStep(
                    hotbarStep.slot()
                );
            case InventorySlotStep inventorySlotStep ->
                new InventorySlotStep(
                    inventorySlotStep.inventorySlot(),
                    inventorySlotStep.hotbarSlot()
                );
            case JumpStep jumpStep ->
                new JumpStep(
                    jumpStep.mode(),
                    jumpStep.stopMode(),
                    jumpStep.durationTicks(),
                    jumpStep.jumpCount()
                );
            case MoveStep moveStep ->
                new MoveStep(
                    moveStep.direction(),
                    moveStep.mode(),
                    moveStep.jumping(),
                    moveStep.durationTicks()
                );
            case MouseStep mouseStep ->
                new MouseStep(
                    mouseStep.action(),
                    mouseStep.inputMode(),
                    mouseStep.stopMode(),
                    mouseStep.durationTicks(),
                    mouseStep.clicksPerSecondHalfSteps(),
                    mouseStep.clickCount()
                );
            case RestockItemsStep restockItemsStep ->
                new RestockItemsStep(
                    restockItemsStep.options()
                );
            case RepeatStep repeatStep ->
                new RepeatStep(
                    repeatStep.mode(),
                    repeatStep.repeatCount(),
                    copySteps(repeatStep.steps())
                );
            case WaitStep waitStep ->
                new WaitStep(
                    waitStep.durationTicks()
                );
        };
    }

    private static List<ScenarioStep> copySteps(
        List<ScenarioStep> steps
    ) {
        return steps.stream()
            .map(ScenarioEditorClipboard::copyOf)
            .toList();
    }
}