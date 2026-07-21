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
    private static final int PANEL_MAX_WIDTH = 980;
    private static final int PANEL_MAX_HEIGHT = 480;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 10;
    private static final int PANEL_GAP = 8;
    private static final int HEADER_HEIGHT = 52;
    private static final int FOOTER_HEIGHT = 40;
    private static final int BUTTON_HEIGHT = 22;
    private static final int BUTTON_GAP = 6;

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

    private int libraryX;
    private int libraryY;
    private int libraryWidth;
    private int libraryHeight;

    private int inspectorX;
    private int inspectorY;
    private int inspectorWidth;
    private int inspectorHeight;

    private int nameFrameX;
    private int nameFrameY;
    private int nameFrameWidth;

    private int durationFrameX;
    private int durationFrameY;
    private int durationFrameWidth;

    private ScenarioActionLibrary actionLibrary;
    private ScenarioWorkflowCanvas workflowCanvas;

    private EditBox nameField;
    private EditBox durationField;

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
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();
        int panelHeight = getPanelHeight();

        int contentWidth = panelWidth - CONTENT_MARGIN * 2;
        int bodyY = panelY + HEADER_HEIGHT;
        int footerY = panelY + panelHeight - FOOTER_HEIGHT;
        int bodyHeight = footerY - bodyY - PANEL_GAP;

        libraryWidth = Math.clamp(
            contentWidth * 18 / 100,
            150,
            178
        );

        inspectorWidth = Math.clamp(
            contentWidth * 24 / 100,
            205,
            236
        );

        int canvasWidth = contentWidth
            - libraryWidth
            - inspectorWidth
            - PANEL_GAP * 2;

        libraryX = panelX + CONTENT_MARGIN;
        libraryY = bodyY;
        libraryHeight = bodyHeight;

        int canvasX = libraryX + libraryWidth + PANEL_GAP;

        inspectorX = canvasX + canvasWidth + PANEL_GAP;
        inspectorY = bodyY;
        inspectorHeight = bodyHeight;

        createNameField(panelX, panelY, panelWidth);

        actionLibrary = new ScenarioActionLibrary(
            font,
            libraryX,
            libraryY,
            libraryWidth,
            libraryHeight,
            this::insertMoveStep,
            this::insertWaitStep
        );

        for (
            KarakuriButton widget :
            actionLibrary.widgets()
        ) {
            addRenderableWidget(widget);
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
        createFooterWidgets(panelX, panelY, panelWidth, panelHeight);

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

        graphics.drawString(
            font,
            title,
            panelX + 16,
            panelY + 20,
            0xFFF5F1F8,
            false
        );

        renderNameField(graphics);
        actionLibrary.render(graphics);

        workflowCanvas.render(
            graphics,
            mouseX,
            mouseY,
            Component.literal(
                "Drag blocks to reorder  |  Scroll block to adjust duration"
            ),
            0xFF81798E
        );

        renderInspector(graphics);
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

        return workflowCanvas.mouseClicked(event);
    }

    @Override
    public boolean mouseDragged(
        MouseButtonEvent event,
        double offsetX,
        double offsetY
    ) {
        if (workflowCanvas.mouseDragged(event)) {
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

    private void createNameField(
        int panelX,
        int panelY,
        int panelWidth
    ) {
        nameFrameX = panelX + 142;
        nameFrameY = panelY + 13;
        nameFrameWidth = panelWidth
            - 142
            - CONTENT_MARGIN;

        nameField = new EditBox(
            font,
            nameFrameX + 7,
            nameFrameY + 4,
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

    private void createInspectorWidgets() {
        int contentX = inspectorX + 10;
        int contentWidth = inspectorWidth - 20;
        int halfWidth =
            (contentWidth - BUTTON_GAP) / 2;

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

        durationField = new EditBox(
            font,
            durationFrameX + 6,
            durationFrameY + 3,
            durationFrameWidth - 12,
            16,
            Component.literal("Duration in seconds")
        );

        durationField.setBordered(false);
        durationField.setTextColor(0xFFF4F0F7);
        durationField.setTextColorUneditable(0xFF81798E);
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

    private void createFooterWidgets(
        int panelX,
        int panelY,
        int panelWidth,
        int panelHeight
    ) {
        int buttonWidth = 126;
        int buttonY = panelY
            + panelHeight
            - FOOTER_HEIGHT
            + 8;

        saveButton = createButton(
            panelX
                + panelWidth
                - CONTENT_MARGIN
                - buttonWidth * 2
                - BUTTON_GAP,
            buttonY,
            buttonWidth,
            Component.literal("Save Scenario"),
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

    private void renderNameField(
        GuiGraphics graphics
    ) {
        boolean valid = isScenarioNameValid();

        graphics.fill(
            nameFrameX,
            nameFrameY,
            nameFrameX + nameFrameWidth,
            nameFrameY + 24,
            0xFF100E16
        );

        graphics.renderOutline(
            nameFrameX,
            nameFrameY,
            nameFrameWidth,
            24,
            valid
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
            inspectorY + 10,
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
            inspectorY + 10,
            0xFF81778A,
            false
        );

        graphics.drawString(
            font,
            Component.literal(getStepTitle(step)),
            inspectorX + 10,
            inspectorY + 34,
            0xFFF4F0F7,
            false
        );

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

        graphics.drawString(
            font,
            Component.literal("Step actions"),
            inspectorX + 10,
            inspectorY + 194,
            0xFF918699,
            false
        );

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

    private void renderFooter(
        GuiGraphics graphics
    ) {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();
        int panelHeight = getPanelHeight();
        int footerY = panelY + panelHeight - FOOTER_HEIGHT;

        graphics.fill(
            panelX + CONTENT_MARGIN,
            footerY,
            panelX + panelWidth - CONTENT_MARGIN,
            footerY + 1,
            0xFF332D3A
        );

        String validationMessage =
            getValidationMessage();

        graphics.drawString(
            font,
            Component.literal(
                validationMessage == null
                    ? "Scenario will be stored as a .karakuri file"
                    : validationMessage
            ),
            panelX + CONTENT_MARGIN,
            footerY + 15,
            validationMessage == null
                ? 0xFF81798E
                : 0xFFE66777,
            false
        );
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

        if (!(step instanceof Scenario.MoveStep moveStep)) {
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

        Scenario.Step step = getSelectedStep();
        boolean movement =
            step instanceof Scenario.MoveStep;

        forwardDirectionButton.visible = movement;
        backwardDirectionButton.visible = movement;
        leftDirectionButton.visible = movement;
        rightDirectionButton.visible = movement;

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
        return (width - getPanelWidth()) / 2;
    }

    private int getPanelY() {
        return (height - getPanelHeight()) / 2;
    }
}