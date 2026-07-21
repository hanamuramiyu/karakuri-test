package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.Scenario;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.ScenarioTaskFactory;
import hanamuramiyu.karakuri.task.RepeatTask;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class KarakuriScreen extends Screen {
    private static final int PANEL_HEIGHT = 330;
    private static final int PANEL_MAX_WIDTH = 560;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 24;
    private static final int MAX_VISIBLE_STEPS = 5;

    private final Screen parent;

    private List<Scenario> scenarios;
    private int selectedScenarioIndex;
    private ExecutionMode executionMode =
        ExecutionMode.ONCE;

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
        scenarios =
            ScenarioLibrary.getScenarios();
    }

    @Override
    protected void init() {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();

        int contentX =
            panelX + CONTENT_MARGIN;

        int contentWidth =
            panelWidth
                - CONTENT_MARGIN * 2;

        int scenarioButtonWidth =
            (
                contentWidth
                    - BUTTON_GAP * 2
            ) / 3;

        int executionModeWidth = 148;

        int executionButtonWidth =
            (
                contentWidth
                    - executionModeWidth
                    - BUTTON_GAP * 3
            ) / 3;

        reloadButton =
            new KarakuriButton(
                font,
                panelX
                    + panelWidth
                    - CONTENT_MARGIN
                    - 78,
                panelY + 14,
                78,
                BUTTON_HEIGHT,
                Component.literal("Reload"),
                this::reloadScenarios,
                KarakuriButton.Style.GHOST
            );

        previousButton =
            new KarakuriButton(
                font,
                contentX + 8,
                panelY + 88,
                28,
                44,
                Component.literal("<"),
                () -> selectScenario(-1),
                KarakuriButton.Style.GHOST
            );

        nextButton =
            new KarakuriButton(
                font,
                contentX
                    + contentWidth
                    - 36,
                panelY + 88,
                28,
                44,
                Component.literal(">"),
                () -> selectScenario(1),
                KarakuriButton.Style.GHOST
            );

        int scenarioButtonsY =
            panelY + 232;

        newButton =
            new KarakuriButton(
                font,
                contentX,
                scenarioButtonsY,
                scenarioButtonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    "New Scenario"
                ),
                this::openNewEditor,
                KarakuriButton.Style.PRIMARY
            );

        editButton =
            new KarakuriButton(
                font,
                contentX
                    + scenarioButtonWidth
                    + BUTTON_GAP,
                scenarioButtonsY,
                scenarioButtonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    "Edit Scenario"
                ),
                this::openSelectedEditor,
                KarakuriButton.Style.SECONDARY
            );

        deleteButton =
            new KarakuriButton(
                font,
                contentX
                    + (
                        scenarioButtonWidth
                            + BUTTON_GAP
                    ) * 2,
                scenarioButtonsY,
                scenarioButtonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    "Delete Scenario"
                ),
                this::openDeleteConfirmation,
                KarakuriButton.Style.DANGER
            );

        int executionButtonsY =
            panelY + 288;

        modeButton =
            new KarakuriButton(
                font,
                contentX,
                executionButtonsY,
                executionModeWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::cycleExecutionMode,
                KarakuriButton.Style.SECONDARY
            );

        startButton =
            new KarakuriButton(
                font,
                contentX
                    + executionModeWidth
                    + BUTTON_GAP,
                executionButtonsY,
                executionButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Start"),
                this::startOrResume,
                KarakuriButton.Style.SUCCESS
            );

        pauseButton =
            new KarakuriButton(
                font,
                contentX
                    + executionModeWidth
                    + executionButtonWidth
                    + BUTTON_GAP * 2,
                executionButtonsY,
                executionButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Pause"),
                () -> TaskManager.pause(
                    minecraft
                ),
                KarakuriButton.Style.SECONDARY
            );

        stopButton =
            new KarakuriButton(
                font,
                contentX
                    + executionModeWidth
                    + executionButtonWidth * 2
                    + BUTTON_GAP * 3,
                executionButtonsY,
                executionButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Stop"),
                () -> TaskManager.stop(
                    minecraft
                ),
                KarakuriButton.Style.DANGER
            );

        addRenderableWidget(reloadButton);
        addRenderableWidget(previousButton);
        addRenderableWidget(nextButton);
        addRenderableWidget(newButton);
        addRenderableWidget(editButton);
        addRenderableWidget(deleteButton);
        addRenderableWidget(modeButton);
        addRenderableWidget(startButton);
        addRenderableWidget(pauseButton);
        addRenderableWidget(stopButton);

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
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();
        int contentX =
            panelX + CONTENT_MARGIN;

        int contentWidth =
            panelWidth
                - CONTENT_MARGIN * 2;

        TaskStatus status =
            TaskManager.getStatus();

        Scenario scenario =
            getSelectedScenario();

        graphics.fill(
            0,
            0,
            width,
            height,
            0xC0100E16
        );

        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + PANEL_HEIGHT,
            0xFF181620
        );

        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            PANEL_HEIGHT,
            0xFF6F5A91
        );

        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + PANEL_HEIGHT,
            0xFF9B79D1
        );

        drawCenteredText(
            graphics,
            title,
            panelY + 18,
            0xFFF6F2FA
        );

        graphics.drawString(
            font,
            Component.literal("Scenarios"),
            contentX,
            panelY + 54,
            0xFFAFA5BA,
            false
        );

        renderScenarioCard(
            graphics,
            scenario,
            contentX,
            panelY + 68,
            contentWidth,
            142
        );

        graphics.drawString(
            font,
            Component.literal(
                "Scenario Management"
            ),
            contentX,
            panelY + 218,
            0xFF8F8499,
            false
        );

        if (scenario != null) {
            graphics.drawString(
                font,
                Component.literal("Execution"),
                contentX,
                panelY + 274,
                0xFF8F8499,
                false
            );

            Component statusText =
                Component.literal(
                    "Status: "
                        + status.label()
                );

            graphics.drawString(
                font,
                statusText,
                panelX
                    + panelWidth
                    - CONTENT_MARGIN
                    - font.width(statusText),
                panelY + 274,
                getStatusColor(status),
                false
            );
        }

        super.render(
            graphics,
            mouseX,
            mouseY,
            delta
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

    void refreshScenarios(
        String selectedScenarioName
    ) {
        scenarios =
            ScenarioLibrary.getScenarios();

        selectedScenarioIndex = 0;

        for (
            int index = 0;
            index < scenarios.size();
            index++
        ) {
            if (
                scenarios
                    .get(index)
                    .name()
                    .equals(
                        selectedScenarioName
                    )
            ) {
                selectedScenarioIndex =
                    index;
                break;
            }
        }

        updateButtons();
    }

    void refreshAfterDeletion(
        int deletedIndex
    ) {
        scenarios =
            ScenarioLibrary.getScenarios();

        if (scenarios.isEmpty()) {
            selectedScenarioIndex = 0;
        } else {
            selectedScenarioIndex =
                Math.min(
                    deletedIndex,
                    scenarios.size() - 1
                );
        }

        updateButtons();
    }

    private void renderScenarioCard(
        GuiGraphics graphics,
        Scenario scenario,
        int cardX,
        int cardY,
        int cardWidth,
        int cardHeight
    ) {
        graphics.fill(
            cardX,
            cardY,
            cardX + cardWidth,
            cardY + cardHeight,
            0xFF100E16
        );

        graphics.renderOutline(
            cardX,
            cardY,
            cardWidth,
            cardHeight,
            0xFF393243
        );

        if (scenario == null) {
            Component emptyTitle =
                Component.literal(
                    "No scenarios"
                );

            Component emptyDescription =
                Component.literal(
                    "Create a workflow to begin."
                );

            graphics.drawString(
                font,
                emptyTitle,
                cardX
                    + (
                        cardWidth
                            - font.width(
                                emptyTitle
                            )
                    ) / 2,
                cardY + 50,
                0xFFF0EBF4,
                false
            );

            graphics.drawString(
                font,
                emptyDescription,
                cardX
                    + (
                        cardWidth
                            - font.width(
                                emptyDescription
                            )
                    ) / 2,
                cardY + 72,
                0xFF8F8499,
                false
            );

            return;
        }

        Component scenarioPosition =
            Component.literal(
                (selectedScenarioIndex + 1)
                    + " / "
                    + scenarios.size()
            );

        graphics.drawString(
            font,
            Component.literal(
                scenario.name()
            ),
            cardX + 46,
            cardY + 14,
            0xFFF4F0F7,
            false
        );

        graphics.drawString(
            font,
            scenarioPosition,
            cardX
                + cardWidth
                - 46
                - font.width(
                    scenarioPosition
                ),
            cardY + 14,
            0xFF8F8499,
            false
        );

        int visibleStepCount =
            Math.min(
                scenario.steps().size(),
                MAX_VISIBLE_STEPS
            );

        for (
            int index = 0;
            index < visibleStepCount;
            index++
        ) {
            Scenario.Step step =
                scenario.steps().get(index);

            graphics.drawString(
                font,
                Component.literal(
                    (index + 1)
                        + ". "
                        + step.label()
                ),
                cardX + 46,
                cardY + 38 + index * 16,
                0xFFC4BACB,
                false
            );
        }

        if (
            scenario.steps().size()
                > MAX_VISIBLE_STEPS
        ) {
            int remainingSteps =
                scenario.steps().size()
                    - MAX_VISIBLE_STEPS;

            graphics.drawString(
                font,
                Component.literal(
                    "+ "
                        + remainingSteps
                        + " more"
                ),
                cardX + 46,
                cardY + 118,
                0xFF81768A,
                false
            );
        }
    }

    private void startOrResume() {
        if (
            TaskManager.getStatus()
                == TaskStatus.PAUSED
        ) {
            TaskManager.resume(minecraft);
            return;
        }

        Scenario selectedScenario =
            getSelectedScenario();

        if (selectedScenario == null) {
            return;
        }

        TaskManager.start(
            new RepeatTask(
                () ->
                    ScenarioTaskFactory.create(
                        selectedScenario
                    ),
                executionMode.repeatCount()
            ),
            minecraft
        );
    }

    private void selectScenario(int offset) {
        if (scenarios.size() <= 1) {
            return;
        }

        selectedScenarioIndex =
            Math.floorMod(
                selectedScenarioIndex
                    + offset,
                scenarios.size()
            );

        updateButtons();
    }

    private void reloadScenarios() {
        Scenario selectedScenario =
            getSelectedScenario();

        String selectedScenarioName =
            selectedScenario == null
                ? null
                : selectedScenario.name();

        ScenarioLibrary.reload();

        scenarios =
            ScenarioLibrary.getScenarios();

        selectedScenarioIndex = 0;

        if (selectedScenarioName != null) {
            for (
                int index = 0;
                index < scenarios.size();
                index++
            ) {
                if (
                    scenarios
                        .get(index)
                        .name()
                        .equals(
                            selectedScenarioName
                        )
                ) {
                    selectedScenarioIndex =
                        index;
                    break;
                }
            }
        }

        updateButtons();
    }

    private void openNewEditor() {
        minecraft.setScreen(
            new ScenarioEditorScreen(
                this,
                -1,
                null
            )
        );
    }

    private void openSelectedEditor() {
        Scenario scenario =
            getSelectedScenario();

        if (scenario == null) {
            return;
        }

        minecraft.setScreen(
            new ScenarioEditorScreen(
                this,
                selectedScenarioIndex,
                scenario
            )
        );
    }

    private void openDeleteConfirmation() {
        Scenario scenario =
            getSelectedScenario();

        if (scenario == null) {
            return;
        }

        minecraft.setScreen(
            new DeleteScenarioScreen(
                this,
                selectedScenarioIndex,
                scenario
            )
        );
    }

    private void cycleExecutionMode() {
        executionMode =
            executionMode.next();

        updateButtons();
    }

    private Scenario getSelectedScenario() {
        if (scenarios.isEmpty()) {
            return null;
        }

        selectedScenarioIndex =
            Math.clamp(
                selectedScenarioIndex,
                0,
                scenarios.size() - 1
            );

        return scenarios.get(
            selectedScenarioIndex
        );
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

        TaskStatus status =
            TaskManager.getStatus();

        boolean idle =
            status == TaskStatus.IDLE;

        boolean hasScenario =
            !scenarios.isEmpty();

        boolean hasMultipleScenarios =
            scenarios.size() > 1;

        previousButton.visible =
            hasScenario;

        nextButton.visible =
            hasScenario;

        previousButton.active =
            idle && hasMultipleScenarios;

        nextButton.active =
            idle && hasMultipleScenarios;

        reloadButton.active = idle;
        newButton.active = idle;

        editButton.visible =
            hasScenario;

        deleteButton.visible =
            hasScenario;

        editButton.active =
            idle && hasScenario;

        deleteButton.active =
            idle && hasScenario;

        modeButton.visible =
            hasScenario;

        startButton.visible =
            hasScenario;

        pauseButton.visible =
            hasScenario;

        stopButton.visible =
            hasScenario;

        modeButton.setMessage(
            Component.literal(
                "Mode: "
                    + executionMode.label()
            )
        );

        modeButton.active =
            idle && hasScenario;

        startButton.setMessage(
            Component.literal(
                status == TaskStatus.PAUSED
                    ? "Resume"
                    : "Start"
            )
        );

        startButton.active =
            hasScenario
                && status
                    != TaskStatus.RUNNING;

        pauseButton.active =
            hasScenario
                && status
                    == TaskStatus.RUNNING;

        stopButton.active =
            hasScenario && !idle;
    }

    private int getStatusColor(
        TaskStatus status
    ) {
        return switch (status) {
            case IDLE -> 0xFFB7AFBF;
            case RUNNING -> 0xFF61D394;
            case PAUSED -> 0xFFF1C36E;
        };
    }

    private int getPanelWidth() {
        return Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int getPanelX() {
        return (
            width - getPanelWidth()
        ) / 2;
    }

    private int getPanelY() {
        return (
            height - PANEL_HEIGHT
        ) / 2;
    }

    private void drawCenteredText(
        GuiGraphics graphics,
        Component text,
        int y,
        int color
    ) {
        graphics.drawString(
            font,
            text,
            (
                width
                    - font.width(text)
            ) / 2,
            y,
            color,
            false
        );
    }

    private enum ExecutionMode {
        ONCE(
            "Once",
            1
        ),
        LOOP(
            "Loop",
            RepeatTask.INFINITE
        );

        private final String label;
        private final int repeatCount;

        ExecutionMode(
            String label,
            int repeatCount
        ) {
            this.label = label;
            this.repeatCount = repeatCount;
        }

        private String label() {
            return label;
        }

        private int repeatCount() {
            return repeatCount;
        }

        private ExecutionMode next() {
            return this == ONCE
                ? LOOP
                : ONCE;
        }
    }
}