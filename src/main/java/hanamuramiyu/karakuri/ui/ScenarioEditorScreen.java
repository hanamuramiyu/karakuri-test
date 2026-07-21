package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.Scenario;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ScenarioEditorScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 720;
    private static final int PANEL_MAX_HEIGHT = 360;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 12;
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_HEIGHT = 20;
    private static final int DURATION_STEP_TICKS = 10;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS = 72000;
    private static final int DEFAULT_MOVE_DURATION_TICKS = 40;
    private static final int DEFAULT_WAIT_DURATION_TICKS = 20;

    private final KarakuriScreen parent;
    private final int scenarioIndex;
    private final List<Scenario.Step> steps;
    private final String initialName;

    private int selectedStepIndex;
    private boolean syncingDurationField;
    private boolean durationFieldValid = true;

    private ScenarioWorkflowCanvas workflowCanvas;
    private EditBox nameField;
    private EditBox durationField;
    private Button directionButton;
    private Button durationDecreaseButton;
    private Button durationIncreaseButton;
    private Button deleteButton;
    private Button saveButton;

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
            steps = new ArrayList<>(
                scenario.steps()
            );
        }
    }

    @Override
    protected void init() {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int contentX = panelX
            + CONTENT_MARGIN;
        int contentWidth = getPanelWidth()
            - CONTENT_MARGIN * 2;
        int firstRowY = getFirstControlRowY();
        int secondRowY = getSecondControlRowY();
        int thirdRowY = getThirdControlRowY();
        int nameLabelWidth = font.width(
            "Scenario"
        ) + 10;

        workflowCanvas =
            new ScenarioWorkflowCanvas(
                font,
                steps,
                this::onCanvasSelectionChanged,
                this::onCanvasContentChanged
            );

        workflowCanvas.setBounds(
            getCanvasX(),
            getCanvasY(),
            getCanvasWidth(),
            getCanvasHeight()
        );
        workflowCanvas.setSelectedIndex(
            selectedStepIndex
        );

        nameField = new EditBox(
            font,
            contentX + nameLabelWidth,
            panelY + 24,
            contentWidth - nameLabelWidth,
            BUTTON_HEIGHT,
            Component.literal(
                "Scenario name"
            )
        );
        nameField.setMaxLength(64);
        nameField.setValue(initialName);
        nameField.setResponder(
            value -> updateButtons()
        );

        int directionWidth = Math.max(
            110,
            contentWidth * 40 / 100
        );
        int fixedButtonWidth = 58;
        int durationWidth = contentWidth
            - directionWidth
            - fixedButtonWidth * 2
            - BUTTON_GAP * 3;

        directionButton = Button.builder(
            Component.empty(),
            button -> cycleDirection()
        ).bounds(
            contentX,
            firstRowY,
            directionWidth,
            BUTTON_HEIGHT
        ).build();

        durationDecreaseButton =
            Button.builder(
                Component.literal("- 0.5s"),
                button -> changeDuration(
                    -DURATION_STEP_TICKS
                )
            ).bounds(
                contentX
                    + directionWidth
                    + BUTTON_GAP,
                firstRowY,
                fixedButtonWidth,
                BUTTON_HEIGHT
            ).build();

        durationField = new EditBox(
            font,
            contentX
                + directionWidth
                + fixedButtonWidth
                + BUTTON_GAP * 2,
            firstRowY,
            durationWidth,
            BUTTON_HEIGHT,
            Component.literal(
                "Duration in seconds"
            )
        );
        durationField.setMaxLength(7);
        durationField.setFilter(
            value -> value.matches(
                "[0-9]{0,4}(\\.[0-9]{0,2})?"
            )
        );
        durationField.setResponder(
            this::onDurationFieldChanged
        );

        durationIncreaseButton =
            Button.builder(
                Component.literal("+ 0.5s"),
                button -> changeDuration(
                    DURATION_STEP_TICKS
                )
            ).bounds(
                contentX
                    + directionWidth
                    + fixedButtonWidth
                    + durationWidth
                    + BUTTON_GAP * 3,
                firstRowY,
                fixedButtonWidth,
                BUTTON_HEIGHT
            ).build();

        int thirdWidth = (
            contentWidth
                - BUTTON_GAP * 2
        ) / 3;

        Button addMoveButton =
            Button.builder(
                Component.literal("Add Move"),
                button -> addMoveStep()
            ).bounds(
                contentX,
                secondRowY,
                thirdWidth,
                BUTTON_HEIGHT
            ).build();

        Button addWaitButton =
            Button.builder(
                Component.literal("Add Wait"),
                button -> addWaitStep()
            ).bounds(
                contentX
                    + thirdWidth
                    + BUTTON_GAP,
                secondRowY,
                thirdWidth,
                BUTTON_HEIGHT
            ).build();

        deleteButton = Button.builder(
            Component.literal("Delete Step"),
            button -> deleteSelectedStep()
        ).bounds(
            contentX
                + (
                    thirdWidth
                        + BUTTON_GAP
                ) * 2,
            secondRowY,
            thirdWidth,
            BUTTON_HEIGHT
        ).build();

        int halfWidth = (
            contentWidth - BUTTON_GAP
        ) / 2;

        saveButton = Button.builder(
            Component.literal("Save Scenario"),
            button -> saveScenario()
        ).bounds(
            contentX,
            thirdRowY,
            halfWidth,
            BUTTON_HEIGHT
        ).build();

        Button cancelButton =
            Button.builder(
                Component.literal("Cancel"),
                button -> minecraft.setScreen(
                    parent
                )
            ).bounds(
                contentX
                    + halfWidth
                    + BUTTON_GAP,
                thirdRowY,
                halfWidth,
                BUTTON_HEIGHT
            ).build();

        addRenderableWidget(nameField);
        addRenderableWidget(directionButton);
        addRenderableWidget(
            durationDecreaseButton
        );
        addRenderableWidget(durationField);
        addRenderableWidget(
            durationIncreaseButton
        );
        addRenderableWidget(addMoveButton);
        addRenderableWidget(addWaitButton);
        addRenderableWidget(deleteButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(cancelButton);

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
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();
        int panelHeight = getPanelHeight();

        graphics.fill(
            0,
            0,
            width,
            height,
            0xC0101018
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + panelHeight,
            0xF0181622
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            0xFF6F5A91
        );

        drawCenteredText(
            graphics,
            title,
            panelY + 8,
            0xFFF4F4F7
        );

        graphics.drawString(
            font,
            Component.literal("Scenario"),
            panelX + CONTENT_MARGIN,
            panelY + 30,
            0xFFAAA1B8,
            false
        );

        String validationMessage =
            getValidationMessage();

        workflowCanvas.render(
            graphics,
            mouseX,
            mouseY,
            Component.literal(
                validationMessage == null
                    ? "Drag to reorder | Scroll card: duration"
                    : validationMessage
            ),
            validationMessage == null
                ? 0xFF81798E
                : 0xFFFF7777
        );

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
        if (
            super.mouseClicked(
                event,
                doubled
            )
        ) {
            return true;
        }

        return workflowCanvas.mouseClicked(
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
            workflowCanvas.mouseDragged(
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
            workflowCanvas.mouseReleased();

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
            workflowCanvas.mouseScrolled(
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

    private void addMoveStep() {
        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.MoveStep(
                Scenario.MoveDirection.FORWARD,
                DEFAULT_MOVE_DURATION_TICKS
            )
        );

        selectStep(insertIndex);
    }

    private void addWaitStep() {
        int insertIndex =
            selectedStepIndex + 1;

        steps.add(
            insertIndex,
            new Scenario.WaitStep(
                DEFAULT_WAIT_DURATION_TICKS
            )
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

    private void cycleDirection() {
        Scenario.Step step =
            getSelectedStep();

        if (
            !(
                step
                    instanceof
                    Scenario.MoveStep moveStep
            )
        ) {
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

    private void changeDuration(
        int offsetTicks
    ) {
        Scenario.Step step =
            getSelectedStep();

        int durationTicks = Math.clamp(
            step.durationTicks()
                + offsetTicks,
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );

        replaceSelectedDuration(
            durationTicks
        );
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
            double seconds =
                Double.parseDouble(value);

            int durationTicks =
                (int) Math.round(
                    seconds * 20.0
                );

            if (
                !Double.isFinite(seconds)
                    || durationTicks
                    < MIN_DURATION_TICKS
                    || durationTicks
                    > MAX_DURATION_TICKS
            ) {
                durationFieldValid = false;
                updateButtons();
                return;
            }

            replaceSelectedDuration(
                durationTicks
            );
            durationFieldValid = true;
        } catch (
            NumberFormatException exception
        ) {
            durationFieldValid = false;
        }

        updateButtons();
    }

    private void replaceSelectedDuration(
        int durationTicks
    ) {
        Scenario.Step step =
            getSelectedStep();

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
                getSelectedStep()
                    .durationTicks()
            )
        );

        syncingDurationField = false;
        durationFieldValid = true;
    }

    private void saveScenario() {
        if (
            getValidationMessage() != null
        ) {
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

        parent.refreshScenarios(
            scenario.name()
        );

        minecraft.setScreen(parent);
    }

    private Scenario.Step getSelectedStep() {
        selectedStepIndex = Math.clamp(
            selectedStepIndex,
            0,
            steps.size() - 1
        );

        return steps.get(
            selectedStepIndex
        );
    }

    private String getValidationMessage() {
        String name = nameField == null
            ? initialName
            : nameField
                .getValue()
                .trim();

        if (name.isBlank()) {
            return "Scenario name is required";
        }

        if (
            ScenarioLibrary.containsName(
                name,
                scenarioIndex
            )
        ) {
            return "A scenario with this name already exists";
        }

        if (!durationFieldValid) {
            return "Duration must be between 0.05 and 3600 seconds";
        }

        return null;
    }

    private void updateButtons() {
        if (
            directionButton == null
                || durationDecreaseButton == null
                || durationIncreaseButton == null
                || deleteButton == null
                || saveButton == null
        ) {
            return;
        }

        Scenario.Step step =
            getSelectedStep();

        if (
            step
                instanceof
                Scenario.MoveStep moveStep
        ) {
            directionButton.setMessage(
                Component.literal(
                    "Direction: "
                        + moveStep
                        .direction()
                        .label()
                )
            );
            directionButton.active = true;
        } else {
            directionButton.setMessage(
                Component.literal(
                    "Step: Wait"
                )
            );
            directionButton.active = false;
        }

        durationDecreaseButton.active =
            step.durationTicks()
                > MIN_DURATION_TICKS;

        durationIncreaseButton.active =
            step.durationTicks()
                < MAX_DURATION_TICKS;

        deleteButton.active =
            steps.size() > 1;

        saveButton.active =
            getValidationMessage() == null;
    }

    private String formatDurationForField(
        int durationTicks
    ) {
        return BigDecimal
            .valueOf(durationTicks)
            .divide(
                BigDecimal.valueOf(20)
            )
            .stripTrailingZeros()
            .toPlainString();
    }

    private int getPanelWidth() {
        return Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int getPanelHeight() {
        return Math.min(
            PANEL_MAX_HEIGHT,
            height - PANEL_MARGIN * 2
        );
    }

    private int getPanelX() {
        return (
            width - getPanelWidth()
        ) / 2;
    }

    private int getPanelY() {
        return (
            height - getPanelHeight()
        ) / 2;
    }

    private int getCanvasX() {
        return getPanelX()
            + CONTENT_MARGIN;
    }

    private int getCanvasY() {
        return getPanelY() + 50;
    }

    private int getCanvasWidth() {
        return getPanelWidth()
            - CONTENT_MARGIN * 2;
    }

    private int getCanvasHeight() {
        return Math.max(
            80,
            getFirstControlRowY()
                - getCanvasY()
                - 8
        );
    }

    private int getFirstControlRowY() {
        return getSecondControlRowY()
            - BUTTON_GAP
            - BUTTON_HEIGHT;
    }

    private int getSecondControlRowY() {
        return getThirdControlRowY()
            - BUTTON_GAP
            - BUTTON_HEIGHT;
    }

    private int getThirdControlRowY() {
        return getPanelY()
            + getPanelHeight()
            - CONTENT_MARGIN
            - BUTTON_HEIGHT;
    }

    private void drawCenteredText(
        GuiGraphics graphics,
        Component text,
        int y,
        int color
    ) {
        int x = (
            width - font.width(text)
        ) / 2;

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