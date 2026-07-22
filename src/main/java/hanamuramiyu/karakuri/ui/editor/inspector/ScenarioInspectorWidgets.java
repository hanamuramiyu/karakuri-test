package hanamuramiyu.karakuri.ui.editor.inspector;

import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.JumpMode;
import hanamuramiyu.karakuri.scenario.model.JumpStopMode;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MouseInputMode;
import hanamuramiyu.karakuri.scenario.model.MouseStopMode;
import hanamuramiyu.karakuri.scenario.model.MoveDirection;
import hanamuramiyu.karakuri.scenario.model.MoveMode;
import hanamuramiyu.karakuri.scenario.model.RepeatMode;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ScenarioInspectorWidgets {
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_HEIGHT = 22;

    private final Font font;
    private final ScenarioInspectorLayout.Mode layoutMode;
    private final Actions actions;
    private final List<AbstractWidget> widgets = new ArrayList<>();

    private final int inspectorX;
    private final int inspectorY;
    private final int inspectorWidth;
    private final int inspectorHeight;

    int secondaryFrameX;
    int secondaryFrameY;
    int secondaryFrameWidth;
    int primaryFrameX;
    int primaryFrameY;
    int primaryFrameWidth;

    KarakuriButton forwardDirectionButton;
    KarakuriButton backwardDirectionButton;
    KarakuriButton leftDirectionButton;
    KarakuriButton rightDirectionButton;
    KarakuriButton walkModeButton;
    KarakuriButton sprintModeButton;
    KarakuriButton sneakModeButton;
    KarakuriButton jumpToggleButton;
    KarakuriButton singleJumpModeButton;
    KarakuriButton holdJumpModeButton;
    KarakuriButton repeatJumpModeButton;
    KarakuriButton jumpDurationStopButton;
    KarakuriButton jumpCountStopButton;
    KarakuriButton jumpManualStopButton;
    KarakuriButton repeatCountModeButton;
    KarakuriButton repeatForeverModeButton;
    KarakuriButton cameraLeftButton;
    KarakuriButton cameraRightButton;
    KarakuriButton cameraUpButton;
    KarakuriButton cameraDownButton;
    KarakuriButton instantMotionButton;
    KarakuriButton smoothMotionButton;
    KarakuriButton leftMouseButton;
    KarakuriButton rightMouseButton;
    KarakuriButton holdModeButton;
    KarakuriButton clickModeButton;
    KarakuriButton durationStopButton;
    KarakuriButton clickCountStopButton;
    KarakuriButton manualStopButton;
    KarakuriButton cpsDecreaseButton;
    KarakuriButton cpsIncreaseButton;
    KarakuriButton angleDecreaseButton;
    KarakuriButton angleIncreaseButton;
    KarakuriButton primaryDecreaseButton;
    KarakuriButton primaryIncreaseButton;
    KarakuriButton testButton;
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

        this.actions = Objects.requireNonNull(
            actions,
            "Inspector actions must not be null"
        );

        createInspectorWidgets();
    }

    List<AbstractWidget> widgets() {
        return List.copyOf(widgets);
    }

    private void addWidget(
        AbstractWidget widget
    ) {
        widgets.add(widget);
    }

    private void createInspectorWidgets() {
        if (
            layoutMode
                == ScenarioInspectorLayout.Mode.WIDE
        ) {
            createWideInspectorWidgets();
        } else {
            createCompactInspectorWidgets();
        }

        addWidget(
            forwardDirectionButton
        );
        addWidget(
            backwardDirectionButton
        );
        addWidget(
            leftDirectionButton
        );
        addWidget(
            rightDirectionButton
        );

        addWidget(
            walkModeButton
        );
        addWidget(
            sprintModeButton
        );
        addWidget(
            sneakModeButton
        );
        addWidget(
            jumpToggleButton
        );

        addWidget(
            singleJumpModeButton
        );
        addWidget(
            holdJumpModeButton
        );
        addWidget(
            repeatJumpModeButton
        );
        addWidget(
            jumpDurationStopButton
        );
        addWidget(
            jumpCountStopButton
        );
        addWidget(
            jumpManualStopButton
        );
        addWidget(
            repeatCountModeButton
        );
        addWidget(
            repeatForeverModeButton
        );

        addWidget(
            cameraLeftButton
        );
        addWidget(
            cameraRightButton
        );
        addWidget(
            cameraUpButton
        );
        addWidget(
            cameraDownButton
        );
        addWidget(
            instantMotionButton
        );
        addWidget(
            smoothMotionButton
        );

        addWidget(
            leftMouseButton
        );
        addWidget(
            rightMouseButton
        );
        addWidget(
            holdModeButton
        );
        addWidget(
            clickModeButton
        );
        addWidget(
            durationStopButton
        );
        addWidget(
            clickCountStopButton
        );
        addWidget(
            manualStopButton
        );
        addWidget(
            cpsDecreaseButton
        );
        addWidget(
            cpsIncreaseButton
        );

        addWidget(
            angleDecreaseButton
        );
        addWidget(
            angleField
        );
        addWidget(
            angleIncreaseButton
        );

        addWidget(
            primaryDecreaseButton
        );
        addWidget(
            durationField
        );
        addWidget(
            countField
        );
        addWidget(
            primaryIncreaseButton
        );

        addWidget(
            testButton
        );
        addWidget(
            duplicateButton
        );
        addWidget(
            deleteButton
        );
    }

    private void createWideInspectorWidgets() {
        int contentX =
            inspectorX + 10;

        int contentWidth =
            inspectorWidth - 20;

        int halfWidth =
            (
                contentWidth
                    - BUTTON_GAP
            ) / 2;

        int thirdWidth =
            (
                contentWidth
                    - BUTTON_GAP * 2
            ) / 3;

        int selectorY =
            inspectorY + 58;

        forwardDirectionButton =
            createButton(
                contentX,
                selectorY,
                halfWidth,
                Component.literal(
                    "Forward"
                ),
                () -> actions.setDirection(
                    MoveDirection
                        .FORWARD
                ),
                KarakuriButton
                    .Style.GHOST
            );

        backwardDirectionButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                selectorY,
                halfWidth,
                Component.literal(
                    "Backward"
                ),
                () -> actions.setDirection(
                    MoveDirection
                        .BACKWARD
                ),
                KarakuriButton
                    .Style.GHOST
            );

        leftDirectionButton =
            createButton(
                contentX,
                selectorY + 26,
                halfWidth,
                Component.literal(
                    "Left"
                ),
                () -> actions.setDirection(
                    MoveDirection
                        .LEFT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        rightDirectionButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                selectorY + 26,
                halfWidth,
                Component.literal(
                    "Right"
                ),
                () -> actions.setDirection(
                    MoveDirection
                        .RIGHT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        singleJumpModeButton =
            createButton(
                contentX,
                selectorY,
                thirdWidth,
                Component.literal("Single"),
                () -> actions.setJumpMode(
                    JumpMode.SINGLE
                ),
                KarakuriButton
                    .Style.GHOST
            );

        holdJumpModeButton =
            createButton(
                contentX
                    + thirdWidth
                    + BUTTON_GAP,
                selectorY,
                thirdWidth,
                Component.literal("Hold"),
                () -> actions.setJumpMode(
                    JumpMode.HOLD
                ),
                KarakuriButton
                    .Style.GHOST
            );

        repeatJumpModeButton =
            createButton(
                contentX
                    + (
                        thirdWidth
                            + BUTTON_GAP
                    ) * 2,
                selectorY,
                thirdWidth,
                Component.literal("Repeat"),
                () -> actions.setJumpMode(
                    JumpMode.REPEAT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        repeatCountModeButton =
            createButton(
                contentX,
                selectorY,
                halfWidth,
                Component.literal("Count"),
                () -> actions.setRepeatMode(
                    RepeatMode.COUNT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        repeatForeverModeButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                selectorY,
                halfWidth,
                Component.literal("Forever"),
                () -> actions.setRepeatMode(
                    RepeatMode.FOREVER
                ),
                KarakuriButton
                    .Style.GHOST
            );

        cameraLeftButton =
            createButton(
                contentX,
                selectorY,
                halfWidth,
                Component.literal(
                    "Turn Left"
                ),
                () -> actions.setCameraDirection(
                    CameraDirection
                        .LEFT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        cameraRightButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                selectorY,
                halfWidth,
                Component.literal(
                    "Turn Right"
                ),
                () -> actions.setCameraDirection(
                    CameraDirection
                        .RIGHT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        cameraUpButton =
            createButton(
                contentX,
                selectorY + 26,
                halfWidth,
                Component.literal(
                    "Look Up"
                ),
                () -> actions.setCameraDirection(
                    CameraDirection
                        .UP
                ),
                KarakuriButton
                    .Style.GHOST
            );

        cameraDownButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                selectorY + 26,
                halfWidth,
                Component.literal(
                    "Look Down"
                ),
                () -> actions.setCameraDirection(
                    CameraDirection
                        .DOWN
                ),
                KarakuriButton
                    .Style.GHOST
            );

        leftMouseButton =
            createButton(
                contentX,
                selectorY,
                halfWidth,
                Component.literal(
                    "Left"
                ),
                () -> actions.setMouseAction(
                    MouseAction
                        .LEFT_CLICK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        rightMouseButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                selectorY,
                halfWidth,
                Component.literal(
                    "Right"
                ),
                () -> actions.setMouseAction(
                    MouseAction
                        .RIGHT_CLICK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        int inputModeY =
            inspectorY + 84;

        holdModeButton =
            createButton(
                contentX,
                inputModeY,
                halfWidth,
                Component.literal(
                    "Hold"
                ),
                () -> actions.setMouseInputMode(
                    MouseInputMode
                        .HOLD
                ),
                KarakuriButton
                    .Style.GHOST
            );

        clickModeButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                inputModeY,
                halfWidth,
                Component.literal(
                    "Click"
                ),
                () -> actions.setMouseInputMode(
                    MouseInputMode
                        .CLICK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        int modeY =
            inspectorY + 110;

        walkModeButton =
            createButton(
                contentX,
                modeY,
                thirdWidth,
                Component.literal(
                    "Walk"
                ),
                () -> actions.setMoveMode(
                    MoveMode.WALK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        sprintModeButton =
            createButton(
                contentX
                    + thirdWidth
                    + BUTTON_GAP,
                modeY,
                thirdWidth,
                Component.literal(
                    "Sprint"
                ),
                () -> actions.setMoveMode(
                    MoveMode.SPRINT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        sneakModeButton =
            createButton(
                contentX
                    + (
                        thirdWidth
                            + BUTTON_GAP
                    ) * 2,
                modeY,
                thirdWidth,
                Component.literal(
                    "Sneak"
                ),
                () -> actions.setMoveMode(
                    MoveMode.SNEAK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        jumpDurationStopButton =
            createButton(
                contentX,
                modeY,
                thirdWidth,
                Component.literal("Time"),
                () -> actions.setJumpStopMode(
                    JumpStopMode.DURATION
                ),
                KarakuriButton
                    .Style.GHOST
            );

        jumpCountStopButton =
            createButton(
                contentX
                    + thirdWidth
                    + BUTTON_GAP,
                modeY,
                thirdWidth,
                Component.literal("Jumps"),
                () -> actions.setJumpStopMode(
                    JumpStopMode.JUMP_COUNT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        jumpManualStopButton =
            createButton(
                contentX
                    + (
                        thirdWidth
                            + BUTTON_GAP
                    ) * 2,
                modeY,
                thirdWidth,
                Component.literal("Manual"),
                () -> actions.setJumpStopMode(
                    JumpStopMode.MANUAL
                ),
                KarakuriButton
                    .Style.GHOST
            );

        instantMotionButton =
            createButton(
                contentX,
                modeY,
                halfWidth,
                Component.literal(
                    "Instant"
                ),
                () -> actions.setCameraMotion(
                    CameraMotion
                        .INSTANT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        smoothMotionButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                modeY,
                halfWidth,
                Component.literal(
                    "Smooth"
                ),
                () -> actions.setCameraMotion(
                    CameraMotion
                        .SMOOTH
                ),
                KarakuriButton
                    .Style.GHOST
            );

        durationStopButton =
            createButton(
                contentX,
                modeY,
                thirdWidth,
                Component.literal(
                    "Time"
                ),
                () -> actions.setMouseStopMode(
                    MouseStopMode
                        .DURATION
                ),
                KarakuriButton
                    .Style.GHOST
            );

        clickCountStopButton =
            createButton(
                contentX
                    + thirdWidth
                    + BUTTON_GAP,
                modeY,
                thirdWidth,
                Component.literal(
                    "Clicks"
                ),
                () -> actions.setMouseStopMode(
                    MouseStopMode
                        .CLICK_COUNT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        manualStopButton =
            createButton(
                contentX
                    + (
                        thirdWidth
                            + BUTTON_GAP
                    ) * 2,
                modeY,
                thirdWidth,
                Component.literal(
                    "Manual"
                ),
                () -> actions.setMouseStopMode(
                    MouseStopMode
                        .MANUAL
                ),
                KarakuriButton
                    .Style.GHOST
            );

        secondaryFrameX =
            contentX + 40;

        secondaryFrameY =
            inspectorY + 136;

        secondaryFrameWidth =
            contentWidth - 80;

        jumpToggleButton =
            createButton(
                contentX,
                secondaryFrameY,
                contentWidth,
                Component.literal(
                    "Jumping: Off"
                ),
                actions::toggleMoveJumping,
                KarakuriButton
                    .Style.GHOST
            );

        cpsDecreaseButton =
            createButton(
                contentX,
                secondaryFrameY,
                34,
                Component.literal("-"),
                () -> actions.changeCps(-1),
                KarakuriButton
                    .Style.GHOST
            );

        cpsIncreaseButton =
            createButton(
                contentX
                    + contentWidth
                    - 34,
                secondaryFrameY,
                34,
                Component.literal("+"),
                () -> actions.changeCps(1),
                KarakuriButton
                    .Style.GHOST
            );

        angleDecreaseButton =
            createButton(
                contentX,
                secondaryFrameY,
                34,
                Component.literal("-"),
                () -> actions.changeAngle(-1),
                KarakuriButton
                    .Style.GHOST
            );

        angleIncreaseButton =
            createButton(
                contentX
                    + contentWidth
                    - 34,
                secondaryFrameY,
                34,
                Component.literal("+"),
                () -> actions.changeAngle(1),
                KarakuriButton
                    .Style.GHOST
            );

        primaryFrameX =
            contentX + 40;

        primaryFrameY =
            inspectorY + 162;

        primaryFrameWidth =
            contentWidth - 80;

        primaryDecreaseButton =
            createButton(
                contentX,
                primaryFrameY,
                34,
                Component.literal("-"),
                () -> actions.changePrimaryValue(
                    -1
                ),
                KarakuriButton
                    .Style.GHOST
            );

        createValueFields();

        primaryIncreaseButton =
            createButton(
                contentX
                    + contentWidth
                    - 34,
                primaryFrameY,
                34,
                Component.literal("+"),
                () -> actions.changePrimaryValue(
                    1
                ),
                KarakuriButton
                    .Style.GHOST
            );

        int actionY =
            inspectorY + 188;

        int actionWidth =
            (
                contentWidth
                    - BUTTON_GAP * 2
            ) / 3;

        testButton =
            createButton(
                contentX,
                actionY,
                actionWidth,
                Component.literal(
                    "Test Step"
                ),
                actions::testSelectedStep,
                KarakuriButton
                    .Style.SUCCESS
            );

        duplicateButton =
            createButton(
                contentX
                    + actionWidth
                    + BUTTON_GAP,
                actionY,
                actionWidth,
                Component.literal(
                    "Duplicate"
                ),
                actions::duplicateSelectedStep,
                KarakuriButton
                    .Style.SECONDARY
            );

        deleteButton =
            createButton(
                contentX
                    + (
                        actionWidth
                            + BUTTON_GAP
                    ) * 2,
                actionY,
                actionWidth,
                Component.literal(
                    "Delete"
                ),
                actions::deleteSelectedStep,
                KarakuriButton
                    .Style.DANGER
            );
    }

    private void createCompactInspectorWidgets() {
        int padding = 8;

        int contentX =
            inspectorX + padding;

        int contentWidth =
            inspectorWidth
                - padding * 2;

        int quarterWidth =
            (
                contentWidth
                    - BUTTON_GAP * 3
            ) / 4;

        int thirdWidth =
            (
                contentWidth
                    - BUTTON_GAP * 2
            ) / 3;

        int halfWidth =
            (
                contentWidth
                    - BUTTON_GAP
            ) / 2;

        int firstRowY =
            inspectorY + 42;

        forwardDirectionButton =
            createButton(
                contentX,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Forward"
                ),
                () -> actions.setDirection(
                    MoveDirection
                        .FORWARD
                ),
                KarakuriButton
                    .Style.GHOST
            );

        backwardDirectionButton =
            createButton(
                contentX
                    + quarterWidth
                    + BUTTON_GAP,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Back"
                ),
                () -> actions.setDirection(
                    MoveDirection
                        .BACKWARD
                ),
                KarakuriButton
                    .Style.GHOST
            );

        leftDirectionButton =
            createButton(
                contentX
                    + (
                        quarterWidth
                            + BUTTON_GAP
                    ) * 2,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Left"
                ),
                () -> actions.setDirection(
                    MoveDirection
                        .LEFT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        rightDirectionButton =
            createButton(
                contentX
                    + (
                        quarterWidth
                            + BUTTON_GAP
                    ) * 3,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Right"
                ),
                () -> actions.setDirection(
                    MoveDirection
                        .RIGHT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        singleJumpModeButton =
            createButton(
                contentX,
                firstRowY,
                thirdWidth,
                Component.literal("Single"),
                () -> actions.setJumpMode(
                    JumpMode.SINGLE
                ),
                KarakuriButton
                    .Style.GHOST
            );

        holdJumpModeButton =
            createButton(
                contentX
                    + thirdWidth
                    + BUTTON_GAP,
                firstRowY,
                thirdWidth,
                Component.literal("Hold"),
                () -> actions.setJumpMode(
                    JumpMode.HOLD
                ),
                KarakuriButton
                    .Style.GHOST
            );

        repeatJumpModeButton =
            createButton(
                contentX
                    + (
                        thirdWidth
                            + BUTTON_GAP
                    ) * 2,
                firstRowY,
                thirdWidth,
                Component.literal("Repeat"),
                () -> actions.setJumpMode(
                    JumpMode.REPEAT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        repeatCountModeButton =
            createButton(
                contentX,
                firstRowY,
                halfWidth,
                Component.literal("Count"),
                () -> actions.setRepeatMode(
                    RepeatMode.COUNT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        repeatForeverModeButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                firstRowY,
                halfWidth,
                Component.literal("Forever"),
                () -> actions.setRepeatMode(
                    RepeatMode.FOREVER
                ),
                KarakuriButton
                    .Style.GHOST
            );

        cameraLeftButton =
            createButton(
                contentX,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Left"
                ),
                () -> actions.setCameraDirection(
                    CameraDirection
                        .LEFT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        cameraRightButton =
            createButton(
                contentX
                    + quarterWidth
                    + BUTTON_GAP,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Right"
                ),
                () -> actions.setCameraDirection(
                    CameraDirection
                        .RIGHT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        cameraUpButton =
            createButton(
                contentX
                    + (
                        quarterWidth
                            + BUTTON_GAP
                    ) * 2,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Up"
                ),
                () -> actions.setCameraDirection(
                    CameraDirection
                        .UP
                ),
                KarakuriButton
                    .Style.GHOST
            );

        cameraDownButton =
            createButton(
                contentX
                    + (
                        quarterWidth
                            + BUTTON_GAP
                    ) * 3,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Down"
                ),
                () -> actions.setCameraDirection(
                    CameraDirection
                        .DOWN
                ),
                KarakuriButton
                    .Style.GHOST
            );

        leftMouseButton =
            createButton(
                contentX,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Left"
                ),
                () -> actions.setMouseAction(
                    MouseAction
                        .LEFT_CLICK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        rightMouseButton =
            createButton(
                contentX
                    + quarterWidth
                    + BUTTON_GAP,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Right"
                ),
                () -> actions.setMouseAction(
                    MouseAction
                        .RIGHT_CLICK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        holdModeButton =
            createButton(
                contentX
                    + (
                        quarterWidth
                            + BUTTON_GAP
                    ) * 2,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Hold"
                ),
                () -> actions.setMouseInputMode(
                    MouseInputMode
                        .HOLD
                ),
                KarakuriButton
                    .Style.GHOST
            );

        clickModeButton =
            createButton(
                contentX
                    + (
                        quarterWidth
                            + BUTTON_GAP
                    ) * 3,
                firstRowY,
                quarterWidth,
                Component.literal(
                    "Click"
                ),
                () -> actions.setMouseInputMode(
                    MouseInputMode
                        .CLICK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        int secondRowY =
            inspectorY + 68;

        walkModeButton =
            createButton(
                contentX,
                secondRowY,
                quarterWidth,
                Component.literal(
                    "Walk"
                ),
                () -> actions.setMoveMode(
                    MoveMode.WALK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        sprintModeButton =
            createButton(
                contentX
                    + quarterWidth
                    + BUTTON_GAP,
                secondRowY,
                quarterWidth,
                Component.literal(
                    "Sprint"
                ),
                () -> actions.setMoveMode(
                    MoveMode.SPRINT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        sneakModeButton =
            createButton(
                contentX
                    + (
                        quarterWidth
                            + BUTTON_GAP
                    ) * 2,
                secondRowY,
                quarterWidth,
                Component.literal(
                    "Sneak"
                ),
                () -> actions.setMoveMode(
                    MoveMode.SNEAK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        jumpToggleButton =
            createButton(
                contentX
                    + (
                        quarterWidth
                            + BUTTON_GAP
                    ) * 3,
                secondRowY,
                quarterWidth,
                Component.literal(
                    "Jump"
                ),
                actions::toggleMoveJumping,
                KarakuriButton
                    .Style.GHOST
            );

        jumpDurationStopButton =
            createButton(
                contentX,
                secondRowY,
                thirdWidth,
                Component.literal("Time"),
                () -> actions.setJumpStopMode(
                    JumpStopMode.DURATION
                ),
                KarakuriButton
                    .Style.GHOST
            );

        jumpCountStopButton =
            createButton(
                contentX
                    + thirdWidth
                    + BUTTON_GAP,
                secondRowY,
                thirdWidth,
                Component.literal("Jumps"),
                () -> actions.setJumpStopMode(
                    JumpStopMode.JUMP_COUNT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        jumpManualStopButton =
            createButton(
                contentX
                    + (
                        thirdWidth
                            + BUTTON_GAP
                    ) * 2,
                secondRowY,
                thirdWidth,
                Component.literal("Manual"),
                () -> actions.setJumpStopMode(
                    JumpStopMode.MANUAL
                ),
                KarakuriButton
                    .Style.GHOST
            );

        instantMotionButton =
            createButton(
                contentX,
                secondRowY,
                halfWidth,
                Component.literal(
                    "Instant"
                ),
                () -> actions.setCameraMotion(
                    CameraMotion
                        .INSTANT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        smoothMotionButton =
            createButton(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                secondRowY,
                halfWidth,
                Component.literal(
                    "Smooth"
                ),
                () -> actions.setCameraMotion(
                    CameraMotion
                        .SMOOTH
                ),
                KarakuriButton
                    .Style.GHOST
            );

        durationStopButton =
            createButton(
                contentX,
                secondRowY,
                thirdWidth,
                Component.literal(
                    "Time"
                ),
                () -> actions.setMouseStopMode(
                    MouseStopMode
                        .DURATION
                ),
                KarakuriButton
                    .Style.GHOST
            );

        clickCountStopButton =
            createButton(
                contentX
                    + thirdWidth
                    + BUTTON_GAP,
                secondRowY,
                thirdWidth,
                Component.literal(
                    "Clicks"
                ),
                () -> actions.setMouseStopMode(
                    MouseStopMode
                        .CLICK_COUNT
                ),
                KarakuriButton
                    .Style.GHOST
            );

        manualStopButton =
            createButton(
                contentX
                    + (
                        thirdWidth
                            + BUTTON_GAP
                    ) * 2,
                secondRowY,
                thirdWidth,
                Component.literal(
                    "Manual"
                ),
                () -> actions.setMouseStopMode(
                    MouseStopMode
                        .MANUAL
                ),
                KarakuriButton
                    .Style.GHOST
            );

        int columnGap = 10;

        int columnWidth =
            (
                contentWidth
                    - columnGap
            ) / 2;

        int leftColumnX =
            contentX;

        int rightColumnX =
            contentX
                + columnWidth
                + columnGap;

        int valueRowY =
            inspectorY + 94;

        secondaryFrameX =
            leftColumnX + 28;

        secondaryFrameY =
            valueRowY;

        secondaryFrameWidth =
            columnWidth - 56;

        cpsDecreaseButton =
            createButton(
                leftColumnX,
                valueRowY,
                24,
                Component.literal("-"),
                () -> actions.changeCps(-1),
                KarakuriButton
                    .Style.GHOST
            );

        cpsIncreaseButton =
            createButton(
                leftColumnX
                    + columnWidth
                    - 24,
                valueRowY,
                24,
                Component.literal("+"),
                () -> actions.changeCps(1),
                KarakuriButton
                    .Style.GHOST
            );

        angleDecreaseButton =
            createButton(
                leftColumnX,
                valueRowY,
                24,
                Component.literal("-"),
                () -> actions.changeAngle(-1),
                KarakuriButton
                    .Style.GHOST
            );

        angleIncreaseButton =
            createButton(
                leftColumnX
                    + columnWidth
                    - 24,
                valueRowY,
                24,
                Component.literal("+"),
                () -> actions.changeAngle(1),
                KarakuriButton
                    .Style.GHOST
            );

        primaryFrameX =
            rightColumnX + 28;

        primaryFrameY =
            valueRowY;

        primaryFrameWidth =
            columnWidth - 56;

        primaryDecreaseButton =
            createButton(
                rightColumnX,
                valueRowY,
                24,
                Component.literal("-"),
                () -> actions.changePrimaryValue(
                    -1
                ),
                KarakuriButton
                    .Style.GHOST
            );

        createValueFields();

        primaryIncreaseButton =
            createButton(
                rightColumnX
                    + columnWidth
                    - 24,
                valueRowY,
                24,
                Component.literal("+"),
                () -> actions.changePrimaryValue(
                    1
                ),
                KarakuriButton
                    .Style.GHOST
            );

        int actionRowY =
            inspectorY + 120;

        int actionWidth =
            (
                contentWidth
                    - BUTTON_GAP * 2
            ) / 3;

        testButton =
            createButton(
                contentX,
                actionRowY,
                actionWidth,
                Component.literal(
                    "Test"
                ),
                actions::testSelectedStep,
                KarakuriButton
                    .Style.SUCCESS
            );

        duplicateButton =
            createButton(
                contentX
                    + actionWidth
                    + BUTTON_GAP,
                actionRowY,
                actionWidth,
                Component.literal(
                    "Copy"
                ),
                actions::duplicateSelectedStep,
                KarakuriButton
                    .Style.SECONDARY
            );

        deleteButton =
            createButton(
                contentX
                    + (
                        actionWidth
                            + BUTTON_GAP
                    ) * 2,
                actionRowY,
                actionWidth,
                Component.literal(
                    "Delete"
                ),
                actions::deleteSelectedStep,
                KarakuriButton
                    .Style.DANGER
            );
    }

    private void createValueFields() {
        durationField = new EditBox(
            font,
            primaryFrameX + 6,
            primaryFrameY + 3,
            primaryFrameWidth - 12,
            16,
            Component.literal(
                "Duration in seconds"
            )
        );

        durationField.setBordered(false);
        durationField.setTextColor(
            0xFFF4F0F7
        );
        durationField.setTextColorUneditable(
            0xFF81798E
        );
        durationField.setTextShadow(false);
        durationField.setMaxLength(7);

        durationField.setFilter(
            value -> value.matches(
                "[0-9]{0,4}(\\.[0-9]{0,2})?"
            )
        );

        durationField.setResponder(
            actions::onDurationFieldChanged
        );

        countField = new EditBox(
            font,
            primaryFrameX + 6,
            primaryFrameY + 3,
            primaryFrameWidth - 12,
            16,
            Component.literal(
                "Count"
            )
        );

        countField.setBordered(false);
        countField.setTextColor(
            0xFFF4F0F7
        );
        countField.setTextColorUneditable(
            0xFF81798E
        );
        countField.setTextShadow(false);
        countField.setMaxLength(6);

        countField.setFilter(
            value -> value.matches(
                "[0-9]{0,6}"
            )
        );

        countField.setResponder(
            actions::onCountFieldChanged
        );

        angleField = new EditBox(
            font,
            secondaryFrameX + 6,
            secondaryFrameY + 3,
            secondaryFrameWidth - 12,
            16,
            Component.literal(
                "Camera angle"
            )
        );

        angleField.setBordered(false);
        angleField.setTextColor(
            0xFFF4F0F7
        );
        angleField.setTextColorUneditable(
            0xFF81798E
        );
        angleField.setTextShadow(false);
        angleField.setMaxLength(3);

        angleField.setFilter(
            value -> value.matches(
                "[0-9]{0,3}"
            )
        );

        angleField.setResponder(
            actions::onAngleFieldChanged
        );
    }

    private KarakuriButton createButton(
        int x,
        int y,
        int buttonWidth,
        Component message,
        Runnable action,
        KarakuriButton.Style style
    ) {
        return new KarakuriButton(
            font,
            x,
            y,
            buttonWidth,
            BUTTON_HEIGHT,
            message,
            action,
            style
        );
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

        void testSelectedStep();

        void duplicateSelectedStep();

        void deleteSelectedStep();

        void onDurationFieldChanged(String value);

        void onCountFieldChanged(String value);

        void onAngleFieldChanged(String value);
    }
}
