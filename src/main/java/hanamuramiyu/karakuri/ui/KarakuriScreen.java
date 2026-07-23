package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.persistence.ScenarioTransferService;
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
import hanamuramiyu.karakuri.ui.widget.KarakuriMenuButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Path;
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
    private static final int HEADER_BUTTON_HEIGHT = 22;
    private static final int HEADER_ACTION_Y_OFFSET = 6;
    private static final int FILTER_Y_OFFSET = 31;
    private static final int HEADER_HEIGHT = 63;
    private static final int FILTER_HEIGHT = 24;
    private static final int FEEDBACK_HEIGHT = 14;

    private final Screen parent;
    private final KarakuriScreenState state;
    private final ScenarioTransferService transferService;

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
    private int feedbackY;
    private int statusFrameX;
    private int statusFrameY;
    private int statusFrameWidth;
    private int statusFrameHeight;

    private EditBox searchField;
    private KarakuriDropdown<ScenarioSortMode> sortDropdown;
    private KarakuriButton helpButton;
    private KarakuriMenuButton globalToolsMenu;
    private KarakuriButton newButton;
    private KarakuriButton editButton;
    private KarakuriMenuButton scenarioMoreMenu;
    private KarakuriButton modeButton;
    private KarakuriButton startButton;
    private KarakuriButton pauseButton;
    private KarakuriButton stopButton;

    private String feedbackMessage;
    private int feedbackColor;
    private int feedbackTicks;

    public KarakuriScreen(Screen parent) {
        super(Component.literal("Karakuri"));
        this.parent = parent;
        this.state = new KarakuriScreenState();
        this.transferService = ScenarioTransferService.createDefault();
    }

    @Override
    protected void init() {
        compactLayout = width < 760 || height < 390;
        panelWidth = Math.min(PANEL_MAX_WIDTH, width - PANEL_MARGIN * 2);
        panelHeight = Math.min(PANEL_MAX_HEIGHT, height - PANEL_MARGIN * 2);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
        contentX = panelX + CONTENT_MARGIN;
        contentWidth = panelWidth - CONTENT_MARGIN * 2;

        filterY = panelY + FILTER_Y_OFFSET;
        managementY = panelY + panelHeight - 58;
        executionY = panelY + panelHeight - 28;
        feedbackY = managementY - FEEDBACK_HEIGHT;
        browserY = filterY + FILTER_HEIGHT + PANEL_GAP;
        browserHeight = Math.max(
            64,
            feedbackY - browserY - PANEL_GAP
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
            listWidth = Math.clamp(contentWidth * 38 / 100, 300, 410);
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
        createHeaderActions();
        createManagementButtons();
        createExecutionButtons();
        scenarioBrowser.ensureSelectedVisible();
        updateButtons();
    }

    @Override
    public void tick() {
        if (feedbackTicks > 0) {
            feedbackTicks--;

            if (feedbackTicks == 0) {
                feedbackMessage = null;
            }
        }

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

        renderFeedback(graphics);
        renderStatusBadge(graphics, status);
        super.render(graphics, mouseX, mouseY, delta);
        sortDropdown.renderOverlay(graphics, mouseX, mouseY, delta);
        globalToolsMenu.renderOverlay(graphics, mouseX, mouseY, delta);
        scenarioMoreMenu.renderOverlay(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (
            scenarioMoreMenu != null
                && scenarioMoreMenu.isExpanded()
                && scenarioMoreMenu.mouseClicked(event, doubled)
        ) {
            return true;
        }

        if (
            globalToolsMenu != null
                && globalToolsMenu.isExpanded()
                && globalToolsMenu.mouseClicked(event, doubled)
        ) {
            return true;
        }

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
    public boolean keyPressed(KeyEvent event) {
        if (
            event.key() == GLFW.GLFW_KEY_ESCAPE
                && closeTransientUi()
        ) {
            return true;
        }

        if (event.hasControlDownWithQuirk()) {
            if (event.key() == GLFW.GLFW_KEY_F) {
                searchField.setFocused(true);
                return true;
            }

            if (event.key() == GLFW.GLFW_KEY_N) {
                openNewEditor();
                return true;
            }
        }

        if (event.key() == GLFW.GLFW_KEY_F5) {
            reloadScenarios();
            return true;
        }

        if (searchField != null && searchField.isFocused()) {
            return super.keyPressed(event);
        }

        if (event.key() == GLFW.GLFW_KEY_UP) {
            selectVisibleOffset(-1);
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_DOWN) {
            selectVisibleOffset(1);
            return true;
        }

        if (
            event.key() == GLFW.GLFW_KEY_ENTER
                || event.key() == GLFW.GLFW_KEY_KP_ENTER
        ) {
            startOrResume();
            return true;
        }

        return super.keyPressed(event);
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

    void showFeedback(
        String message,
        boolean success
    ) {
        feedbackMessage = message;
        feedbackColor = success ? 0xFF61D394 : 0xFFE66777;
        feedbackTicks = 120;
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
        searchField.setHint(Component.literal("Search scenarios..."));
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
        sortDropdown.setLabelPrefix(compactLayout ? "" : "Sort: ");
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

    private void createHeaderActions() {
        int helpWidth = compactLayout ? 28 : 56;
        int toolsWidth = compactLayout ? 64 : 82;
        int statusWidth = 58;
        int actionsY = panelY + HEADER_ACTION_Y_OFFSET;
        int toolsX = panelX + panelWidth - CONTENT_MARGIN - toolsWidth;
        int helpX = toolsX - BUTTON_GAP - helpWidth;

        statusFrameX = helpX - BUTTON_GAP - statusWidth;
        statusFrameY = actionsY;
        statusFrameWidth = statusWidth;
        statusFrameHeight = HEADER_BUTTON_HEIGHT;

        helpButton = addRenderableWidget(
            new KarakuriButton(
                font,
                helpX,
                actionsY,
                helpWidth,
                HEADER_BUTTON_HEIGHT,
                Component.literal(compactLayout ? "?" : "Guide"),
                this::openShortcuts,
                KarakuriButton.Style.SECONDARY
            )
        );

        globalToolsMenu = addRenderableWidget(
            new KarakuriMenuButton(
                font,
                toolsX,
                actionsY,
                toolsWidth,
                HEADER_BUTTON_HEIGHT,
                Component.literal("Tools"),
                List.of(
                    new KarakuriMenuButton.Item(
                        "Import Scenarios",
                        this::openImportScreen,
                        0xFFE8E2ED,
                        this::isIdle
                    ),
                    new KarakuriMenuButton.Item(
                        "Open Import Folder",
                        this::openImportFolder
                    ),
                    new KarakuriMenuButton.Item(
                        "Open Export Folder",
                        this::openExportFolder
                    ),
                    new KarakuriMenuButton.Item(
                        "Open Karakuri Folder",
                        this::openConfigFolder
                    ),
                    new KarakuriMenuButton.Item(
                        "Reload Scenarios",
                        this::reloadScenarios,
                        0xFFE8E2ED,
                        this::isIdle
                    )
                ),
                KarakuriMenuButton.Direction.DOWN
            )
        );
        globalToolsMenu.setOptionWidth(compactLayout ? 158 : 184);
        globalToolsMenu.setAlignOptionsRight(true);
    }

    private void createManagementButtons() {
        newButton = addRenderableWidget(
            createButton(
                contentX,
                managementY,
                contentWidth,
                compactLayout ? "+ New" : "+ New Scenario",
                this::openNewEditor,
                KarakuriButton.Style.PRIMARY
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
        scenarioMoreMenu = addRenderableWidget(
            new KarakuriMenuButton(
                font,
                contentX,
                managementY,
                1,
                BUTTON_HEIGHT,
                Component.literal("More"),
                List.of(
                    new KarakuriMenuButton.Item(
                        "Duplicate",
                        this::duplicateSelectedScenario,
                        0xFFE8E2ED,
                        this::canManageSelectedScenario
                    ),
                    new KarakuriMenuButton.Item(
                        "Export",
                        this::exportSelectedScenario,
                        0xFF67C7E8,
                        this::canManageSelectedScenario
                    ),
                    new KarakuriMenuButton.Item(
                        "Delete",
                        this::openDeleteConfirmation,
                        0xFFE66777,
                        this::canManageSelectedScenario
                    )
                ),
                KarakuriMenuButton.Direction.UP
            )
        );
        scenarioMoreMenu.setOptionWidth(compactLayout ? 104 : 126);
        scenarioMoreMenu.setAlignOptionsRight(true);
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

    private void renderFeedback(GuiGraphics graphics) {
        if (feedbackMessage == null || feedbackMessage.isBlank()) {
            return;
        }

        Component message = Component.literal(feedbackMessage);
        graphics.drawString(
            font,
            message,
            contentX + (contentWidth - font.width(message)) / 2,
            feedbackY + 2,
            feedbackColor,
            false
        );
    }

    private void renderStatusBadge(
        GuiGraphics graphics,
        TaskStatus status
    ) {
        Component statusText = Component.literal(status.label());
        int minimumX = panelX
            + CONTENT_MARGIN
            + font.width(title)
            + 12;

        if (statusFrameX < minimumX) {
            return;
        }

        int color = getStatusColor(status);

        graphics.fill(
            statusFrameX,
            statusFrameY,
            statusFrameX + statusFrameWidth,
            statusFrameY + statusFrameHeight,
            0xFF15121B
        );
        graphics.renderOutline(
            statusFrameX,
            statusFrameY,
            statusFrameWidth,
            statusFrameHeight,
            0xFF484052
        );
        graphics.fill(
            statusFrameX + 7,
            statusFrameY + statusFrameHeight / 2 - 2,
            statusFrameX + 11,
            statusFrameY + statusFrameHeight / 2 + 2,
            color
        );
        graphics.drawString(
            font,
            statusText,
            statusFrameX + 16,
            statusFrameY
                + (statusFrameHeight - font.lineHeight) / 2
                + 1,
            color,
            false
        );
    }

    private void startOrResume() {
        if (TaskManager.getStatus() == TaskStatus.PAUSED) {
            TaskManager.resume(minecraft);
            return;
        }

        Scenario scenario = state.selectedScenario();

        if (scenario == null || TaskManager.getStatus() == TaskStatus.RUNNING) {
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
        if (!isIdle()) {
            return;
        }

        state.selectScenarioIndex(libraryIndex);
        scenarioBrowser.ensureSelectedVisible();
        updateButtons();
    }

    private void selectVisibleOffset(int offset) {
        if (!isIdle() || !state.hasVisibleScenario()) {
            return;
        }

        state.selectVisibleOffset(offset);
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
        if (!isIdle()) {
            return;
        }

        state.reload();
        scenarioBrowser.resetScroll();
        showFeedback("Scenarios reloaded", true);
        updateButtons();
    }

    private void openNewEditor() {
        if (!isIdle()) {
            return;
        }

        minecraft.setScreen(new ScenarioEditorScreen(this, -1, null));
    }

    private void duplicateSelectedScenario() {
        if (!canManageSelectedScenario()) {
            return;
        }

        String duplicateName = state.duplicateSelectedScenario();

        if (duplicateName != null) {
            scenarioBrowser.ensureSelectedVisible();
            showFeedback("Created \"" + duplicateName + "\"", true);
            updateButtons();
        }
    }

    private void exportSelectedScenario() {
        if (!canManageSelectedScenario()) {
            return;
        }

        Scenario scenario = state.selectedScenario();

        try {
            Path exportedPath = transferService.exportScenario(scenario);
            showFeedback(
                "Exported to export/" + exportedPath.getFileName(),
                true
            );
        } catch (IOException | RuntimeException exception) {
            showFeedback(
                exception.getMessage() == null
                    ? "Failed to export scenario"
                    : "Export failed: " + exception.getMessage(),
                false
            );
        }
    }

    private void openSelectedEditor() {
        if (!canManageSelectedScenario()) {
            return;
        }

        Scenario scenario = state.selectedScenario();

        minecraft.setScreen(
            new ScenarioEditorScreen(
                this,
                state.selectedScenarioIndex(),
                scenario
            )
        );
    }

    private void openDeleteConfirmation() {
        if (!canManageSelectedScenario()) {
            return;
        }

        Scenario scenario = state.selectedScenario();

        minecraft.setScreen(
            new DeleteScenarioScreen(
                this,
                state.selectedScenarioIndex(),
                scenario
            )
        );
    }

    private void openImportScreen() {
        if (!isIdle()) {
            return;
        }

        minecraft.setScreen(
            new ImportScenariosScreen(this, transferService)
        );
    }

    private void openImportFolder() {
        openFolder(
            transferService.importDirectory(),
            "Opened import folder"
        );
    }

    private void openExportFolder() {
        openFolder(
            transferService.exportDirectory(),
            "Opened export folder"
        );
    }

    private void openConfigFolder() {
        openFolder(
            transferService.configDirectory(),
            "Opened Karakuri folder"
        );
    }

    private void openFolder(
        Path path,
        String successMessage
    ) {
        try {
            transferService.prepareDirectories();
            String error = KarakuriPathOpener.open(path);

            if (error == null) {
                showFeedback(successMessage, true);
            } else {
                showFeedback(error, false);
            }
        } catch (IOException exception) {
            showFeedback(
                exception.getMessage() == null
                    ? "Failed to prepare Karakuri folders"
                    : exception.getMessage(),
                false
            );
        }
    }

    private void openShortcuts() {
        minecraft.setScreen(new ScenarioShortcutsScreen(this));
    }

    private void cycleExecutionMode() {
        state.cycleExecutionMode();
        updateButtons();
    }

    private boolean closeTransientUi() {
        if (scenarioMoreMenu != null && scenarioMoreMenu.isExpanded()) {
            scenarioMoreMenu.collapse();
            return true;
        }

        if (globalToolsMenu != null && globalToolsMenu.isExpanded()) {
            globalToolsMenu.collapse();
            return true;
        }

        if (sortDropdown != null && sortDropdown.isExpanded()) {
            sortDropdown.collapse();
            return true;
        }

        return false;
    }

    private void updateButtons() {
        if (
            helpButton == null
                || globalToolsMenu == null
                || searchField == null
                || sortDropdown == null
                || newButton == null
                || editButton == null
                || scenarioMoreMenu == null
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
        helpButton.active = true;
        globalToolsMenu.active = true;
        newButton.active = idle;

        layoutManagementButtons(hasSelection);

        editButton.visible = hasSelection;
        scenarioMoreMenu.visible = hasSelection;
        editButton.active = idle && hasSelection;
        scenarioMoreMenu.active = hasSelection;

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
                status == TaskStatus.PAUSED ? "Resume" : "Start"
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

        int moreWidth = compactLayout
            ? Math.clamp(contentWidth / 4, 62, 82)
            : Math.clamp(contentWidth / 5, 104, 132);
        int remainingWidth = contentWidth - moreWidth - BUTTON_GAP * 2;
        int newWidth = remainingWidth / 2;
        int editWidth = remainingWidth - newWidth;

        newButton.setX(contentX);
        newButton.setWidth(newWidth);
        editButton.setX(contentX + newWidth + BUTTON_GAP);
        editButton.setWidth(editWidth);
        scenarioMoreMenu.setX(
            contentX + newWidth + editWidth + BUTTON_GAP * 2
        );
        scenarioMoreMenu.setWidth(moreWidth);
    }

    private boolean isIdle() {
        return TaskManager.getStatus() == TaskStatus.IDLE;
    }

    private boolean canManageSelectedScenario() {
        return isIdle() && state.selectedScenario() != null;
    }

    private int getStatusColor(TaskStatus status) {
        return switch (status) {
            case IDLE -> 0xFFB7AFBF;
            case RUNNING -> 0xFF61D394;
            case PAUSED -> 0xFFF1C36E;
        };
    }
}