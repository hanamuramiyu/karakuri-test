package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MoveDirection;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.task.factory.ScenarioTaskFactory;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.ui.editor.ScenarioEditorState;
import hanamuramiyu.karakuri.ui.editor.inspector.ScenarioInspector;
import hanamuramiyu.karakuri.ui.editor.inspector.ScenarioInspectorLayout;
import hanamuramiyu.karakuri.ui.editor.workflow.ScenarioWorkflowCanvas;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
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

    private ScenarioActionLibrary actionLibrary;
    private ScenarioWorkflowCanvas workflowCanvas;
    private ScenarioInspector inspector;

    private EditBox nameField;

    private KarakuriButton workflowTabButton;
    private KarakuriButton actionsTabButton;
    private KarakuriButton inspectorTabButton;

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

        inspector =
            new ScenarioInspector(
                font,
                state,
                new ScenarioInspectorLayout(
                    layoutMode
                        == LayoutMode.WIDE
                            ? ScenarioInspectorLayout
                                .Mode.WIDE
                            : ScenarioInspectorLayout
                                .Mode.COMPACT,
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

        for (
            AbstractWidget widget :
            inspector.widgets()
        ) {
            addRenderableWidget(widget);
        }

        createFooterWidgets();

        if (
            layoutMode
                == LayoutMode.COMPACT
        ) {
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

        inspector.render(graphics);

        renderFooter(graphics);

        super.render(
            graphics,
            mouseX,
            mouseY,
            delta
        );

        inspector.renderCenteredValues(
            graphics
        );
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
        inspector.syncSelectedStep();
        updateButtons();
    }

    private void onCanvasContentChanged() {
        stopRunningTest();
        state.select(
            workflowCanvas.getSelectedIndex()
        );
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
            actionLibrary == null
                || inspector == null
                || saveButton == null
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
                    ? state.initialName()
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