package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MoveDirection;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.task.factory.ScenarioTaskFactory;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorState;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorTheme;
import hanamuramiyu.karakuri.ui.editor.inspector.ScenarioInspector;
import hanamuramiyu.karakuri.ui.editor.inspector.ScenarioInspectorLayout;
import hanamuramiyu.karakuri.ui.editor.library.ScenarioActionLibrary;
import hanamuramiyu.karakuri.ui.editor.library.ScenarioActionLibraryActions;
import hanamuramiyu.karakuri.ui.editor.library.ScenarioActionLibraryLayout;
import hanamuramiyu.karakuri.ui.editor.workflow.ScenarioWorkflowCanvas;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class ScenarioEditorScreen extends Screen {
    private static final int WIDE_MIN_WIDTH = 900;
    private static final int WIDE_MIN_HEIGHT = 420;
    private static final int WIDE_MAX_WIDTH = 1600;
    private static final int WIDE_MAX_HEIGHT = 900;
    private static final int WIDE_MARGIN = 8;
    private static final int COMPACT_MARGIN = 4;
    private static final int CONTENT_MARGIN = 6;
    private static final int PANEL_GAP = 6;
    private static final int BUTTON_GAP = 4;
    private static final int BUTTON_HEIGHT = 20;
    private static final int WIDE_TOOLBAR_HEIGHT = 42;
    private static final int COMPACT_TOOLBAR_HEIGHT = 66;
    private static final int COMPACT_TAB_HEIGHT = 20;
    private static final int STATUS_HEIGHT = 22;
    private static final int ACTION_TOOLBAR_HEIGHT = 28;
    private static final int WIDE_BACK_WIDTH = 72;
    private static final int COMPACT_BACK_WIDTH = 68;
    private static final int WIDE_SAVE_WIDTH = 86;
    private static final int COMPACT_SAVE_WIDTH = 72;
    private static final int WIDE_HISTORY_WIDTH = 50;
    private static final int COMPACT_HISTORY_WIDTH = 42;
    private static final int WIDE_WORKFLOW_HEADER_HEIGHT = 28;
    private static final int COMPACT_WORKFLOW_HEADER_HEIGHT = 50;

    private final KarakuriScreen parent;
    private final ScenarioEditorState state;

    private LayoutMode layoutMode;
    private CompactTab compactTab = CompactTab.WORKFLOW;

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
    private int modeBadgeX;
    private int modeBadgeY;
    private int modeBadgeWidth;
    private int modeBadgeHeight;
    private int workflowX;
    private int workflowWidth;
    private int workflowHeaderY;
    private int workflowHeaderHeight;
    private int workflowY;
    private int workflowHeight;
    private int actionLibraryX;
    private int actionLibraryY;
    private int actionLibraryWidth;
    private int actionLibraryHeight;

    private ScenarioActionLibrary actionLibrary;
    private ScenarioWorkflowCanvas workflowCanvas;
    private ScenarioInspector inspector;

    private EditBox nameField;

    private KarakuriButton workflowTabButton;
    private KarakuriButton actionsTabButton;
    private KarakuriButton inspectorTabButton;
    private KarakuriButton backButton;
    private KarakuriButton undoButton;
    private KarakuriButton redoButton;
    private KarakuriButton saveButton;
    private KarakuriButton groupBackButton;
    private KarakuriButton groupActionsButton;
    private KarakuriButton moveOutButton;
    private KarakuriButton openGroupButton;
    private KarakuriButton includePreviousButton;
    private KarakuriButton includeNextButton;

    private boolean groupActionsOpen;

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
        layoutMode = isWideScreen()
            ? LayoutMode.WIDE
            : LayoutMode.COMPACT;

        panelWidth = getResponsivePanelWidth();
        panelHeight = getResponsivePanelHeight();
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        if (layoutMode == LayoutMode.WIDE) {
            initializeWideLayout();
        } else {
            initializeCompactLayout();
        }

        createToolbarWidgets();
        createNameField();

        actionLibrary = new ScenarioActionLibrary(
            font,
            new ScenarioActionLibraryLayout(
                actionLibraryX,
                actionLibraryY,
                actionLibraryWidth,
                actionLibraryHeight,
                layoutMode == LayoutMode.WIDE
                    ? ScenarioActionLibraryLayout.Mode.TOOLBAR
                    : ScenarioActionLibraryLayout.Mode.PANEL
            ),
            new ScenarioActionLibraryActions(
                this::insertMoveStep,
                this::insertJumpStep,
                this::insertWaitStep,
                this::insertRepeatGroup,
                this::insertMouseStep,
                this::insertCameraStep,
                this::insertHotbarStep
            )
        );

        for (KarakuriButton widget : actionLibrary.widgets()) {
            addRenderableWidget(widget);
        }

        workflowCanvas = new ScenarioWorkflowCanvas(
            font,
            state.steps(),
            this::onCanvasSelectionChanged,
            this::onCanvasEditStarted,
            this::onCanvasContentChanged
        );

        workflowCanvas.setBounds(
            workflowX,
            workflowY,
            workflowWidth,
            workflowHeight
        );
        workflowCanvas.setSelectedIndex(state.selectedIndex());

        createGroupNavigationWidgets();

        inspector = new ScenarioInspector(
            font,
            state,
            new ScenarioInspectorLayout(
                layoutMode == LayoutMode.WIDE
                    ? ScenarioInspectorLayout.Mode.WIDE
                    : ScenarioInspectorLayout.Mode.COMPACT,
                inspectorX,
                inspectorY,
                inspectorWidth,
                inspectorHeight
            ),
            this::stopRunningTest,
            this::testSelectedStep,
            this::duplicateSelectedStep,
            this::deleteSelectedStep,
            this::updateButtons
        );

        for (AbstractWidget widget : inspector.widgets()) {
            addRenderableWidget(widget);
        }

        if (layoutMode == LayoutMode.COMPACT) {
            createCompactTabs();
        }

        inspector.syncSelectedStep();
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
            ScenarioEditorTheme.SCREEN_OVERLAY
        );

        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + panelHeight,
            ScenarioEditorTheme.SHELL
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            ScenarioEditorTheme.OUTLINE_STRONG
        );

        renderToolbar(graphics);
        actionLibrary.render(graphics);

        if (isWorkflowVisible()) {
            renderWorkflowHeader(graphics);
            workflowCanvas.render(
                graphics,
                mouseX,
                mouseY,
                Component.literal(
                    "Drag to reorder  ·  Scroll a card to adjust its main value"
                ),
                ScenarioEditorTheme.TEXT_MUTED
            );
        }

        actionLibrary.renderDrawer(graphics);
        inspector.render(graphics);
        renderStatusBar(graphics);

        super.render(graphics, mouseX, mouseY, delta);
        inspector.renderCenteredValues(graphics);
        inspector.renderDropdownOverlay(
            graphics,
            mouseX,
            mouseY,
            delta
        );
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (
            inspector != null
                && inspector.mouseClicked(event, doubled)
        ) {
            return true;
        }

        if (super.mouseClicked(event, doubled)) {
            return true;
        }

        if (groupActionsOpen) {
            groupActionsOpen = false;
            updateButtons();
            return true;
        }

        if (!isWorkflowVisible()) {
            return false;
        }

        boolean handled = workflowCanvas.mouseClicked(event);

        if (
            handled
                && doubled
                && state.selectedStep() instanceof RepeatStep
        ) {
            openSelectedGroup();
        }

        return handled;
    }

    @Override
    public boolean mouseDragged(
        MouseButtonEvent event,
        double offsetX,
        double offsetY
    ) {
        if (
            isWorkflowVisible()
                && workflowCanvas.mouseDragged(event)
        ) {
            return true;
        }

        return super.mouseDragged(event, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        boolean handled =
            isWorkflowVisible()
                && workflowCanvas.mouseReleased();

        return super.mouseReleased(event) || handled;
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
                && workflowCanvas.mouseScrolled(
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
    public boolean keyPressed(
        KeyEvent event
    ) {
        if (event.hasControlDownWithQuirk()) {
            if (event.key() == GLFW.GLFW_KEY_Z) {
                if (event.hasShiftDown()) {
                    redoEdit();
                } else {
                    undoEdit();
                }

                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_Y) {
                redoEdit();
                return true;
            }
        }

        return super.keyPressed(event);
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
        int toolbarHeight = WIDE_TOOLBAR_HEIGHT;

        footerY = panelY + panelHeight - STATUS_HEIGHT;
        bodyX = panelX + CONTENT_MARGIN;
        bodyY = panelY + toolbarHeight + CONTENT_MARGIN;
        bodyWidth = panelWidth - CONTENT_MARGIN * 2;
        bodyHeight = footerY - bodyY - CONTENT_MARGIN;

        inspectorWidth = Math.clamp(
            bodyWidth * 24 / 100,
            236,
            292
        );
        inspectorX = bodyX + bodyWidth - inspectorWidth;
        inspectorY = bodyY;
        inspectorHeight = bodyHeight;

        workflowX = bodyX;
        workflowWidth =
            bodyWidth
                - inspectorWidth
                - PANEL_GAP;

        actionLibraryX = workflowX;
        actionLibraryY = bodyY;
        actionLibraryWidth = workflowWidth;
        actionLibraryHeight = ACTION_TOOLBAR_HEIGHT;

        workflowHeaderY =
            actionLibraryY
                + actionLibraryHeight
                + PANEL_GAP;
        workflowHeaderHeight = WIDE_WORKFLOW_HEADER_HEIGHT;
        workflowY = workflowHeaderY + workflowHeaderHeight;
        workflowHeight = bodyY + bodyHeight - workflowY;

        modeBadgeX = panelX + 86;
        modeBadgeY = panelY + 11;
        modeBadgeWidth = 52;
        modeBadgeHeight = BUTTON_HEIGHT;

        int undoX =
            modeBadgeX
                + modeBadgeWidth
                + 8;

        int redoX =
            undoX
                + WIDE_HISTORY_WIDTH
                + BUTTON_GAP;

        nameFrameX =
            redoX
                + WIDE_HISTORY_WIDTH
                + 8;
        nameFrameY = panelY + 9;
        nameFrameWidth =
            panelX
                + panelWidth
                - 8
                - WIDE_SAVE_WIDTH
                - 8
                - nameFrameX;
        nameFrameHeight = 24;
    }

    private void initializeCompactLayout() {
        int toolbarHeight = COMPACT_TOOLBAR_HEIGHT;

        footerY = panelY + panelHeight - STATUS_HEIGHT;
        bodyX = panelX + CONTENT_MARGIN;
        bodyY =
            panelY
                + toolbarHeight
                + COMPACT_TAB_HEIGHT
                + CONTENT_MARGIN;
        bodyWidth = panelWidth - CONTENT_MARGIN * 2;
        bodyHeight = footerY - bodyY - CONTENT_MARGIN;

        inspectorX = bodyX;
        inspectorY = bodyY;
        inspectorWidth = bodyWidth;
        inspectorHeight = bodyHeight;

        workflowX = bodyX;
        workflowWidth = bodyWidth;
        workflowHeaderY = bodyY;
        workflowHeaderHeight = COMPACT_WORKFLOW_HEADER_HEIGHT;
        workflowY = workflowHeaderY + workflowHeaderHeight;
        workflowHeight = bodyY + bodyHeight - workflowY;

        actionLibraryX = bodyX;
        actionLibraryY = bodyY;
        actionLibraryWidth = bodyWidth;
        actionLibraryHeight = bodyHeight;

        modeBadgeX = panelX + 82;
        modeBadgeY = panelY + 8;
        modeBadgeWidth = 48;
        modeBadgeHeight = BUTTON_HEIGHT;

        nameFrameX = panelX + CONTENT_MARGIN;
        nameFrameY = panelY + 38;
        nameFrameWidth = panelWidth - CONTENT_MARGIN * 2;
        nameFrameHeight = 24;
    }

    private void createToolbarWidgets() {
        int backWidth = layoutMode == LayoutMode.WIDE
            ? WIDE_BACK_WIDTH
            : COMPACT_BACK_WIDTH;

        int saveWidth = layoutMode == LayoutMode.WIDE
            ? WIDE_SAVE_WIDTH
            : COMPACT_SAVE_WIDTH;

        int historyWidth =
            layoutMode == LayoutMode.WIDE
                ? WIDE_HISTORY_WIDTH
                : COMPACT_HISTORY_WIDTH;

        int undoX =
            modeBadgeX
                + modeBadgeWidth
                + (
                    layoutMode == LayoutMode.WIDE
                        ? 8
                        : BUTTON_GAP
                );

        int redoX =
            undoX
                + historyWidth
                + BUTTON_GAP;

        backButton = createButton(
            panelX + 8,
            modeBadgeY,
            backWidth,
            Component.literal("← Back"),
            this::cancelEditing,
            KarakuriButton.Style.SECONDARY
        );

        undoButton = createButton(
            undoX,
            modeBadgeY,
            historyWidth,
            Component.literal("Undo"),
            this::undoEdit,
            KarakuriButton.Style.GHOST
        );

        redoButton = createButton(
            redoX,
            modeBadgeY,
            historyWidth,
            Component.literal("Redo"),
            this::redoEdit,
            KarakuriButton.Style.GHOST
        );

        saveButton = createButton(
            panelX + panelWidth - saveWidth - 8,
            modeBadgeY,
            saveWidth,
            Component.literal("Save"),
            this::saveScenario,
            KarakuriButton.Style.SUCCESS
        );

        addRenderableWidget(backButton);
        addRenderableWidget(undoButton);
        addRenderableWidget(redoButton);
        addRenderableWidget(saveButton);
    }

    private void createNameField() {
        nameField = new EditBox(
            font,
            nameFrameX + 7,
            nameFrameY + (nameFrameHeight - 16) / 2 + 3,
            nameFrameWidth - 14,
            16,
            Component.literal("Scenario name")
        );

        nameField.setBordered(false);
        nameField.setTextColor(ScenarioEditorTheme.TEXT);
        nameField.setTextColorUneditable(ScenarioEditorTheme.TEXT_MUTED);
        nameField.setTextShadow(false);
        nameField.setMaxLength(64);
        nameField.setHint(Component.literal("Scenario name"));
        nameField.setValue(state.name());
        nameField.setResponder(value -> {
            state.updateName(value);
            updateButtons();
        });
        addRenderableWidget(nameField);
    }

    private void createCompactTabs() {
        int tabX = panelX + CONTENT_MARGIN;
        int tabY = bodyY - COMPACT_TAB_HEIGHT - CONTENT_MARGIN;
        int tabWidth =
            (
                panelWidth
                    - CONTENT_MARGIN * 2
                    - BUTTON_GAP * 2
            ) / 3;

        workflowTabButton = createButton(
            tabX,
            tabY,
            tabWidth,
            Component.literal("Workflow"),
            () -> setCompactTab(CompactTab.WORKFLOW),
            KarakuriButton.Style.GHOST
        );
        actionsTabButton = createButton(
            tabX + tabWidth + BUTTON_GAP,
            tabY,
            tabWidth,
            Component.literal("Add"),
            () -> setCompactTab(CompactTab.ACTIONS),
            KarakuriButton.Style.GHOST
        );
        inspectorTabButton = createButton(
            tabX + (tabWidth + BUTTON_GAP) * 2,
            tabY,
            tabWidth,
            Component.literal("Properties"),
            () -> setCompactTab(CompactTab.INSPECTOR),
            KarakuriButton.Style.GHOST
        );

        addRenderableWidget(workflowTabButton);
        addRenderableWidget(actionsTabButton);
        addRenderableWidget(inspectorTabButton);
    }

    private void createGroupNavigationWidgets() {
        int y = layoutMode == LayoutMode.WIDE
            ? workflowHeaderY + 4
            : workflowHeaderY + 27;
        int actionsWidth = 112;
        int parentWidth = 72;
        int actionsX = workflowX + workflowWidth - actionsWidth - 6;
        int parentX = actionsX - parentWidth - BUTTON_GAP;

        groupBackButton = createButton(
            parentX,
            y,
            parentWidth,
            Component.literal("Parent"),
            this::exitCurrentGroup,
            KarakuriButton.Style.GHOST
        );
        groupActionsButton = createButton(
            actionsX,
            y,
            actionsWidth,
            Component.literal("Group actions ▾"),
            this::toggleGroupActions,
            KarakuriButton.Style.GHOST
        );

        int menuY = y + BUTTON_HEIGHT + 2;

        openGroupButton = createButton(
            actionsX,
            menuY,
            actionsWidth,
            Component.literal("Open Group"),
            this::openSelectedGroup,
            KarakuriButton.Style.SECONDARY
        );
        moveOutButton = createButton(
            actionsX,
            menuY + BUTTON_HEIGHT,
            actionsWidth,
            Component.literal("Move Out"),
            this::moveSelectedOutOfGroup,
            KarakuriButton.Style.SECONDARY
        );
        includePreviousButton = createButton(
            actionsX,
            menuY + BUTTON_HEIGHT * 2,
            actionsWidth,
            Component.literal("Include Previous"),
            this::includePreviousStep,
            KarakuriButton.Style.SECONDARY
        );
        includeNextButton = createButton(
            actionsX,
            menuY + BUTTON_HEIGHT * 3,
            actionsWidth,
            Component.literal("Include Next"),
            this::includeNextStep,
            KarakuriButton.Style.SECONDARY
        );

        addRenderableWidget(groupBackButton);
        addRenderableWidget(groupActionsButton);
        addRenderableWidget(openGroupButton);
        addRenderableWidget(moveOutButton);
        addRenderableWidget(includePreviousButton);
        addRenderableWidget(includeNextButton);
    }

    private void toggleGroupActions() {
        groupActionsOpen = !groupActionsOpen;
        updateButtons();
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

    private void renderToolbar(GuiGraphics graphics) {
        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + (
                layoutMode == LayoutMode.WIDE
                    ? WIDE_TOOLBAR_HEIGHT
                    : COMPACT_TOOLBAR_HEIGHT
            ),
            ScenarioEditorTheme.TOOLBAR
        );

        graphics.fill(
            panelX,
            panelY,
            panelX + 3,
            panelY + (
                layoutMode == LayoutMode.WIDE
                    ? WIDE_TOOLBAR_HEIGHT
                    : COMPACT_TOOLBAR_HEIGHT
            ),
            ScenarioEditorTheme.ACCENT
        );

        renderModeBadge(graphics);

        graphics.fill(
            nameFrameX,
            nameFrameY,
            nameFrameX + nameFrameWidth,
            nameFrameY + nameFrameHeight,
            ScenarioEditorTheme.CANVAS
        );
        graphics.renderOutline(
            nameFrameX,
            nameFrameY,
            nameFrameWidth,
            nameFrameHeight,
            isScenarioNameValid()
                ? ScenarioEditorTheme.OUTLINE
                : ScenarioEditorTheme.ERROR
        );
    }

    private void renderModeBadge(GuiGraphics graphics) {
        graphics.fill(
            modeBadgeX,
            modeBadgeY,
            modeBadgeX + modeBadgeWidth,
            modeBadgeY + modeBadgeHeight,
            ScenarioEditorTheme.PANEL_ELEVATED
        );
        graphics.renderOutline(
            modeBadgeX,
            modeBadgeY,
            modeBadgeWidth,
            modeBadgeHeight,
            ScenarioEditorTheme.OUTLINE_STRONG
        );
        graphics.fill(
            modeBadgeX,
            modeBadgeY,
            modeBadgeX + 3,
            modeBadgeY + modeBadgeHeight,
            ScenarioEditorTheme.ACCENT
        );

        Component label = Component.literal(
            state.scenarioIndex() < 0
                ? "NEW"
                : "EDIT"
        );

        graphics.drawString(
            font,
            label,
            modeBadgeX
                + (modeBadgeWidth - font.width(label)) / 2
                + 1,
            modeBadgeY
                + (modeBadgeHeight - font.lineHeight) / 2
                + 1,
            ScenarioEditorTheme.TEXT_SECONDARY,
            false
        );
    }

    private void renderWorkflowHeader(GuiGraphics graphics) {
        graphics.fill(
            workflowX,
            workflowHeaderY,
            workflowX + workflowWidth,
            workflowHeaderY + workflowHeaderHeight,
            ScenarioEditorTheme.PANEL_ELEVATED
        );
        graphics.renderOutline(
            workflowX,
            workflowHeaderY,
            workflowWidth,
            workflowHeaderHeight,
            ScenarioEditorTheme.OUTLINE
        );

        graphics.drawString(
            font,
            Component.literal(state.workflowLabel()),
            workflowX + 8,
            workflowHeaderY + 10,
            ScenarioEditorTheme.TEXT_SECONDARY,
            false
        );
    }

    private void renderStatusBar(GuiGraphics graphics) {
        graphics.fill(
            panelX,
            footerY,
            panelX + panelWidth,
            panelY + panelHeight,
            ScenarioEditorTheme.TOOLBAR
        );
        graphics.fill(
            panelX + CONTENT_MARGIN,
            footerY,
            panelX + panelWidth - CONTENT_MARGIN,
            footerY + 1,
            ScenarioEditorTheme.DIVIDER
        );

        String validationMessage = getValidationMessage();
        graphics.drawString(
            font,
            Component.literal(
                validationMessage == null
                    ? "Ready  ·  Stored as a .karakuri file"
                    : validationMessage
            ),
            panelX + CONTENT_MARGIN,
            footerY + 8,
            validationMessage == null
                ? ScenarioEditorTheme.TEXT_MUTED
                : ScenarioEditorTheme.ERROR,
            false
        );
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
        inspector.syncSelectedStep();
        updateButtons();
    }

    private void onCanvasEditStarted() {
        stopRunningTest();
        state.beginCanvasEdit();
    }

    private void onCanvasContentChanged() {
        stopRunningTest();
        state.select(
            workflowCanvas.getSelectedIndex()
        );
        state.commitCanvasChanges();
        inspector.syncSelectedStep();
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

    private void insertRepeatGroup() {
        stopRunningTest();
        state.wrapSelectedInRepeatGroup();
        openSelectedGroup();
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

    private void syncWorkflowLevel() {
        workflowCanvas.setSteps(
            state.steps()
        );

        syncSelectedStep();
    }

    private void syncSelectedStep() {
        workflowCanvas.setSelectedIndex(
            state.selectedIndex()
        );

        inspector.syncSelectedStep();
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

    private void openSelectedGroup() {
        groupActionsOpen = false;
        stopRunningTest();

        if (state.enterSelectedGroup()) {
            syncWorkflowLevel();
            returnToWorkflow();
        }
    }

    private void exitCurrentGroup() {
        groupActionsOpen = false;
        stopRunningTest();

        if (state.exitGroup()) {
            syncWorkflowLevel();
            returnToWorkflow();
        }
    }

    private void moveSelectedOutOfGroup() {
        groupActionsOpen = false;
        stopRunningTest();

        if (state.moveSelectedOutOfGroup()) {
            syncWorkflowLevel();
            returnToWorkflow();
        }
    }

    private void includePreviousStep() {
        groupActionsOpen = false;
        stopRunningTest();

        if (state.includePreviousStep()) {
            syncSelectedStep();
        }
    }

    private void includeNextStep() {
        groupActionsOpen = false;
        stopRunningTest();

        if (state.includeNextStep()) {
            syncSelectedStep();
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

    private void undoEdit() {
        stopRunningTest();

        if (state.undo()) {
            restoreEditorFromHistory();
        }
    }

    private void redoEdit() {
        stopRunningTest();

        if (state.redo()) {
            restoreEditorFromHistory();
        }
    }

    private void restoreEditorFromHistory() {
        groupActionsOpen = false;
        nameField.setValue(state.name());
        workflowCanvas.setSteps(state.steps());
        workflowCanvas.setSelectedIndex(
            state.selectedIndex()
        );
        inspector.syncSelectedStep();
        updateButtons();
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

        if (state.selectedGroupHasInfiniteStepBeforeEnd()) {
            updateButtons();
            return;
        }

        TaskManager.start(
            ScenarioTaskFactory.createStep(
                state.selectedStep()
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
            state.toScenario();

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
            actionLibrary == null
                || inspector == null
                || undoButton == null
                || redoButton == null
                || saveButton == null
                || groupBackButton == null
                || groupActionsButton == null
        ) {
            return;
        }

        boolean actionsVisible =
            layoutMode
                == LayoutMode.WIDE
                || compactTab
                    == CompactTab.ACTIONS;

        actionLibrary.setVisible(
            actionsVisible
        );

        inspector.setVisible(
            isInspectorVisible()
        );

        TaskStatus status =
            TaskManager.getStatus();

        boolean workflowVisible =
            isWorkflowVisible();

        boolean insideGroup =
            state.isInsideGroup();

        boolean repeatSelected =
            state.selectedStep()
                instanceof RepeatStep;

        boolean idle =
            status == TaskStatus.IDLE;

        boolean groupActionsAvailable =
            insideGroup || repeatSelected;

        if (!workflowVisible || !groupActionsAvailable) {
            groupActionsOpen = false;
        }

        groupBackButton.visible =
            workflowVisible && insideGroup;

        groupActionsButton.visible =
            workflowVisible && groupActionsAvailable;
        groupActionsButton.setStyle(
            groupActionsOpen
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.GHOST
        );

        openGroupButton.visible =
            groupActionsOpen && repeatSelected;
        moveOutButton.visible =
            groupActionsOpen && insideGroup;
        includePreviousButton.visible =
            groupActionsOpen && repeatSelected;
        includeNextButton.visible =
            groupActionsOpen && repeatSelected;

        groupBackButton.active = idle;
        groupActionsButton.active = idle;
        moveOutButton.active =
            idle
                && state.canMoveSelectedOutOfGroup();
        openGroupButton.active = idle;
        includePreviousButton.active =
            idle
                && state.canIncludePreviousStep();
        includeNextButton.active =
            idle
                && state.canIncludeNextStep();

        undoButton.active = state.canUndo();
        redoButton.active = state.canRedo();

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

    private String getValidationMessage() {
        if (!isScenarioNameValid()) {
            String name =
                nameField == null
                    ? state.name()
                    : nameField
                        .getValue()
                        .trim();

            return name.isBlank()
                ? "Scenario name is required"
                : "A scenario with this name already exists";
        }

        String inspectorMessage =
            inspector == null
                ? null
                : inspector.validationMessage();

        if (inspectorMessage != null) {
            return inspectorMessage;
        }

        if (state.hasInfiniteStepBeforeEnd()) {
            return "An infinite step must be final in every group";
        }

        return null;
    }

    private boolean isScenarioNameValid() {
        String name =
            nameField == null
                ? state.name()
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