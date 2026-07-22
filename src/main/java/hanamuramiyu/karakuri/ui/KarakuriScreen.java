package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.task.factory.ScenarioTaskFactory;
import hanamuramiyu.karakuri.task.composite.RepeatTask;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class KarakuriScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 1280;
    private static final int PANEL_MAX_HEIGHT = 640;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_HEIGHT = 24;
    private static final int MAX_VISIBLE_STEPS = 12;

    private final Screen parent;

    private List<Scenario> scenarios;
    private int selectedScenarioIndex;

    private ExecutionMode executionMode =
        ExecutionMode.ONCE;

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
        scenarios = ScenarioLibrary.getScenarios();
    }

    @Override
    protected void init() {
        compactLayout =
            width < 560 || height < 320;

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

        boolean hasScenario =
            !scenarios.isEmpty();

        int contentX = panelX + CONTENT_MARGIN;
        int contentWidth =
            panelWidth - CONTENT_MARGIN * 2;

        int managementY;
        int executionY;

        if (hasScenario) {
            managementY = panelY
                + panelHeight
                - 62;

            executionY = panelY
                + panelHeight
                - 32;
        } else {
            managementY = panelY
                + panelHeight
                - 32;

            executionY = managementY;
        }

        int headerHeight = compactLayout
            ? 42
            : 54;

        cardX = contentX;
        cardY = panelY + headerHeight;

        cardWidth = contentWidth;
        cardHeight = managementY
            - cardY
            - (
                compactLayout
                    ? 8
                    : 20
            );

        int reloadWidth = compactLayout
            ? 60
            : 78;

        reloadButton = new KarakuriButton(
            font,
            panelX
                + panelWidth
                - CONTENT_MARGIN
                - reloadWidth,
            panelY + 10,
            reloadWidth,
            BUTTON_HEIGHT,
            Component.literal(
                compactLayout
                    ? "Reload"
                    : "Reload"
            ),
            this::reloadScenarios,
            KarakuriButton.Style.GHOST
        );

        previousButton = new KarakuriButton(
            font,
            cardX + 8,
            cardY
                + Math.max(
                    4,
                    (cardHeight - 44) / 2
                ),
            28,
            44,
            Component.literal("<"),
            () -> selectScenario(-1),
            KarakuriButton.Style.GHOST
        );

        nextButton = new KarakuriButton(
            font,
            cardX + cardWidth - 36,
            cardY
                + Math.max(
                    4,
                    (cardHeight - 44) / 2
                ),
            28,
            44,
            Component.literal(">"),
            () -> selectScenario(1),
            KarakuriButton.Style.GHOST
        );

        int scenarioButtonWidth = hasScenario
            ? (
                contentWidth - BUTTON_GAP * 2
            ) / 3
            : contentWidth;

        newButton = new KarakuriButton(
            font,
            contentX,
            managementY,
            scenarioButtonWidth,
            BUTTON_HEIGHT,
            Component.literal(
                compactLayout
                    ? "New"
                    : "New Scenario"
            ),
            this::openNewEditor,
            KarakuriButton.Style.PRIMARY
        );

        editButton = new KarakuriButton(
            font,
            contentX
                + scenarioButtonWidth
                + BUTTON_GAP,
            managementY,
            scenarioButtonWidth,
            BUTTON_HEIGHT,
            Component.literal(
                compactLayout
                    ? "Edit"
                    : "Edit Scenario"
            ),
            this::openSelectedEditor,
            KarakuriButton.Style.SECONDARY
        );

        deleteButton = new KarakuriButton(
            font,
            contentX
                + (
                    scenarioButtonWidth
                        + BUTTON_GAP
                ) * 2,
            managementY,
            scenarioButtonWidth,
            BUTTON_HEIGHT,
            Component.literal(
                compactLayout
                    ? "Delete"
                    : "Delete Scenario"
            ),
            this::openDeleteConfirmation,
            KarakuriButton.Style.DANGER
        );

        int modeWidth = compactLayout
            ? Math.min(92, contentWidth / 4)
            : Math.min(148, contentWidth / 3);

        int executionButtonWidth = (
            contentWidth
                - modeWidth
                - BUTTON_GAP * 3
        ) / 3;

        modeButton = new KarakuriButton(
            font,
            contentX,
            executionY,
            modeWidth,
            BUTTON_HEIGHT,
            Component.empty(),
            this::cycleExecutionMode,
            KarakuriButton.Style.SECONDARY
        );

        startButton = new KarakuriButton(
            font,
            contentX
                + modeWidth
                + BUTTON_GAP,
            executionY,
            executionButtonWidth,
            BUTTON_HEIGHT,
            Component.literal("Start"),
            this::startOrResume,
            KarakuriButton.Style.SUCCESS
        );

        pauseButton = new KarakuriButton(
            font,
            contentX
                + modeWidth
                + executionButtonWidth
                + BUTTON_GAP * 2,
            executionY,
            executionButtonWidth,
            BUTTON_HEIGHT,
            Component.literal("Pause"),
            () -> TaskManager.pause(minecraft),
            KarakuriButton.Style.SECONDARY
        );

        stopButton = new KarakuriButton(
            font,
            contentX
                + modeWidth
                + executionButtonWidth * 2
                + BUTTON_GAP * 3,
            executionY,
            executionButtonWidth,
            BUTTON_HEIGHT,
            Component.literal("Stop"),
            () -> TaskManager.stop(minecraft),
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

        renderScenarioCard(
            graphics,
            scenario
        );

        if (
            scenario != null
                && !compactLayout
        ) {
            int managementY =
                newButton.getY();

            int executionY =
                modeButton.getY();

            graphics.drawString(
                font,
                Component.literal(
                    "Scenario Management"
                ),
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

            Component statusText =
                Component.literal(
                    "Status: " + status.label()
                );

            graphics.drawString(
                font,
                statusText,
                panelX
                    + panelWidth
                    - CONTENT_MARGIN
                    - font.width(statusText),
                executionY - 14,
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
                    .equals(selectedScenarioName)
            ) {
                selectedScenarioIndex = index;
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
        Scenario scenario
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
                Component.literal("No scenarios");

            Component emptyDescription =
                Component.literal(
                    compactLayout
                        ? "Create a workflow"
                        : "Create a workflow to begin."
                );

            int centerY = cardY
                + cardHeight / 2;

            graphics.drawString(
                font,
                emptyTitle,
                cardX
                    + (
                        cardWidth
                            - font.width(emptyTitle)
                    ) / 2,
                centerY - 12,
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
                centerY + 8,
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
            Component.literal(scenario.name()),
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
                - font.width(scenarioPosition),
            cardY + 14,
            0xFF8F8499,
            false
        );

        int availableStepHeight =
            cardHeight - 50;

        int maxVisibleByHeight =
            Math.max(
                1,
                availableStepHeight / 16
            );

        int visibleStepCount =
            Math.min(
                scenario.steps().size(),
                Math.min(
                    MAX_VISIBLE_STEPS,
                    maxVisibleByHeight
                )
            );

        for (
            int index = 0;
            index < visibleStepCount;
            index++
        ) {
            ScenarioStep step =
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
                > visibleStepCount
        ) {
            int remainingSteps =
                scenario.steps().size()
                    - visibleStepCount;

            int moreY = Math.min(
                cardY + cardHeight - 16,
                cardY
                    + 38
                    + visibleStepCount * 16
            );

            graphics.drawString(
                font,
                Component.literal(
                    "+ "
                        + remainingSteps
                        + " more"
                ),
                cardX + 46,
                moreY,
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
                () -> ScenarioTaskFactory.create(
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
                selectedScenarioIndex + offset,
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
                        .equals(selectedScenarioName)
                ) {
                    selectedScenarioIndex = index;
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
                compactLayout
                    ? executionMode.label()
                    : "Mode: "
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
                && status != TaskStatus.RUNNING;

        pauseButton.active =
            hasScenario
                && status == TaskStatus.RUNNING;

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