package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.Scenario;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.ScenarioTaskFactory;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ScenarioEditorScreen extends Screen {
    private static final int WIDE_MIN_WIDTH = 760;
    private static final int WIDE_MIN_HEIGHT = 390;
    private static final int WIDE_MAX_WIDTH = 1440;
    private static final int WIDE_MAX_HEIGHT = 760;
    private static final int WIDE_MARGIN = 12;
    private static final int COMPACT_MARGIN = 4;
    private static final int CONTENT_MARGIN = 10;
    private static final int PANEL_GAP = 8;
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_HEIGHT = 22;
    private static final int DURATION_STEP_TICKS = 10;
    private static final int CAMERA_ANGLE_STEP = 5;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS = 72000;
    private static final int DEFAULT_MOVE_DURATION_TICKS = 40;
    private static final int DEFAULT_WAIT_DURATION_TICKS = 20;
    private static final int DEFAULT_MOUSE_DURATION_TICKS = 20;

    private final KarakuriScreen parent;
    private final int scenarioIndex;
    private final List<Scenario.Step> steps;
    private final String initialName;

    private LayoutMode layoutMode;
    private CompactTab compactTab =
        CompactTab.WORKFLOW;

    private int selectedStepIndex;

    private boolean syncingDurationField;
    private boolean syncingCountField;
    private boolean syncingAngleField;
    private boolean durationFieldValid = true;
    private boolean countFieldValid = true;
    private boolean angleFieldValid = true;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int bodyX;
    private int bodyY;
    private int bodyWidth;
    private int bodyHeight;
    private int footerY;
    private int inspectorX;
    private int inspectorY;
    private int inspectorWidth;
    private int inspectorHeight;
    private int nameFrameX;
    private int nameFrameY;
    private int nameFrameWidth;
    private int nameFrameHeight;
    private int secondaryFrameX;
    private int secondaryFrameY;
    private int secondaryFrameWidth;
    private int primaryFrameX;
    private int primaryFrameY;
    private int primaryFrameWidth;

    private ScenarioActionLibrary actionLibrary;
    private ScenarioWorkflowCanvas workflowCanvas;

    private EditBox nameField;
    private EditBox durationField;
    private EditBox countField;
    private EditBox angleField;

    private KarakuriButton workflowTabButton;
    private KarakuriButton actionsTabButton;
    private KarakuriButton inspectorTabButton;

    private KarakuriButton forwardDirectionButton;
    private KarakuriButton backwardDirectionButton;
    private KarakuriButton leftDirectionButton;
    private KarakuriButton rightDirectionButton;

    private KarakuriButton walkModeButton;
    private KarakuriButton sprintModeButton;
    private KarakuriButton sneakModeButton;
    private KarakuriButton jumpToggleButton;

    private KarakuriButton singleJumpModeButton;
    private KarakuriButton holdJumpModeButton;
    private KarakuriButton repeatJumpModeButton;
    private KarakuriButton jumpDurationStopButton;
    private KarakuriButton jumpCountStopButton;
    private KarakuriButton jumpManualStopButton;

    private KarakuriButton cameraLeftButton;
    private KarakuriButton cameraRightButton;
    private KarakuriButton cameraUpButton;
    private KarakuriButton cameraDownButton;
    private KarakuriButton instantMotionButton;
    private KarakuriButton smoothMotionButton;

    private KarakuriButton leftMouseButton;
    private KarakuriButton rightMouseButton;
    private KarakuriButton holdModeButton;
    private KarakuriButton clickModeButton;
    private KarakuriButton durationStopButton;
    private KarakuriButton clickCountStopButton;
    private KarakuriButton manualStopButton;
    private KarakuriButton cpsDecreaseButton;
    private KarakuriButton cpsIncreaseButton;

    private KarakuriButton angleDecreaseButton;
    private KarakuriButton angleIncreaseButton;
    private KarakuriButton primaryDecreaseButton;
    private KarakuriButton primaryIncreaseButton;
    private KarakuriButton testButton;
    private KarakuriButton duplicateButton;
    private KarakuriButton deleteButton;
    private KarakuriButton saveButton;

    public ScenarioEditorScreen(
        KarakuriScreen parent,
        int scenarioIndex,
        Scenario scenario
    ) {
        super(
            Component.literal(
                scenario == null
                    ? "New Scenario"
                    : "Edit Scenario"
            )
        );

        this.parent = parent;
        this.scenarioIndex = scenarioIndex;

        if (scenario == null) {
            initialName = "New Scenario";

            steps = new ArrayList<>(
                List.of(
                    new Scenario.MoveStep(
                        Scenario.MoveDirection.FORWARD,
                        DEFAULT_MOVE_DURATION_TICKS
                    )
                )
            );
        } else {
            initialName = scenario.name();

            steps = new ArrayList<>(
                scenario.steps()
            );
        }
    }

    @Override
    protected void init() {
        layoutMode =
            isWideScreen()
                ? LayoutMode.WIDE
                : LayoutMode.COMPACT;

        panelWidth =
            getResponsivePanelWidth();

        panelHeight =
            getResponsivePanelHeight();

        panelX =
            (width - panelWidth) / 2;

        panelY =
            (height - panelHeight) / 2;

        if (
            layoutMode
                == LayoutMode.WIDE
        ) {
            initializeWideLayout();
        } else {
            initializeCompactLayout();
        }

        createNameField();

        actionLibrary =
            new ScenarioActionLibrary(
                font,
                bodyX,
                bodyY,
                layoutMode
                    == LayoutMode.WIDE
                        ? getWideLibraryWidth()
                        : bodyWidth,
                bodyHeight,
                layoutMode
                    == LayoutMode.WIDE
                        ? ScenarioActionLibrary
                            .Layout.SIDEBAR
                        : ScenarioActionLibrary
                            .Layout.HORIZONTAL,
                this::insertMoveStep,
                this::insertJumpStep,
                this::insertWaitStep,
                this::insertMouseStep,
                this::insertCameraStep,
                this::insertHotbarStep
            );

        for (
            KarakuriButton widget :
            actionLibrary.widgets()
        ) {
            addRenderableWidget(widget);
        }

        int canvasX;
        int canvasWidth;

        if (
            layoutMode
                == LayoutMode.WIDE
        ) {
            int libraryWidth =
                getWideLibraryWidth();

            canvasX =
                bodyX
                    + libraryWidth
                    + PANEL_GAP;

            canvasWidth =
                bodyWidth
                    - libraryWidth
                    - inspectorWidth
                    - PANEL_GAP * 2;
        } else {
            canvasX = bodyX;
            canvasWidth = bodyWidth;
        }

        workflowCanvas =
            new ScenarioWorkflowCanvas(
                font,
                steps,
                this::onCanvasSelectionChanged,
                this::onCanvasContentChanged
            );

        workflowCanvas.setBounds(
            canvasX,
            bodyY,
            canvasWidth,
            bodyHeight
        );

        workflowCanvas.setSelectedIndex(
            selectedStepIndex
        );

        createInspectorWidgets();
        createFooterWidgets();

        if (
            layoutMode
                == LayoutMode.COMPACT
        ) {
            createCompactTabs();
        }

        syncValueFields();
        updateButtons();
    }

    @Override
    public void tick() {
        updateButtons();
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        graphics.fill(
            0,
            0,
            width,
            height,
            0xD0100E16
        );

        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + panelHeight,
            0xFF181620
        );

        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            0xFF6F5A91
        );

        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + panelHeight,
            0xFF9B79D1
        );

        renderHeader(graphics);
        actionLibrary.render(graphics);

        if (isWorkflowVisible()) {
            workflowCanvas.render(
                graphics,
                mouseX,
                mouseY,
                Component.literal(
                    layoutMode
                        == LayoutMode.WIDE
                            ? "Drag to reorder  |  Scroll to adjust the main value"
                            : "Drag to reorder  |  Scroll to adjust value"
                ),
                0xFF81798E
            );
        }

        if (isInspectorVisible()) {
            renderInspector(graphics);
        }

        renderFooter(graphics);

        super.render(
            graphics,
            mouseX,
            mouseY,
            delta
        );

        renderCenteredValues(graphics);
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (
            super.mouseClicked(
                event,
                doubled
            )
        ) {
            return true;
        }

        return isWorkflowVisible()
            && workflowCanvas.mouseClicked(
                event
            );
    }

    @Override
    public boolean mouseDragged(
        MouseButtonEvent event,
        double offsetX,
        double offsetY
    ) {
        if (
            isWorkflowVisible()
                && workflowCanvas.mouseDragged(
                    event
                )
        ) {
            return true;
        }

        return super.mouseDragged(
            event,
            offsetX,
            offsetY
        );
    }

    @Override
    public boolean mouseReleased(
        MouseButtonEvent event
    ) {
        boolean handled =
            isWorkflowVisible()
                && workflowCanvas
                    .mouseReleased();

        return super.mouseReleased(event)
            || handled;
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        if (
            isWorkflowVisible()
                && workflowCanvas
                    .mouseScrolled(
                        mouseX,
                        mouseY,
                        horizontalAmount,
                        verticalAmount
                    )
        ) {
            return true;
        }

        return super.mouseScrolled(
            mouseX,
            mouseY,
            horizontalAmount,
            verticalAmount
        );
    }

    @Override
    public void onClose() {
        stopRunningTest();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void initializeWideLayout() {
        int headerHeight = 52;
        int footerHeight = 40;

        bodyX =
            panelX + CONTENT_MARGIN;

        bodyY =
            panelY + headerHeight;

        bodyWidth =
            panelWidth
                - CONTENT_MARGIN * 2;

        footerY =
            panelY
                + panelHeight
                - footerHeight;

        bodyHeight =
            footerY
                - bodyY
                - PANEL_GAP;

        inspectorWidth = Math.clamp(
            bodyWidth * 26 / 100,
            230,
            310
        );

        inspectorX =
            bodyX
                + bodyWidth
                - inspectorWidth;

        inspectorY = bodyY;
        inspectorHeight = bodyHeight;
    }

    private void initializeCompactLayout() {
        int headerHeight =
            panelHeight < 240
                ? 36
                : 42;

        int tabHeight = 22;
        int footerHeight = 30;

        int tabY =
            panelY + headerHeight;

        bodyX =
            panelX + 6;

        bodyY =
            tabY
                + tabHeight
                + 4;

        bodyWidth =
            panelWidth - 12;

        footerY =
            panelY
                + panelHeight
                - footerHeight;

        bodyHeight =
            footerY
                - bodyY
                - 4;

        inspectorX = bodyX;
        inspectorY = bodyY;
        inspectorWidth = bodyWidth;
        inspectorHeight = bodyHeight;
    }

    private void createNameField() {
        int titleWidth =
            layoutMode
                == LayoutMode.WIDE
                    ? 132
                    : 104;

        nameFrameX =
            panelX + titleWidth;

        nameFrameY =
            panelY
                + (
                    layoutMode
                        == LayoutMode.WIDE
                            ? 13
                            : 8
                );

        nameFrameWidth =
            panelWidth
                - titleWidth
                - CONTENT_MARGIN;

        nameFrameHeight =
            layoutMode
                == LayoutMode.WIDE
                    ? 24
                    : 22;

        nameField = new EditBox(
            font,
            nameFrameX + 7,
            nameFrameY + 3,
            nameFrameWidth - 14,
            16,
            Component.literal(
                "Scenario name"
            )
        );

        nameField.setBordered(false);
        nameField.setTextColor(
            0xFFF4F0F7
        );
        nameField.setTextColorUneditable(
            0xFF81798E
        );
        nameField.setTextShadow(false);
        nameField.setMaxLength(64);

        nameField.setHint(
            Component.literal(
                "Scenario name"
            )
        );

        nameField.setValue(
            initialName
        );

        nameField.setResponder(
            value -> updateButtons()
        );

        addRenderableWidget(nameField);
    }

    private void createCompactTabs() {
        int tabX =
            panelX + 6;

        int tabY =
            bodyY - 26;

        int tabWidth =
            (
                panelWidth
                    - 12
                    - BUTTON_GAP * 2
            ) / 3;

        workflowTabButton = createButton(
            tabX,
            tabY,
            tabWidth,
            Component.literal("Workflow"),
            () -> setCompactTab(
                CompactTab.WORKFLOW
            ),
            KarakuriButton.Style.GHOST
        );

        actionsTabButton = createButton(
            tabX
                + tabWidth
                + BUTTON_GAP,
            tabY,
            tabWidth,
            Component.literal("Actions"),
            () -> setCompactTab(
                CompactTab.ACTIONS
            ),
            KarakuriButton.Style.GHOST
        );

        inspectorTabButton = createButton(
            tabX
                + (
                    tabWidth
                        + BUTTON_GAP
                ) * 2,
            tabY,
            tabWidth,
            Component.literal("Inspector"),
            () -> setCompactTab(
                CompactTab.INSPECTOR
            ),
            KarakuriButton.Style.GHOST
        );

        addRenderableWidget(
            workflowTabButton
        );

        addRenderableWidget(
            actionsTabButton
        );

        addRenderableWidget(
            inspectorTabButton
        );
    }

    private void createInspectorWidgets() {
        if (
            layoutMode
                == LayoutMode.WIDE
        ) {
            createWideInspectorWidgets();
        } else {
            createCompactInspectorWidgets();
        }

        addRenderableWidget(
            forwardDirectionButton
        );
        addRenderableWidget(
            backwardDirectionButton
        );
        addRenderableWidget(
            leftDirectionButton
        );
        addRenderableWidget(
            rightDirectionButton
        );

        addRenderableWidget(
            walkModeButton
        );
        addRenderableWidget(
            sprintModeButton
        );
        addRenderableWidget(
            sneakModeButton
        );
        addRenderableWidget(
            jumpToggleButton
        );

        addRenderableWidget(
            singleJumpModeButton
        );
        addRenderableWidget(
            holdJumpModeButton
        );
        addRenderableWidget(
            repeatJumpModeButton
        );
        addRenderableWidget(
            jumpDurationStopButton
        );
        addRenderableWidget(
            jumpCountStopButton
        );
        addRenderableWidget(
            jumpManualStopButton
        );

        addRenderableWidget(
            cameraLeftButton
        );
        addRenderableWidget(
            cameraRightButton
        );
        addRenderableWidget(
            cameraUpButton
        );
        addRenderableWidget(
            cameraDownButton
        );
        addRenderableWidget(
            instantMotionButton
        );
        addRenderableWidget(
            smoothMotionButton
        );

        addRenderableWidget(
            leftMouseButton
        );
        addRenderableWidget(
            rightMouseButton
        );
        addRenderableWidget(
            holdModeButton
        );
        addRenderableWidget(
            clickModeButton
        );
        addRenderableWidget(
            durationStopButton
        );
        addRenderableWidget(
            clickCountStopButton
        );
        addRenderableWidget(
            manualStopButton
        );
        addRenderableWidget(
            cpsDecreaseButton
        );
        addRenderableWidget(
            cpsIncreaseButton
        );

        addRenderableWidget(
            angleDecreaseButton
        );
        addRenderableWidget(
            angleField
        );
        addRenderableWidget(
            angleIncreaseButton
        );

        addRenderableWidget(
            primaryDecreaseButton
        );
        addRenderableWidget(
            durationField
        );
        addRenderableWidget(
            countField
        );
        addRenderableWidget(
            primaryIncreaseButton
        );

        addRenderableWidget(
            testButton
        );
        addRenderableWidget(
            duplicateButton
        );
        addRenderableWidget(
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
                () -> setDirection(
                    Scenario.MoveDirection
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
                () -> setDirection(
                    Scenario.MoveDirection
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
                () -> setDirection(
                    Scenario.MoveDirection
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
                () -> setDirection(
                    Scenario.MoveDirection
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
                () -> setJumpMode(
                    Scenario.JumpMode.SINGLE
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
                () -> setJumpMode(
                    Scenario.JumpMode.HOLD
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
                () -> setJumpMode(
                    Scenario.JumpMode.REPEAT
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
                () -> setCameraDirection(
                    Scenario.CameraDirection
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
                () -> setCameraDirection(
                    Scenario.CameraDirection
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
                () -> setCameraDirection(
                    Scenario.CameraDirection
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
                () -> setCameraDirection(
                    Scenario.CameraDirection
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
                () -> setMouseAction(
                    Scenario.MouseAction
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
                () -> setMouseAction(
                    Scenario.MouseAction
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
                () -> setMouseInputMode(
                    Scenario.MouseInputMode
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
                () -> setMouseInputMode(
                    Scenario.MouseInputMode
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
                () -> setMoveMode(
                    Scenario.MoveMode.WALK
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
                () -> setMoveMode(
                    Scenario.MoveMode.SPRINT
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
                () -> setMoveMode(
                    Scenario.MoveMode.SNEAK
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
                () -> setJumpStopMode(
                    Scenario.JumpStopMode.DURATION
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
                () -> setJumpStopMode(
                    Scenario.JumpStopMode.JUMP_COUNT
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
                () -> setJumpStopMode(
                    Scenario.JumpStopMode.MANUAL
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
                () -> setCameraMotion(
                    Scenario.CameraMotion
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
                () -> setCameraMotion(
                    Scenario.CameraMotion
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
                () -> setMouseStopMode(
                    Scenario.MouseStopMode
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
                () -> setMouseStopMode(
                    Scenario.MouseStopMode
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
                () -> setMouseStopMode(
                    Scenario.MouseStopMode
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
                this::toggleMoveJumping,
                KarakuriButton
                    .Style.GHOST
            );

        cpsDecreaseButton =
            createButton(
                contentX,
                secondaryFrameY,
                34,
                Component.literal("-"),
                () -> changeCps(-1),
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
                () -> changeCps(1),
                KarakuriButton
                    .Style.GHOST
            );

        angleDecreaseButton =
            createButton(
                contentX,
                secondaryFrameY,
                34,
                Component.literal("-"),
                () -> changeAngle(-1),
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
                () -> changeAngle(1),
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
                () -> changePrimaryValue(
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
                () -> changePrimaryValue(
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
                this::testSelectedStep,
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
                this::duplicateSelectedStep,
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
                this::deleteSelectedStep,
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
                () -> setDirection(
                    Scenario.MoveDirection
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
                () -> setDirection(
                    Scenario.MoveDirection
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
                () -> setDirection(
                    Scenario.MoveDirection
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
                () -> setDirection(
                    Scenario.MoveDirection
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
                () -> setJumpMode(
                    Scenario.JumpMode.SINGLE
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
                () -> setJumpMode(
                    Scenario.JumpMode.HOLD
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
                () -> setJumpMode(
                    Scenario.JumpMode.REPEAT
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
                () -> setCameraDirection(
                    Scenario.CameraDirection
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
                () -> setCameraDirection(
                    Scenario.CameraDirection
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
                () -> setCameraDirection(
                    Scenario.CameraDirection
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
                () -> setCameraDirection(
                    Scenario.CameraDirection
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
                () -> setMouseAction(
                    Scenario.MouseAction
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
                () -> setMouseAction(
                    Scenario.MouseAction
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
                () -> setMouseInputMode(
                    Scenario.MouseInputMode
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
                () -> setMouseInputMode(
                    Scenario.MouseInputMode
                        .CLICK
                ),
                KarakuriButton
                    .Style.GHOST
            );

        int secondRowY =
            inspectorY + 68;

        int halfWidth =
            (
                contentWidth
                    - BUTTON_GAP
            ) / 2;

        walkModeButton =
            createButton(
                contentX,
                secondRowY,
                quarterWidth,
                Component.literal(
                    "Walk"
                ),
                () -> setMoveMode(
                    Scenario.MoveMode.WALK
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
                () -> setMoveMode(
                    Scenario.MoveMode.SPRINT
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
                () -> setMoveMode(
                    Scenario.MoveMode.SNEAK
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
                this::toggleMoveJumping,
                KarakuriButton
                    .Style.GHOST
            );

        jumpDurationStopButton =
            createButton(
                contentX,
                secondRowY,
                thirdWidth,
                Component.literal("Time"),
                () -> setJumpStopMode(
                    Scenario.JumpStopMode.DURATION
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
                () -> setJumpStopMode(
                    Scenario.JumpStopMode.JUMP_COUNT
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
                () -> setJumpStopMode(
                    Scenario.JumpStopMode.MANUAL
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
                () -> setCameraMotion(
                    Scenario.CameraMotion
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
                () -> setCameraMotion(
                    Scenario.CameraMotion
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
                () -> setMouseStopMode(
                    Scenario.MouseStopMode
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
                () -> setMouseStopMode(
                    Scenario.MouseStopMode
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
                () -> setMouseStopMode(
                    Scenario.MouseStopMode
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
                () -> changeCps(-1),
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
                () -> changeCps(1),
                KarakuriButton
                    .Style.GHOST
            );

        angleDecreaseButton =
            createButton(
                leftColumnX,
                valueRowY,
                24,
                Component.literal("-"),
                () -> changeAngle(-1),
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
                () -> changeAngle(1),
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
                () -> changePrimaryValue(
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
                () -> changePrimaryValue(
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
                this::testSelectedStep,
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
                this::duplicateSelectedStep,
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
                this::deleteSelectedStep,
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
            this::onDurationFieldChanged
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
            this::onCountFieldChanged
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
            this::onAngleFieldChanged
        );
    }

    private void createFooterWidgets() {
        int buttonWidth =
            layoutMode
                == LayoutMode.WIDE
                    ? 126
                    : Math.min(
                        108,
                        (
                            panelWidth
                                - 24
                                - BUTTON_GAP
                        ) / 2
                    );

        int buttonY =
            footerY + 4;

        saveButton = createButton(
            panelX
                + panelWidth
                - CONTENT_MARGIN
                - buttonWidth * 2
                - BUTTON_GAP,
            buttonY,
            buttonWidth,
            Component.literal(
                layoutMode
                    == LayoutMode.WIDE
                        ? "Save Scenario"
                        : "Save"
            ),
            this::saveScenario,
            KarakuriButton.Style.SUCCESS
        );

        KarakuriButton cancelButton =
            createButton(
                panelX
                    + panelWidth
                    - CONTENT_MARGIN
                    - buttonWidth,
                buttonY,
                buttonWidth,
                Component.literal(
                    "Cancel"
                ),
                this::cancelEditing,
                KarakuriButton
                    .Style.SECONDARY
            );

        addRenderableWidget(saveButton);
        addRenderableWidget(cancelButton);
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

    private void renderHeader(
        GuiGraphics graphics
    ) {
        graphics.drawString(
            font,
            title,
            panelX + 12,
            panelY
                + (
                    layoutMode
                        == LayoutMode.WIDE
                            ? 20
                            : 13
                ),
            0xFFF5F1F8,
            false
        );

        graphics.fill(
            nameFrameX,
            nameFrameY,
            nameFrameX + nameFrameWidth,
            nameFrameY + nameFrameHeight,
            0xFF100E16
        );

        graphics.renderOutline(
            nameFrameX,
            nameFrameY,
            nameFrameWidth,
            nameFrameHeight,
            isScenarioNameValid()
                ? 0xFF51475E
                : 0xFFC75B69
        );
    }

    private void renderInspector(
        GuiGraphics graphics
    ) {
        Scenario.Step step =
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
            getStepAccentColor(step)
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
                        selectedStepIndex
                            + 1
                    )
                    + " of "
                    + steps.size()
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
                getStepTitle(step)
            ),
            inspectorX + 10,
            inspectorY + 24,
            0xFFF4F0F7,
            false
        );

        if (
            layoutMode
                == LayoutMode.WIDE
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
        Scenario.Step step
    ) {
        if (
            step
                instanceof
                Scenario.JumpStep jumpStep
        ) {
            drawInspectorLabel(
                graphics,
                "Jump mode",
                46
            );

            if (
                jumpStep.mode()
                    != Scenario.JumpMode.SINGLE
            ) {
                drawInspectorLabel(
                    graphics,
                    "Stop after",
                    98
                );
            }

            String primaryLabel =
                getJumpPrimaryValueLabel(
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
                    getJumpDescription(
                        jumpStep
                    )
                ),
                inspectorX + 10,
                inspectorY + 216,
                jumpStep.isInfinite()
                    && selectedStepIndex
                        < steps.size() - 1
                        ? 0xFFE66777
                        : 0xFF81798E,
                false
            );

            return;
        }

        if (
            step
                instanceof
                Scenario.HotbarStep
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
                Scenario.MoveStep moveStep
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
                Scenario.CameraStep cameraStep
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
                    == Scenario.CameraMotion
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
                        == Scenario.CameraMotion
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
                Scenario.WaitStep
        ) {
            drawInspectorLabel(
                graphics,
                "Duration in seconds",
                150
            );

            return;
        }

        Scenario.MouseStep mouseStep =
            (Scenario.MouseStep) step;

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
                == Scenario.MouseInputMode
                    .CLICK
        ) {
            drawInspectorLabel(
                graphics,
                "Click rate",
                124
            );
        }

        String primaryLabel =
            getMousePrimaryValueLabel(
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
                getMouseEstimate(
                    mouseStep
                )
            ),
            inspectorX + 10,
            inspectorY + 216,
            mouseStep.isInfinite()
                && selectedStepIndex
                    < steps.size() - 1
                        ? 0xFFE66777
                        : 0xFF81798E,
            false
        );
    }

    private void renderCompactInspectorLabels(
        GuiGraphics graphics,
        Scenario.Step step
    ) {
        String label =
            switch (step) {
                case Scenario.CameraStep cameraStep ->
                    "Direction / motion";

                case Scenario.HotbarStep hotbarStep ->
                    "Select active hotbar slot";

                case Scenario.JumpStep jumpStep ->
                    "Mode / stop condition";

                case Scenario.MoveStep moveStep ->
                    "Direction / style / jumping";

                case Scenario.MouseStep mouseStep ->
                    "Button / input / stop";

                case Scenario.WaitStep waitStep ->
                    "Timing action";
            };

        graphics.drawString(
            font,
            Component.literal(label),
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
        Scenario.Step step
    ) {
        if (
            usesCps(step)
                || usesAngle(step)
        ) {
            graphics.fill(
                secondaryFrameX,
                secondaryFrameY,
                secondaryFrameX
                    + secondaryFrameWidth,
                secondaryFrameY
                    + BUTTON_HEIGHT,
                0xFF100E16
            );

            graphics.renderOutline(
                secondaryFrameX,
                secondaryFrameY,
                secondaryFrameWidth,
                BUTTON_HEIGHT,
                usesAngle(step)
                    && !angleFieldValid
                        ? 0xFFC75B69
                        : 0xFF51475E
            );
        }

        if (usesPrimaryValue(step)) {
            graphics.fill(
                primaryFrameX,
                primaryFrameY,
                primaryFrameX
                    + primaryFrameWidth,
                primaryFrameY
                    + BUTTON_HEIGHT,
                0xFF100E16
            );

            graphics.renderOutline(
                primaryFrameX,
                primaryFrameY,
                primaryFrameWidth,
                BUTTON_HEIGHT,
                isPrimaryValueValid(step)
                    ? 0xFF51475E
                    : 0xFFC75B69
            );
        }
    }

    private void renderCenteredValues(
        GuiGraphics graphics
    ) {
        if (!isInspectorVisible()) {
            return;
        }

        Scenario.Step step =
            getSelectedStep();

        if (
            step
                instanceof
                Scenario.HotbarStep hotbarStep
        ) {
            coverFrameAndRenderValue(
                graphics,
                primaryFrameX,
                primaryFrameY,
                primaryFrameWidth,
                "Slot "
                    + (
                        hotbarStep.slot()
                            + 1
                    ),
                0xFFF4F0F7
            );
        }

        if (usesCps(step)) {
            renderCenteredFrameText(
                graphics,
                secondaryFrameX,
                secondaryFrameY,
                secondaryFrameWidth,
                Scenario
                    .formatClicksPerSecondLabel(
                        (
                            (Scenario.MouseStep)
                                step
                        )
                            .clicksPerSecondHalfSteps()
                    ),
                0xFFF4F0F7
            );
        }

        if (
            angleField.visible
                && !angleField.isFocused()
        ) {
            Scenario.CameraStep cameraStep =
                (Scenario.CameraStep) step;

            coverFrameAndRenderValue(
                graphics,
                secondaryFrameX,
                secondaryFrameY,
                secondaryFrameWidth,
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
            durationField.visible
                && !durationField.isFocused()
        ) {
            coverFrameAndRenderValue(
                graphics,
                primaryFrameX,
                primaryFrameY,
                primaryFrameWidth,
                durationFieldValid
                    ? formatDurationValue(
                        step.durationTicks()
                    )
                    : "Invalid",
                durationFieldValid
                    ? 0xFFF4F0F7
                    : 0xFFE66777
            );
        }

        if (
            countField.visible
                && !countField.isFocused()
        ) {
            coverFrameAndRenderValue(
                graphics,
                primaryFrameX,
                primaryFrameY,
                primaryFrameWidth,
                countFieldValid
                    ? getCountDisplayValue(step)
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

    private void renderFooter(
        GuiGraphics graphics
    ) {
        graphics.fill(
            panelX + CONTENT_MARGIN,
            footerY,
            panelX
                + panelWidth
                - CONTENT_MARGIN,
            footerY + 1,
            0xFF332D3A
        );

        int saveX =
            saveButton.getX();

        if (
            saveX
                - (
                    panelX
                        + CONTENT_MARGIN
                )
                >= 170
        ) {
            String validationMessage =
                getValidationMessage();

            graphics.drawString(
                font,
                Component.literal(
                    validationMessage == null
                        ? "Stored as a .karakuri file"
                        : validationMessage
                ),
                panelX + CONTENT_MARGIN,
                footerY + 11,
                validationMessage == null
                    ? 0xFF81798E
                    : 0xFFE66777,
                false
            );
        }
    }

    private void setCompactTab(
        CompactTab updatedTab
    ) {
        compactTab = updatedTab;
        updateButtons();
    }

    private void onCanvasSelectionChanged(
        int index
    ) {
        stopRunningTest();

        selectedStepIndex = index;

        syncValueFields();
        updateButtons();
    }

    private void onCanvasContentChanged() {
        stopRunningTest();

        selectedStepIndex =
            workflowCanvas
                .getSelectedIndex();

        syncValueFields();
        updateButtons();
    }

    private void insertMoveStep(
        Scenario.MoveDirection direction
    ) {
        stopRunningTest();

        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.MoveStep(
                direction,
                DEFAULT_MOVE_DURATION_TICKS
            )
        );

        selectStep(insertIndex);
        returnToWorkflow();
    }

    private void insertJumpStep() {
        stopRunningTest();

        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.JumpStep(
                Scenario.JumpMode.SINGLE,
                Scenario.JumpStopMode.DURATION,
                Scenario.JumpStep
                    .DEFAULT_DURATION_TICKS,
                Scenario.JumpStep
                    .DEFAULT_JUMP_COUNT
            )
        );

        selectStep(insertIndex);
        returnToWorkflow();
    }

    private void insertWaitStep() {
        stopRunningTest();

        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.WaitStep(
                DEFAULT_WAIT_DURATION_TICKS
            )
        );

        selectStep(insertIndex);
        returnToWorkflow();
    }

    private void insertMouseStep(
        Scenario.MouseAction action
    ) {
        stopRunningTest();

        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.MouseStep(
                action,
                Scenario.MouseInputMode.HOLD,
                Scenario.MouseStopMode.DURATION,
                DEFAULT_MOUSE_DURATION_TICKS,
                Scenario.MouseStep
                    .DEFAULT_CPS_HALF_STEPS,
                Scenario.MouseStep
                    .DEFAULT_CLICK_COUNT
            )
        );

        selectStep(insertIndex);
        returnToWorkflow();
    }

    private void insertCameraStep(
        Scenario.CameraDirection direction
    ) {
        stopRunningTest();

        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.CameraStep(
                direction,
                Scenario.CameraMotion.SMOOTH,
                Scenario.CameraStep
                    .DEFAULT_ANGLE_DEGREES,
                Scenario.CameraStep
                    .DEFAULT_DURATION_TICKS
            )
        );

        selectStep(insertIndex);
        returnToWorkflow();
    }

    private void insertHotbarStep() {
        stopRunningTest();

        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.HotbarStep(
                Scenario.HotbarStep
                    .DEFAULT_SLOT
            )
        );

        selectStep(insertIndex);
        returnToWorkflow();
    }

    private void returnToWorkflow() {
        if (
            layoutMode
                == LayoutMode.COMPACT
        ) {
            setCompactTab(
                CompactTab.WORKFLOW
            );
        }
    }

    private void duplicateSelectedStep() {
        stopRunningTest();

        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            getSelectedStep()
        );

        selectStep(insertIndex);
    }

    private void deleteSelectedStep() {
        stopRunningTest();

        if (steps.size() <= 1) {
            return;
        }

        steps.remove(
            selectedStepIndex
        );

        selectStep(
            Math.min(
                selectedStepIndex,
                steps.size() - 1
            )
        );
    }

    private void selectStep(
        int index
    ) {
        selectedStepIndex =
            Math.clamp(
                index,
                0,
                steps.size() - 1
            );

        workflowCanvas.setSelectedIndex(
            selectedStepIndex
        );

        syncValueFields();
        updateButtons();
    }

    private void setDirection(
        Scenario.MoveDirection direction
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.MoveStep moveStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            moveStep.withDirection(
                direction
            )
        );

        updateButtons();
    }

    private void setMoveMode(
        Scenario.MoveMode mode
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.MoveStep moveStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            moveStep.withMode(mode)
        );

        updateButtons();
    }

    private void toggleMoveJumping() {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.MoveStep moveStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            moveStep.withJumping(
                !moveStep.jumping()
            )
        );

        updateButtons();
    }

    private void setJumpMode(
        Scenario.JumpMode mode
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.JumpStep jumpStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            jumpStep.withMode(mode)
        );

        syncValueFields();
        updateButtons();
    }

    private void setJumpStopMode(
        Scenario.JumpStopMode stopMode
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.JumpStep jumpStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            jumpStep.withStopMode(
                stopMode
            )
        );

        syncValueFields();
        updateButtons();
    }

    private void setCameraDirection(
        Scenario.CameraDirection direction
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.CameraStep cameraStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            cameraStep.withDirection(
                direction
            )
        );

        updateButtons();
    }

    private void setCameraMotion(
        Scenario.CameraMotion motion
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.CameraStep cameraStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            cameraStep.withMotion(
                motion
            )
        );

        syncValueFields();
        updateButtons();
    }

    private void setMouseAction(
        Scenario.MouseAction action
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.MouseStep mouseStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            mouseStep.withAction(
                action
            )
        );

        updateButtons();
    }

    private void setMouseInputMode(
        Scenario.MouseInputMode inputMode
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.MouseStep mouseStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            mouseStep.withInputMode(
                inputMode
            )
        );

        syncValueFields();
        updateButtons();
    }

    private void setMouseStopMode(
        Scenario.MouseStopMode stopMode
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.MouseStep mouseStep
            )
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            mouseStep.withStopMode(
                stopMode
            )
        );

        syncValueFields();
        updateButtons();
    }

    private void changeCps(
        int direction
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.MouseStep mouseStep
            )
        ) {
            return;
        }

        int updatedValue =
            Math.clamp(
                mouseStep
                    .clicksPerSecondHalfSteps()
                    + direction,
                Scenario.MouseStep
                    .MIN_CPS_HALF_STEPS,
                Scenario.MouseStep
                    .MAX_CPS_HALF_STEPS
            );

        steps.set(
            selectedStepIndex,
            mouseStep
                .withClicksPerSecondHalfSteps(
                    updatedValue
                )
        );

        updateButtons();
    }

    private void changeAngle(
        int direction
    ) {
        stopRunningTest();

        if (
            !(
                getSelectedStep()
                    instanceof
                    Scenario.CameraStep cameraStep
            )
        ) {
            return;
        }

        int updatedAngle =
            Math.clamp(
                cameraStep.angleDegrees()
                    + direction
                    * CAMERA_ANGLE_STEP,
                Scenario.CameraStep
                    .MIN_ANGLE_DEGREES,
                Scenario.CameraStep
                    .MAX_ANGLE_DEGREES
            );

        steps.set(
            selectedStepIndex,
            cameraStep.withAngleDegrees(
                updatedAngle
            )
        );

        syncAngleField();
        updateButtons();
    }

    private void changePrimaryValue(
        int direction
    ) {
        stopRunningTest();

        Scenario.Step step =
            getSelectedStep();

        if (
            step
                instanceof
                Scenario.HotbarStep hotbarStep
        ) {
            steps.set(
                selectedStepIndex,
                hotbarStep.withSlot(
                    Math.clamp(
                        hotbarStep.slot()
                            + direction,
                        Scenario.HotbarStep
                            .MIN_SLOT,
                        Scenario.HotbarStep
                            .MAX_SLOT
                    )
                )
            );

            updateButtons();
            return;
        }

        if (
            step
                instanceof
                Scenario.JumpStep jumpStep
                && usesCount(step)
        ) {
            steps.set(
                selectedStepIndex,
                jumpStep.withJumpCount(
                    Math.clamp(
                        jumpStep.jumpCount()
                            + direction,
                        Scenario.JumpStep
                            .MIN_JUMP_COUNT,
                        Scenario.JumpStep
                            .MAX_JUMP_COUNT
                    )
                )
            );

            syncValueFields();
            updateButtons();
            return;
        }

        if (
            step
                instanceof
                Scenario.MouseStep mouseStep
                && usesCount(step)
        ) {
            steps.set(
                selectedStepIndex,
                mouseStep.withClickCount(
                    Math.clamp(
                        mouseStep.clickCount()
                            + direction,
                        Scenario.MouseStep
                            .MIN_CLICK_COUNT,
                        Scenario.MouseStep
                            .MAX_CLICK_COUNT
                    )
                )
            );

            syncValueFields();
            updateButtons();
            return;
        }

        if (!usesDuration(step)) {
            return;
        }

        int updatedDuration =
            Math.clamp(
                step.durationTicks()
                    + direction
                    * DURATION_STEP_TICKS,
                MIN_DURATION_TICKS,
                MAX_DURATION_TICKS
            );

        replaceSelectedDuration(
            updatedDuration
        );

        syncValueFields();
        updateButtons();
    }

    private void onDurationFieldChanged(
        String value
    ) {
        if (syncingDurationField) {
            return;
        }

        stopRunningTest();

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
                        < MIN_DURATION_TICKS
                    || durationTicks
                        > MAX_DURATION_TICKS
            ) {
                durationFieldValid = false;
                updateButtons();
                return;
            }

            replaceSelectedDuration(
                durationTicks
            );

            durationFieldValid = true;
        } catch (
            NumberFormatException exception
        ) {
            durationFieldValid = false;
        }

        updateButtons();
    }

    private void onCountFieldChanged(
        String value
    ) {
        if (syncingCountField) {
            return;
        }

        stopRunningTest();

        try {
            int count =
                Integer.parseInt(value);

            if (
                count < 1
                    || count > 100000
            ) {
                countFieldValid = false;
                updateButtons();
                return;
            }

            Scenario.Step step =
                getSelectedStep();

            if (
                step
                    instanceof
                    Scenario.JumpStep jumpStep
            ) {
                steps.set(
                    selectedStepIndex,
                    jumpStep.withJumpCount(
                        count
                    )
                );
            } else if (
                step
                    instanceof
                    Scenario.MouseStep mouseStep
            ) {
                steps.set(
                    selectedStepIndex,
                    mouseStep.withClickCount(
                        count
                    )
                );
            }

            countFieldValid = true;
        } catch (
            NumberFormatException exception
        ) {
            countFieldValid = false;
        }

        updateButtons();
    }

    private void onAngleFieldChanged(
        String value
    ) {
        if (syncingAngleField) {
            return;
        }

        stopRunningTest();

        try {
            int angle =
                Integer.parseInt(value);

            if (
                angle
                    < Scenario.CameraStep
                        .MIN_ANGLE_DEGREES
                    || angle
                        > Scenario.CameraStep
                            .MAX_ANGLE_DEGREES
            ) {
                angleFieldValid = false;
                updateButtons();
                return;
            }

            if (
                getSelectedStep()
                    instanceof
                    Scenario.CameraStep cameraStep
            ) {
                steps.set(
                    selectedStepIndex,
                    cameraStep.withAngleDegrees(
                        angle
                    )
                );
            }

            angleFieldValid = true;
        } catch (
            NumberFormatException exception
        ) {
            angleFieldValid = false;
        }

        updateButtons();
    }

    private void replaceSelectedDuration(
        int durationTicks
    ) {
        Scenario.Step step =
            getSelectedStep();

        steps.set(
            selectedStepIndex,
            switch (step) {
                case Scenario.CameraStep cameraStep ->
                    cameraStep.withDurationTicks(
                        durationTicks
                    );

                case Scenario.HotbarStep hotbarStep ->
                    hotbarStep;

                case Scenario.JumpStep jumpStep ->
                    jumpStep.withDurationTicks(
                        durationTicks
                    );

                case Scenario.MoveStep moveStep ->
                    moveStep.withDurationTicks(
                        durationTicks
                    );

                case Scenario.MouseStep mouseStep ->
                    mouseStep.withDurationTicks(
                        durationTicks
                    );

                case Scenario.WaitStep waitStep ->
                    new Scenario.WaitStep(
                        durationTicks
                    );
            }
        );
    }

    private void syncValueFields() {
        syncDurationField();
        syncCountField();
        syncAngleField();
    }

    private void syncDurationField() {
        if (durationField == null) {
            return;
        }

        syncingDurationField = true;

        durationField.setValue(
            formatDurationForField(
                getSelectedStep()
                    .durationTicks()
            )
        );

        syncingDurationField = false;
        durationFieldValid = true;
    }

    private void syncCountField() {
        if (countField == null) {
            return;
        }

        Scenario.Step step =
            getSelectedStep();

        syncingCountField = true;

        if (
            step
                instanceof
                Scenario.JumpStep jumpStep
        ) {
            countField.setValue(
                Integer.toString(
                    jumpStep.jumpCount()
                )
            );
        } else if (
            step
                instanceof
                Scenario.MouseStep mouseStep
        ) {
            countField.setValue(
                Integer.toString(
                    mouseStep.clickCount()
                )
            );
        }

        syncingCountField = false;
        countFieldValid = true;
    }

    private void syncAngleField() {
        if (angleField == null) {
            return;
        }

        if (
            getSelectedStep()
                instanceof
                Scenario.CameraStep cameraStep
        ) {
            syncingAngleField = true;

            angleField.setValue(
                Integer.toString(
                    cameraStep.angleDegrees()
                )
            );

            syncingAngleField = false;
        }

        angleFieldValid = true;
    }

    private void testSelectedStep() {
        if (
            TaskManager.getStatus()
                != TaskStatus.IDLE
        ) {
            TaskManager.stop(minecraft);
            updateButtons();
            return;
        }

        TaskManager.start(
            ScenarioTaskFactory.createStep(
                getSelectedStep()
            ),
            minecraft
        );

        updateButtons();
    }

    private void stopRunningTest() {
        if (
            TaskManager.getStatus()
                != TaskStatus.IDLE
        ) {
            TaskManager.stop(minecraft);
        }
    }

    private void saveScenario() {
        if (
            getValidationMessage()
                != null
        ) {
            return;
        }

        stopRunningTest();

        Scenario scenario =
            new Scenario(
                nameField.getValue(),
                steps
            );

        if (scenarioIndex < 0) {
            ScenarioLibrary.add(
                scenario
            );
        } else {
            ScenarioLibrary.replace(
                scenarioIndex,
                scenario
            );
        }

        parent.refreshScenarios(
            scenario.name()
        );

        minecraft.setScreen(parent);
    }

    private void cancelEditing() {
        stopRunningTest();
        minecraft.setScreen(parent);
    }

    private void updateButtons() {
        if (
            forwardDirectionButton == null
                || backwardDirectionButton == null
                || leftDirectionButton == null
                || rightDirectionButton == null
                || walkModeButton == null
                || sprintModeButton == null
                || sneakModeButton == null
                || jumpToggleButton == null
                || singleJumpModeButton == null
                || holdJumpModeButton == null
                || repeatJumpModeButton == null
                || jumpDurationStopButton == null
                || jumpCountStopButton == null
                || jumpManualStopButton == null
                || cameraLeftButton == null
                || cameraRightButton == null
                || cameraUpButton == null
                || cameraDownButton == null
                || leftMouseButton == null
                || rightMouseButton == null
                || holdModeButton == null
                || clickModeButton == null
                || instantMotionButton == null
                || smoothMotionButton == null
                || durationStopButton == null
                || clickCountStopButton == null
                || manualStopButton == null
                || cpsDecreaseButton == null
                || cpsIncreaseButton == null
                || angleDecreaseButton == null
                || angleIncreaseButton == null
                || primaryDecreaseButton == null
                || primaryIncreaseButton == null
                || testButton == null
                || duplicateButton == null
                || deleteButton == null
                || saveButton == null
        ) {
            return;
        }

        boolean actionsVisible =
            layoutMode
                == LayoutMode.WIDE
                || compactTab
                    == CompactTab.ACTIONS;

        boolean inspectorVisible =
            isInspectorVisible();

        actionLibrary.setVisible(
            actionsVisible
        );

        Scenario.Step step =
            getSelectedStep();

        boolean movement =
            step
                instanceof
                Scenario.MoveStep;

        boolean jump =
            step
                instanceof
                Scenario.JumpStep;

        boolean camera =
            step
                instanceof
                Scenario.CameraStep;

        boolean mouse =
            step
                instanceof
                Scenario.MouseStep;

        boolean hotbar =
            step
                instanceof
                Scenario.HotbarStep;

        boolean mouseClickMode =
            mouse
                && (
                    (Scenario.MouseStep)
                        step
                ).inputMode()
                    == Scenario.MouseInputMode
                        .CLICK;

        boolean jumpSingleMode =
            jump
                && (
                    (Scenario.JumpStep)
                        step
                ).mode()
                    == Scenario.JumpMode
                        .SINGLE;

        boolean jumpRepeatMode =
            jump
                && (
                    (Scenario.JumpStep)
                        step
                ).mode()
                    == Scenario.JumpMode
                        .REPEAT;

        boolean cpsUsed =
            usesCps(step);

        boolean angleUsed =
            usesAngle(step);

        boolean durationUsed =
            usesDuration(step);

        boolean countUsed =
            usesCount(step);

        boolean primaryValueUsed =
            durationUsed
                || countUsed
                || hotbar;

        forwardDirectionButton.visible =
            inspectorVisible && movement;

        backwardDirectionButton.visible =
            inspectorVisible && movement;

        leftDirectionButton.visible =
            inspectorVisible && movement;

        rightDirectionButton.visible =
            inspectorVisible && movement;

        walkModeButton.visible =
            inspectorVisible && movement;

        sprintModeButton.visible =
            inspectorVisible && movement;

        sneakModeButton.visible =
            inspectorVisible && movement;

        jumpToggleButton.visible =
            inspectorVisible && movement;

        singleJumpModeButton.visible =
            inspectorVisible && jump;

        holdJumpModeButton.visible =
            inspectorVisible && jump;

        repeatJumpModeButton.visible =
            inspectorVisible && jump;

        jumpDurationStopButton.visible =
            inspectorVisible
                && jump
                && !jumpSingleMode;

        jumpCountStopButton.visible =
            inspectorVisible
                && jumpRepeatMode;

        jumpManualStopButton.visible =
            inspectorVisible
                && jump
                && !jumpSingleMode;

        cameraLeftButton.visible =
            inspectorVisible && camera;

        cameraRightButton.visible =
            inspectorVisible && camera;

        cameraUpButton.visible =
            inspectorVisible && camera;

        cameraDownButton.visible =
            inspectorVisible && camera;

        instantMotionButton.visible =
            inspectorVisible && camera;

        smoothMotionButton.visible =
            inspectorVisible && camera;

        leftMouseButton.visible =
            inspectorVisible && mouse;

        rightMouseButton.visible =
            inspectorVisible && mouse;

        holdModeButton.visible =
            inspectorVisible && mouse;

        clickModeButton.visible =
            inspectorVisible && mouse;

        durationStopButton.visible =
            inspectorVisible && mouse;

        clickCountStopButton.visible =
            inspectorVisible
                && mouseClickMode;

        manualStopButton.visible =
            inspectorVisible && mouse;

        cpsDecreaseButton.visible =
            inspectorVisible && cpsUsed;

        cpsIncreaseButton.visible =
            inspectorVisible && cpsUsed;

        angleDecreaseButton.visible =
            inspectorVisible && angleUsed;

        angleField.visible =
            inspectorVisible && angleUsed;

        angleIncreaseButton.visible =
            inspectorVisible && angleUsed;

        primaryDecreaseButton.visible =
            inspectorVisible
                && primaryValueUsed;

        primaryIncreaseButton.visible =
            inspectorVisible
                && primaryValueUsed;

        durationField.visible =
            inspectorVisible
                && durationUsed;

        countField.visible =
            inspectorVisible
                && countUsed;

        testButton.visible =
            inspectorVisible;

        duplicateButton.visible =
            inspectorVisible;

        deleteButton.visible =
            inspectorVisible;

        if (
            step
                instanceof
                Scenario.MoveStep moveStep
        ) {
            updateSelectorButton(
                forwardDirectionButton,
                moveStep.direction()
                    == Scenario.MoveDirection
                        .FORWARD
            );

            updateSelectorButton(
                backwardDirectionButton,
                moveStep.direction()
                    == Scenario.MoveDirection
                        .BACKWARD
            );

            updateSelectorButton(
                leftDirectionButton,
                moveStep.direction()
                    == Scenario.MoveDirection
                        .LEFT
            );

            updateSelectorButton(
                rightDirectionButton,
                moveStep.direction()
                    == Scenario.MoveDirection
                        .RIGHT
            );

            updateSelectorButton(
                walkModeButton,
                moveStep.mode()
                    == Scenario.MoveMode.WALK
            );

            updateSelectorButton(
                sprintModeButton,
                moveStep.mode()
                    == Scenario.MoveMode.SPRINT
            );

            updateSelectorButton(
                sneakModeButton,
                moveStep.mode()
                    == Scenario.MoveMode.SNEAK
            );

            jumpToggleButton.setMessage(
                Component.literal(
                    layoutMode
                        == LayoutMode.WIDE
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
                jumpToggleButton,
                moveStep.jumping()
            );
        }

        if (
            step
                instanceof
                Scenario.JumpStep jumpStep
        ) {
            updateSelectorButton(
                singleJumpModeButton,
                jumpStep.mode()
                    == Scenario.JumpMode.SINGLE
            );

            updateSelectorButton(
                holdJumpModeButton,
                jumpStep.mode()
                    == Scenario.JumpMode.HOLD
            );

            updateSelectorButton(
                repeatJumpModeButton,
                jumpStep.mode()
                    == Scenario.JumpMode.REPEAT
            );

            updateSelectorButton(
                jumpDurationStopButton,
                jumpStep.stopMode()
                    == Scenario.JumpStopMode.DURATION
            );

            updateSelectorButton(
                jumpCountStopButton,
                jumpStep.stopMode()
                    == Scenario.JumpStopMode.JUMP_COUNT
            );

            updateSelectorButton(
                jumpManualStopButton,
                jumpStep.stopMode()
                    == Scenario.JumpStopMode.MANUAL
            );
        }

        if (
            step
                instanceof
                Scenario.CameraStep cameraStep
        ) {
            updateSelectorButton(
                cameraLeftButton,
                cameraStep.direction()
                    == Scenario.CameraDirection
                        .LEFT
            );

            updateSelectorButton(
                cameraRightButton,
                cameraStep.direction()
                    == Scenario.CameraDirection
                        .RIGHT
            );

            updateSelectorButton(
                cameraUpButton,
                cameraStep.direction()
                    == Scenario.CameraDirection
                        .UP
            );

            updateSelectorButton(
                cameraDownButton,
                cameraStep.direction()
                    == Scenario.CameraDirection
                        .DOWN
            );

            updateSelectorButton(
                instantMotionButton,
                cameraStep.motion()
                    == Scenario.CameraMotion
                        .INSTANT
            );

            updateSelectorButton(
                smoothMotionButton,
                cameraStep.motion()
                    == Scenario.CameraMotion
                        .SMOOTH
            );

            angleDecreaseButton.active =
                cameraStep.angleDegrees()
                    > Scenario.CameraStep
                        .MIN_ANGLE_DEGREES;

            angleIncreaseButton.active =
                cameraStep.angleDegrees()
                    < Scenario.CameraStep
                        .MAX_ANGLE_DEGREES;
        }

        if (
            step
                instanceof
                Scenario.MouseStep mouseStep
        ) {
            updateSelectorButton(
                leftMouseButton,
                mouseStep.action()
                    == Scenario.MouseAction
                        .LEFT_CLICK
            );

            updateSelectorButton(
                rightMouseButton,
                mouseStep.action()
                    == Scenario.MouseAction
                        .RIGHT_CLICK
            );

            updateSelectorButton(
                holdModeButton,
                mouseStep.inputMode()
                    == Scenario.MouseInputMode
                        .HOLD
            );

            updateSelectorButton(
                clickModeButton,
                mouseStep.inputMode()
                    == Scenario.MouseInputMode
                        .CLICK
            );

            updateSelectorButton(
                durationStopButton,
                mouseStep.stopMode()
                    == Scenario.MouseStopMode
                        .DURATION
            );

            updateSelectorButton(
                clickCountStopButton,
                mouseStep.stopMode()
                    == Scenario.MouseStopMode
                        .CLICK_COUNT
            );

            updateSelectorButton(
                manualStopButton,
                mouseStep.stopMode()
                    == Scenario.MouseStopMode
                        .MANUAL
            );

            cpsDecreaseButton.active =
                mouseStep
                    .clicksPerSecondHalfSteps()
                    > Scenario.MouseStep
                        .MIN_CPS_HALF_STEPS;

            cpsIncreaseButton.active =
                mouseStep
                    .clicksPerSecondHalfSteps()
                    < Scenario.MouseStep
                        .MAX_CPS_HALF_STEPS;
        }

        if (durationUsed) {
            primaryDecreaseButton.active =
                step.durationTicks()
                    > MIN_DURATION_TICKS;

            primaryIncreaseButton.active =
                step.durationTicks()
                    < MAX_DURATION_TICKS;
        } else if (
            step
                instanceof
                Scenario.JumpStep jumpStep
                && countUsed
        ) {
            primaryDecreaseButton.active =
                jumpStep.jumpCount()
                    > Scenario.JumpStep
                        .MIN_JUMP_COUNT;

            primaryIncreaseButton.active =
                jumpStep.jumpCount()
                    < Scenario.JumpStep
                        .MAX_JUMP_COUNT;
        } else if (
            step
                instanceof
                Scenario.MouseStep mouseStep
                && countUsed
        ) {
            primaryDecreaseButton.active =
                mouseStep.clickCount()
                    > Scenario.MouseStep
                        .MIN_CLICK_COUNT;

            primaryIncreaseButton.active =
                mouseStep.clickCount()
                    < Scenario.MouseStep
                        .MAX_CLICK_COUNT;
        } else if (
            step
                instanceof
                Scenario.HotbarStep hotbarStep
        ) {
            primaryDecreaseButton.active =
                hotbarStep.slot()
                    > Scenario.HotbarStep
                        .MIN_SLOT;

            primaryIncreaseButton.active =
                hotbarStep.slot()
                    < Scenario.HotbarStep
                        .MAX_SLOT;
        }

        TaskStatus status =
            TaskManager.getStatus();

        testButton.setMessage(
            Component.literal(
                status == TaskStatus.IDLE
                    ? layoutMode
                        == LayoutMode.WIDE
                            ? "Test Step"
                            : "Test"
                    : layoutMode
                        == LayoutMode.WIDE
                            ? "Stop Test"
                            : "Stop"
            )
        );

        testButton.setStyle(
            status == TaskStatus.IDLE
                ? KarakuriButton.Style.SUCCESS
                : KarakuriButton.Style.DANGER
        );

        duplicateButton.active =
            status == TaskStatus.IDLE;

        deleteButton.active =
            status == TaskStatus.IDLE
                && steps.size() > 1;

        saveButton.active =
            status == TaskStatus.IDLE
                && getValidationMessage()
                    == null;

        if (
            layoutMode
                == LayoutMode.COMPACT
        ) {
            workflowTabButton.setStyle(
                compactTab
                    == CompactTab.WORKFLOW
                        ? KarakuriButton
                            .Style.PRIMARY
                        : KarakuriButton
                            .Style.GHOST
            );

            actionsTabButton.setStyle(
                compactTab
                    == CompactTab.ACTIONS
                        ? KarakuriButton
                            .Style.PRIMARY
                        : KarakuriButton
                            .Style.GHOST
            );

            inspectorTabButton.setStyle(
                compactTab
                    == CompactTab.INSPECTOR
                        ? KarakuriButton
                            .Style.PRIMARY
                        : KarakuriButton
                            .Style.GHOST
            );
        }
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

    private boolean usesCps(
        Scenario.Step step
    ) {
        return step
            instanceof
            Scenario.MouseStep mouseStep
            && mouseStep.inputMode()
                == Scenario.MouseInputMode
                    .CLICK;
    }

    private boolean usesAngle(
        Scenario.Step step
    ) {
        return step
            instanceof
            Scenario.CameraStep;
    }

    private boolean usesHotbarSlot(
        Scenario.Step step
    ) {
        return step
            instanceof
            Scenario.HotbarStep;
    }

    private boolean usesDuration(
        Scenario.Step step
    ) {
        if (
            step
                instanceof
                Scenario.HotbarStep
        ) {
            return false;
        }

        if (
            step
                instanceof
                Scenario.JumpStep jumpStep
        ) {
            return jumpStep.mode()
                != Scenario.JumpMode.SINGLE
                && jumpStep.stopMode()
                    == Scenario.JumpStopMode.DURATION;
        }

        if (
            step
                instanceof
                Scenario.MouseStep mouseStep
        ) {
            return mouseStep.stopMode()
                == Scenario.MouseStopMode
                    .DURATION;
        }

        if (
            step
                instanceof
                Scenario.CameraStep cameraStep
        ) {
            return cameraStep.motion()
                == Scenario.CameraMotion
                    .SMOOTH;
        }

        return true;
    }

    private boolean usesCount(
        Scenario.Step step
    ) {
        if (
            step
                instanceof
                Scenario.JumpStep jumpStep
        ) {
            return jumpStep.mode()
                == Scenario.JumpMode.REPEAT
                && jumpStep.stopMode()
                    == Scenario.JumpStopMode.JUMP_COUNT;
        }

        return step
            instanceof
            Scenario.MouseStep mouseStep
            && mouseStep.inputMode()
                == Scenario.MouseInputMode
                    .CLICK
            && mouseStep.stopMode()
                == Scenario.MouseStopMode
                    .CLICK_COUNT;
    }

    private boolean usesPrimaryValue(
        Scenario.Step step
    ) {
        return usesDuration(step)
            || usesCount(step)
            || usesHotbarSlot(step);
    }

    private boolean isPrimaryValueValid(
        Scenario.Step step
    ) {
        if (usesDuration(step)) {
            return durationFieldValid;
        }

        if (usesCount(step)) {
            return countFieldValid;
        }

        return true;
    }

    private String getJumpPrimaryValueLabel(
        Scenario.JumpStep step
    ) {
        if (
            step.mode()
                == Scenario.JumpMode.SINGLE
        ) {
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

    private String getJumpDescription(
        Scenario.JumpStep step
    ) {
        return switch (step.mode()) {
            case SINGLE ->
                "Presses Jump once";

            case HOLD ->
                step.stopMode()
                    == Scenario.JumpStopMode.MANUAL
                        ? "Keeps Jump held until Stop"
                        : "Keeps Jump held for the selected time";

            case REPEAT ->
                step.stopMode()
                    == Scenario.JumpStopMode.MANUAL
                        ? "Jumps after every landing until Stop"
                        : "Jumps again after every landing";
        };
    }

    private String getMousePrimaryValueLabel(
        Scenario.MouseStep step
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

    private String getMouseEstimate(
        Scenario.MouseStep step
    ) {
        if (
            step.inputMode()
                == Scenario.MouseInputMode
                    .HOLD
        ) {
            return step.stopMode()
                == Scenario.MouseStopMode
                    .MANUAL
                        ? "Runs until Stop"
                        : "Held for "
                            + formatDurationValue(
                                step.durationTicks()
                            );
        }

        return switch (step.stopMode()) {
            case DURATION ->
                "Estimated clicks: "
                    + step.estimatedClickCount();

            case CLICK_COUNT ->
                "Estimated time: "
                    + formatDurationValue(
                        step.estimatedDurationTicks()
                    );

            case MANUAL ->
                "Clicks until Stop";
        };
    }

    private String getCountDisplayValue(
        Scenario.Step step
    ) {
        if (
            step
                instanceof
                Scenario.JumpStep jumpStep
        ) {
            return jumpStep.jumpCount()
                + (
                    jumpStep.jumpCount() == 1
                        ? " jump"
                        : " jumps"
                );
        }

        Scenario.MouseStep mouseStep =
            (Scenario.MouseStep) step;

        return mouseStep.clickCount()
            + (
                mouseStep.clickCount() == 1
                    ? " click"
                    : " clicks"
            );
    }

    private boolean isWorkflowVisible() {
        return layoutMode
            == LayoutMode.WIDE
            || compactTab
                == CompactTab.WORKFLOW;
    }

    private boolean isInspectorVisible() {
        return layoutMode
            == LayoutMode.WIDE
            || compactTab
                == CompactTab.INSPECTOR;
    }

    private Scenario.Step getSelectedStep() {
        selectedStepIndex =
            Math.clamp(
                selectedStepIndex,
                0,
                steps.size() - 1
            );

        return steps.get(
            selectedStepIndex
        );
    }

    private String getValidationMessage() {
        if (!isScenarioNameValid()) {
            String name =
                nameField == null
                    ? initialName
                    : nameField
                        .getValue()
                        .trim();

            return name.isBlank()
                ? "Scenario name is required"
                : "A scenario with this name already exists";
        }

        Scenario.Step step =
            getSelectedStep();

        if (
            usesAngle(step)
                && !angleFieldValid
        ) {
            return "Camera angle must be between 1 and 180 degrees";
        }

        if (
            usesDuration(step)
                && !durationFieldValid
        ) {
            return "Duration must be between 0.05 and 3600 seconds";
        }

        if (
            usesCount(step)
                && !countFieldValid
        ) {
            return step
                instanceof
                Scenario.JumpStep
                    ? "Jump count must be between 1 and 100000"
                    : "Click count must be between 1 and 100000";
        }

        for (
            int index = 0;
            index < steps.size() - 1;
            index++
        ) {
            if (steps.get(index).isInfinite()) {
                return "A manual step must be the final block";
            }
        }

        return null;
    }

    private boolean isScenarioNameValid() {
        String name =
            nameField == null
                ? initialName
                : nameField
                    .getValue()
                    .trim();

        return !name.isBlank()
            && !ScenarioLibrary
                .containsName(
                    name,
                    scenarioIndex
                );
    }

    private String formatDurationForField(
        int durationTicks
    ) {
        return BigDecimal
            .valueOf(durationTicks)
            .divide(
                BigDecimal.valueOf(20)
            )
            .stripTrailingZeros()
            .toPlainString();
    }

    private String formatDurationValue(
        int durationTicks
    ) {
        return formatDurationForField(
            durationTicks
        ) + " s";
    }

    private String getStepTitle(
        Scenario.Step step
    ) {
        return switch (step) {
            case Scenario.CameraStep cameraStep ->
                cameraStep
                    .direction()
                    .label();

            case Scenario.HotbarStep hotbarStep ->
                "Select Hotbar Slot "
                    + (
                        hotbarStep.slot()
                            + 1
                    );

            case Scenario.JumpStep jumpStep ->
                switch (jumpStep.mode()) {
                    case SINGLE ->
                        "Single Jump";

                    case HOLD ->
                        "Hold Jump";

                    case REPEAT ->
                        "Repeat Jumps";
                };

            case Scenario.MoveStep moveStep -> {
                String movement =
                    moveStep.mode().label()
                        + " "
                        + moveStep
                            .direction()
                            .label();

                yield moveStep.jumping()
                    ? "Jump + " + movement
                    : movement;
            }

            case Scenario.MouseStep mouseStep ->
                mouseStep.inputMode()
                    == Scenario.MouseInputMode
                        .HOLD
                            ? "Hold "
                                + mouseStep
                                    .action()
                                    .label()
                            : mouseStep
                                .action()
                                .label();

            case Scenario.WaitStep waitStep ->
                "Wait";
        };
    }

    private int getStepAccentColor(
        Scenario.Step step
    ) {
        return switch (step) {
            case Scenario.CameraStep cameraStep ->
                switch (cameraStep.direction()) {
                    case LEFT ->
                        0xFF67B6E8;
                    case RIGHT ->
                        0xFFB38AE8;
                    case UP ->
                        0xFF61D394;
                    case DOWN ->
                        0xFFF0A765;
                };

            case Scenario.HotbarStep hotbarStep ->
                0xFFE8D26A;

            case Scenario.JumpStep jumpStep ->
                0xFF78D6C6;

            case Scenario.MoveStep moveStep ->
                switch (moveStep.direction()) {
                    case FORWARD ->
                        0xFF61D394;
                    case BACKWARD ->
                        0xFFF0A765;
                    case LEFT ->
                        0xFF67B6E8;
                    case RIGHT ->
                        0xFFB38AE8;
                };

            case Scenario.MouseStep mouseStep ->
                switch (mouseStep.action()) {
                    case LEFT_CLICK ->
                        0xFFE66777;
                    case RIGHT_CLICK ->
                        0xFF67C7E8;
                };

            case Scenario.WaitStep waitStep ->
                0xFFA49BAD;
        };
    }

    private int getWideLibraryWidth() {
        return Math.clamp(
            bodyWidth * 18 / 100,
            150,
            220
        );
    }

    private boolean isWideScreen() {
        return width >= WIDE_MIN_WIDTH
            && height >= WIDE_MIN_HEIGHT;
    }

    private int getResponsivePanelWidth() {
        int margin =
            isWideScreen()
                ? WIDE_MARGIN
                : COMPACT_MARGIN;

        return Math.min(
            WIDE_MAX_WIDTH,
            width - margin * 2
        );
    }

    private int getResponsivePanelHeight() {
        int margin =
            isWideScreen()
                ? WIDE_MARGIN
                : COMPACT_MARGIN;

        return Math.min(
            WIDE_MAX_HEIGHT,
            height - margin * 2
        );
    }

    private enum LayoutMode {
        WIDE,
        COMPACT
    }

    private enum CompactTab {
        WORKFLOW,
        ACTIONS,
        INSPECTOR
    }
}