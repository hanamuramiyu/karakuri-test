package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.Scenario;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ScenarioEditorScreen extends Screen {
    private static final int PANEL_HEIGHT = 340;
    private static final int PANEL_MAX_WIDTH = 420;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DURATION_STEP_TICKS = 10;
    private static final int MIN_DURATION_TICKS = 10;
    private static final int MAX_DURATION_TICKS = 72000;
    private static final int DEFAULT_MOVE_DURATION_TICKS = 40;
    private static final int DEFAULT_WAIT_DURATION_TICKS = 20;

    private final KarakuriScreen parent;
    private final int scenarioIndex;
    private final List<Scenario.Step> steps;

    private final String initialName;

    private int selectedStepIndex;

    private EditBox nameField;
    private Button previousStepButton;
    private Button nextStepButton;
    private Button addMoveButton;
    private Button addWaitButton;
    private Button deleteButton;
    private Button directionButton;
    private Button durationDecreaseButton;
    private Button durationDisplayButton;
    private Button durationIncreaseButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button saveButton;

    public ScenarioEditorScreen(
        KarakuriScreen parent,
        int scenarioIndex,
        Scenario scenario
    ) {
        super(
            Component.literal(
                scenario == null ? "New Scenario" : "Edit Scenario"
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
        int panelX = getPanelX();
        int panelY = getPanelY();
        int contentWidth = getPanelWidth() - CONTENT_MARGIN * 2;
        int halfButtonWidth = (contentWidth - BUTTON_GAP) / 2;
        int thirdButtonWidth = (contentWidth - BUTTON_GAP * 2) / 3;

        nameField = new EditBox(
            font,
            panelX + CONTENT_MARGIN,
            panelY + 58,
            contentWidth,
            BUTTON_HEIGHT,
            Component.literal("Scenario name")
        );
        nameField.setMaxLength(64);
        nameField.setValue(initialName);
        nameField.setResponder(value -> updateButtons());

        previousStepButton = Button.builder(
            Component.literal("Previous Step"),
            button -> selectStep(-1)
        ).bounds(
            panelX + CONTENT_MARGIN,
            panelY + 128,
            halfButtonWidth,
            BUTTON_HEIGHT
        ).build();

        nextStepButton = Button.builder(
            Component.literal("Next Step"),
            button -> selectStep(1)
        ).bounds(
            panelX + CONTENT_MARGIN + halfButtonWidth + BUTTON_GAP,
            panelY + 128,
            halfButtonWidth,
            BUTTON_HEIGHT
        ).build();

        addMoveButton = Button.builder(
            Component.literal("Add Move"),
            button -> addMoveStep()
        ).bounds(
            panelX + CONTENT_MARGIN,
            panelY + 156,
            thirdButtonWidth,
            BUTTON_HEIGHT
        ).build();

        addWaitButton = Button.builder(
            Component.literal("Add Wait"),
            button -> addWaitStep()
        ).bounds(
            panelX + CONTENT_MARGIN + thirdButtonWidth + BUTTON_GAP,
            panelY + 156,
            thirdButtonWidth,
            BUTTON_HEIGHT
        ).build();

        deleteButton = Button.builder(
            Component.literal("Delete"),
            button -> deleteSelectedStep()
        ).bounds(
            panelX + CONTENT_MARGIN
                + (thirdButtonWidth + BUTTON_GAP) * 2,
            panelY + 156,
            thirdButtonWidth,
            BUTTON_HEIGHT
        ).build();

        directionButton = Button.builder(
            Component.empty(),
            button -> cycleDirection()
        ).bounds(
            panelX + CONTENT_MARGIN,
            panelY + 184,
            contentWidth,
            BUTTON_HEIGHT
        ).build();

        durationDecreaseButton = Button.builder(
            Component.literal("- 0.5s"),
            button -> changeDuration(-DURATION_STEP_TICKS)
        ).bounds(
            panelX + CONTENT_MARGIN,
            panelY + 212,
            thirdButtonWidth,
            BUTTON_HEIGHT
        ).build();

        durationDisplayButton = Button.builder(
            Component.empty(),
            button -> {
            }
        ).bounds(
            panelX + CONTENT_MARGIN + thirdButtonWidth + BUTTON_GAP,
            panelY + 212,
            thirdButtonWidth,
            BUTTON_HEIGHT
        ).build();
        durationDisplayButton.active = false;

        durationIncreaseButton = Button.builder(
            Component.literal("+ 0.5s"),
            button -> changeDuration(DURATION_STEP_TICKS)
        ).bounds(
            panelX + CONTENT_MARGIN
                + (thirdButtonWidth + BUTTON_GAP) * 2,
            panelY + 212,
            thirdButtonWidth,
            BUTTON_HEIGHT
        ).build();

        moveUpButton = Button.builder(
            Component.literal("Move Up"),
            button -> moveSelectedStep(-1)
        ).bounds(
            panelX + CONTENT_MARGIN,
            panelY + 240,
            halfButtonWidth,
            BUTTON_HEIGHT
        ).build();

        moveDownButton = Button.builder(
            Component.literal("Move Down"),
            button -> moveSelectedStep(1)
        ).bounds(
            panelX + CONTENT_MARGIN + halfButtonWidth + BUTTON_GAP,
            panelY + 240,
            halfButtonWidth,
            BUTTON_HEIGHT
        ).build();

        saveButton = Button.builder(
            Component.literal("Save"),
            button -> saveScenario()
        ).bounds(
            panelX + CONTENT_MARGIN,
            panelY + 304,
            halfButtonWidth,
            BUTTON_HEIGHT
        ).build();

        Button cancelButton = Button.builder(
            Component.literal("Cancel"),
            button -> minecraft.setScreen(parent)
        ).bounds(
            panelX + CONTENT_MARGIN + halfButtonWidth + BUTTON_GAP,
            panelY + 304,
            halfButtonWidth,
            BUTTON_HEIGHT
        ).build();

        addRenderableWidget(nameField);
        addRenderableWidget(previousStepButton);
        addRenderableWidget(nextStepButton);
        addRenderableWidget(addMoveButton);
        addRenderableWidget(addWaitButton);
        addRenderableWidget(deleteButton);
        addRenderableWidget(directionButton);
        addRenderableWidget(durationDecreaseButton);
        addRenderableWidget(durationDisplayButton);
        addRenderableWidget(durationIncreaseButton);
        addRenderableWidget(moveUpButton);
        addRenderableWidget(moveDownButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(cancelButton);

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
        Scenario.Step selectedStep = getSelectedStep();

        graphics.fill(0, 0, width, height, 0xC0101018);
        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + PANEL_HEIGHT,
            0xF01A1A24
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            PANEL_HEIGHT,
            0xFF626278
        );

        drawCenteredText(
            graphics,
            title,
            panelY + 18,
            0xFFF4F4F7
        );

        graphics.drawString(
            font,
            Component.literal("Scenario name"),
            panelX + CONTENT_MARGIN,
            panelY + 44,
            0xFF9999AA,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Steps"),
            panelX + CONTENT_MARGIN,
            panelY + 88,
            0xFF9999AA,
            false
        );

        Component stepPosition = Component.literal(
            (selectedStepIndex + 1) + " of " + steps.size()
        );

        graphics.drawString(
            font,
            stepPosition,
            panelX + panelWidth
                - CONTENT_MARGIN
                - font.width(stepPosition),
            panelY + 88,
            0xFF9999AA,
            false
        );

        graphics.drawString(
            font,
            Component.literal(selectedStep.label()),
            panelX + CONTENT_MARGIN,
            panelY + 106,
            0xFFF4F4F7,
            false
        );

        String validationMessage = getValidationMessage();

        if (validationMessage != null) {
            graphics.drawString(
                font,
                Component.literal(validationMessage),
                panelX + CONTENT_MARGIN,
                panelY + 278,
                0xFFFF7777,
                false
            );
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

    private void selectStep(int offset) {
        selectedStepIndex = Math.floorMod(
            selectedStepIndex + offset,
            steps.size()
        );

        updateButtons();
    }

    private void addMoveStep() {
        steps.add(
            new Scenario.MoveStep(
                Scenario.MoveDirection.FORWARD,
                DEFAULT_MOVE_DURATION_TICKS
            )
        );

        selectedStepIndex = steps.size() - 1;
        updateButtons();
    }

    private void addWaitStep() {
        steps.add(
            new Scenario.WaitStep(DEFAULT_WAIT_DURATION_TICKS)
        );

        selectedStepIndex = steps.size() - 1;
        updateButtons();
    }

    private void deleteSelectedStep() {
        if (steps.size() <= 1) {
            return;
        }

        steps.remove(selectedStepIndex);

        if (selectedStepIndex >= steps.size()) {
            selectedStepIndex = steps.size() - 1;
        }

        updateButtons();
    }

    private void cycleDirection() {
        Scenario.Step selectedStep = getSelectedStep();

        if (!(selectedStep instanceof Scenario.MoveStep moveStep)) {
            return;
        }

        steps.set(
            selectedStepIndex,
            new Scenario.MoveStep(
                moveStep.direction().next(),
                moveStep.durationTicks()
            )
        );

        updateButtons();
    }

    private void changeDuration(int offsetTicks) {
        Scenario.Step selectedStep = getSelectedStep();

        int durationTicks = Math.clamp(
            selectedStep.durationTicks() + offsetTicks,
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );

        steps.set(
            selectedStepIndex,
            switch (selectedStep) {
                case Scenario.MoveStep moveStep ->
                    new Scenario.MoveStep(
                        moveStep.direction(),
                        durationTicks
                    );
                case Scenario.WaitStep waitStep ->
                    new Scenario.WaitStep(durationTicks);
            }
        );

        updateButtons();
    }

    private void moveSelectedStep(int offset) {
        int destinationIndex = selectedStepIndex + offset;

        if (
            destinationIndex < 0
                || destinationIndex >= steps.size()
        ) {
            return;
        }

        Collections.swap(
            steps,
            selectedStepIndex,
            destinationIndex
        );

        selectedStepIndex = destinationIndex;
        updateButtons();
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
            ScenarioLibrary.replace(scenarioIndex, scenario);
        }

        parent.refreshScenarios(scenario.name());
        minecraft.setScreen(parent);
    }

    private Scenario.Step getSelectedStep() {
        if (selectedStepIndex >= steps.size()) {
            selectedStepIndex = steps.size() - 1;
        }

        return steps.get(selectedStepIndex);
    }

    private String getValidationMessage() {
        String scenarioName = nameField == null
            ? initialName
            : nameField.getValue().trim();

        if (scenarioName.isBlank()) {
            return "Scenario name is required";
        }

        if (
            ScenarioLibrary.containsName(
                scenarioName,
                scenarioIndex
            )
        ) {
            return "A scenario with this name already exists";
        }

        if (steps.isEmpty()) {
            return "At least one step is required";
        }

        return null;
    }

    private void updateButtons() {
        if (
            previousStepButton == null
                || nextStepButton == null
                || deleteButton == null
                || directionButton == null
                || durationDecreaseButton == null
                || durationDisplayButton == null
                || durationIncreaseButton == null
                || moveUpButton == null
                || moveDownButton == null
                || saveButton == null
        ) {
            return;
        }

        Scenario.Step selectedStep = getSelectedStep();
        boolean multipleSteps = steps.size() > 1;

        previousStepButton.active = multipleSteps;
        nextStepButton.active = multipleSteps;
        deleteButton.active = multipleSteps;

        if (selectedStep instanceof Scenario.MoveStep moveStep) {
            directionButton.setMessage(
                Component.literal(
                    "Direction: " + moveStep.direction().label()
                )
            );
            directionButton.active = true;
        } else {
            directionButton.setMessage(
                Component.literal("Direction: Not applicable")
            );
            directionButton.active = false;
        }

        durationDisplayButton.setMessage(
            Component.literal(
                "Duration: "
                    + Scenario.formatDuration(
                        selectedStep.durationTicks()
                    )
            )
        );

        durationDecreaseButton.active =
            selectedStep.durationTicks() > MIN_DURATION_TICKS;

        durationIncreaseButton.active =
            selectedStep.durationTicks() < MAX_DURATION_TICKS;

        moveUpButton.active = selectedStepIndex > 0;
        moveDownButton.active =
            selectedStepIndex < steps.size() - 1;

        saveButton.active = getValidationMessage() == null;
    }

    private int getPanelWidth() {
        return Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int getPanelX() {
        return (width - getPanelWidth()) / 2;
    }

    private int getPanelY() {
        return (height - PANEL_HEIGHT) / 2;
    }

    private void drawCenteredText(
        GuiGraphics graphics,
        Component text,
        int y,
        int color
    ) {
        int x = (width - font.width(text)) / 2;
        graphics.drawString(
            font,
            text,
            x,
            y,
            color,
            false
        );
    }
}