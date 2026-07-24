package hanamuramiyu.karakuri.ui.editor.inspector;

import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
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
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorState;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorTheme;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepPresentation;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepRules;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import hanamuramiyu.karakuri.ui.widget.KarakuriDropdown;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ScenarioInspectorWidgets {
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_HEIGHT = 20;
    private static final int WIDE_ROW_STRIDE = 40;
    private static final int COMPACT_ROW_STRIDE = 22;

    private final Font font;
    private final ScenarioInspectorLayout.Mode layoutMode;
    private final Actions actions;
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private final List<KarakuriDropdown<?>> dropdowns = new ArrayList<>();
    private final List<AbstractWidget> regularWidgets = new ArrayList<>();

    private final int inspectorX;
    private final int inspectorY;
    private final int inspectorWidth;
    private final int inspectorHeight;
    private final int contentX;
    private final int contentWidth;

    private String description;
    private int descriptionX;
    private int descriptionY;
    private int descriptionColor;
    private boolean infiniteWarning;

    int secondaryFrameX;
    int secondaryFrameY;
    int secondaryFrameWidth;
    int primaryFrameX;
    int primaryFrameY;
    int primaryFrameWidth;

    final KarakuriDropdown<MoveDirection> moveDirectionDropdown;
    final KarakuriDropdown<MoveMode> moveModeDropdown;
    final KarakuriDropdown<JumpMode> jumpModeDropdown;
    final KarakuriDropdown<JumpStopMode> jumpStopDropdown;
    final KarakuriDropdown<RepeatMode> repeatModeDropdown;
    final KarakuriDropdown<CameraDirection> cameraDirectionDropdown;
    final KarakuriDropdown<CameraMotion> cameraMotionDropdown;
    final KarakuriDropdown<MouseAction> mouseActionDropdown;
    final KarakuriDropdown<MouseInputMode> mouseInputDropdown;
    final KarakuriDropdown<MouseStopMode> mouseStopDropdown;

    KarakuriButton jumpToggleButton;
    KarakuriButton inventorySlotButton;
    KarakuriButton cpsDecreaseButton;
    KarakuriButton cpsIncreaseButton;
    KarakuriButton angleDecreaseButton;
    KarakuriButton angleIncreaseButton;
    KarakuriButton primaryDecreaseButton;
    KarakuriButton primaryIncreaseButton;
    KarakuriButton testButton;
    KarakuriButton resetButton;
    KarakuriButton moveLeftButton;
    KarakuriButton moveRightButton;
    KarakuriButton duplicateButton;
    KarakuriButton deleteButton;

    EditBox durationField;
    EditBox countField;
    EditBox angleField;

    ScenarioInspectorWidgets(
        Font font,
        ScenarioInspectorLayout layout,
        Actions actions
    ) {
        this.font = Objects.requireNonNull(
            font,
            "Font must not be null"
        );

        Objects.requireNonNull(
            layout,
            "Inspector layout must not be null"
        );

        this.layoutMode = layout.mode();
        this.inspectorX = layout.x();
        this.inspectorY = layout.y();
        this.inspectorWidth = layout.width();
        this.inspectorHeight = layout.height();
        this.contentX = inspectorX + 10;
        this.contentWidth = inspectorWidth - 20;
        this.actions = Objects.requireNonNull(
            actions,
            "Inspector actions must not be null"
        );

        moveDirectionDropdown = createDropdown(
            options(
                option(MoveDirection.FORWARD, "Forward"),
                option(MoveDirection.BACKWARD, "Backward"),
                option(MoveDirection.LEFT, "Left"),
                option(MoveDirection.RIGHT, "Right")
            ),
            MoveDirection.FORWARD,
            actions::setDirection
        );
        moveModeDropdown = createDropdown(
            options(
                option(MoveMode.WALK, "Walk"),
                option(MoveMode.SPRINT, "Sprint"),
                option(MoveMode.SNEAK, "Sneak")
            ),
            MoveMode.WALK,
            actions::setMoveMode
        );
        jumpModeDropdown = createDropdown(
            options(
                option(JumpMode.SINGLE, "Single"),
                option(JumpMode.HOLD, "Hold"),
                option(JumpMode.REPEAT, "Repeat")
            ),
            JumpMode.SINGLE,
            actions::setJumpMode
        );
        jumpStopDropdown = createDropdown(
            options(
                option(JumpStopMode.DURATION, "Time"),
                option(JumpStopMode.JUMP_COUNT, "Jump count"),
                option(JumpStopMode.MANUAL, "Manual")
            ),
            JumpStopMode.DURATION,
            actions::setJumpStopMode
        );
        repeatModeDropdown = createDropdown(
            options(
                option(RepeatMode.COUNT, "Count"),
                option(RepeatMode.FOREVER, "Forever")
            ),
            RepeatMode.COUNT,
            actions::setRepeatMode
        );
        cameraDirectionDropdown = createDropdown(
            options(
                option(CameraDirection.LEFT, "Turn Left"),
                option(CameraDirection.RIGHT, "Turn Right"),
                option(CameraDirection.UP, "Look Up"),
                option(CameraDirection.DOWN, "Look Down")
            ),
            CameraDirection.LEFT,
            actions::setCameraDirection
        );
        cameraMotionDropdown = createDropdown(
            options(
                option(CameraMotion.INSTANT, "Instant"),
                option(CameraMotion.SMOOTH, "Smooth")
            ),
            CameraMotion.INSTANT,
            actions::setCameraMotion
        );
        mouseActionDropdown = createDropdown(
            options(
                option(MouseAction.LEFT_CLICK, "Left"),
                option(MouseAction.RIGHT_CLICK, "Right")
            ),
            MouseAction.LEFT_CLICK,
            actions::setMouseAction
        );
        mouseInputDropdown = createDropdown(
            options(
                option(MouseInputMode.HOLD, "Hold"),
                option(MouseInputMode.CLICK, "Repeated click")
            ),
            MouseInputMode.HOLD,
            actions::setMouseInputMode
        );
        mouseStopDropdown = createDropdown(
            options(
                option(MouseStopMode.DURATION, "Time"),
                option(MouseStopMode.CLICK_COUNT, "Click count"),
                option(MouseStopMode.MANUAL, "Manual")
            ),
            MouseStopMode.DURATION,
            actions::setMouseStopMode
        );

        createRegularWidgets();

        widgets.addAll(regularWidgets);
        widgets.addAll(dropdowns);
    }

    List<AbstractWidget> widgets() {
        return List.copyOf(widgets);
    }

    boolean hasFocusedTextField() {
        return durationField != null
            && durationField.isFocused()
            || countField != null
                && countField.isFocused()
            || angleField != null
                && angleField.isFocused();
    }

    boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        KarakuriDropdown<?> expanded = expandedDropdown();

        return expanded != null
            && expanded.mouseClicked(event, doubled);
    }

    boolean collapseDropdowns() {
        boolean collapsed = expandedDropdown() != null;

        for (KarakuriDropdown<?> dropdown : dropdowns) {
            dropdown.collapse();
        }

        return collapsed;
    }

    void renderDropdownOverlay(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        KarakuriDropdown<?> expanded = expandedDropdown();

        if (expanded != null) {
            expanded.renderOverlay(
                graphics,
                mouseX,
                mouseY,
                delta
            );
        }
    }

    void renderDecorations(
        GuiGraphics graphics
    ) {
        for (Label label : labels) {
            graphics.drawString(
                font,
                Component.literal(label.text()),
                label.x(),
                label.y(),
                label.color(),
                false
            );
        }

        if (description != null) {
            graphics.drawString(
                font,
                Component.literal(description),
                descriptionX,
                descriptionY,
                descriptionColor,
                false
            );
        }
    }

    void update(
        ScenarioStep step,
        boolean visible,
        boolean infiniteWarning
    ) {
        this.infiniteWarning = infiniteWarning;
        hideAll();
        labels.clear();
        description = null;

        if (!visible) {
            collapseHiddenDropdowns();
            return;
        }

        if (layoutMode == ScenarioInspectorLayout.Mode.WIDE) {
            layoutWide(step);
        } else {
            layoutCompact(step);
        }

        for (KarakuriDropdown<?> dropdown : dropdowns) {
            if (!dropdown.visible) {
                dropdown.collapse();
            }
        }

        updateValues(step);
        updateDropdownInteractionState();
    }

    private void layoutWide(
        ScenarioStep step
    ) {
        int row1 = inspectorY + 58;
        int row2 = row1 + WIDE_ROW_STRIDE;
        int row3 = row2 + WIDE_ROW_STRIDE;
        int row4 = row3 + WIDE_ROW_STRIDE;
        int row5 = row4 + WIDE_ROW_STRIDE;

        setWidePrefixes();

        if (step instanceof MoveStep moveStep) {
            placeWideDropdown(moveDirectionDropdown, "Direction", row1);
            placeWideDropdown(moveModeDropdown, "Movement style", row2);
            placeWideButton(jumpToggleButton, "Jumping", row3);
            placeWidePrimary("Duration in seconds", row4);
            setDescription(
                moveStep.jumping()
                    ? "Jump is held together with movement"
                    : "Jump is disabled for this movement",
                row5 - 10,
                ScenarioEditorTheme.TEXT_MUTED
            );
        } else if (step instanceof JumpStep jumpStep) {
            placeWideDropdown(jumpModeDropdown, "Jump mode", row1);

            if (jumpStep.mode() != JumpMode.SINGLE) {
                placeWideDropdown(jumpStopDropdown, "Stop after", row2);
            }

            String label = ScenarioStepPresentation.jumpPrimaryValueLabel(jumpStep);
            if (label != null) {
                placeWidePrimary(label, row3);
            }

            setDescription(
                ScenarioStepPresentation.jumpDescription(jumpStep),
                row5 - 10,
                warningColor(jumpStep.isInfinite())
            );
        } else if (step instanceof RepeatStep repeatStep) {
            placeWideDropdown(repeatModeDropdown, "Repeat mode", row1);

            if (repeatStep.mode() == RepeatMode.COUNT) {
                placeWidePrimary("Repeat count", row2);
            }

            setDescription(
                ScenarioStepPresentation.repeatDescription(repeatStep),
                row4 - 10,
                warningColor(repeatStep.isInfinite())
            );
        } else if (step instanceof CameraStep cameraStep) {
            placeWideDropdown(cameraDirectionDropdown, "Direction", row1);
            placeWideDropdown(cameraMotionDropdown, "Motion", row2);
            placeWideSecondary("Angle", row3);

            if (cameraStep.motion() == CameraMotion.SMOOTH) {
                placeWidePrimary("Duration in seconds", row4);
            }

            setDescription(
                cameraStep.motion() == CameraMotion.INSTANT
                    ? "Rotation is applied immediately"
                    : "Smooth camera movement",
                row5 - 10,
                ScenarioEditorTheme.TEXT_MUTED
            );
        } else if (step instanceof MouseStep mouseStep) {
            placeWideDropdown(mouseActionDropdown, "Mouse button", row1);
            placeWideDropdown(mouseInputDropdown, "Input mode", row2);
            placeWideDropdown(mouseStopDropdown, "Stop after", row3);

            if (ScenarioStepRules.usesCps(step)) {
                placeWideSecondary("Click rate", row4);
            }

            String label = ScenarioStepPresentation.mousePrimaryValueLabel(mouseStep);
            if (label != null) {
                placeWidePrimary(label, row5);
            }

            int descriptionY = Math.min(
                inspectorY + inspectorHeight - 76,
                row5 + 26
            );
            setDescription(
                ScenarioStepPresentation.mouseEstimate(mouseStep),
                descriptionY,
                warningColor(mouseStep.isInfinite())
            );
        } else if (step instanceof HotbarStep) {
            placeWidePrimary("Hotbar slot", row1);
            setDescription(
                "Selects the item held in the main hand",
                row3 - 10,
                ScenarioEditorTheme.TEXT_MUTED
            );
        } else if (step instanceof InventorySlotStep inventorySlotStep) {
            placeWideButton(
                inventorySlotButton,
                "Inventory selection",
                row1
            );
            setDescription(
                InventorySlotStep.inventorySlotLabel(
                    inventorySlotStep.inventorySlot()
                )
                    + " will be moved to hotbar slot "
                    + (inventorySlotStep.hotbarSlot() + 1),
                row3 - 10,
                ScenarioEditorTheme.TEXT_MUTED
            );
        } else {
            placeWidePrimary("Duration in seconds", row1);
        }

        placeUtilityButtons(
            contentX,
            inspectorY
                + inspectorHeight
                - BUTTON_HEIGHT * 2
                - 12,
            contentWidth
        );
        placeActionButtons(
            contentX,
            inspectorY + inspectorHeight - BUTTON_HEIGHT - 8,
            contentWidth
        );
    }

    private void layoutCompact(
        ScenarioStep step
    ) {
        int top = inspectorY + 36;
        int row1 = top;
        int row2 = row1 + COMPACT_ROW_STRIDE;
        int row3 = row2 + COMPACT_ROW_STRIDE;
        int halfWidth = (contentWidth - BUTTON_GAP) / 2;
        int rightX = contentX + halfWidth + BUTTON_GAP;

        setCompactPrefixes();

        if (step instanceof MoveStep moveStep) {
            placeDropdown(moveDirectionDropdown, contentX, row1, halfWidth);
            placeDropdown(moveModeDropdown, rightX, row1, halfWidth);
            placeButton(jumpToggleButton, contentX, row2, halfWidth);
            placePrimary(rightX, row2, halfWidth, "Duration");
            jumpToggleButton.setMessage(
                Component.literal(
                    "Jump: " + (moveStep.jumping() ? "On" : "Off")
                )
            );
        } else if (step instanceof JumpStep jumpStep) {
            placeDropdown(jumpModeDropdown, contentX, row1, halfWidth);
            if (jumpStep.mode() != JumpMode.SINGLE) {
                placeDropdown(jumpStopDropdown, rightX, row1, halfWidth);
            }
            if (ScenarioStepRules.usesPrimaryValue(step)) {
                placePrimary(contentX, row2, contentWidth, "Value");
            }
        } else if (step instanceof RepeatStep repeatStep) {
            placeDropdown(repeatModeDropdown, contentX, row1, contentWidth);
            if (repeatStep.mode() == RepeatMode.COUNT) {
                placePrimary(contentX, row2, contentWidth, "Count");
            }
        } else if (step instanceof CameraStep cameraStep) {
            placeDropdown(cameraDirectionDropdown, contentX, row1, halfWidth);
            placeDropdown(cameraMotionDropdown, rightX, row1, halfWidth);
            placeSecondary(contentX, row2, halfWidth, "Angle");
            if (cameraStep.motion() == CameraMotion.SMOOTH) {
                placePrimary(rightX, row2, halfWidth, "Duration");
            }
        } else if (step instanceof MouseStep mouseStep) {
            placeDropdown(mouseActionDropdown, contentX, row1, halfWidth);
            placeDropdown(mouseInputDropdown, rightX, row1, halfWidth);
            placeDropdown(mouseStopDropdown, contentX, row2, halfWidth);

            if (ScenarioStepRules.usesCps(step)) {
                placeSecondary(rightX, row2, halfWidth, "Rate");
            }

            if (ScenarioStepRules.usesPrimaryValue(step)) {
                placePrimary(contentX, row3, halfWidth, "Value");
            }
        } else if (step instanceof InventorySlotStep) {
            placeButton(
                inventorySlotButton,
                contentX,
                row1,
                contentWidth
            );
        } else {
            placePrimary(contentX, row1, contentWidth, "Value");
        }

        boolean compactMouseValue =
            step instanceof MouseStep
                && ScenarioStepRules.usesPrimaryValue(step);

        placeCompactActionButtons(
            contentX,
            compactMouseValue
                ? inspectorY + inspectorHeight - 12
                : row3,
            contentWidth,
            compactMouseValue ? 12 : BUTTON_HEIGHT
        );
    }

    private void updateValues(
        ScenarioStep step
    ) {
        durationField.visible = ScenarioStepRules.usesDuration(step);
        countField.visible = ScenarioStepRules.usesCount(step);
        angleField.visible = ScenarioStepRules.usesAngle(step);

        if (step instanceof InventorySlotStep inventorySlotStep) {
            inventorySlotButton.setMessage(
                Component.literal(
                    layoutMode == ScenarioInspectorLayout.Mode.WIDE
                        ? "Choose Source and Hotbar Slot"
                        : InventorySlotStep.inventorySlotLabel(
                            inventorySlotStep.inventorySlot()
                        )
                            + " → "
                            + (inventorySlotStep.hotbarSlot() + 1)
                )
            );
        }

        if (step instanceof MoveStep moveStep) {
            moveDirectionDropdown.setValue(moveStep.direction());
            moveModeDropdown.setValue(moveStep.mode());
            jumpToggleButton.setMessage(
                Component.literal(
                    layoutMode == ScenarioInspectorLayout.Mode.WIDE
                        ? "Jumping: " + (moveStep.jumping() ? "On" : "Off")
                        : "Jump: " + (moveStep.jumping() ? "On" : "Off")
                )
            );
            jumpToggleButton.setStyle(
                moveStep.jumping()
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.SECONDARY
            );
        }

        if (step instanceof JumpStep jumpStep) {
            jumpModeDropdown.setValue(jumpStep.mode());
            jumpStopDropdown.setValue(jumpStep.stopMode());
        }

        if (step instanceof RepeatStep repeatStep) {
            repeatModeDropdown.setValue(repeatStep.mode());
        }

        if (step instanceof CameraStep cameraStep) {
            cameraDirectionDropdown.setValue(cameraStep.direction());
            cameraMotionDropdown.setValue(cameraStep.motion());
            angleDecreaseButton.active = cameraStep.angleDegrees() > CameraStep.MIN_ANGLE_DEGREES;
            angleIncreaseButton.active = cameraStep.angleDegrees() < CameraStep.MAX_ANGLE_DEGREES;
        }

        if (step instanceof MouseStep mouseStep) {
            mouseActionDropdown.setValue(mouseStep.action());
            mouseInputDropdown.setValue(mouseStep.inputMode());
            mouseStopDropdown.setValue(mouseStep.stopMode());
            cpsDecreaseButton.active = mouseStep.clicksPerSecondHalfSteps() > MouseStep.MIN_CPS_HALF_STEPS;
            cpsIncreaseButton.active = mouseStep.clicksPerSecondHalfSteps() < MouseStep.MAX_CPS_HALF_STEPS;
        }

        if (ScenarioStepRules.usesDuration(step)) {
            primaryDecreaseButton.active = step.durationTicks() > ScenarioEditorState.MIN_DURATION_TICKS;
            primaryIncreaseButton.active = step.durationTicks() < ScenarioEditorState.MAX_DURATION_TICKS;
        } else if (step instanceof RepeatStep repeatStep && ScenarioStepRules.usesCount(step)) {
            primaryDecreaseButton.active = repeatStep.repeatCount() > RepeatStep.MIN_REPEAT_COUNT;
            primaryIncreaseButton.active = repeatStep.repeatCount() < RepeatStep.MAX_REPEAT_COUNT;
        } else if (step instanceof JumpStep jumpStep && ScenarioStepRules.usesCount(step)) {
            primaryDecreaseButton.active = jumpStep.jumpCount() > JumpStep.MIN_JUMP_COUNT;
            primaryIncreaseButton.active = jumpStep.jumpCount() < JumpStep.MAX_JUMP_COUNT;
        } else if (step instanceof MouseStep mouseStep && ScenarioStepRules.usesCount(step)) {
            primaryDecreaseButton.active = mouseStep.clickCount() > MouseStep.MIN_CLICK_COUNT;
            primaryIncreaseButton.active = mouseStep.clickCount() < MouseStep.MAX_CLICK_COUNT;
        } else if (step instanceof HotbarStep hotbarStep) {
            primaryDecreaseButton.active = hotbarStep.slot() > HotbarStep.MIN_SLOT;
            primaryIncreaseButton.active = hotbarStep.slot() < HotbarStep.MAX_SLOT;
        }
    }

    private void createRegularWidgets() {
        inventorySlotButton = createButton(
            contentX,
            inspectorY,
            contentWidth,
            "Choose Source and Hotbar Slot",
            actions::openInventorySlotSelection,
            KarakuriButton.Style.SECONDARY
        );
        jumpToggleButton = createButton(
            contentX,
            inspectorY,
            contentWidth,
            "Jumping: Off",
            actions::toggleMoveJumping,
            KarakuriButton.Style.SECONDARY
        );
        cpsDecreaseButton = createButton(contentX, inspectorY, 34, "-", () -> actions.changeCps(-1), KarakuriButton.Style.GHOST);
        cpsIncreaseButton = createButton(contentX, inspectorY, 34, "+", () -> actions.changeCps(1), KarakuriButton.Style.GHOST);
        angleDecreaseButton = createButton(contentX, inspectorY, 34, "-", () -> actions.changeAngle(-1), KarakuriButton.Style.GHOST);
        angleIncreaseButton = createButton(contentX, inspectorY, 34, "+", () -> actions.changeAngle(1), KarakuriButton.Style.GHOST);
        primaryDecreaseButton = createButton(contentX, inspectorY, 34, "-", () -> actions.changePrimaryValue(-1), KarakuriButton.Style.GHOST);
        primaryIncreaseButton = createButton(contentX, inspectorY, 34, "+", () -> actions.changePrimaryValue(1), KarakuriButton.Style.GHOST);

        durationField = createField("Duration in seconds", 7, "[0-9]{0,4}(\\.[0-9]{0,2})?", actions::onDurationFieldChanged);
        countField = createField("Count", 6, "[0-9]{0,6}", actions::onCountFieldChanged);
        angleField = createField("Camera angle", 3, "[0-9]{0,3}", actions::onAngleFieldChanged);

        testButton = createButton(contentX, inspectorY, 80, "Test Step", actions::testSelectedStep, KarakuriButton.Style.SUCCESS);
        resetButton = createButton(contentX, inspectorY, 80, "Reset", actions::resetSelectedStep, KarakuriButton.Style.SECONDARY);
        moveLeftButton = createButton(contentX, inspectorY, 80, "Move Left", actions::moveSelectedLeft, KarakuriButton.Style.GHOST);
        moveRightButton = createButton(contentX, inspectorY, 80, "Move Right", actions::moveSelectedRight, KarakuriButton.Style.GHOST);
        duplicateButton = createButton(contentX, inspectorY, 80, "Duplicate", actions::duplicateSelectedStep, KarakuriButton.Style.SECONDARY);
        deleteButton = createButton(contentX, inspectorY, 80, "Delete", actions::deleteSelectedStep, KarakuriButton.Style.DANGER);

        regularWidgets.add(inventorySlotButton);
        regularWidgets.add(jumpToggleButton);
        regularWidgets.add(cpsDecreaseButton);
        regularWidgets.add(cpsIncreaseButton);
        regularWidgets.add(angleDecreaseButton);
        regularWidgets.add(angleField);
        regularWidgets.add(angleIncreaseButton);
        regularWidgets.add(primaryDecreaseButton);
        regularWidgets.add(durationField);
        regularWidgets.add(countField);
        regularWidgets.add(primaryIncreaseButton);
        regularWidgets.add(testButton);
        regularWidgets.add(resetButton);
        regularWidgets.add(moveLeftButton);
        regularWidgets.add(moveRightButton);
        regularWidgets.add(duplicateButton);
        regularWidgets.add(deleteButton);
    }

    private void placeWideDropdown(
        KarakuriDropdown<?> dropdown,
        String label,
        int y
    ) {
        addLabel(label, contentX, y - 12);
        placeDropdown(dropdown, contentX, y, contentWidth);
    }

    private void placeWideButton(
        KarakuriButton button,
        String label,
        int y
    ) {
        addLabel(label, contentX, y - 12);
        placeButton(button, contentX, y, contentWidth);
    }

    private void placeWideSecondary(
        String label,
        int y
    ) {
        addLabel(label, contentX, y - 12);
        placeSecondary(contentX, y, contentWidth, label);
    }

    private void placeWidePrimary(
        String label,
        int y
    ) {
        addLabel(label, contentX, y - 12);
        placePrimary(contentX, y, contentWidth, label);
    }

    private void placeDropdown(
        KarakuriDropdown<?> dropdown,
        int x,
        int y,
        int width
    ) {
        dropdown.setX(x);
        dropdown.setY(y);
        dropdown.setWidth(width);
        dropdown.visible = true;
        dropdown.active = true;
    }

    private void placeButton(
        KarakuriButton button,
        int x,
        int y,
        int width
    ) {
        button.setX(x);
        button.setY(y);
        button.setWidth(width);
        button.visible = true;
    }

    private void placeSecondary(
        int x,
        int y,
        int width,
        String label
    ) {
        int sideWidth = layoutMode == ScenarioInspectorLayout.Mode.WIDE ? 34 : 24;
        secondaryFrameX = x + sideWidth + 6;
        secondaryFrameY = y;
        secondaryFrameWidth = width - sideWidth * 2 - 12;

        positionButton(cpsDecreaseButton, x, y, sideWidth);
        positionButton(cpsIncreaseButton, x + width - sideWidth, y, sideWidth);
        positionButton(angleDecreaseButton, x, y, sideWidth);
        positionButton(angleIncreaseButton, x + width - sideWidth, y, sideWidth);
        positionField(angleField, secondaryFrameX + 6, y + 3, secondaryFrameWidth - 12);

        boolean angle = label.equals("Angle");
        cpsDecreaseButton.visible = !angle;
        cpsIncreaseButton.visible = !angle;
        angleDecreaseButton.visible = angle;
        angleField.visible = angle;
        angleIncreaseButton.visible = angle;
    }

    private void placePrimary(
        int x,
        int y,
        int width,
        String label
    ) {
        int sideWidth = layoutMode == ScenarioInspectorLayout.Mode.WIDE ? 34 : 24;
        primaryFrameX = x + sideWidth + 6;
        primaryFrameY = y;
        primaryFrameWidth = width - sideWidth * 2 - 12;

        positionButton(primaryDecreaseButton, x, y, sideWidth);
        positionButton(primaryIncreaseButton, x + width - sideWidth, y, sideWidth);
        positionField(durationField, primaryFrameX + 6, y + 3, primaryFrameWidth - 12);
        positionField(countField, primaryFrameX + 6, y + 3, primaryFrameWidth - 12);

        primaryDecreaseButton.visible = true;
        primaryIncreaseButton.visible = true;
    }

    private void placeUtilityButtons(
        int x,
        int y,
        int width
    ) {
        int actionWidth = (width - BUTTON_GAP * 2) / 3;

        resetButton.setMessage(
            Component.literal("Reset")
        );
        moveLeftButton.setMessage(
            Component.literal("Move Left")
        );
        moveRightButton.setMessage(
            Component.literal("Move Right")
        );

        positionButton(resetButton, x, y, actionWidth);
        positionButton(
            moveLeftButton,
            x + actionWidth + BUTTON_GAP,
            y,
            actionWidth
        );
        positionButton(
            moveRightButton,
            x + (actionWidth + BUTTON_GAP) * 2,
            y,
            actionWidth
        );

        resetButton.setHeight(BUTTON_HEIGHT);
        moveLeftButton.setHeight(BUTTON_HEIGHT);
        moveRightButton.setHeight(BUTTON_HEIGHT);
        resetButton.visible = true;
        moveLeftButton.visible = true;
        moveRightButton.visible = true;
    }

    private void placeCompactActionButtons(
        int x,
        int y,
        int width,
        int height
    ) {
        int gap = 3;
        int actionWidth = (width - gap * 5) / 6;
        KarakuriButton[] buttons = {
            testButton,
            resetButton,
            moveLeftButton,
            moveRightButton,
            duplicateButton,
            deleteButton
        };
        String[] labels = {
            "Test",
            "Reset",
            "←",
            "→",
            "Dup",
            "Del"
        };

        for (int index = 0; index < buttons.length; index++) {
            KarakuriButton button = buttons[index];
            button.setMessage(
                Component.literal(labels[index])
            );
            positionButton(
                button,
                x + index * (actionWidth + gap),
                y,
                actionWidth
            );
            button.setHeight(height);
            button.visible = true;
        }
    }

    private void placeActionButtons(
        int x,
        int y,
        int width
    ) {
        int actionWidth = (width - BUTTON_GAP * 2) / 3;
        testButton.setHeight(BUTTON_HEIGHT);
        duplicateButton.setHeight(BUTTON_HEIGHT);
        deleteButton.setHeight(BUTTON_HEIGHT);
        positionButton(testButton, x, y, actionWidth);
        positionButton(duplicateButton, x + actionWidth + BUTTON_GAP, y, actionWidth);
        positionButton(deleteButton, x + (actionWidth + BUTTON_GAP) * 2, y, actionWidth);
        testButton.visible = true;
        duplicateButton.visible = true;
        deleteButton.visible = true;
    }

    private void positionButton(
        AbstractWidget widget,
        int x,
        int y,
        int width
    ) {
        widget.setX(x);
        widget.setY(y);
        widget.setWidth(width);
    }

    private void positionField(
        EditBox field,
        int x,
        int y,
        int width
    ) {
        field.setX(x);
        field.setY(y);
        field.setWidth(width);
    }

    private void setDescription(
        String text,
        int y,
        int color
    ) {
        description = text;
        descriptionX = contentX;
        descriptionY = y;
        descriptionColor = color;
    }

    private void addLabel(
        String text,
        int x,
        int y
    ) {
        labels.add(
            new Label(
                text,
                x,
                y,
                ScenarioEditorTheme.TEXT_MUTED
            )
        );
    }

    private int warningColor(boolean infinite) {
        return infinite && infiniteWarning
            ? ScenarioEditorTheme.ERROR
            : ScenarioEditorTheme.TEXT_MUTED;
    }

    private void setWidePrefixes() {
        for (KarakuriDropdown<?> dropdown : dropdowns) {
            dropdown.setLabelPrefix("");
        }
    }

    private void setCompactPrefixes() {
        moveDirectionDropdown.setLabelPrefix("Dir: ");
        moveModeDropdown.setLabelPrefix("Style: ");
        jumpModeDropdown.setLabelPrefix("Mode: ");
        jumpStopDropdown.setLabelPrefix("Stop: ");
        repeatModeDropdown.setLabelPrefix("Repeat: ");
        cameraDirectionDropdown.setLabelPrefix("Dir: ");
        cameraMotionDropdown.setLabelPrefix("Motion: ");
        mouseActionDropdown.setLabelPrefix("Button: ");
        mouseInputDropdown.setLabelPrefix("Input: ");
        mouseStopDropdown.setLabelPrefix("Stop: ");
    }

    private void hideAll() {
        for (AbstractWidget widget : widgets) {
            widget.visible = false;
            widget.active = true;
        }
    }

    private void collapseHiddenDropdowns() {
        for (KarakuriDropdown<?> dropdown : dropdowns) {
            dropdown.collapse();
        }
    }

    private void updateDropdownInteractionState() {
        KarakuriDropdown<?> expanded = expandedDropdown();

        if (expanded == null) {
            return;
        }

        for (KarakuriDropdown<?> dropdown : dropdowns) {
            if (dropdown != expanded) {
                dropdown.collapse();
            }
        }
    }

    private KarakuriDropdown<?> expandedDropdown() {
        for (KarakuriDropdown<?> dropdown : dropdowns) {
            if (dropdown.visible && dropdown.isExpanded()) {
                return dropdown;
            }
        }

        return null;
    }

    private <T> KarakuriDropdown<T> createDropdown(
        List<KarakuriDropdown.Option<T>> options,
        T initialValue,
        java.util.function.Consumer<T> action
    ) {
        KarakuriDropdown<T> dropdown = new KarakuriDropdown<>(
            font,
            contentX,
            inspectorY,
            contentWidth,
            BUTTON_HEIGHT,
            options,
            initialValue,
            action,
            actions::dropdownStateChanged
        );
        dropdowns.add(dropdown);
        return dropdown;
    }

    @SafeVarargs
    private static <T> List<KarakuriDropdown.Option<T>> options(
        KarakuriDropdown.Option<T>... options
    ) {
        return List.of(options);
    }

    private static <T> KarakuriDropdown.Option<T> option(
        T value,
        String label
    ) {
        return new KarakuriDropdown.Option<>(value, label);
    }

    private KarakuriButton createButton(
        int x,
        int y,
        int width,
        String message,
        Runnable action,
        KarakuriButton.Style style
    ) {
        return new KarakuriButton(
            font,
            x,
            y,
            width,
            BUTTON_HEIGHT,
            Component.literal(message),
            action,
            style
        );
    }

    private EditBox createField(
        String narration,
        int maxLength,
        String regex,
        java.util.function.Consumer<String> responder
    ) {
        EditBox field = new EditBox(
            font,
            contentX,
            inspectorY,
            Math.max(24, contentWidth - 80),
            16,
            Component.literal(narration)
        );
        field.setBordered(false);
        field.setTextColor(ScenarioEditorTheme.TEXT);
        field.setTextColorUneditable(ScenarioEditorTheme.TEXT_MUTED);
        field.setTextShadow(false);
        field.setMaxLength(maxLength);
        field.setFilter(value -> value.matches(regex));
        field.setResponder(responder);
        return field;
    }

    private record Label(
        String text,
        int x,
        int y,
        int color
    ) {
    }

    interface Actions {
        void setDirection(MoveDirection direction);

        void setMoveMode(MoveMode mode);

        void toggleMoveJumping();

        void setJumpMode(JumpMode mode);

        void setJumpStopMode(JumpStopMode stopMode);

        void setRepeatMode(RepeatMode mode);

        void setCameraDirection(CameraDirection direction);

        void setCameraMotion(CameraMotion motion);

        void setMouseAction(MouseAction action);

        void setMouseInputMode(MouseInputMode inputMode);

        void setMouseStopMode(MouseStopMode stopMode);

        void changeCps(int direction);

        void changeAngle(int direction);

        void changePrimaryValue(int direction);

        void openInventorySlotSelection();

        void testSelectedStep();

        void resetSelectedStep();

        void moveSelectedLeft();

        void moveSelectedRight();

        void duplicateSelectedStep();

        void deleteSelectedStep();

        void onDurationFieldChanged(String value);

        void onCountFieldChanged(String value);

        void onAngleFieldChanged(String value);

        void dropdownStateChanged();
    }
}