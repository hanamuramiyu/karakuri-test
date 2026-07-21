package hanamuramiyu.karakuri.ui;

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
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioFormat;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.ScenarioTaskFactory;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorState;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepPresentation;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepRules;
import hanamuramiyu.karakuri.ui.editor.ScenarioValueFormatter;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;


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

    private final KarakuriScreen parent;
    private final ScenarioEditorState state;

    private LayoutMode layoutMode;
    private CompactTab compactTab =
        CompactTab.WORKFLOW;

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
        this.state = ScenarioEditorState.create(
            scenarioIndex,
            scenario
        );
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
                state.steps(),
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
            state.selectedIndex()
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
            state.initialName()
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
                () -> setDirection(
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
                () -> setDirection(
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
                () -> setDirection(
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
                () -> setJumpMode(
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
                () -> setJumpMode(
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
                () -> setJumpMode(
                    JumpMode.REPEAT
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
                () -> setCameraDirection(
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
                () -> setCameraDirection(
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
                () -> setCameraDirection(
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
                () -> setMouseAction(
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
                () -> setMouseAction(
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
                () -> setMouseInputMode(
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
                () -> setMouseInputMode(
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
                () -> setMoveMode(
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
                () -> setMoveMode(
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
                () -> setMoveMode(
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
                () -> setJumpStopMode(
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
                () -> setJumpStopMode(
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
                () -> setJumpStopMode(
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
                () -> setCameraMotion(
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
                () -> setCameraMotion(
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
                () -> setMouseStopMode(
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
                () -> setMouseStopMode(
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
                () -> setMouseStopMode(
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
                () -> setDirection(
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
                () -> setDirection(
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
                () -> setDirection(
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
                () -> setJumpMode(
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
                () -> setJumpMode(
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
                () -> setJumpMode(
                    JumpMode.REPEAT
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
                () -> setCameraDirection(
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
                () -> setCameraDirection(
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
                () -> setCameraDirection(
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
                () -> setMouseAction(
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
                () -> setMouseAction(
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
                () -> setMouseInputMode(
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
                () -> setMouseInputMode(
                    MouseInputMode
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
                () -> setMoveMode(
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
                () -> setMoveMode(
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
                () -> setJumpStopMode(
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
                () -> setJumpStopMode(
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
                () -> setCameraMotion(
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
                () -> setCameraMotion(
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
                () -> setMouseStopMode(
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
                () -> setMouseStopMode(
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
                () -> setMouseStopMode(
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
                ScenarioStepRules.usesAngle(step)
                    && !angleFieldValid
                        ? 0xFFC75B69
                        : 0xFF51475E
            );
        }

        if (ScenarioStepRules.usesPrimaryValue(step)) {
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

        ScenarioStep step =
            getSelectedStep();

        if (
            step
                instanceof
                HotbarStep hotbarStep
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

        if (ScenarioStepRules.usesCps(step)) {
            renderCenteredFrameText(
                graphics,
                secondaryFrameX,
                secondaryFrameY,
                secondaryFrameWidth,
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
            angleField.visible
                && !angleField.isFocused()
        ) {
            CameraStep cameraStep =
                (CameraStep) step;

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
            countField.visible
                && !countField.isFocused()
        ) {
            coverFrameAndRenderValue(
                graphics,
                primaryFrameX,
                primaryFrameY,
                primaryFrameWidth,
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
        state.select(index);
        syncValueFields();
        updateButtons();
    }

    private void onCanvasContentChanged() {
        stopRunningTest();
        state.select(
            workflowCanvas.getSelectedIndex()
        );
        syncValueFields();
        updateButtons();
    }

    private void insertMoveStep(
        MoveDirection direction
    ) {
        stopRunningTest();
        state.insertMoveStep(direction);
        syncSelectedStep();
        returnToWorkflow();
    }

    private void insertJumpStep() {
        stopRunningTest();
        state.insertJumpStep();
        syncSelectedStep();
        returnToWorkflow();
    }

    private void insertWaitStep() {
        stopRunningTest();
        state.insertWaitStep();
        syncSelectedStep();
        returnToWorkflow();
    }

    private void insertMouseStep(
        MouseAction action
    ) {
        stopRunningTest();
        state.insertMouseStep(action);
        syncSelectedStep();
        returnToWorkflow();
    }

    private void insertCameraStep(
        CameraDirection direction
    ) {
        stopRunningTest();
        state.insertCameraStep(direction);
        syncSelectedStep();
        returnToWorkflow();
    }

    private void insertHotbarStep() {
        stopRunningTest();
        state.insertHotbarStep();
        syncSelectedStep();
        returnToWorkflow();
    }

    private void syncSelectedStep() {
        workflowCanvas.setSelectedIndex(
            state.selectedIndex()
        );

        syncValueFields();
        updateButtons();
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
        state.duplicateSelectedStep();
        syncSelectedStep();
    }

    private void deleteSelectedStep() {
        stopRunningTest();

        if (state.deleteSelectedStep()) {
            syncSelectedStep();
        }
    }

    private void selectStep(
        int index
    ) {
        state.select(index);
        syncSelectedStep();
    }

    private void setDirection(
        MoveDirection direction
    ) {
        stopRunningTest();
        state.setMoveDirection(direction);
        updateButtons();
    }

    private void setMoveMode(
        MoveMode mode
    ) {
        stopRunningTest();
        state.setMoveMode(mode);
        updateButtons();
    }

    private void toggleMoveJumping() {
        stopRunningTest();
        state.toggleMoveJumping();
        updateButtons();
    }

    private void setJumpMode(
        JumpMode mode
    ) {
        stopRunningTest();
        state.setJumpMode(mode);
        syncValueFields();
        updateButtons();
    }

    private void setJumpStopMode(
        JumpStopMode stopMode
    ) {
        stopRunningTest();
        state.setJumpStopMode(stopMode);
        syncValueFields();
        updateButtons();
    }

    private void setCameraDirection(
        CameraDirection direction
    ) {
        stopRunningTest();
        state.setCameraDirection(direction);
        updateButtons();
    }

    private void setCameraMotion(
        CameraMotion motion
    ) {
        stopRunningTest();
        state.setCameraMotion(motion);
        syncValueFields();
        updateButtons();
    }

    private void setMouseAction(
        MouseAction action
    ) {
        stopRunningTest();
        state.setMouseAction(action);
        updateButtons();
    }

    private void setMouseInputMode(
        MouseInputMode inputMode
    ) {
        stopRunningTest();
        state.setMouseInputMode(inputMode);
        syncValueFields();
        updateButtons();
    }

    private void setMouseStopMode(
        MouseStopMode stopMode
    ) {
        stopRunningTest();
        state.setMouseStopMode(stopMode);
        syncValueFields();
        updateButtons();
    }

    private void changeCps(
        int direction
    ) {
        stopRunningTest();
        state.changeCps(direction);
        updateButtons();
    }

    private void changeAngle(
        int direction
    ) {
        stopRunningTest();
        state.changeAngle(direction);
        syncAngleField();
        updateButtons();
    }

    private void changePrimaryValue(
        int direction
    ) {
        stopRunningTest();
        state.changePrimaryValue(direction);
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
                        < ScenarioEditorState.MIN_DURATION_TICKS
                    || durationTicks
                        > ScenarioEditorState.MAX_DURATION_TICKS
            ) {
                durationFieldValid = false;
                updateButtons();
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

            if (count < 1 || count > 100000) {
                countFieldValid = false;
                updateButtons();
                return;
            }

            state.setCount(count);
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
                angle < CameraStep.MIN_ANGLE_DEGREES
                    || angle > CameraStep.MAX_ANGLE_DEGREES
            ) {
                angleFieldValid = false;
                updateButtons();
                return;
            }

            state.setAngle(angle);
            angleFieldValid = true;
        } catch (
            NumberFormatException exception
        ) {
            angleFieldValid = false;
        }

        updateButtons();
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
            ScenarioValueFormatter.durationForField(
                getSelectedStep().durationTicks()
            )
        );

        syncingDurationField = false;
        durationFieldValid = true;
    }

    private void syncCountField() {
        if (countField == null) {
            return;
        }

        ScenarioStep step = getSelectedStep();
        syncingCountField = true;

        if (step instanceof JumpStep jumpStep) {
            countField.setValue(
                Integer.toString(
                    jumpStep.jumpCount()
                )
            );
        } else if (step instanceof MouseStep mouseStep) {
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

        if (getSelectedStep() instanceof CameraStep cameraStep) {
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
            state.toScenario(
                nameField.getValue()
            );

        if (state.scenarioIndex() < 0) {
            ScenarioLibrary.add(
                scenario
            );
        } else {
            ScenarioLibrary.replace(
                state.scenarioIndex(),
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
                MoveStep moveStep
        ) {
            updateSelectorButton(
                forwardDirectionButton,
                moveStep.direction()
                    == MoveDirection
                        .FORWARD
            );

            updateSelectorButton(
                backwardDirectionButton,
                moveStep.direction()
                    == MoveDirection
                        .BACKWARD
            );

            updateSelectorButton(
                leftDirectionButton,
                moveStep.direction()
                    == MoveDirection
                        .LEFT
            );

            updateSelectorButton(
                rightDirectionButton,
                moveStep.direction()
                    == MoveDirection
                        .RIGHT
            );

            updateSelectorButton(
                walkModeButton,
                moveStep.mode()
                    == MoveMode.WALK
            );

            updateSelectorButton(
                sprintModeButton,
                moveStep.mode()
                    == MoveMode.SPRINT
            );

            updateSelectorButton(
                sneakModeButton,
                moveStep.mode()
                    == MoveMode.SNEAK
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
                JumpStep jumpStep
        ) {
            updateSelectorButton(
                singleJumpModeButton,
                jumpStep.mode()
                    == JumpMode.SINGLE
            );

            updateSelectorButton(
                holdJumpModeButton,
                jumpStep.mode()
                    == JumpMode.HOLD
            );

            updateSelectorButton(
                repeatJumpModeButton,
                jumpStep.mode()
                    == JumpMode.REPEAT
            );

            updateSelectorButton(
                jumpDurationStopButton,
                jumpStep.stopMode()
                    == JumpStopMode.DURATION
            );

            updateSelectorButton(
                jumpCountStopButton,
                jumpStep.stopMode()
                    == JumpStopMode.JUMP_COUNT
            );

            updateSelectorButton(
                jumpManualStopButton,
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
                cameraLeftButton,
                cameraStep.direction()
                    == CameraDirection
                        .LEFT
            );

            updateSelectorButton(
                cameraRightButton,
                cameraStep.direction()
                    == CameraDirection
                        .RIGHT
            );

            updateSelectorButton(
                cameraUpButton,
                cameraStep.direction()
                    == CameraDirection
                        .UP
            );

            updateSelectorButton(
                cameraDownButton,
                cameraStep.direction()
                    == CameraDirection
                        .DOWN
            );

            updateSelectorButton(
                instantMotionButton,
                cameraStep.motion()
                    == CameraMotion
                        .INSTANT
            );

            updateSelectorButton(
                smoothMotionButton,
                cameraStep.motion()
                    == CameraMotion
                        .SMOOTH
            );

            angleDecreaseButton.active =
                cameraStep.angleDegrees()
                    > CameraStep
                        .MIN_ANGLE_DEGREES;

            angleIncreaseButton.active =
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
                leftMouseButton,
                mouseStep.action()
                    == MouseAction
                        .LEFT_CLICK
            );

            updateSelectorButton(
                rightMouseButton,
                mouseStep.action()
                    == MouseAction
                        .RIGHT_CLICK
            );

            updateSelectorButton(
                holdModeButton,
                mouseStep.inputMode()
                    == MouseInputMode
                        .HOLD
            );

            updateSelectorButton(
                clickModeButton,
                mouseStep.inputMode()
                    == MouseInputMode
                        .CLICK
            );

            updateSelectorButton(
                durationStopButton,
                mouseStep.stopMode()
                    == MouseStopMode
                        .DURATION
            );

            updateSelectorButton(
                clickCountStopButton,
                mouseStep.stopMode()
                    == MouseStopMode
                        .CLICK_COUNT
            );

            updateSelectorButton(
                manualStopButton,
                mouseStep.stopMode()
                    == MouseStopMode
                        .MANUAL
            );

            cpsDecreaseButton.active =
                mouseStep
                    .clicksPerSecondHalfSteps()
                    > MouseStep
                        .MIN_CPS_HALF_STEPS;

            cpsIncreaseButton.active =
                mouseStep
                    .clicksPerSecondHalfSteps()
                    < MouseStep
                        .MAX_CPS_HALF_STEPS;
        }

        if (durationUsed) {
            primaryDecreaseButton.active =
                step.durationTicks()
                    > ScenarioEditorState.MIN_DURATION_TICKS;

            primaryIncreaseButton.active =
                step.durationTicks()
                    < ScenarioEditorState.MAX_DURATION_TICKS;
        } else if (
            step
                instanceof
                JumpStep jumpStep
                && countUsed
        ) {
            primaryDecreaseButton.active =
                jumpStep.jumpCount()
                    > JumpStep
                        .MIN_JUMP_COUNT;

            primaryIncreaseButton.active =
                jumpStep.jumpCount()
                    < JumpStep
                        .MAX_JUMP_COUNT;
        } else if (
            step
                instanceof
                MouseStep mouseStep
                && countUsed
        ) {
            primaryDecreaseButton.active =
                mouseStep.clickCount()
                    > MouseStep
                        .MIN_CLICK_COUNT;

            primaryIncreaseButton.active =
                mouseStep.clickCount()
                    < MouseStep
                        .MAX_CLICK_COUNT;
        } else if (
            step
                instanceof
                HotbarStep hotbarStep
        ) {
            primaryDecreaseButton.active =
                hotbarStep.slot()
                    > HotbarStep
                        .MIN_SLOT;

            primaryIncreaseButton.active =
                hotbarStep.slot()
                    < HotbarStep
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
                && state.size() > 1;

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

    private ScenarioStep getSelectedStep() {
        return state.selectedStep();
    }

    private String getValidationMessage() {
        if (!isScenarioNameValid()) {
            String name =
                nameField == null
                    ? state.initialName()
                    : nameField
                        .getValue()
                        .trim();

            return name.isBlank()
                ? "Scenario name is required"
                : "A scenario with this name already exists";
        }

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
            return step
                instanceof
                JumpStep
                    ? "Jump count must be between 1 and 100000"
                    : "Click count must be between 1 and 100000";
        }

        if (state.hasInfiniteStepBeforeEnd()) {
            return "A manual step must be the final block";
        }

        return null;
    }

    private boolean isScenarioNameValid() {
        String name =
            nameField == null
                ? state.initialName()
                : nameField
                    .getValue()
                    .trim();

        return !name.isBlank()
            && !ScenarioLibrary
                .containsName(
                    name,
                    state.scenarioIndex()
                );
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