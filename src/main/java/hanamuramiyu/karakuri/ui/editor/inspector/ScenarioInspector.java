package hanamuramiyu.karakuri.ui.editor.inspector;

import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.DepositItemsStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.JumpMode;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.JumpStopMode;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MouseInputMode;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MouseStopMode;
import hanamuramiyu.karakuri.scenario.model.MoveDirection;
import hanamuramiyu.karakuri.scenario.model.MoveMode;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.RepeatMode;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioFormat;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorState;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorTheme;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepPresentation;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepRules;
import hanamuramiyu.karakuri.ui.editor.ScenarioValueFormatter;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public final class ScenarioInspector
    implements ScenarioInspectorWidgets.Actions {
    private static final int BUTTON_HEIGHT = 20;

    private final Font font;
    private final ScenarioEditorState state;
    private final ScenarioInspectorLayout layout;
    private final ScenarioInspectorWidgets widgets;
    private final Runnable stopRunningTest;
    private final int inspectorX;
    private final int inspectorY;
    private final int inspectorWidth;
    private final int inspectorHeight;
    private final Runnable testSelectedStep;
    private final Runnable openInventorySlotSelection;
    private final Runnable openDepositItemsSelection;
    private final Runnable resetSelectedStep;
    private final Runnable moveSelectedLeft;
    private final Runnable moveSelectedRight;
    private final Runnable duplicateSelectedStep;
    private final Runnable deleteSelectedStep;
    private final Runnable editorChanged;

    private boolean syncingDurationField;
    private boolean syncingCountField;
    private boolean syncingAngleField;
    private boolean durationFieldValid = true;
    private boolean countFieldValid = true;
    private boolean angleFieldValid = true;
    private boolean visible = true;

    public ScenarioInspector(
        Font font,
        ScenarioEditorState state,
        ScenarioInspectorLayout layout,
        Runnable stopRunningTest,
        Runnable testSelectedStep,
        Runnable openInventorySlotSelection,
        Runnable openDepositItemsSelection,
        Runnable resetSelectedStep,
        Runnable moveSelectedLeft,
        Runnable moveSelectedRight,
        Runnable duplicateSelectedStep,
        Runnable deleteSelectedStep,
        Runnable editorChanged
    ) {
        this.font = Objects.requireNonNull(
            font,
            "Font must not be null"
        );

        this.state = Objects.requireNonNull(
            state,
            "Editor state must not be null"
        );

        this.layout = Objects.requireNonNull(
            layout,
            "Inspector layout must not be null"
        );

        this.inspectorX = layout.x();
        this.inspectorY = layout.y();
        this.inspectorWidth = layout.width();
        this.inspectorHeight = layout.height();

        this.stopRunningTest = Objects.requireNonNull(
            stopRunningTest,
            "Stop-test action must not be null"
        );

        this.testSelectedStep = Objects.requireNonNull(
            testSelectedStep,
            "Test-step action must not be null"
        );

        this.openInventorySlotSelection = Objects.requireNonNull(
            openInventorySlotSelection,
            "Inventory selection action must not be null"
        );

        this.openDepositItemsSelection = Objects.requireNonNull(
            openDepositItemsSelection,
            "Deposit selection action must not be null"
        );

        this.resetSelectedStep = Objects.requireNonNull(
            resetSelectedStep,
            "Reset action must not be null"
        );

        this.moveSelectedLeft = Objects.requireNonNull(
            moveSelectedLeft,
            "Move-left action must not be null"
        );

        this.moveSelectedRight = Objects.requireNonNull(
            moveSelectedRight,
            "Move-right action must not be null"
        );

        this.duplicateSelectedStep = Objects.requireNonNull(
            duplicateSelectedStep,
            "Duplicate action must not be null"
        );

        this.deleteSelectedStep = Objects.requireNonNull(
            deleteSelectedStep,
            "Delete action must not be null"
        );

        this.editorChanged = Objects.requireNonNull(
            editorChanged,
            "Editor change listener must not be null"
        );

        this.widgets = new ScenarioInspectorWidgets(
            font,
            layout,
            this
        );

        syncSelectedStep();
        update();
    }

    public List<AbstractWidget> widgets() {
        return widgets.widgets();
    }

    public boolean hasFocusedTextField() {
        return widgets.hasFocusedTextField();
    }

    public boolean closeDropdowns() {
        return widgets.collapseDropdowns();
    }

    public void setVisible(
        boolean visible
    ) {
        this.visible = visible;
        update();
    }

    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        return visible
            && widgets.mouseClicked(event, doubled);
    }

    public void renderDropdownOverlay(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        if (!visible) {
            return;
        }

        widgets.renderDropdownOverlay(
            graphics,
            mouseX,
            mouseY,
            delta
        );
    }

    public boolean isValid() {
        return validationMessage() == null;
    }

    public String validationMessage() {
        ScenarioStep step =
            getSelectedStep();

        if (
            step instanceof DepositItemsStep depositItemsStep
                && !depositItemsStep.hasAssignedGroup()
        ) {
            return "Select a storage group for Deposit Items";
        }

        if (
            ScenarioStepRules.usesAngle(step)
                && !angleFieldValid
        ) {
            return "Camera angle must be between 1 and 180 degrees";
        }

        if (
            ScenarioStepRules.usesDuration(step)
                && !durationFieldValid
        ) {
            return "Duration must be between 0.05 and 3600 seconds";
        }

        if (
            ScenarioStepRules.usesCount(step)
                && !countFieldValid
        ) {
            if (step instanceof JumpStep) {
                return "Jump count must be between 1 and 100000";
            }

            if (step instanceof RepeatStep) {
                return "Repeat count must be between 1 and 100000";
            }

            return "Click count must be between 1 and 100000";
        }

        return null;
    }

    public void render(
        GuiGraphics graphics
    ) {
        if (!visible) {
            return;
        }

        ScenarioStep step =
            getSelectedStep();

        graphics.fill(
            inspectorX,
            inspectorY,
            inspectorX + inspectorWidth,
            inspectorY + inspectorHeight,
            ScenarioEditorTheme.PANEL
        );

        graphics.renderOutline(
            inspectorX,
            inspectorY,
            inspectorWidth,
            inspectorHeight,
            ScenarioEditorTheme.OUTLINE
        );

        graphics.fill(
            inspectorX,
            inspectorY,
            inspectorX + 3,
            inspectorY + inspectorHeight,
            ScenarioStepPresentation.inspectorAccentColor(step)
        );

        graphics.drawString(
            font,
            Component.literal(
                "Properties"
            ),
            inspectorX + 10,
            inspectorY + 8,
            ScenarioEditorTheme.TEXT,
            false
        );

        Component position =
            Component.literal(
                "#"
                    + (
                        state.selectedIndex()
                            + 1
                    )
                    + " of "
                    + state.size()
            );

        graphics.drawString(
            font,
            position,
            inspectorX
                + inspectorWidth
                - 10
                - font.width(position),
            inspectorY + 8,
            ScenarioEditorTheme.TEXT_MUTED,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                ScenarioStepPresentation.inspectorTitle(step)
            ),
            inspectorX + 10,
            inspectorY + 24,
            ScenarioEditorTheme.TEXT,
            false
        );

        widgets.renderDecorations(graphics);

        renderValueFrames(
            graphics,
            step
        );
    }
    private void renderValueFrames(
        GuiGraphics graphics,
        ScenarioStep step
    ) {
        if (
            ScenarioStepRules.usesCps(step)
                || ScenarioStepRules.usesAngle(step)
        ) {
            graphics.fill(
                widgets.secondaryFrameX,
                widgets.secondaryFrameY,
                widgets.secondaryFrameX
                    + widgets.secondaryFrameWidth,
                widgets.secondaryFrameY
                    + BUTTON_HEIGHT,
                0xFF100E16
            );

            graphics.renderOutline(
                widgets.secondaryFrameX,
                widgets.secondaryFrameY,
                widgets.secondaryFrameWidth,
                BUTTON_HEIGHT,
                ScenarioStepRules.usesAngle(step)
                    && !angleFieldValid
                        ? 0xFFC75B69
                        : 0xFF51475E
            );
        }

        if (ScenarioStepRules.usesPrimaryValue(step)) {
            graphics.fill(
                widgets.primaryFrameX,
                widgets.primaryFrameY,
                widgets.primaryFrameX
                    + widgets.primaryFrameWidth,
                widgets.primaryFrameY
                    + BUTTON_HEIGHT,
                0xFF100E16
            );

            graphics.renderOutline(
                widgets.primaryFrameX,
                widgets.primaryFrameY,
                widgets.primaryFrameWidth,
                BUTTON_HEIGHT,
                isPrimaryValueValid(step)
                    ? 0xFF51475E
                    : 0xFFC75B69
            );
        }
    }
    public void renderCenteredValues(
        GuiGraphics graphics
    ) {
        if (!visible) {
            return;
        }

        ScenarioStep step =
            getSelectedStep();

        if (
            step
                instanceof
                HotbarStep hotbarStep
        ) {
            coverFrameAndRenderValue(
                graphics,
                widgets.primaryFrameX,
                widgets.primaryFrameY,
                widgets.primaryFrameWidth,
                "Slot "
                    + (
                        hotbarStep.slot()
                            + 1
                    ),
                ScenarioEditorTheme.TEXT
            );
        }

        if (ScenarioStepRules.usesCps(step)) {
            renderCenteredFrameText(
                graphics,
                widgets.secondaryFrameX,
                widgets.secondaryFrameY,
                widgets.secondaryFrameWidth,
                ScenarioFormat
                    .formatClicksPerSecondLabel(
                        (
                            (MouseStep)
                                step
                        )
                            .clicksPerSecondHalfSteps()
                    ),
                ScenarioEditorTheme.TEXT
            );
        }

        if (
            widgets.angleField.visible
                && !widgets.angleField.isFocused()
        ) {
            CameraStep cameraStep =
                (CameraStep) step;

            coverFrameAndRenderValue(
                graphics,
                widgets.secondaryFrameX,
                widgets.secondaryFrameY,
                widgets.secondaryFrameWidth,
                angleFieldValid
                    ? cameraStep.angleDegrees()
                        + "°"
                    : "Invalid",
                angleFieldValid
                    ? 0xFFF4F0F7
                    : 0xFFE66777
            );
        }

        if (
            widgets.durationField.visible
                && !widgets.durationField.isFocused()
        ) {
            coverFrameAndRenderValue(
                graphics,
                widgets.primaryFrameX,
                widgets.primaryFrameY,
                widgets.primaryFrameWidth,
                durationFieldValid
                    ? ScenarioValueFormatter.durationValue(
                        step.durationTicks()
                    )
                    : "Invalid",
                durationFieldValid
                    ? 0xFFF4F0F7
                    : 0xFFE66777
            );
        }

        if (
            widgets.countField.visible
                && !widgets.countField.isFocused()
        ) {
            coverFrameAndRenderValue(
                graphics,
                widgets.primaryFrameX,
                widgets.primaryFrameY,
                widgets.primaryFrameWidth,
                countFieldValid
                    ? ScenarioStepPresentation.countDisplayValue(step)
                    : "Invalid",
                countFieldValid
                    ? 0xFFF4F0F7
                    : 0xFFE66777
            );
        }
    }
    private void coverFrameAndRenderValue(
        GuiGraphics graphics,
        int frameX,
        int frameY,
        int frameWidth,
        String value,
        int color
    ) {
        graphics.fill(
            frameX + 1,
            frameY + 1,
            frameX + frameWidth - 1,
            frameY + BUTTON_HEIGHT - 1,
            0xFF100E16
        );

        renderCenteredFrameText(
            graphics,
            frameX,
            frameY,
            frameWidth,
            value,
            color
        );
    }
    private void renderCenteredFrameText(
        GuiGraphics graphics,
        int frameX,
        int frameY,
        int frameWidth,
        String value,
        int color
    ) {
        Component text =
            Component.literal(value);

        int textX =
            frameX
                + (
                    frameWidth
                        - font.width(text)
                ) / 2;

        int textY =
            frameY
                + (
                    BUTTON_HEIGHT
                        - font.lineHeight
                ) / 2
                + 1;

        graphics.drawString(
            font,
            text,
            textX,
            textY,
            color,
            false
        );
    }

    public void setDirection(
        MoveDirection direction
    ) {
        stopRunningTest.run();
        state.setMoveDirection(direction);
        refreshEditor();
    }
    public void setMoveMode(
        MoveMode mode
    ) {
        stopRunningTest.run();
        state.setMoveMode(mode);
        refreshEditor();
    }
    public void toggleMoveJumping() {
        stopRunningTest.run();
        state.toggleMoveJumping();
        refreshEditor();
    }
    public void setJumpMode(
        JumpMode mode
    ) {
        stopRunningTest.run();
        state.setJumpMode(mode);
        syncValueFields();
        refreshEditor();
    }
    public void setJumpStopMode(
        JumpStopMode stopMode
    ) {
        stopRunningTest.run();
        state.setJumpStopMode(stopMode);
        syncValueFields();
        refreshEditor();
    }
    public void setRepeatMode(
        RepeatMode mode
    ) {
        stopRunningTest.run();
        state.setRepeatMode(mode);
        syncCountField();
        refreshEditor();
    }
    public void setCameraDirection(
        CameraDirection direction
    ) {
        stopRunningTest.run();
        state.setCameraDirection(direction);
        refreshEditor();
    }
    public void setCameraMotion(
        CameraMotion motion
    ) {
        stopRunningTest.run();
        state.setCameraMotion(motion);
        syncValueFields();
        refreshEditor();
    }
    public void setMouseAction(
        MouseAction action
    ) {
        stopRunningTest.run();
        state.setMouseAction(action);
        refreshEditor();
    }
    public void setMouseInputMode(
        MouseInputMode inputMode
    ) {
        stopRunningTest.run();
        state.setMouseInputMode(inputMode);
        syncValueFields();
        refreshEditor();
    }
    public void setMouseStopMode(
        MouseStopMode stopMode
    ) {
        stopRunningTest.run();
        state.setMouseStopMode(stopMode);
        syncValueFields();
        refreshEditor();
    }
    public void changeCps(
        int direction
    ) {
        stopRunningTest.run();
        state.changeCps(direction);
        refreshEditor();
    }
    public void changeAngle(
        int direction
    ) {
        stopRunningTest.run();
        state.changeAngle(direction);
        syncAngleField();
        refreshEditor();
    }
    public void changePrimaryValue(
        int direction
    ) {
        stopRunningTest.run();
        state.changePrimaryValue(direction);
        syncValueFields();
        refreshEditor();
    }
    @Override
    public void openInventorySlotSelection() {
        stopRunningTest.run();
        openInventorySlotSelection.run();
    }

    @Override
    public void openDepositItemsSelection() {
        stopRunningTest.run();
        openDepositItemsSelection.run();
    }

    public void onDurationFieldChanged(
        String value
    ) {
        if (syncingDurationField) {
            return;
        }

        stopRunningTest.run();

        try {
            double seconds =
                Double.parseDouble(value);

            int durationTicks =
                (int) Math.round(
                    seconds * 20.0
                );

            if (
                !Double.isFinite(seconds)
                    || durationTicks
                        < ScenarioEditorState.MIN_DURATION_TICKS
                    || durationTicks
                        > ScenarioEditorState.MAX_DURATION_TICKS
            ) {
                durationFieldValid = false;
                refreshEditor();
                return;
            }

            state.setDurationTicks(
                durationTicks
            );

            durationFieldValid = true;
        } catch (
            NumberFormatException exception
        ) {
            durationFieldValid = false;
        }

        refreshEditor();
    }
    public void onCountFieldChanged(
        String value
    ) {
        if (syncingCountField) {
            return;
        }

        stopRunningTest.run();

        try {
            int count =
                Integer.parseInt(value);

            if (count < 1 || count > 100000) {
                countFieldValid = false;
                refreshEditor();
                return;
            }

            state.setCount(count);
            countFieldValid = true;
        } catch (
            NumberFormatException exception
        ) {
            countFieldValid = false;
        }

        refreshEditor();
    }
    public void onAngleFieldChanged(
        String value
    ) {
        if (syncingAngleField) {
            return;
        }

        stopRunningTest.run();

        try {
            int angle =
                Integer.parseInt(value);

            if (
                angle < CameraStep.MIN_ANGLE_DEGREES
                    || angle > CameraStep.MAX_ANGLE_DEGREES
            ) {
                angleFieldValid = false;
                refreshEditor();
                return;
            }

            state.setAngle(angle);
            angleFieldValid = true;
        } catch (
            NumberFormatException exception
        ) {
            angleFieldValid = false;
        }

        refreshEditor();
    }

    @Override
    public void testSelectedStep() {
        testSelectedStep.run();
        refreshEditor();
    }

    @Override
    public void resetSelectedStep() {
        resetSelectedStep.run();
    }

    @Override
    public void moveSelectedLeft() {
        moveSelectedLeft.run();
    }

    @Override
    public void moveSelectedRight() {
        moveSelectedRight.run();
    }

    @Override
    public void duplicateSelectedStep() {
        duplicateSelectedStep.run();
    }

    @Override
    public void deleteSelectedStep() {
        deleteSelectedStep.run();
    }

    public void syncSelectedStep() {
        widgets.collapseDropdowns();
        syncValueFields();
        update();
    }

    private void syncValueFields() {
        syncDurationField();
        syncCountField();
        syncAngleField();
    }
    private void syncDurationField() {
        if (widgets.durationField == null) {
            return;
        }

        syncingDurationField = true;

        widgets.durationField.setValue(
            ScenarioValueFormatter.durationForField(
                getSelectedStep().durationTicks()
            )
        );

        syncingDurationField = false;
        durationFieldValid = true;
    }
    private void syncCountField() {
        if (widgets.countField == null) {
            return;
        }

        ScenarioStep step = getSelectedStep();
        syncingCountField = true;

        if (step instanceof JumpStep jumpStep) {
            widgets.countField.setValue(
                Integer.toString(
                    jumpStep.jumpCount()
                )
            );
        } else if (step instanceof MouseStep mouseStep) {
            widgets.countField.setValue(
                Integer.toString(
                    mouseStep.clickCount()
                )
            );
        } else if (step instanceof RepeatStep repeatStep) {
            widgets.countField.setValue(
                Integer.toString(
                    repeatStep.repeatCount()
                )
            );
        }

        syncingCountField = false;
        countFieldValid = true;
    }
    private void syncAngleField() {
        if (widgets.angleField == null) {
            return;
        }

        if (getSelectedStep() instanceof CameraStep cameraStep) {
            syncingAngleField = true;

            widgets.angleField.setValue(
                Integer.toString(
                    cameraStep.angleDegrees()
                )
            );

            syncingAngleField = false;
        }

        angleFieldValid = true;
    }

    public void update() {
        ScenarioStep step = getSelectedStep();

        widgets.update(
            step,
            visible,
            step.isInfinite()
                && state.selectedIndex() < state.size() - 1
        );

        TaskStatus status = TaskManager.getStatus();

        widgets.testButton.setMessage(
            Component.literal(
                status == TaskStatus.IDLE
                    ? step instanceof RepeatStep
                        ? layout.mode() == ScenarioInspectorLayout.Mode.WIDE
                            ? "Test Group"
                            : "Test"
                        : layout.mode() == ScenarioInspectorLayout.Mode.WIDE
                            ? "Test Step"
                            : "Test"
                    : layout.mode() == ScenarioInspectorLayout.Mode.WIDE
                        ? "Stop Test"
                        : "Stop"
            )
        );

        widgets.testButton.setStyle(
            status == TaskStatus.IDLE
                ? KarakuriButton.Style.SUCCESS
                : KarakuriButton.Style.DANGER
        );

        widgets.testButton.active =
            status != TaskStatus.IDLE
                || !state.selectedGroupHasInfiniteStepBeforeEnd();

        widgets.inventorySlotButton.active =
            status == TaskStatus.IDLE;

        widgets.resetButton.active =
            status == TaskStatus.IDLE
                && !state.isSelectedStepDefault();

        widgets.moveLeftButton.active =
            status == TaskStatus.IDLE
                && state.canMoveSelectedStep(-1);

        widgets.moveRightButton.active =
            status == TaskStatus.IDLE
                && state.canMoveSelectedStep(1);

        widgets.duplicateButton.active =
            status == TaskStatus.IDLE;

        widgets.deleteButton.active =
            status == TaskStatus.IDLE
                && state.canRemoveSelectedStep();
    }

    @Override
    public void dropdownStateChanged() {
        update();
    }

    private boolean isPrimaryValueValid(
        ScenarioStep step
    ) {
        if (ScenarioStepRules.usesDuration(step)) {
            return durationFieldValid;
        }

        if (ScenarioStepRules.usesCount(step)) {
            return countFieldValid;
        }

        return true;
    }

    private void refreshEditor() {
        update();
        editorChanged.run();
    }

    private ScenarioStep getSelectedStep() {
        return state.selectedStep();
    }
}