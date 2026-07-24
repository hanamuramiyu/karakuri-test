package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.DepositItemsStep;
import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MoveDirection;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.storage.StorageRegistry;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.task.factory.ScenarioTaskFactory;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorClipboard;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorPreferences;
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
    private static final int COMPACT_BACK_WIDTH = 54;
    private static final int WIDE_SAVE_WIDTH = 86;
    private static final int COMPACT_SAVE_WIDTH = 54;
    private static final int WIDE_SAVE_AS_WIDTH = 68;
    private static final int UNSAVED_LABEL_WIDTH = 66;
    private static final int WIDE_HISTORY_WIDTH = 50;
    private static final int COMPACT_HISTORY_WIDTH = 36;
    private static final int WIDE_CLIPBOARD_WIDTH = 48;
    private static final int COMPACT_CLIPBOARD_WIDTH = 40;
    private static final int COMPACT_CLIPBOARD_MENU_WIDTH = 72;
    private static final int WIDE_WORKFLOW_HEADER_HEIGHT = 28;
    private static final int COMPACT_WORKFLOW_HEADER_HEIGHT = 50;

    private final KarakuriScreen parent;
    private final ScenarioEditorState state;

    private LayoutMode layoutMode;
    private CompactTab compactTab;

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
    private KarakuriButton copyButton;
    private KarakuriButton cutButton;
    private KarakuriButton pasteButton;
    private KarakuriButton clipboardMenuButton;
    private KarakuriButton saveAsButton;
    private KarakuriButton saveButton;
    private KarakuriButton shortcutsButton;
    private KarakuriButton groupBackButton;
    private KarakuriButton groupActionsButton;
    private KarakuriButton moveOutButton;
    private KarakuriButton openGroupButton;
    private KarakuriButton includePreviousButton;
    private KarakuriButton includeNextButton;

    private boolean groupActionsOpen;
    private boolean clipboardMenuOpen;

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
        this.compactTab = CompactTab.fromId(
            ScenarioEditorPreferences.compactTab()
        );
    }

    @Override
    protected void init() {
        clipboardMenuOpen = false;
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

        createNameField();
        createToolbarWidgets();

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
                this::insertHotbarStep,
                this::insertInventorySlotStep,
                this::insertDepositItemsStep
            ),
            ScenarioEditorPreferences.actionCategory(),
            ScenarioEditorPreferences::setActionCategory
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
            this::openSelectedInventorySlot,
            this::openSelectedDepositItems,
            this::resetSelectedStep,
            () -> moveSelectedStep(-1),
            () -> moveSelectedStep(1),
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
        if (handleClipboardMenuClick(event, doubled)) {
            return true;
        }

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

        if (handled && doubled) {
            if (state.selectedStep() instanceof RepeatStep) {
                openSelectedGroup();
            } else if (
                state.selectedStep()
                    instanceof InventorySlotStep
            ) {
                openSelectedInventorySlot();
            } else if (
                state.selectedStep()
                    instanceof DepositItemsStep
            ) {
                openSelectedDepositItems();
            }
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
        if (
            event.key() == GLFW.GLFW_KEY_ESCAPE
                && closeTransientUi()
        ) {
            return true;
        }

        if (
            event.hasControlDownWithQuirk()
                && event.key() == GLFW.GLFW_KEY_S
        ) {
            if (event.hasShiftDown()) {
                openSaveAs();
            } else {
                saveScenario();
            }

            return true;
        }

        if (hasFocusedTextField()) {
            return super.keyPressed(event);
        }

        if (event.hasControlDownWithQuirk()) {
            if (event.key() == GLFW.GLFW_KEY_D) {
                duplicateSelectedStep();
                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_C) {
                copySelectedStep();
                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_X) {
                cutSelectedStep();
                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_V) {
                pasteClipboardStep();
                return true;
            }

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

        if (minecraft.hasAltDown()) {
            if (event.key() == GLFW.GLFW_KEY_LEFT) {
                moveSelectedStep(-1);
                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_RIGHT) {
                moveSelectedStep(1);
                return true;
            }
        }

        if (event.key() == GLFW.GLFW_KEY_DELETE) {
            deleteSelectedStep();
            return true;
        }

        if (
            event.key() == GLFW.GLFW_KEY_ENTER
                || event.key() == GLFW.GLFW_KEY_KP_ENTER
        ) {
            if (
                state.selectedStep()
                    instanceof InventorySlotStep
            ) {
                openSelectedInventorySlot();
            } else if (
                state.selectedStep()
                    instanceof DepositItemsStep
            ) {
                openSelectedDepositItems();
            } else {
                openSelectedGroup();
            }
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_BACKSPACE) {
            exitCurrentGroup();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        requestClose();
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

        int copyX =
            redoX
                + WIDE_HISTORY_WIDTH
                + 8;

        int cutX =
            copyX
                + WIDE_CLIPBOARD_WIDTH
                + BUTTON_GAP;

        int pasteX =
            cutX
                + WIDE_CLIPBOARD_WIDTH
                + BUTTON_GAP;

        nameFrameX =
            pasteX
                + WIDE_CLIPBOARD_WIDTH
                + 8;
        nameFrameY = panelY + 9;
        int saveX =
            panelX
                + panelWidth
                - WIDE_SAVE_WIDTH
                - 8;

        int saveAsX =
            saveX
                - WIDE_SAVE_AS_WIDTH
                - BUTTON_GAP;

        nameFrameWidth =
            saveAsX
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

        modeBadgeX = panelX + 66;
        modeBadgeY = panelY + 8;
        modeBadgeWidth = 38;
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

        int clipboardX =
            redoX
                + historyWidth
                + (
                    layoutMode == LayoutMode.WIDE
                        ? 8
                        : BUTTON_GAP
                );

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

        if (layoutMode == LayoutMode.WIDE) {
            copyButton = createButton(
                clipboardX,
                modeBadgeY,
                WIDE_CLIPBOARD_WIDTH,
                Component.literal("Copy"),
                this::copySelectedStep,
                KarakuriButton.Style.GHOST
            );

            cutButton = createButton(
                clipboardX
                    + WIDE_CLIPBOARD_WIDTH
                    + BUTTON_GAP,
                modeBadgeY,
                WIDE_CLIPBOARD_WIDTH,
                Component.literal("Cut"),
                this::cutSelectedStep,
                KarakuriButton.Style.GHOST
            );

            pasteButton = createButton(
                clipboardX
                    + (
                        WIDE_CLIPBOARD_WIDTH
                            + BUTTON_GAP
                    ) * 2,
                modeBadgeY,
                WIDE_CLIPBOARD_WIDTH,
                Component.literal("Paste"),
                this::pasteClipboardStep,
                KarakuriButton.Style.GHOST
            );
        } else {
            clipboardMenuButton = createButton(
                clipboardX,
                modeBadgeY,
                COMPACT_CLIPBOARD_WIDTH,
                Component.literal("Edit"),
                this::toggleClipboardMenu,
                KarakuriButton.Style.GHOST
            );

            int menuX =
                clipboardX
                    + COMPACT_CLIPBOARD_WIDTH
                    - COMPACT_CLIPBOARD_MENU_WIDTH;

            int menuY =
                modeBadgeY
                    + BUTTON_HEIGHT
                    + 2;

            copyButton = createButton(
                menuX,
                menuY,
                COMPACT_CLIPBOARD_MENU_WIDTH,
                Component.literal("Copy"),
                this::copySelectedStep,
                KarakuriButton.Style.SECONDARY
            );

            cutButton = createButton(
                menuX,
                menuY + BUTTON_HEIGHT,
                COMPACT_CLIPBOARD_MENU_WIDTH,
                Component.literal("Cut"),
                this::cutSelectedStep,
                KarakuriButton.Style.SECONDARY
            );

            pasteButton = createButton(
                menuX,
                menuY + BUTTON_HEIGHT * 2,
                COMPACT_CLIPBOARD_MENU_WIDTH,
                Component.literal("Paste"),
                this::pasteClipboardStep,
                KarakuriButton.Style.SECONDARY
            );
        }

        int saveX =
            panelX + panelWidth - saveWidth - 8;

        if (layoutMode == LayoutMode.WIDE) {
            saveAsButton = createButton(
                saveX
                    - WIDE_SAVE_AS_WIDTH
                    - BUTTON_GAP,
                modeBadgeY,
                WIDE_SAVE_AS_WIDTH,
                Component.literal("Save As"),
                this::openSaveAs,
                KarakuriButton.Style.SECONDARY
            );
        } else {
            int menuX =
                clipboardX
                    + COMPACT_CLIPBOARD_WIDTH
                    - COMPACT_CLIPBOARD_MENU_WIDTH;

            int menuY =
                modeBadgeY
                    + BUTTON_HEIGHT
                    + 2;

            saveAsButton = createButton(
                menuX,
                menuY + BUTTON_HEIGHT * 3,
                COMPACT_CLIPBOARD_MENU_WIDTH,
                Component.literal("Save As"),
                this::openSaveAs,
                KarakuriButton.Style.SECONDARY
            );
        }

        saveButton = createButton(
            saveX,
            modeBadgeY,
            saveWidth,
            Component.literal("Save"),
            this::saveScenario,
            KarakuriButton.Style.SUCCESS
        );

        int shortcutsWidth =
            layoutMode == LayoutMode.WIDE
                ? 76
                : 22;

        shortcutsButton = createButton(
            panelX + panelWidth - shortcutsWidth - 6,
            footerY + 1,
            shortcutsWidth,
            Component.literal(
                layoutMode == LayoutMode.WIDE
                    ? "Shortcuts"
                    : "?"
            ),
            this::openShortcuts,
            KarakuriButton.Style.GHOST
        );

        addRenderableWidget(backButton);
        addRenderableWidget(undoButton);
        addRenderableWidget(redoButton);

        if (clipboardMenuButton != null) {
            addRenderableWidget(clipboardMenuButton);
        }

        addRenderableWidget(copyButton);
        addRenderableWidget(cutButton);
        addRenderableWidget(pasteButton);
        addRenderableWidget(saveAsButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(shortcutsButton);
    }

    private void createNameField() {
        nameField = new EditBox(
            font,
            nameFrameX + 7,
            nameFrameY + (nameFrameHeight - 16) / 2 + 3,
            Math.max(
                20,
                nameFrameWidth
                    - 14
                    - UNSAVED_LABEL_WIDTH
            ),
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

        if (state.isUnsaved()) {
            Component unsaved =
                Component.literal("• Unsaved");

            graphics.drawString(
                font,
                unsaved,
                nameFrameX
                    + nameFrameWidth
                    - font.width(unsaved)
                    - 7,
                nameFrameY
                    + (
                        nameFrameHeight
                            - font.lineHeight
                    ) / 2
                    + 1,
                ScenarioEditorTheme.WARNING,
                false
            );
        }
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
        String statusMessage = validationMessage == null
            ? state.isUnsaved()
                ? layoutMode == LayoutMode.WIDE
                    ? "Unsaved changes  ·  Ctrl+S to save"
                    : "Unsaved  ·  Ctrl+S"
                : layoutMode == LayoutMode.WIDE
                    ? "Ready  ·  Stored as a .karakuri file"
                    : "Ready"
            : validationMessage;

        graphics.drawString(
            font,
            Component.literal(statusMessage),
            panelX + CONTENT_MARGIN,
            footerY + 8,
            validationMessage == null
                ? (
                    state.isUnsaved()
                        ? ScenarioEditorTheme.WARNING
                        : ScenarioEditorTheme.TEXT_MUTED
                )
                : ScenarioEditorTheme.ERROR,
            false
        );
    }

    private void setCompactTab(
        CompactTab updatedTab
    ) {
        clipboardMenuOpen = false;
        compactTab = updatedTab;
        ScenarioEditorPreferences.setCompactTab(
            updatedTab.id
        );

        if (updatedTab == CompactTab.WORKFLOW) {
            workflowCanvas.ensureSelectedVisible();
        }

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

    private void insertDepositItemsStep() {
        stopRunningTest();

        String defaultGroupId =
            StorageRegistry.groups().isEmpty()
                ? DepositItemsStep.UNASSIGNED_GROUP_ID
                : StorageRegistry.groups()
                    .getFirst()
                    .id();

        state.insertDepositItemsStep(
            defaultGroupId
        );
        syncSelectedStep();
        returnToWorkflow();
        openSelectedDepositItems();
    }

    private void insertHotbarStep() {
        stopRunningTest();
        state.insertHotbarStep();
        syncSelectedStep();
        returnToWorkflow();
    }

    private void insertInventorySlotStep() {
        stopRunningTest();
        state.insertInventorySlotStep();
        syncSelectedStep();
        returnToWorkflow();
        openSelectedInventorySlot();
    }

    private void openSelectedDepositItems() {
        closeTransientUi();

        if (
            !(state.selectedStep()
                instanceof DepositItemsStep step)
        ) {
            return;
        }

        minecraft.setScreen(
            new DepositItemsSelectionScreen(
                this,
                step.storageGroupId(),
                step.includeHotbar(),
                state::setDepositItemsSelection
            )
        );
    }

    private void openSelectedInventorySlot() {
        closeTransientUi();

        if (
            !(state.selectedStep()
                instanceof InventorySlotStep step)
        ) {
            return;
        }

        minecraft.setScreen(
            new InventorySlotSelectionScreen(
                this,
                step.inventorySlot(),
                step.hotbarSlot(),
                state::setInventorySlotSelection
            )
        );
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
        workflowCanvas.ensureSelectedVisible();

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
        closeTransientUi();
        stopRunningTest();
        state.duplicateSelectedStep();
        syncSelectedStep();
    }

    private void resetSelectedStep() {
        closeTransientUi();
        stopRunningTest();

        if (state.resetSelectedStep()) {
            syncSelectedStep();
        } else {
            updateButtons();
        }
    }

    private void moveSelectedStep(
        int direction
    ) {
        closeTransientUi();
        stopRunningTest();

        if (state.moveSelectedStep(direction)) {
            syncSelectedStep();
        } else {
            updateButtons();
        }
    }

    private void copySelectedStep() {
        clipboardMenuOpen = false;
        ScenarioEditorClipboard.copy(
            state.selectedStep()
        );
        updateButtons();
    }

    private void cutSelectedStep() {
        clipboardMenuOpen = false;

        if (!state.canRemoveSelectedStep()) {
            updateButtons();
            return;
        }

        stopRunningTest();
        ScenarioEditorClipboard.copy(
            state.selectedStep()
        );

        if (state.deleteSelectedStep()) {
            syncSelectedStep();
        }
    }

    private void pasteClipboardStep() {
        clipboardMenuOpen = false;

        ScenarioStep step =
            ScenarioEditorClipboard.createPaste();

        if (step == null) {
            updateButtons();
            return;
        }

        stopRunningTest();
        state.pasteStep(step);
        syncSelectedStep();
        returnToWorkflow();
    }

    private void deleteSelectedStep() {
        stopRunningTest();

        if (state.deleteSelectedStep()) {
            syncSelectedStep();
        }
    }

    private void undoEdit() {
        clipboardMenuOpen = false;
        stopRunningTest();

        if (state.undo()) {
            restoreEditorFromHistory();
        }
    }

    private void redoEdit() {
        clipboardMenuOpen = false;
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
        saveAndCloseFromPrompt();
    }

    void saveAndCloseFromPrompt() {
        if (!canSaveFromPrompt()) {
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

        state.markSaved();

        parent.refreshScenarios(
            scenario.name()
        );

        minecraft.setScreen(parent);
    }

    boolean canSaveFromPrompt() {
        return TaskManager.getStatus()
                == TaskStatus.IDLE
            && getValidationMessage()
                == null;
    }

    void discardAndClose() {
        stopRunningTest();
        minecraft.setScreen(parent);
    }

    private void openSaveAs() {
        closeTransientUi();

        if (
            TaskManager.getStatus()
                != TaskStatus.IDLE
            || getStructuralValidationMessage()
                != null
        ) {
            updateButtons();
            return;
        }

        minecraft.setScreen(
            new SaveScenarioAsScreen(
                this,
                createSaveAsName(),
                this::saveScenarioAs
            )
        );
    }

    private void saveScenarioAs(
        String scenarioName
    ) {
        if (
            getStructuralValidationMessage()
                != null
            || scenarioName == null
            || scenarioName.isBlank()
            || ScenarioLibrary.containsName(
                scenarioName,
                -1
            )
        ) {
            return;
        }

        stopRunningTest();

        Scenario scenario =
            state.toScenario(
                scenarioName
            );

        ScenarioLibrary.add(scenario);

        parent.refreshScenarios(
            scenario.name()
        );

        minecraft.setScreen(parent);
    }

    private void openShortcuts() {
        closeTransientUi();
        minecraft.setScreen(
            new ScenarioShortcutsScreen(this)
        );
    }

    private void requestClose() {
        closeTransientUi();
        stopRunningTest();

        if (!state.isUnsaved()) {
            minecraft.setScreen(parent);
            return;
        }

        minecraft.setScreen(
            new UnsavedChangesScreen(
                this,
                state.name().isBlank()
                    ? "Untitled Scenario"
                    : state.name().trim()
            )
        );
    }

    private void cancelEditing() {
        requestClose();
    }

    private void updateButtons() {
        if (
            actionLibrary == null
                || inspector == null
                || undoButton == null
                || redoButton == null
                || copyButton == null
                || cutButton == null
                || pasteButton == null
                || saveAsButton == null
                || saveButton == null
                || shortcutsButton == null
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

        boolean compactClipboard =
            layoutMode == LayoutMode.COMPACT;

        if (clipboardMenuButton != null) {
            clipboardMenuButton.visible = true;
            clipboardMenuButton.setStyle(
                clipboardMenuOpen
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.GHOST
            );
        }

        copyButton.visible =
            !compactClipboard || clipboardMenuOpen;
        cutButton.visible =
            !compactClipboard || clipboardMenuOpen;
        pasteButton.visible =
            !compactClipboard || clipboardMenuOpen;
        saveAsButton.visible =
            !compactClipboard || clipboardMenuOpen;

        copyButton.active = true;
        cutButton.active =
            idle && state.canRemoveSelectedStep();
        pasteButton.active =
            idle && ScenarioEditorClipboard.hasContents();
        saveAsButton.active =
            idle
                && getStructuralValidationMessage()
                    == null;

        saveButton.active =
            canSaveFromPrompt()
                && state.isUnsaved();

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

    private void toggleClipboardMenu() {
        clipboardMenuOpen = !clipboardMenuOpen;
        groupActionsOpen = false;
        updateButtons();
    }

    private boolean handleClipboardMenuClick(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (
            layoutMode != LayoutMode.COMPACT
                || !clipboardMenuOpen
        ) {
            return false;
        }

        if (copyButton.isMouseOver(event.x(), event.y())) {
            copyButton.onClick(event, doubled);
            return true;
        }

        if (cutButton.isMouseOver(event.x(), event.y())) {
            cutButton.onClick(event, doubled);
            return true;
        }

        if (pasteButton.isMouseOver(event.x(), event.y())) {
            pasteButton.onClick(event, doubled);
            return true;
        }

        if (saveAsButton.isMouseOver(event.x(), event.y())) {
            saveAsButton.onClick(event, doubled);
            return true;
        }

        if (
            clipboardMenuButton != null
                && clipboardMenuButton.isMouseOver(
                    event.x(),
                    event.y()
                )
        ) {
            return false;
        }

        clipboardMenuOpen = false;
        updateButtons();
        return false;
    }

    private boolean closeTransientUi() {
        boolean closed = false;

        if (inspector != null) {
            closed = inspector.closeDropdowns();
        }

        if (actionLibrary != null) {
            closed = actionLibrary.closeDrawerIfOpen()
                || closed;
        }

        if (groupActionsOpen) {
            groupActionsOpen = false;
            closed = true;
        }

        if (clipboardMenuOpen) {
            clipboardMenuOpen = false;
            closed = true;
        }

        if (closed) {
            updateButtons();
        }

        return closed;
    }

    private boolean hasFocusedTextField() {
        return nameField != null
            && nameField.isFocused()
            || inspector != null
                && inspector.hasFocusedTextField();
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

        return getStructuralValidationMessage();
    }

    private String getStructuralValidationMessage() {
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

    private String createSaveAsName() {
        String sourceName =
            state.name().trim();

        if (sourceName.isBlank()) {
            sourceName = "New Scenario";
        }

        int copyNumber = 1;

        while (true) {
            String suffix = copyNumber == 1
                ? " Copy"
                : " Copy " + copyNumber;

            int maximumBaseLength =
                64 - suffix.length();

            String baseName =
                sourceName.length()
                    <= maximumBaseLength
                    ? sourceName
                    : sourceName
                        .substring(
                            0,
                            maximumBaseLength
                        )
                        .stripTrailing();

            String candidate =
                baseName + suffix;

            if (
                !ScenarioLibrary.containsName(
                    candidate,
                    -1
                )
            ) {
                return candidate;
            }

            copyNumber++;
        }
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
        WORKFLOW("workflow"),
        ACTIONS("actions"),
        INSPECTOR("inspector");

        private final String id;

        CompactTab(
            String id
        ) {
            this.id = id;
        }

        private static CompactTab fromId(
            String id
        ) {
            for (CompactTab tab : values()) {
                if (tab.id.equals(id)) {
                    return tab;
                }
            }

            return WORKFLOW;
        }
    }
}