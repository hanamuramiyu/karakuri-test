package hanamuramiyu.karakuri.ui.editor.inspector;

import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.CameraStep;
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
import hanamuramiyu.karakuri.scenario.model.ScenarioFormat;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorState;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepPresentation;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepRules;
import hanamuramiyu.karakuri.ui.editor.ScenarioValueFormatter;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public final class ScenarioInspector
    implements ScenarioInspectorWidgets.Actions {
    private static final int BUTTON_HEIGHT = 22;

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

    public void setVisible(
        boolean visible
    ) {
        this.visible = visible;
        update();
    }

    public boolean isValid() {
        return validationMessage() == null;
    }

    public String validationMessage() {
        ScenarioStep step =
            getSelectedStep();

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
            return step instanceof JumpStep
                ? "Jump count must be between 1 and 100000"
                : "Click count must be between 1 and 100000";
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
            0xFF121018
        );

        graphics.renderOutline(
            inspectorX,
            inspectorY,
            inspectorWidth,
            inspectorHeight,
            0xFF393243
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
                "Inspector"
            ),
            inspectorX + 10,
            inspectorY + 8,
            0xFFF1ECF5,
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
            0xFF81778A,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                ScenarioStepPresentation.inspectorTitle(step)
            ),
            inspectorX + 10,
            inspectorY + 24,
            0xFFF4F0F7,
            false
        );

        if (
            layout.mode()
                == ScenarioInspectorLayout.Mode.WIDE
        ) {
            renderWideInspectorLabels(
                graphics,
                step
            );
        } else {
            renderCompactInspectorLabels(
                graphics,
                step
            );
        }

        renderValueFrames(
            graphics,
            step
        );
    }
    private void renderWideInspectorLabels(
        GuiGraphics graphics,
        ScenarioStep step
    ) {
        if (
            step
                instanceof
                JumpStep jumpStep
        ) {
            drawInspectorLabel(
                graphics,
                "Jump mode",
                46
            );

            if (
                jumpStep.mode()
                    != JumpMode.SINGLE
            ) {
                drawInspectorLabel(
                    graphics,
                    "Stop after",
                    98
                );
            }

            String primaryLabel =
                ScenarioStepPresentation.jumpPrimaryValueLabel(
                    jumpStep
                );

            if (primaryLabel != null) {
                drawInspectorLabel(
                    graphics,
                    primaryLabel,
                    150
                );
            }

            graphics.drawString(
                font,
                Component.literal(
                    ScenarioStepPresentation.jumpDescription(
                        jumpStep
                    )
                ),
                inspectorX + 10,
                inspectorY + 216,
                jumpStep.isInfinite()
                    && state.selectedIndex()
                        < state.size() - 1
                        ? 0xFFE66777
                        : 0xFF81798E,
                false
            );

            return;
        }

        if (
            step
                instanceof
                HotbarStep
        ) {
            drawInspectorLabel(
                graphics,
                "Hotbar slot",
                150
            );

            graphics.drawString(
                font,
                Component.literal(
                    "Selects the item held in the main hand"
                ),
                inspectorX + 10,
                inspectorY + 216,
                0xFF81798E,
                false
            );

            return;
        }

        if (
            step
                instanceof
                MoveStep moveStep
        ) {
            drawInspectorLabel(
                graphics,
                "Direction",
                46
            );

            drawInspectorLabel(
                graphics,
                "Movement style",
                98
            );

            drawInspectorLabel(
                graphics,
                "Jumping",
                124
            );

            drawInspectorLabel(
                graphics,
                "Duration in seconds",
                150
            );

            graphics.drawString(
                font,
                Component.literal(
                    moveStep.jumping()
                        ? "Jump is held together with movement"
                        : "Jump is disabled for this movement"
                ),
                inspectorX + 10,
                inspectorY + 216,
                0xFF81798E,
                false
            );

            return;
        }

        if (
            step
                instanceof
                CameraStep cameraStep
        ) {
            drawInspectorLabel(
                graphics,
                "Direction",
                46
            );

            drawInspectorLabel(
                graphics,
                "Motion",
                98
            );

            drawInspectorLabel(
                graphics,
                "Angle",
                124
            );

            if (
                cameraStep.motion()
                    == CameraMotion
                        .SMOOTH
            ) {
                drawInspectorLabel(
                    graphics,
                    "Duration in seconds",
                    150
                );
            }

            graphics.drawString(
                font,
                Component.literal(
                    cameraStep.motion()
                        == CameraMotion
                            .INSTANT
                                ? "Rotation is applied immediately"
                                : "Smooth camera movement"
                ),
                inspectorX + 10,
                inspectorY + 216,
                0xFF81798E,
                false
            );

            return;
        }

        if (
            step
                instanceof
                WaitStep
        ) {
            drawInspectorLabel(
                graphics,
                "Duration in seconds",
                150
            );

            return;
        }

        MouseStep mouseStep =
            (MouseStep) step;

        drawInspectorLabel(
            graphics,
            "Mouse button",
            46
        );

        drawInspectorLabel(
            graphics,
            "Input mode",
            72
        );

        drawInspectorLabel(
            graphics,
            "Stop after",
            98
        );

        if (
            mouseStep.inputMode()
                == MouseInputMode
                    .CLICK
        ) {
            drawInspectorLabel(
                graphics,
                "Click rate",
                124
            );
        }

        String primaryLabel =
            ScenarioStepPresentation.mousePrimaryValueLabel(
                mouseStep
            );

        if (primaryLabel != null) {
            drawInspectorLabel(
                graphics,
                primaryLabel,
                150
            );
        }

        graphics.drawString(
            font,
            Component.literal(
                ScenarioStepPresentation.mouseEstimate(
                    mouseStep
                )
            ),
            inspectorX + 10,
            inspectorY + 216,
            mouseStep.isInfinite()
                && state.selectedIndex()
                    < state.size() - 1
                        ? 0xFFE66777
                        : 0xFF81798E,
            false
        );
    }
    private void renderCompactInspectorLabels(
        GuiGraphics graphics,
        ScenarioStep step
    ) {
        graphics.drawString(
            font,
            Component.literal(
                ScenarioStepPresentation.compactInspectorLabel(
                    step
                )
            ),
            inspectorX + 8,
            inspectorY + 32,
            0xFF918699,
            false
        );
    }
    private void drawInspectorLabel(
        GuiGraphics graphics,
        String label,
        int offsetY
    ) {
        graphics.drawString(
            font,
            Component.literal(label),
            inspectorX + 10,
            inspectorY + offsetY,
            0xFF918699,
            false
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
                0xFFF4F0F7
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
                0xFFF4F0F7
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
    public void duplicateSelectedStep() {
        duplicateSelectedStep.run();
    }

    @Override
    public void deleteSelectedStep() {
        deleteSelectedStep.run();
    }

    public void syncSelectedStep() {
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
        boolean inspectorVisible =
            visible;

        ScenarioStep step =
            getSelectedStep();

        boolean movement =
            step
                instanceof
                MoveStep;

        boolean jump =
            step
                instanceof
                JumpStep;

        boolean camera =
            step
                instanceof
                CameraStep;

        boolean mouse =
            step
                instanceof
                MouseStep;

        boolean hotbar =
            step
                instanceof
                HotbarStep;

        boolean mouseClickMode =
            mouse
                && (
                    (MouseStep)
                        step
                ).inputMode()
                    == MouseInputMode
                        .CLICK;

        boolean jumpSingleMode =
            jump
                && (
                    (JumpStep)
                        step
                ).mode()
                    == JumpMode
                        .SINGLE;

        boolean jumpRepeatMode =
            jump
                && (
                    (JumpStep)
                        step
                ).mode()
                    == JumpMode
                        .REPEAT;

        boolean cpsUsed =
            ScenarioStepRules.usesCps(step);

        boolean angleUsed =
            ScenarioStepRules.usesAngle(step);

        boolean durationUsed =
            ScenarioStepRules.usesDuration(step);

        boolean countUsed =
            ScenarioStepRules.usesCount(step);

        boolean primaryValueUsed =
            durationUsed
                || countUsed
                || hotbar;

        widgets.forwardDirectionButton.visible =
            inspectorVisible && movement;

        widgets.backwardDirectionButton.visible =
            inspectorVisible && movement;

        widgets.leftDirectionButton.visible =
            inspectorVisible && movement;

        widgets.rightDirectionButton.visible =
            inspectorVisible && movement;

        widgets.walkModeButton.visible =
            inspectorVisible && movement;

        widgets.sprintModeButton.visible =
            inspectorVisible && movement;

        widgets.sneakModeButton.visible =
            inspectorVisible && movement;

        widgets.jumpToggleButton.visible =
            inspectorVisible && movement;

        widgets.singleJumpModeButton.visible =
            inspectorVisible && jump;

        widgets.holdJumpModeButton.visible =
            inspectorVisible && jump;

        widgets.repeatJumpModeButton.visible =
            inspectorVisible && jump;

        widgets.jumpDurationStopButton.visible =
            inspectorVisible
                && jump
                && !jumpSingleMode;

        widgets.jumpCountStopButton.visible =
            inspectorVisible
                && jumpRepeatMode;

        widgets.jumpManualStopButton.visible =
            inspectorVisible
                && jump
                && !jumpSingleMode;

        widgets.cameraLeftButton.visible =
            inspectorVisible && camera;

        widgets.cameraRightButton.visible =
            inspectorVisible && camera;

        widgets.cameraUpButton.visible =
            inspectorVisible && camera;

        widgets.cameraDownButton.visible =
            inspectorVisible && camera;

        widgets.instantMotionButton.visible =
            inspectorVisible && camera;

        widgets.smoothMotionButton.visible =
            inspectorVisible && camera;

        widgets.leftMouseButton.visible =
            inspectorVisible && mouse;

        widgets.rightMouseButton.visible =
            inspectorVisible && mouse;

        widgets.holdModeButton.visible =
            inspectorVisible && mouse;

        widgets.clickModeButton.visible =
            inspectorVisible && mouse;

        widgets.durationStopButton.visible =
            inspectorVisible && mouse;

        widgets.clickCountStopButton.visible =
            inspectorVisible
                && mouseClickMode;

        widgets.manualStopButton.visible =
            inspectorVisible && mouse;

        widgets.cpsDecreaseButton.visible =
            inspectorVisible && cpsUsed;

        widgets.cpsIncreaseButton.visible =
            inspectorVisible && cpsUsed;

        widgets.angleDecreaseButton.visible =
            inspectorVisible && angleUsed;

        widgets.angleField.visible =
            inspectorVisible && angleUsed;

        widgets.angleIncreaseButton.visible =
            inspectorVisible && angleUsed;

        widgets.primaryDecreaseButton.visible =
            inspectorVisible
                && primaryValueUsed;

        widgets.primaryIncreaseButton.visible =
            inspectorVisible
                && primaryValueUsed;

        widgets.durationField.visible =
            inspectorVisible
                && durationUsed;

        widgets.countField.visible =
            inspectorVisible
                && countUsed;

        widgets.testButton.visible =
            inspectorVisible;

        widgets.duplicateButton.visible =
            inspectorVisible;

        widgets.deleteButton.visible =
            inspectorVisible;

        if (
            step
                instanceof
                MoveStep moveStep
        ) {
            updateSelectorButton(
                widgets.forwardDirectionButton,
                moveStep.direction()
                    == MoveDirection
                        .FORWARD
            );

            updateSelectorButton(
                widgets.backwardDirectionButton,
                moveStep.direction()
                    == MoveDirection
                        .BACKWARD
            );

            updateSelectorButton(
                widgets.leftDirectionButton,
                moveStep.direction()
                    == MoveDirection
                        .LEFT
            );

            updateSelectorButton(
                widgets.rightDirectionButton,
                moveStep.direction()
                    == MoveDirection
                        .RIGHT
            );

            updateSelectorButton(
                widgets.walkModeButton,
                moveStep.mode()
                    == MoveMode.WALK
            );

            updateSelectorButton(
                widgets.sprintModeButton,
                moveStep.mode()
                    == MoveMode.SPRINT
            );

            updateSelectorButton(
                widgets.sneakModeButton,
                moveStep.mode()
                    == MoveMode.SNEAK
            );

            widgets.jumpToggleButton.setMessage(
                Component.literal(
                    layout.mode()
                        == ScenarioInspectorLayout.Mode.WIDE
                            ? "Jumping: "
                                + (
                                    moveStep.jumping()
                                        ? "On"
                                        : "Off"
                                )
                            : "Jump"
                )
            );

            updateSelectorButton(
                widgets.jumpToggleButton,
                moveStep.jumping()
            );
        }

        if (
            step
                instanceof
                JumpStep jumpStep
        ) {
            updateSelectorButton(
                widgets.singleJumpModeButton,
                jumpStep.mode()
                    == JumpMode.SINGLE
            );

            updateSelectorButton(
                widgets.holdJumpModeButton,
                jumpStep.mode()
                    == JumpMode.HOLD
            );

            updateSelectorButton(
                widgets.repeatJumpModeButton,
                jumpStep.mode()
                    == JumpMode.REPEAT
            );

            updateSelectorButton(
                widgets.jumpDurationStopButton,
                jumpStep.stopMode()
                    == JumpStopMode.DURATION
            );

            updateSelectorButton(
                widgets.jumpCountStopButton,
                jumpStep.stopMode()
                    == JumpStopMode.JUMP_COUNT
            );

            updateSelectorButton(
                widgets.jumpManualStopButton,
                jumpStep.stopMode()
                    == JumpStopMode.MANUAL
            );
        }

        if (
            step
                instanceof
                CameraStep cameraStep
        ) {
            updateSelectorButton(
                widgets.cameraLeftButton,
                cameraStep.direction()
                    == CameraDirection
                        .LEFT
            );

            updateSelectorButton(
                widgets.cameraRightButton,
                cameraStep.direction()
                    == CameraDirection
                        .RIGHT
            );

            updateSelectorButton(
                widgets.cameraUpButton,
                cameraStep.direction()
                    == CameraDirection
                        .UP
            );

            updateSelectorButton(
                widgets.cameraDownButton,
                cameraStep.direction()
                    == CameraDirection
                        .DOWN
            );

            updateSelectorButton(
                widgets.instantMotionButton,
                cameraStep.motion()
                    == CameraMotion
                        .INSTANT
            );

            updateSelectorButton(
                widgets.smoothMotionButton,
                cameraStep.motion()
                    == CameraMotion
                        .SMOOTH
            );

            widgets.angleDecreaseButton.active =
                cameraStep.angleDegrees()
                    > CameraStep
                        .MIN_ANGLE_DEGREES;

            widgets.angleIncreaseButton.active =
                cameraStep.angleDegrees()
                    < CameraStep
                        .MAX_ANGLE_DEGREES;
        }

        if (
            step
                instanceof
                MouseStep mouseStep
        ) {
            updateSelectorButton(
                widgets.leftMouseButton,
                mouseStep.action()
                    == MouseAction
                        .LEFT_CLICK
            );

            updateSelectorButton(
                widgets.rightMouseButton,
                mouseStep.action()
                    == MouseAction
                        .RIGHT_CLICK
            );

            updateSelectorButton(
                widgets.holdModeButton,
                mouseStep.inputMode()
                    == MouseInputMode
                        .HOLD
            );

            updateSelectorButton(
                widgets.clickModeButton,
                mouseStep.inputMode()
                    == MouseInputMode
                        .CLICK
            );

            updateSelectorButton(
                widgets.durationStopButton,
                mouseStep.stopMode()
                    == MouseStopMode
                        .DURATION
            );

            updateSelectorButton(
                widgets.clickCountStopButton,
                mouseStep.stopMode()
                    == MouseStopMode
                        .CLICK_COUNT
            );

            updateSelectorButton(
                widgets.manualStopButton,
                mouseStep.stopMode()
                    == MouseStopMode
                        .MANUAL
            );

            widgets.cpsDecreaseButton.active =
                mouseStep
                    .clicksPerSecondHalfSteps()
                    > MouseStep
                        .MIN_CPS_HALF_STEPS;

            widgets.cpsIncreaseButton.active =
                mouseStep
                    .clicksPerSecondHalfSteps()
                    < MouseStep
                        .MAX_CPS_HALF_STEPS;
        }

        if (durationUsed) {
            widgets.primaryDecreaseButton.active =
                step.durationTicks()
                    > ScenarioEditorState.MIN_DURATION_TICKS;

            widgets.primaryIncreaseButton.active =
                step.durationTicks()
                    < ScenarioEditorState.MAX_DURATION_TICKS;
        } else if (
            step
                instanceof
                JumpStep jumpStep
                && countUsed
        ) {
            widgets.primaryDecreaseButton.active =
                jumpStep.jumpCount()
                    > JumpStep
                        .MIN_JUMP_COUNT;

            widgets.primaryIncreaseButton.active =
                jumpStep.jumpCount()
                    < JumpStep
                        .MAX_JUMP_COUNT;
        } else if (
            step
                instanceof
                MouseStep mouseStep
                && countUsed
        ) {
            widgets.primaryDecreaseButton.active =
                mouseStep.clickCount()
                    > MouseStep
                        .MIN_CLICK_COUNT;

            widgets.primaryIncreaseButton.active =
                mouseStep.clickCount()
                    < MouseStep
                        .MAX_CLICK_COUNT;
        } else if (
            step
                instanceof
                HotbarStep hotbarStep
        ) {
            widgets.primaryDecreaseButton.active =
                hotbarStep.slot()
                    > HotbarStep
                        .MIN_SLOT;

            widgets.primaryIncreaseButton.active =
                hotbarStep.slot()
                    < HotbarStep
                        .MAX_SLOT;
        }

        TaskStatus status =
            TaskManager.getStatus();

        widgets.testButton.setMessage(
            Component.literal(
                status == TaskStatus.IDLE
                    ? layout.mode()
                        == ScenarioInspectorLayout.Mode.WIDE
                            ? "Test Step"
                            : "Test"
                    : layout.mode()
                        == ScenarioInspectorLayout.Mode.WIDE
                            ? "Stop Test"
                            : "Stop"
            )
        );

        widgets.testButton.setStyle(
            status == TaskStatus.IDLE
                ? KarakuriButton.Style.SUCCESS
                : KarakuriButton.Style.DANGER
        );

        widgets.duplicateButton.active =
            status == TaskStatus.IDLE;

        widgets.deleteButton.active =
            status == TaskStatus.IDLE
                && state.size() > 1;
    }

    private void updateSelectorButton(
        KarakuriButton button,
        boolean selected
    ) {
        button.setStyle(
            selected
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.GHOST
        );
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