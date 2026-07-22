package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.task.composite.RepeatTask;
import hanamuramiyu.karakuri.task.factory.ScenarioTaskFactory;
import hanamuramiyu.karakuri.ui.main.KarakuriScreenState;
import hanamuramiyu.karakuri.ui.main.ScenarioCardRenderer;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class KarakuriScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 1280;
    private static final int PANEL_MAX_HEIGHT = 640;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_HEIGHT = 24;

    private final Screen parent;
    private final KarakuriScreenState state;

    private ScenarioCardRenderer cardRenderer;
    private boolean compactLayout;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int cardX;
    private int cardY;
    private int cardWidth;
    private int cardHeight;

    private KarakuriButton previousButton;
    private KarakuriButton nextButton;
    private KarakuriButton reloadButton;
    private KarakuriButton newButton;
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
        compactLayout = width < 560 || height < 320;
        panelWidth = Math.min(PANEL_MAX_WIDTH, width - PANEL_MARGIN * 2);
        panelHeight = Math.min(PANEL_MAX_HEIGHT, height - PANEL_MARGIN * 2);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
        cardRenderer = new ScenarioCardRenderer(font);

        boolean hasScenario = state.hasScenario();
        int contentX = panelX + CONTENT_MARGIN;
        int contentWidth = panelWidth - CONTENT_MARGIN * 2;
        int managementY;
        int executionY;

        if (hasScenario) {
            managementY = panelY + panelHeight - 62;
            executionY = panelY + panelHeight - 32;
        } else {
            managementY = panelY + panelHeight - 32;
            executionY = managementY;
        }

        int headerHeight = compactLayout ? 42 : 54;
        cardX = contentX;
        cardY = panelY + headerHeight;
        cardWidth = contentWidth;
        cardHeight = managementY - cardY - (compactLayout ? 8 : 20);

        createHeaderButton();
        createNavigationButtons();
        createManagementButtons(contentX, contentWidth, managementY, hasScenario);
        createExecutionButtons(contentX, contentWidth, executionY);
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

        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 16,
            0xFFF6F2FA,
            false
        );

        if (!compactLayout) {
            graphics.drawString(
                font,
                Component.literal("Scenarios"),
                cardX,
                cardY - 16,
                0xFFAFA5BA,
                false
            );
        }

        cardRenderer.render(
            graphics,
            cardX,
            cardY,
            cardWidth,
            cardHeight,
            scenario,
            state.selectedScenarioIndex(),
            state.scenarioCount(),
            compactLayout
        );

        if (scenario != null && !compactLayout) {
            renderSectionLabels(graphics, status);
        }

        super.render(graphics, mouseX, mouseY, delta);
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
        updateButtons();
    }

    void refreshAfterDeletion(int deletedIndex) {
        state.refreshAfterDeletion(deletedIndex);
        updateButtons();
    }

    private void createHeaderButton() {
        int reloadWidth = compactLayout ? 60 : 78;

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

    private void createNavigationButtons() {
        int buttonY = cardY + Math.max(4, (cardHeight - 44) / 2);

        previousButton = addRenderableWidget(
            new KarakuriButton(
                font,
                cardX + 8,
                buttonY,
                28,
                44,
                Component.literal("<"),
                () -> selectScenario(-1),
                KarakuriButton.Style.GHOST
            )
        );

        nextButton = addRenderableWidget(
            new KarakuriButton(
                font,
                cardX + cardWidth - 36,
                buttonY,
                28,
                44,
                Component.literal(">"),
                () -> selectScenario(1),
                KarakuriButton.Style.GHOST
            )
        );
    }

    private void createManagementButtons(
        int contentX,
        int contentWidth,
        int managementY,
        boolean hasScenario
    ) {
        int buttonWidth = hasScenario
            ? (contentWidth - BUTTON_GAP * 2) / 3
            : contentWidth;

        newButton = addRenderableWidget(
            createButton(
                contentX,
                managementY,
                buttonWidth,
                compactLayout ? "New" : "New Scenario",
                this::openNewEditor,
                KarakuriButton.Style.PRIMARY
            )
        );

        editButton = addRenderableWidget(
            createButton(
                contentX + buttonWidth + BUTTON_GAP,
                managementY,
                buttonWidth,
                compactLayout ? "Edit" : "Edit Scenario",
                this::openSelectedEditor,
                KarakuriButton.Style.SECONDARY
            )
        );

        deleteButton = addRenderableWidget(
            createButton(
                contentX + (buttonWidth + BUTTON_GAP) * 2,
                managementY,
                buttonWidth,
                compactLayout ? "Delete" : "Delete Scenario",
                this::openDeleteConfirmation,
                KarakuriButton.Style.DANGER
            )
        );
    }

    private void createExecutionButtons(
        int contentX,
        int contentWidth,
        int executionY
    ) {
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

    private void renderSectionLabels(
        GuiGraphics graphics,
        TaskStatus status
    ) {
        int managementY = newButton.getY();
        int executionY = modeButton.getY();

        graphics.drawString(
            font,
            Component.literal("Scenario Management"),
            cardX,
            managementY - 14,
            0xFF8F8499,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Execution"),
            cardX,
            executionY - 14,
            0xFF8F8499,
            false
        );

        Component statusText = Component.literal("Status: " + status.label());
        graphics.drawString(
            font,
            statusText,
            panelX + panelWidth - CONTENT_MARGIN - font.width(statusText),
            executionY - 14,
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

    private void selectScenario(int offset) {
        state.selectScenario(offset);
        updateButtons();
    }

    private void reloadScenarios() {
        state.reload();
        updateButtons();
    }

    private void openNewEditor() {
        minecraft.setScreen(new ScenarioEditorScreen(this, -1, null));
    }

    private void openSelectedEditor() {
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
            previousButton == null
                || nextButton == null
                || reloadButton == null
                || newButton == null
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
        boolean hasScenario = state.hasScenario();

        previousButton.visible = hasScenario;
        nextButton.visible = hasScenario;
        previousButton.active = idle && state.hasMultipleScenarios();
        nextButton.active = idle && state.hasMultipleScenarios();
        reloadButton.active = idle;
        newButton.active = idle;
        editButton.visible = hasScenario;
        deleteButton.visible = hasScenario;
        editButton.active = idle && hasScenario;
        deleteButton.active = idle && hasScenario;
        modeButton.visible = hasScenario;
        startButton.visible = hasScenario;
        pauseButton.visible = hasScenario;
        stopButton.visible = hasScenario;

        modeButton.setMessage(
            Component.literal(
                compactLayout
                    ? state.executionModeLabel()
                    : "Mode: " + state.executionModeLabel()
            )
        );
        modeButton.active = idle && hasScenario;

        startButton.setMessage(
            Component.literal(
                status == TaskStatus.PAUSED
                    ? "Resume"
                    : "Start"
            )
        );
        startButton.active = hasScenario && status != TaskStatus.RUNNING;
        pauseButton.active = hasScenario && status == TaskStatus.RUNNING;
        stopButton.active = hasScenario && !idle;
    }

    private int getStatusColor(TaskStatus status) {
        return switch (status) {
            case IDLE -> 0xFFB7AFBF;
            case RUNNING -> 0xFF61D394;
            case PAUSED -> 0xFFF1C36E;
        };
    }
}