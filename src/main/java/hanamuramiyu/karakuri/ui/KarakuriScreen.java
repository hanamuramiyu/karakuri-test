package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.task.composite.RepeatTask;
import hanamuramiyu.karakuri.task.factory.ScenarioTaskFactory;
import hanamuramiyu.karakuri.ui.main.KarakuriScreenState;
import hanamuramiyu.karakuri.ui.main.ScenarioBrowserList;
import hanamuramiyu.karakuri.ui.main.ScenarioDetailsRenderer;
import hanamuramiyu.karakuri.ui.main.ScenarioSortMode;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import hanamuramiyu.karakuri.ui.widget.KarakuriDropdown;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public final class KarakuriScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 1100;
    private static final int PANEL_MAX_HEIGHT = 680;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 12;
    private static final int PANEL_GAP = 8;
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_HEIGHT = 24;
    private static final int HEADER_HEIGHT = 42;
    private static final int FILTER_HEIGHT = 24;

    private final Screen parent;
    private final KarakuriScreenState state;

    private ScenarioBrowserList scenarioBrowser;
    private ScenarioDetailsRenderer detailsRenderer;
    private boolean compactLayout;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int contentX;
    private int contentWidth;
    private int filterY;
    private int browserY;
    private int browserHeight;
    private int listX;
    private int listWidth;
    private int detailsX;
    private int detailsWidth;
    private int searchFrameX;
    private int searchFrameWidth;
    private int managementY;
    private int executionY;

    private EditBox searchField;
    private KarakuriDropdown<ScenarioSortMode> sortDropdown;
    private KarakuriButton reloadButton;
    private KarakuriButton newButton;
    private KarakuriButton duplicateButton;
    private KarakuriButton editButton;
    private KarakuriButton deleteButton;
    private KarakuriButton modeButton;
    private KarakuriButton startButton;
    private KarakuriButton pauseButton;
    private KarakuriButton stopButton;

    public KarakuriScreen(Screen parent) {
        super(Component.literal("Karakuri"));
        this.parent = parent;
        state = new KarakuriScreenState();
    }

    @Override
    protected void init() {
        compactLayout = width < 760 || height < 390;
        panelWidth = Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_MARGIN * 2
        );
        panelHeight = Math.min(
            PANEL_MAX_HEIGHT,
            height - PANEL_MARGIN * 2
        );
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
        contentX = panelX + CONTENT_MARGIN;
        contentWidth = panelWidth - CONTENT_MARGIN * 2;

        filterY = panelY + HEADER_HEIGHT;
        managementY = panelY + panelHeight - 58;
        executionY = panelY + panelHeight - 28;
        browserY = filterY + FILTER_HEIGHT + PANEL_GAP;
        browserHeight = Math.max(
            70,
            managementY - browserY - PANEL_GAP
        );

        int sortWidth = compactLayout
            ? Math.min(128, Math.max(92, contentWidth / 3))
            : 176;

        searchFrameX = contentX;
        searchFrameWidth = Math.max(
            90,
            contentWidth - sortWidth - BUTTON_GAP
        );

        listX = contentX;

        if (compactLayout) {
            listWidth = contentWidth;
            detailsX = contentX;
            detailsWidth = 0;
        } else {
            listWidth = Math.clamp(
                contentWidth * 38 / 100,
                300,
                410
            );
            detailsX = listX + listWidth + PANEL_GAP;
            detailsWidth = contentWidth - listWidth - PANEL_GAP;
        }

        detailsRenderer = new ScenarioDetailsRenderer(font);
        scenarioBrowser = new ScenarioBrowserList(
            font,
            state,
            this::selectScenarioIndex,
            this::openSelectedEditor
        );
        scenarioBrowser.setBounds(
            listX,
            browserY,
            listWidth,
            browserHeight
        );

        createSearchField();
        createSortDropdown(sortWidth);
        createHeaderButton();
        createManagementButtons();
        createExecutionButtons();
        scenarioBrowser.ensureSelectedVisible();
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
        TaskStatus status = TaskManager.getStatus();
        Scenario scenario = state.selectedScenario();

        renderShell(graphics);
        renderHeader(graphics);
        renderSearchFrame(graphics);

        scenarioBrowser.render(graphics, mouseX, mouseY);

        if (!compactLayout) {
            detailsRenderer.render(
                graphics,
                detailsX,
                browserY,
                detailsWidth,
                browserHeight,
                scenario,
                state.selectedVisibleIndex(),
                state.visibleScenarioCount()
            );
        }

        renderStatus(graphics, status);
        super.render(graphics, mouseX, mouseY, delta);
        sortDropdown.renderOverlay(
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
            sortDropdown != null
                && sortDropdown.isExpanded()
                && sortDropdown.mouseClicked(event, doubled)
        ) {
            return true;
        }

        if (super.mouseClicked(event, doubled)) {
            return true;
        }

        return scenarioBrowser != null
            && scenarioBrowser.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        if (
            scenarioBrowser != null
                && scenarioBrowser.mouseScrolled(
                    mouseX,
                    mouseY,
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

    void refreshScenarios(String selectedScenarioName) {
        state.refreshSelected(selectedScenarioName);

        if (scenarioBrowser != null) {
            scenarioBrowser.resetScroll();
        }

        updateButtons();
    }

    void refreshAfterDeletion(int deletedIndex) {
        state.refreshAfterDeletion(deletedIndex);

        if (scenarioBrowser != null) {
            scenarioBrowser.resetScroll();
        }

        updateButtons();
    }

    private void createSearchField() {
        searchField = new EditBox(
            font,
            searchFrameX + 8,
            filterY + 4,
            searchFrameWidth - 16,
            16,
            Component.literal("Search scenarios")
        );
        searchField.setBordered(false);
        searchField.setTextColor(0xFFEDE7F1);
        searchField.setTextColorUneditable(0xFF716879);
        searchField.setTextShadow(false);
        searchField.setMaxLength(64);
        searchField.setHint(
            Component.literal("Search scenarios...")
        );
        searchField.setValue(state.searchQuery());
        searchField.setResponder(this::updateSearchQuery);
        addRenderableWidget(searchField);
    }

    private void createSortDropdown(int sortWidth) {
        int sortX = contentX + contentWidth - sortWidth;

        sortDropdown = new KarakuriDropdown<>(
            font,
            sortX,
            filterY,
            sortWidth,
            FILTER_HEIGHT,
            sortOptions(),
            state.sortMode(),
            this::updateSortMode,
            this::updateButtons
        );
        sortDropdown.setLabelPrefix(
            compactLayout ? "" : "Sort: "
        );
        addRenderableWidget(sortDropdown);
    }

    private List<KarakuriDropdown.Option<ScenarioSortMode>> sortOptions() {
        return Arrays.stream(ScenarioSortMode.values())
            .map(
                mode -> new KarakuriDropdown.Option<>(
                    mode,
                    mode.label()
                )
            )
            .toList();
    }

    private void createHeaderButton() {
        int reloadWidth = compactLayout ? 64 : 78;

        reloadButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + panelWidth - CONTENT_MARGIN - reloadWidth,
                panelY + 10,
                reloadWidth,
                BUTTON_HEIGHT,
                Component.literal("Reload"),
                this::reloadScenarios,
                KarakuriButton.Style.GHOST
            )
        );
    }

    private void createManagementButtons() {
        newButton = addRenderableWidget(
            createButton(
                contentX,
                managementY,
                contentWidth,
                compactLayout ? "New" : "New Scenario",
                this::openNewEditor,
                KarakuriButton.Style.PRIMARY
            )
        );
        duplicateButton = addRenderableWidget(
            createButton(
                contentX,
                managementY,
                1,
                compactLayout ? "Copy" : "Duplicate",
                this::duplicateSelectedScenario,
                KarakuriButton.Style.SECONDARY
            )
        );
        editButton = addRenderableWidget(
            createButton(
                contentX,
                managementY,
                1,
                compactLayout ? "Edit" : "Edit Scenario",
                this::openSelectedEditor,
                KarakuriButton.Style.SECONDARY
            )
        );
        deleteButton = addRenderableWidget(
            createButton(
                contentX,
                managementY,
                1,
                compactLayout ? "Delete" : "Delete Scenario",
                this::openDeleteConfirmation,
                KarakuriButton.Style.DANGER
            )
        );
    }

    private void createExecutionButtons() {
        int modeWidth = compactLayout
            ? Math.min(92, contentWidth / 4)
            : Math.min(148, contentWidth / 3);
        int buttonWidth = (
            contentWidth - modeWidth - BUTTON_GAP * 3
        ) / 3;

        modeButton = addRenderableWidget(
            new KarakuriButton(
                font,
                contentX,
                executionY,
                modeWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::cycleExecutionMode,
                KarakuriButton.Style.SECONDARY
            )
        );
        startButton = addRenderableWidget(
            createButton(
                contentX + modeWidth + BUTTON_GAP,
                executionY,
                buttonWidth,
                "Start",
                this::startOrResume,
                KarakuriButton.Style.SUCCESS
            )
        );
        pauseButton = addRenderableWidget(
            createButton(
                contentX + modeWidth + buttonWidth + BUTTON_GAP * 2,
                executionY,
                buttonWidth,
                "Pause",
                () -> TaskManager.pause(minecraft),
                KarakuriButton.Style.SECONDARY
            )
        );
        stopButton = addRenderableWidget(
            createButton(
                contentX + modeWidth + buttonWidth * 2 + BUTTON_GAP * 3,
                executionY,
                buttonWidth,
                "Stop",
                () -> TaskManager.stop(minecraft),
                KarakuriButton.Style.DANGER
            )
        );
    }

    private KarakuriButton createButton(
        int x,
        int y,
        int buttonWidth,
        String label,
        Runnable action,
        KarakuriButton.Style style
    ) {
        return new KarakuriButton(
            font,
            x,
            y,
            buttonWidth,
            BUTTON_HEIGHT,
            Component.literal(label),
            action,
            style
        );
    }

    private void renderShell(GuiGraphics graphics) {
        graphics.fill(0, 0, width, height, 0xC0100E16);
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
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.fill(
            panelX + 4,
            panelY,
            panelX + panelWidth,
            panelY + HEADER_HEIGHT - 4,
            0xFF1C1824
        );
        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 11,
            0xFFF6F2FA,
            false
        );

        if (!compactLayout) {
            graphics.drawString(
                font,
                Component.literal(
                    "Browse, organize, and run your workflows"
                ),
                panelX + CONTENT_MARGIN,
                panelY + 25,
                0xFF8F8499,
                false
            );
        }
    }

    private void renderSearchFrame(GuiGraphics graphics) {
        graphics.fill(
            searchFrameX,
            filterY,
            searchFrameX + searchFrameWidth,
            filterY + FILTER_HEIGHT,
            0xFF100E16
        );
        graphics.renderOutline(
            searchFrameX,
            filterY,
            searchFrameWidth,
            FILTER_HEIGHT,
            searchField != null && searchField.isFocused()
                ? 0xFF8F6FC0
                : 0xFF484052
        );
        graphics.fill(
            searchFrameX,
            filterY,
            searchFrameX + 3,
            filterY + FILTER_HEIGHT,
            searchField != null && searchField.isFocused()
                ? 0xFFB38AE8
                : 0xFF51465D
        );
    }

    private void renderStatus(
        GuiGraphics graphics,
        TaskStatus status
    ) {
        Component statusText = Component.literal(status.label());
        int statusX = reloadButton.getX()
            - 10
            - font.width(statusText);
        int minimumX = panelX
            + CONTENT_MARGIN
            + font.width(title)
            + 12;

        if (statusX < minimumX) {
            return;
        }

        graphics.drawString(
            font,
            statusText,
            statusX,
            panelY + 17,
            getStatusColor(status),
            false
        );
    }

    private void startOrResume() {
        if (TaskManager.getStatus() == TaskStatus.PAUSED) {
            TaskManager.resume(minecraft);
            return;
        }

        Scenario scenario = state.selectedScenario();

        if (scenario == null) {
            return;
        }

        TaskManager.start(
            new RepeatTask(
                () -> ScenarioTaskFactory.create(scenario),
                state.repeatCount()
            ),
            minecraft
        );
    }

    private void selectScenarioIndex(int libraryIndex) {
        if (TaskManager.getStatus() != TaskStatus.IDLE) {
            return;
        }

        state.selectScenarioIndex(libraryIndex);
        scenarioBrowser.ensureSelectedVisible();
        updateButtons();
    }

    private void updateSearchQuery(String query) {
        state.setSearchQuery(query);
        scenarioBrowser.resetScroll();
        updateButtons();
    }

    private void updateSortMode(ScenarioSortMode sortMode) {
        state.setSortMode(sortMode);
        scenarioBrowser.resetScroll();
        updateButtons();
    }

    private void reloadScenarios() {
        state.reload();
        scenarioBrowser.resetScroll();
        updateButtons();
    }

    private void openNewEditor() {
        minecraft.setScreen(
            new ScenarioEditorScreen(this, -1, null)
        );
    }

    private void duplicateSelectedScenario() {
        if (TaskManager.getStatus() != TaskStatus.IDLE) {
            return;
        }

        String duplicateName = state.duplicateSelectedScenario();

        if (duplicateName != null) {
            scenarioBrowser.ensureSelectedVisible();
            updateButtons();
        }
    }

    private void openSelectedEditor() {
        if (TaskManager.getStatus() != TaskStatus.IDLE) {
            return;
        }

        Scenario scenario = state.selectedScenario();

        if (scenario == null) {
            return;
        }

        minecraft.setScreen(
            new ScenarioEditorScreen(
                this,
                state.selectedScenarioIndex(),
                scenario
            )
        );
    }

    private void openDeleteConfirmation() {
        if (TaskManager.getStatus() != TaskStatus.IDLE) {
            return;
        }

        Scenario scenario = state.selectedScenario();

        if (scenario == null) {
            return;
        }

        minecraft.setScreen(
            new DeleteScenarioScreen(
                this,
                state.selectedScenarioIndex(),
                scenario
            )
        );
    }

    private void cycleExecutionMode() {
        state.cycleExecutionMode();
        updateButtons();
    }

    private void updateButtons() {
        if (
            reloadButton == null
                || searchField == null
                || sortDropdown == null
                || newButton == null
                || duplicateButton == null
                || editButton == null
                || deleteButton == null
                || modeButton == null
                || startButton == null
                || pauseButton == null
                || stopButton == null
        ) {
            return;
        }

        TaskStatus status = TaskManager.getStatus();
        boolean idle = status == TaskStatus.IDLE;
        boolean hasSelection = state.selectedScenario() != null;

        searchField.setEditable(idle);
        sortDropdown.active = idle;
        reloadButton.active = idle;
        newButton.active = idle;

        layoutManagementButtons(hasSelection);

        duplicateButton.visible = hasSelection;
        editButton.visible = hasSelection;
        deleteButton.visible = hasSelection;
        duplicateButton.active = idle && hasSelection;
        editButton.active = idle && hasSelection;
        deleteButton.active = idle && hasSelection;

        modeButton.visible = hasSelection;
        startButton.visible = hasSelection;
        pauseButton.visible = hasSelection;
        stopButton.visible = hasSelection;

        modeButton.setMessage(
            Component.literal(
                compactLayout
                    ? state.executionModeLabel()
                    : "Mode: " + state.executionModeLabel()
            )
        );
        modeButton.active = idle && hasSelection;

        startButton.setMessage(
            Component.literal(
                status == TaskStatus.PAUSED
                    ? "Resume"
                    : "Start"
            )
        );
        startButton.active = hasSelection
            && status != TaskStatus.RUNNING;
        pauseButton.active = hasSelection
            && status == TaskStatus.RUNNING;
        stopButton.active = hasSelection && !idle;
    }

    private void layoutManagementButtons(boolean hasSelection) {
        if (!hasSelection) {
            newButton.setX(contentX);
            newButton.setWidth(contentWidth);
            return;
        }

        int buttonWidth = (
            contentWidth - BUTTON_GAP * 3
        ) / 4;

        newButton.setX(contentX);
        newButton.setWidth(buttonWidth);
        duplicateButton.setX(
            contentX + buttonWidth + BUTTON_GAP
        );
        duplicateButton.setWidth(buttonWidth);
        editButton.setX(
            contentX + (buttonWidth + BUTTON_GAP) * 2
        );
        editButton.setWidth(buttonWidth);
        deleteButton.setX(
            contentX + (buttonWidth + BUTTON_GAP) * 3
        );
        deleteButton.setWidth(buttonWidth);
    }

    private int getStatusColor(TaskStatus status) {
        return switch (status) {
            case IDLE -> 0xFFB7AFBF;
            case RUNNING -> 0xFF61D394;
            case PAUSED -> 0xFFF1C36E;
        };
    }
}