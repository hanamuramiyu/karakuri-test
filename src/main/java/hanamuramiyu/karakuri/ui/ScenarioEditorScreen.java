package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.Scenario;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
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
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS = 72000;
    private static final int DEFAULT_MOVE_DURATION_TICKS = 40;
    private static final int DEFAULT_WAIT_DURATION_TICKS = 20;

    private final KarakuriScreen parent;
    private final int scenarioIndex;
    private final List<Scenario.Step> steps;
    private final String initialName;

    private LayoutMode layoutMode;
    private CompactTab compactTab = CompactTab.WORKFLOW;

    private int selectedStepIndex;
    private boolean syncingDurationField;
    private boolean durationFieldValid = true;

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

    private int durationFrameX;
    private int durationFrameY;
    private int durationFrameWidth;

    private ScenarioActionLibrary actionLibrary;
    private ScenarioWorkflowCanvas workflowCanvas;

    private EditBox nameField;
    private EditBox durationField;

    private KarakuriButton workflowTabButton;
    private KarakuriButton actionsTabButton;
    private KarakuriButton inspectorTabButton;

    private KarakuriButton forwardDirectionButton;
    private KarakuriButton backwardDirectionButton;
    private KarakuriButton leftDirectionButton;
    private KarakuriButton rightDirectionButton;

    private KarakuriButton durationDecreaseButton;
    private KarakuriButton durationIncreaseButton;
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
            steps = new ArrayList<>(scenario.steps());
        }
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

        createNameField();

        actionLibrary = new ScenarioActionLibrary(
            font,
            bodyX,
            bodyY,
            layoutMode == LayoutMode.WIDE
                ? getWideLibraryWidth()
                : bodyWidth,
            bodyHeight,
            layoutMode == LayoutMode.WIDE
                ? ScenarioActionLibrary.Layout.SIDEBAR
                : ScenarioActionLibrary.Layout.HORIZONTAL,
            this::insertMoveStep,
            this::insertWaitStep
        );

        for (KarakuriButton widget : actionLibrary.widgets()) {
            addRenderableWidget(widget);
        }

        int canvasX;
        int canvasWidth;

        if (layoutMode == LayoutMode.WIDE) {
            int libraryWidth = getWideLibraryWidth();

            canvasX = bodyX
                + libraryWidth
                + PANEL_GAP;

            canvasWidth = bodyWidth
                - libraryWidth
                - inspectorWidth
                - PANEL_GAP * 2;
        } else {
            canvasX = bodyX;
            canvasWidth = bodyWidth;
        }

        workflowCanvas = new ScenarioWorkflowCanvas(
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

        if (layoutMode == LayoutMode.COMPACT) {
            createCompactTabs();
        }

        syncDurationField();
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
                    layoutMode == LayoutMode.WIDE
                        ? "Drag blocks to reorder  |  Scroll block to adjust duration"
                        : "Drag to reorder  |  Scroll to adjust duration"
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
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (super.mouseClicked(event, doubled)) {
            return true;
        }

        return isWorkflowVisible()
            && workflowCanvas.mouseClicked(event);
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
                && workflowCanvas.mouseReleased();

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
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void initializeWideLayout() {
        int headerHeight = 52;
        int footerHeight = 40;

        bodyX = panelX + CONTENT_MARGIN;
        bodyY = panelY + headerHeight;
        bodyWidth = panelWidth - CONTENT_MARGIN * 2;

        footerY = panelY + panelHeight - footerHeight;

        bodyHeight = footerY
            - bodyY
            - PANEL_GAP;

        inspectorWidth = Math.clamp(
            bodyWidth * 24 / 100,
            205,
            270
        );

        inspectorX = bodyX
            + bodyWidth
            - inspectorWidth;

        inspectorY = bodyY;
        inspectorHeight = bodyHeight;
    }

    private void initializeCompactLayout() {
        int headerHeight = panelHeight < 240
            ? 36
            : 42;

        int tabHeight = 22;
        int footerHeight = 30;

        int tabY = panelY + headerHeight;

        bodyX = panelX + 6;
        bodyY = tabY + tabHeight + 4;
        bodyWidth = panelWidth - 12;

        footerY = panelY
            + panelHeight
            - footerHeight;

        bodyHeight = footerY
            - bodyY
            - 4;

        inspectorX = bodyX;
        inspectorY = bodyY;
        inspectorWidth = bodyWidth;
        inspectorHeight = bodyHeight;
    }

    private void createNameField() {
        int titleWidth = layoutMode == LayoutMode.WIDE
            ? 132
            : 104;

        nameFrameX = panelX + titleWidth;
        nameFrameY = panelY + (
            layoutMode == LayoutMode.WIDE
                ? 13
                : 8
        );

        nameFrameWidth = panelWidth
            - titleWidth
            - CONTENT_MARGIN;

        nameFrameHeight = layoutMode == LayoutMode.WIDE
            ? 24
            : 22;

        nameField = new EditBox(
            font,
            nameFrameX + 7,
            nameFrameY + 3,
            nameFrameWidth - 14,
            16,
            Component.literal("Scenario name")
        );

        nameField.setBordered(false);
        nameField.setTextColor(0xFFF4F0F7);
        nameField.setTextColorUneditable(0xFF81798E);
        nameField.setTextShadow(false);
        nameField.setMaxLength(64);
        nameField.setHint(
            Component.literal("Scenario name")
        );
        nameField.setValue(initialName);
        nameField.setResponder(
            value -> updateButtons()
        );

        addRenderableWidget(nameField);
    }

    private void createCompactTabs() {
        int tabX = panelX + 6;
        int tabY = bodyY - 26;
        int tabWidth = (
            panelWidth - 12 - BUTTON_GAP * 2
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
            tabX + tabWidth + BUTTON_GAP,
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
                + (tabWidth + BUTTON_GAP) * 2,
            tabY,
            tabWidth,
            Component.literal("Inspector"),
            () -> setCompactTab(
                CompactTab.INSPECTOR
            ),
            KarakuriButton.Style.GHOST
        );

        addRenderableWidget(workflowTabButton);
        addRenderableWidget(actionsTabButton);
        addRenderableWidget(inspectorTabButton);
    }

    private void createInspectorWidgets() {
        if (layoutMode == LayoutMode.WIDE) {
            createWideInspectorWidgets();
        } else {
            createCompactInspectorWidgets();
        }

        addRenderableWidget(forwardDirectionButton);
        addRenderableWidget(backwardDirectionButton);
        addRenderableWidget(leftDirectionButton);
        addRenderableWidget(rightDirectionButton);
        addRenderableWidget(durationDecreaseButton);
        addRenderableWidget(durationField);
        addRenderableWidget(durationIncreaseButton);
        addRenderableWidget(duplicateButton);
        addRenderableWidget(deleteButton);
    }

    private void createWideInspectorWidgets() {
        int contentX = inspectorX + 10;
        int contentWidth = inspectorWidth - 20;

        int halfWidth = (
            contentWidth - BUTTON_GAP
        ) / 2;

        int directionY = inspectorY + 72;

        forwardDirectionButton = createButton(
            contentX,
            directionY,
            halfWidth,
            Component.literal("Forward"),
            () -> setDirection(
                Scenario.MoveDirection.FORWARD
            ),
            KarakuriButton.Style.GHOST
        );

        backwardDirectionButton = createButton(
            contentX + halfWidth + BUTTON_GAP,
            directionY,
            halfWidth,
            Component.literal("Backward"),
            () -> setDirection(
                Scenario.MoveDirection.BACKWARD
            ),
            KarakuriButton.Style.GHOST
        );

        leftDirectionButton = createButton(
            contentX,
            directionY + 28,
            halfWidth,
            Component.literal("Left"),
            () -> setDirection(
                Scenario.MoveDirection.LEFT
            ),
            KarakuriButton.Style.GHOST
        );

        rightDirectionButton = createButton(
            contentX + halfWidth + BUTTON_GAP,
            directionY + 28,
            halfWidth,
            Component.literal("Right"),
            () -> setDirection(
                Scenario.MoveDirection.RIGHT
            ),
            KarakuriButton.Style.GHOST
        );

        durationFrameX = contentX + 40;
        durationFrameY = inspectorY + 158;
        durationFrameWidth = contentWidth - 80;

        durationDecreaseButton = createButton(
            contentX,
            durationFrameY,
            34,
            Component.literal("-"),
            () -> changeDuration(
                -DURATION_STEP_TICKS
            ),
            KarakuriButton.Style.GHOST
        );

        createDurationField();

        durationIncreaseButton = createButton(
            contentX + contentWidth - 34,
            durationFrameY,
            34,
            Component.literal("+"),
            () -> changeDuration(
                DURATION_STEP_TICKS
            ),
            KarakuriButton.Style.GHOST
        );

        int actionY = inspectorY + 208;

        duplicateButton = createButton(
            contentX,
            actionY,
            halfWidth,
            Component.literal("Duplicate"),
            this::duplicateSelectedStep,
            KarakuriButton.Style.SECONDARY
        );

        deleteButton = createButton(
            contentX + halfWidth + BUTTON_GAP,
            actionY,
            halfWidth,
            Component.literal("Delete"),
            this::deleteSelectedStep,
            KarakuriButton.Style.DANGER
        );
    }

    private void createCompactInspectorWidgets() {
        int padding = 8;
        int columnGap = 10;

        int contentWidth = inspectorWidth
            - padding * 2;

        int columnWidth = (
            contentWidth - columnGap
        ) / 2;

        int leftX = inspectorX + padding;
        int rightX = leftX
            + columnWidth
            + columnGap;

        int directionButtonWidth = (
            columnWidth - BUTTON_GAP
        ) / 2;

        int firstRowY = inspectorY + 42;
        int secondRowY = firstRowY + 26;

        forwardDirectionButton = createButton(
            leftX,
            firstRowY,
            directionButtonWidth,
            Component.literal("Forward"),
            () -> setDirection(
                Scenario.MoveDirection.FORWARD
            ),
            KarakuriButton.Style.GHOST
        );

        backwardDirectionButton = createButton(
            leftX
                + directionButtonWidth
                + BUTTON_GAP,
            firstRowY,
            directionButtonWidth,
            Component.literal("Backward"),
            () -> setDirection(
                Scenario.MoveDirection.BACKWARD
            ),
            KarakuriButton.Style.GHOST
        );

        leftDirectionButton = createButton(
            leftX,
            secondRowY,
            directionButtonWidth,
            Component.literal("Left"),
            () -> setDirection(
                Scenario.MoveDirection.LEFT
            ),
            KarakuriButton.Style.GHOST
        );

        rightDirectionButton = createButton(
            leftX
                + directionButtonWidth
                + BUTTON_GAP,
            secondRowY,
            directionButtonWidth,
            Component.literal("Right"),
            () -> setDirection(
                Scenario.MoveDirection.RIGHT
            ),
            KarakuriButton.Style.GHOST
        );

        durationFrameX = rightX + 34;
        durationFrameY = firstRowY;
        durationFrameWidth = columnWidth - 68;

        durationDecreaseButton = createButton(
            rightX,
            durationFrameY,
            28,
            Component.literal("-"),
            () -> changeDuration(
                -DURATION_STEP_TICKS
            ),
            KarakuriButton.Style.GHOST
        );

        createDurationField();

        durationIncreaseButton = createButton(
            rightX + columnWidth - 28,
            durationFrameY,
            28,
            Component.literal("+"),
            () -> changeDuration(
                DURATION_STEP_TICKS
            ),
            KarakuriButton.Style.GHOST
        );

        int actionButtonWidth = (
            columnWidth - BUTTON_GAP
        ) / 2;

        duplicateButton = createButton(
            rightX,
            secondRowY,
            actionButtonWidth,
            Component.literal("Duplicate"),
            this::duplicateSelectedStep,
            KarakuriButton.Style.SECONDARY
        );

        deleteButton = createButton(
            rightX
                + actionButtonWidth
                + BUTTON_GAP,
            secondRowY,
            actionButtonWidth,
            Component.literal("Delete"),
            this::deleteSelectedStep,
            KarakuriButton.Style.DANGER
        );
    }

    private void createDurationField() {
        durationField = new EditBox(
            font,
            durationFrameX + 6,
            durationFrameY + 3,
            durationFrameWidth - 12,
            16,
            Component.literal(
                "Duration in seconds"
            )
        );

        durationField.setBordered(false);
        durationField.setTextColor(0xFFF4F0F7);
        durationField.setTextColorUneditable(
            0xFF81798E
        );
        durationField.setTextShadow(false);
        durationField.setMaxLength(7);
        durationField.setHint(
            Component.literal("Seconds")
        );

        durationField.setFilter(
            value -> value.matches(
                "[0-9]{0,4}(\\.[0-9]{0,2})?"
            )
        );

        durationField.setResponder(
            this::onDurationFieldChanged
        );
    }

    private void createFooterWidgets() {
        int buttonWidth = layoutMode == LayoutMode.WIDE
            ? 126
            : Math.min(
                108,
                (panelWidth - 24 - BUTTON_GAP) / 2
            );

        int buttonY = footerY + 4;

        saveButton = createButton(
            panelX
                + panelWidth
                - CONTENT_MARGIN
                - buttonWidth * 2
                - BUTTON_GAP,
            buttonY,
            buttonWidth,
            Component.literal(
                layoutMode == LayoutMode.WIDE
                    ? "Save Scenario"
                    : "Save"
            ),
            this::saveScenario,
            KarakuriButton.Style.SUCCESS
        );

        KarakuriButton cancelButton = createButton(
            panelX
                + panelWidth
                - CONTENT_MARGIN
                - buttonWidth,
            buttonY,
            buttonWidth,
            Component.literal("Cancel"),
            () -> minecraft.setScreen(parent),
            KarakuriButton.Style.SECONDARY
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
            panelY + (
                layoutMode == LayoutMode.WIDE
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
        Scenario.Step step = getSelectedStep();

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
            Component.literal("Inspector"),
            inspectorX + 10,
            inspectorY + 9,
            0xFFF1ECF5,
            false
        );

        Component position = Component.literal(
            "#"
                + (selectedStepIndex + 1)
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
            inspectorY + 9,
            0xFF81778A,
            false
        );

        graphics.drawString(
            font,
            Component.literal(getStepTitle(step)),
            inspectorX + 10,
            inspectorY + 25,
            0xFFF4F0F7,
            false
        );

        if (layoutMode == LayoutMode.WIDE) {
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

        graphics.fill(
            durationFrameX,
            durationFrameY,
            durationFrameX + durationFrameWidth,
            durationFrameY + BUTTON_HEIGHT,
            0xFF100E16
        );

        graphics.renderOutline(
            durationFrameX,
            durationFrameY,
            durationFrameWidth,
            BUTTON_HEIGHT,
            durationFieldValid
                ? 0xFF51475E
                : 0xFFC75B69
        );
    }

    private void renderWideInspectorLabels(
        GuiGraphics graphics,
        Scenario.Step step
    ) {
        if (step instanceof Scenario.MoveStep) {
            graphics.drawString(
                font,
                Component.literal("Direction"),
                inspectorX + 10,
                inspectorY + 58,
                0xFF918699,
                false
            );
        } else {
            graphics.drawString(
                font,
                Component.literal("Timing action"),
                inspectorX + 10,
                inspectorY + 58,
                0xFF918699,
                false
            );

            graphics.drawString(
                font,
                Component.literal(
                    "No movement direction required"
                ),
                inspectorX + 10,
                inspectorY + 84,
                0xFF716A79,
                false
            );
        }

        graphics.drawString(
            font,
            Component.literal("Duration in seconds"),
            inspectorX + 10,
            inspectorY + 142,
            0xFF918699,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Step actions"),
            inspectorX + 10,
            inspectorY + 194,
            0xFF918699,
            false
        );

        if (inspectorHeight >= 270) {
            graphics.drawString(
                font,
                Component.literal(
                    "Select or drag blocks in the workflow."
                ),
                inspectorX + 10,
                inspectorY + inspectorHeight - 30,
                0xFF716A79,
                false
            );

            graphics.drawString(
                font,
                Component.literal(
                    "New actions appear after selection."
                ),
                inspectorX + 10,
                inspectorY + inspectorHeight - 18,
                0xFF716A79,
                false
            );
        }
    }

    private void renderCompactInspectorLabels(
        GuiGraphics graphics,
        Scenario.Step step
    ) {
        int padding = 8;
        int columnGap = 10;

        int contentWidth = inspectorWidth
            - padding * 2;

        int columnWidth = (
            contentWidth - columnGap
        ) / 2;

        int leftX = inspectorX + padding;
        int rightX = leftX
            + columnWidth
            + columnGap;

        graphics.drawString(
            font,
            Component.literal(
                step instanceof Scenario.MoveStep
                    ? "Direction"
                    : "Timing action"
            ),
            leftX,
            inspectorY + 32,
            0xFF918699,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Duration"),
            rightX,
            inspectorY + 32,
            0xFF918699,
            false
        );

        if (
            step instanceof Scenario.WaitStep
                && inspectorHeight >= 96
        ) {
            graphics.drawString(
                font,
                Component.literal(
                    "Wait does not use a direction"
                ),
                leftX,
                inspectorY + 50,
                0xFF716A79,
                false
            );
        }
    }

    private void renderFooter(
        GuiGraphics graphics
    ) {
        graphics.fill(
            panelX + CONTENT_MARGIN,
            footerY,
            panelX + panelWidth - CONTENT_MARGIN,
            footerY + 1,
            0xFF332D3A
        );

        int saveX = saveButton.getX();

        if (
            saveX
                - (panelX + CONTENT_MARGIN)
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
        CompactTab compactTab
    ) {
        this.compactTab = compactTab;
        updateButtons();
    }

    private void onCanvasSelectionChanged(
        int index
    ) {
        selectedStepIndex = index;
        syncDurationField();
        updateButtons();
    }

    private void onCanvasContentChanged() {
        selectedStepIndex =
            workflowCanvas.getSelectedIndex();

        syncDurationField();
        updateButtons();
    }

    private void insertMoveStep(
        Scenario.MoveDirection direction
    ) {
        int insertIndex = selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.MoveStep(
                direction,
                DEFAULT_MOVE_DURATION_TICKS
            )
        );

        selectStep(insertIndex);

        if (layoutMode == LayoutMode.COMPACT) {
            setCompactTab(CompactTab.WORKFLOW);
        }
    }

    private void insertWaitStep() {
        int insertIndex = selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.WaitStep(
                DEFAULT_WAIT_DURATION_TICKS
            )
        );

        selectStep(insertIndex);

        if (layoutMode == LayoutMode.COMPACT) {
            setCompactTab(CompactTab.WORKFLOW);
        }
    }

    private void duplicateSelectedStep() {
        int insertIndex = selectedStepIndex + 1;

        steps.add(
            insertIndex,
            getSelectedStep()
        );

        selectStep(insertIndex);
    }

    private void deleteSelectedStep() {
        if (steps.size() <= 1) {
            return;
        }

        steps.remove(selectedStepIndex);

        selectStep(
            Math.min(
                selectedStepIndex,
                steps.size() - 1
            )
        );
    }

    private void selectStep(int index) {
        selectedStepIndex = Math.clamp(
            index,
            0,
            steps.size() - 1
        );

        workflowCanvas.setSelectedIndex(
            selectedStepIndex
        );

        syncDurationField();
        updateButtons();
    }

    private void setDirection(
        Scenario.MoveDirection direction
    ) {
        Scenario.Step step = getSelectedStep();

        if (
            !(step instanceof Scenario.MoveStep moveStep)
        ) {
            return;
        }

        steps.set(
            selectedStepIndex,
            new Scenario.MoveStep(
                direction,
                moveStep.durationTicks()
            )
        );

        updateButtons();
    }

    private void changeDuration(
        int offsetTicks
    ) {
        Scenario.Step step = getSelectedStep();

        int durationTicks = Math.clamp(
            step.durationTicks() + offsetTicks,
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );

        replaceSelectedDuration(durationTicks);
        syncDurationField();
        updateButtons();
    }

    private void onDurationFieldChanged(
        String value
    ) {
        if (syncingDurationField) {
            return;
        }

        try {
            double seconds = Double.parseDouble(value);

            int durationTicks = (int) Math.round(
                seconds * 20.0
            );

            if (
                !Double.isFinite(seconds)
                    || durationTicks < MIN_DURATION_TICKS
                    || durationTicks > MAX_DURATION_TICKS
            ) {
                durationFieldValid = false;
                updateButtons();
                return;
            }

            replaceSelectedDuration(durationTicks);
            durationFieldValid = true;
        } catch (NumberFormatException exception) {
            durationFieldValid = false;
        }

        updateButtons();
    }

    private void replaceSelectedDuration(
        int durationTicks
    ) {
        Scenario.Step step = getSelectedStep();

        steps.set(
            selectedStepIndex,
            switch (step) {
                case Scenario.MoveStep moveStep ->
                    new Scenario.MoveStep(
                        moveStep.direction(),
                        durationTicks
                    );
                case Scenario.WaitStep waitStep ->
                    new Scenario.WaitStep(
                        durationTicks
                    );
            }
        );
    }

    private void syncDurationField() {
        if (durationField == null) {
            return;
        }

        syncingDurationField = true;

        durationField.setValue(
            formatDurationForField(
                getSelectedStep().durationTicks()
            )
        );

        syncingDurationField = false;
        durationFieldValid = true;
    }

    private void saveScenario() {
        if (getValidationMessage() != null) {
            return;
        }

        Scenario scenario = new Scenario(
            nameField.getValue(),
            steps
        );

        if (scenarioIndex < 0) {
            ScenarioLibrary.add(scenario);
        } else {
            ScenarioLibrary.replace(
                scenarioIndex,
                scenario
            );
        }

        parent.refreshScenarios(scenario.name());
        minecraft.setScreen(parent);
    }

    private void updateButtons() {
        if (
            forwardDirectionButton == null
                || backwardDirectionButton == null
                || leftDirectionButton == null
                || rightDirectionButton == null
                || durationDecreaseButton == null
                || durationIncreaseButton == null
                || duplicateButton == null
                || deleteButton == null
                || saveButton == null
        ) {
            return;
        }

        boolean actionsVisible =
            layoutMode == LayoutMode.WIDE
                || compactTab == CompactTab.ACTIONS;

        boolean inspectorVisible =
            isInspectorVisible();

        actionLibrary.setVisible(actionsVisible);

        Scenario.Step step = getSelectedStep();

        boolean movement =
            step instanceof Scenario.MoveStep;

        forwardDirectionButton.visible =
            inspectorVisible && movement;

        backwardDirectionButton.visible =
            inspectorVisible && movement;

        leftDirectionButton.visible =
            inspectorVisible && movement;

        rightDirectionButton.visible =
            inspectorVisible && movement;

        durationDecreaseButton.visible =
            inspectorVisible;

        durationField.visible =
            inspectorVisible;

        durationIncreaseButton.visible =
            inspectorVisible;

        duplicateButton.visible =
            inspectorVisible;

        deleteButton.visible =
            inspectorVisible;

        if (step instanceof Scenario.MoveStep moveStep) {
            updateDirectionButton(
                forwardDirectionButton,
                moveStep.direction()
                    == Scenario.MoveDirection.FORWARD
            );

            updateDirectionButton(
                backwardDirectionButton,
                moveStep.direction()
                    == Scenario.MoveDirection.BACKWARD
            );

            updateDirectionButton(
                leftDirectionButton,
                moveStep.direction()
                    == Scenario.MoveDirection.LEFT
            );

            updateDirectionButton(
                rightDirectionButton,
                moveStep.direction()
                    == Scenario.MoveDirection.RIGHT
            );
        }

        durationDecreaseButton.active =
            step.durationTicks()
                > MIN_DURATION_TICKS;

        durationIncreaseButton.active =
            step.durationTicks()
                < MAX_DURATION_TICKS;

        duplicateButton.active = true;
        deleteButton.active = steps.size() > 1;

        saveButton.active =
            getValidationMessage() == null;

        if (layoutMode == LayoutMode.COMPACT) {
            workflowTabButton.setStyle(
                compactTab == CompactTab.WORKFLOW
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.GHOST
            );

            actionsTabButton.setStyle(
                compactTab == CompactTab.ACTIONS
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.GHOST
            );

            inspectorTabButton.setStyle(
                compactTab == CompactTab.INSPECTOR
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.GHOST
            );
        }
    }

    private void updateDirectionButton(
        KarakuriButton button,
        boolean selected
    ) {
        button.setStyle(
            selected
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.GHOST
        );
    }

    private boolean isWorkflowVisible() {
        return layoutMode == LayoutMode.WIDE
            || compactTab == CompactTab.WORKFLOW;
    }

    private boolean isInspectorVisible() {
        return layoutMode == LayoutMode.WIDE
            || compactTab == CompactTab.INSPECTOR;
    }

    private Scenario.Step getSelectedStep() {
        selectedStepIndex = Math.clamp(
            selectedStepIndex,
            0,
            steps.size() - 1
        );

        return steps.get(selectedStepIndex);
    }

    private String getValidationMessage() {
        if (!isScenarioNameValid()) {
            String name = nameField == null
                ? initialName
                : nameField.getValue().trim();

            if (name.isBlank()) {
                return "Scenario name is required";
            }

            return "A scenario with this name already exists";
        }

        if (!durationFieldValid) {
            return "Duration must be between 0.05 and 3600 seconds";
        }

        return null;
    }

    private boolean isScenarioNameValid() {
        String name = nameField == null
            ? initialName
            : nameField.getValue().trim();

        return !name.isBlank()
            && !ScenarioLibrary.containsName(
                name,
                scenarioIndex
            );
    }

    private String formatDurationForField(
        int durationTicks
    ) {
        return BigDecimal
            .valueOf(durationTicks)
            .divide(BigDecimal.valueOf(20))
            .stripTrailingZeros()
            .toPlainString();
    }

    private String getStepTitle(
        Scenario.Step step
    ) {
        return switch (step) {
            case Scenario.MoveStep moveStep ->
                "Move " + moveStep.direction().label();
            case Scenario.WaitStep waitStep ->
                "Wait";
        };
    }

    private int getStepAccentColor(
        Scenario.Step step
    ) {
        return switch (step) {
            case Scenario.MoveStep moveStep ->
                switch (moveStep.direction()) {
                    case FORWARD -> 0xFF61D394;
                    case BACKWARD -> 0xFFF0A765;
                    case LEFT -> 0xFF67B6E8;
                    case RIGHT -> 0xFFB38AE8;
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
        int margin = isWideScreen()
            ? WIDE_MARGIN
            : COMPACT_MARGIN;

        return Math.min(
            WIDE_MAX_WIDTH,
            width - margin * 2
        );
    }

    private int getResponsivePanelHeight() {
        int margin = isWideScreen()
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