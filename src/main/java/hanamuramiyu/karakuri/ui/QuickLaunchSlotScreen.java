package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.quicklaunch.QuickLaunchRegistry;
import hanamuramiyu.karakuri.quicklaunch.QuickLaunchSlot;
import hanamuramiyu.karakuri.quicklaunch.QuickLaunchValidation;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.TaskChannel;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import hanamuramiyu.karakuri.ui.widget.KarakuriCheckboxRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class QuickLaunchSlotScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 760;
    private static final int PANEL_MAX_HEIGHT = 560;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 84;
    private static final int FOOTER_HEIGHT = 44;
    private static final int ROW_HEIGHT = 38;
    private static final int CHECKBOX_SIZE = 14;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 7;
    private static final int SCROLL_STEP = 3;

    private final QuickLaunchScreen parent;
    private final int slotNumber;
    private final Set<String> selectedIds =
        new LinkedHashSet<>();

    private List<Scenario> scenarios = List.of();
    private int missingAssignments;
    private int scrollOffset;

    private KarakuriButton selectAllButton;
    private KarakuriButton clearButton;
    private KarakuriButton clearSlotButton;
    private KarakuriButton saveButton;

    public QuickLaunchSlotScreen(
        QuickLaunchScreen parent,
        int slotNumber
    ) {
        super(
            Component.literal(
                "Configure Quick Slot "
                    + slotNumber
            )
        );

        this.parent = parent;
        this.slotNumber = slotNumber;
    }

    @Override
    protected void init() {
        scenarios = ScenarioLibrary.getScenarios();

        QuickLaunchSlot slot =
            QuickLaunchRegistry.slot(
                slotNumber
            );

        selectedIds.clear();

        for (String scenarioId : slot.scenarioIds()) {
            if (
                ScenarioLibrary.findById(
                    scenarioId
                ) != null
            ) {
                selectedIds.add(scenarioId);
            }
        }

        missingAssignments =
            QuickLaunchRegistry
                .missingScenarioIds(slot)
                .size();

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int contentWidth =
            panelWidth - CONTENT_MARGIN * 2;
        int footerY =
            panelY + panelHeight - FOOTER_HEIGHT;
        int footerButtonWidth =
            (contentWidth - BUTTON_GAP * 2) / 3;

        int headerButtonWidth =
            panelWidth < 470 ? 58 : 84;

        clearButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + panelWidth
                    - CONTENT_MARGIN
                    - headerButtonWidth,
                panelY + 44,
                headerButtonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    panelWidth < 470
                        ? "None"
                        : "Clear"
                ),
                this::clearSelection,
                KarakuriButton.Style.GHOST
            )
        );

        selectAllButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + panelWidth
                    - CONTENT_MARGIN
                    - headerButtonWidth * 2
                    - BUTTON_GAP,
                panelY + 44,
                headerButtonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    panelWidth < 470
                        ? "All"
                        : "Select All"
                ),
                this::selectAll,
                KarakuriButton.Style.GHOST
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                footerY + 10,
                footerButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Back"),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.SECONDARY
            )
        );

        clearSlotButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + footerButtonWidth
                    + BUTTON_GAP,
                footerY + 10,
                footerButtonWidth,
                BUTTON_HEIGHT,
                Component.literal("Clear Slot"),
                this::clearSlot,
                KarakuriButton.Style.DANGER
            )
        );

        saveButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (footerButtonWidth + BUTTON_GAP) * 2,
                footerY + 10,
                footerButtonWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::saveSlot,
                KarakuriButton.Style.PRIMARY
            )
        );

        clampScroll();
        updateButtons();
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int panelRight = panelX + panelWidth;
        int panelBottom = panelY + panelHeight;
        int listX = panelX + CONTENT_MARGIN;
        int listY = panelY + HEADER_HEIGHT;
        int listWidth =
            panelWidth - CONTENT_MARGIN * 2;
        int listHeight =
            panelHeight
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 5;

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
            panelRight,
            panelBottom,
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
            panelBottom,
            0xFF9B79D1
        );
        graphics.fill(
            panelX + 4,
            panelY,
            panelRight,
            panelY + HEADER_HEIGHT - 5,
            0xFF1C1824
        );

        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 12,
            0xFFF6F2FA,
            false
        );

        renderValidationSummary(
            graphics,
            panelX + CONTENT_MARGIN,
            panelY + 29,
            panelWidth
                - CONTENT_MARGIN * 2
                - 190
        );

        graphics.fill(
            listX,
            listY,
            listX + listWidth,
            listY + listHeight,
            0xFF100E16
        );
        graphics.renderOutline(
            listX,
            listY,
            listWidth,
            listHeight,
            0xFF393243
        );

        renderRows(
            graphics,
            mouseX,
            mouseY,
            listX,
            listY,
            listWidth,
            listHeight
        );

        graphics.fill(
            panelX + 1,
            panelBottom - FOOTER_HEIGHT,
            panelRight - 1,
            panelBottom - 1,
            0xFF1C1824
        );
        graphics.fill(
            panelX + CONTENT_MARGIN,
            panelBottom - FOOTER_HEIGHT,
            panelRight - CONTENT_MARGIN,
            panelBottom - FOOTER_HEIGHT + 1,
            0xFF393243
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
        if (super.mouseClicked(event, doubled)) {
            return true;
        }

        if (event.button() != 0) {
            return false;
        }

        int listX = panelX() + CONTENT_MARGIN;
        int listY = panelY() + HEADER_HEIGHT;
        int listWidth =
            panelWidth() - CONTENT_MARGIN * 2;
        int listHeight =
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 5;

        if (
            !contains(
                event.x(),
                event.y(),
                listX,
                listY,
                listWidth,
                listHeight
            )
        ) {
            return false;
        }

        int index = scrollOffset
            + (int) (
                (event.y() - listY)
                    / ROW_HEIGHT
            );

        if (
            index < 0
                || index >= scenarios.size()
        ) {
            return true;
        }

        String scenarioId =
            scenarios.get(index).id();

        if (!selectedIds.add(scenarioId)) {
            selectedIds.remove(scenarioId);
        }

        updateButtons();
        return true;
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        int listX = panelX() + CONTENT_MARGIN;
        int listY = panelY() + HEADER_HEIGHT;
        int listWidth =
            panelWidth() - CONTENT_MARGIN * 2;
        int listHeight =
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 5;

        if (
            !contains(
                mouseX,
                mouseY,
                listX,
                listY,
                listWidth,
                listHeight
            )
        ) {
            return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
            );
        }

        scrollOffset -=
            (int) Math.signum(verticalAmount)
                * SCROLL_STEP;

        clampScroll();
        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderValidationSummary(
        GuiGraphics graphics,
        int x,
        int y,
        int maximumWidth
    ) {
        List<Scenario> selected =
            selectedScenarios();

        QuickLaunchValidation validation =
            QuickLaunchValidation.analyze(
                selected
            );

        String message;
        int color;

        if (selected.isEmpty()) {
            message = "Select one or more scenarios";
            color = 0xFF8F8499;
        } else if (!validation.valid()) {
            QuickLaunchValidation.Conflict conflict =
                validation.conflicts().getFirst();

            String channels =
                conflict.channels()
                    .stream()
                    .map(TaskChannel::label)
                    .sorted()
                    .collect(
                        Collectors.joining(", ")
                    );

            message = "Conflict: "
                + conflict.first().name()
                + " + "
                + conflict.second().name()
                + " use "
                + channels;

            color = 0xFFE66777;
        } else {
            message = selected.size() == 1
                ? "1 scenario selected · No conflicts"
                : selected.size()
                    + " scenarios selected · No conflicts";

            color = 0xFF61D394;
        }

        if (missingAssignments > 0) {
            message += " · "
                + missingAssignments
                + " missing assignment"
                + (missingAssignments == 1
                    ? ""
                    : "s")
                + " will be removed";
        }

        graphics.drawString(
            font,
            Component.literal(
                truncate(message, maximumWidth)
            ),
            x,
            y,
            color,
            false
        );
    }

    private void renderRows(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        int listX,
        int listY,
        int listWidth,
        int listHeight
    ) {
        if (scenarios.isEmpty()) {
            graphics.drawCenteredString(
                font,
                Component.literal(
                    "No scenarios are available"
                ),
                listX + listWidth / 2,
                listY + listHeight / 2 - 4,
                0xFF8F8499
            );
            return;
        }

        graphics.enableScissor(
            listX + 1,
            listY + 1,
            listX + listWidth - 1,
            listY + listHeight - 1
        );

        int visibleRows =
            Math.max(1, listHeight / ROW_HEIGHT);

        int endIndex = Math.min(
            scenarios.size(),
            scrollOffset + visibleRows + 1
        );

        for (
            int index = scrollOffset;
            index < endIndex;
            index++
        ) {
            int rowY = listY
                + (index - scrollOffset)
                    * ROW_HEIGHT;

            renderRow(
                graphics,
                scenarios.get(index),
                rowY,
                listX,
                listWidth,
                mouseX,
                mouseY
            );
        }

        graphics.disableScissor();
    }

    private void renderRow(
        GuiGraphics graphics,
        Scenario scenario,
        int rowY,
        int listX,
        int listWidth,
        int mouseX,
        int mouseY
    ) {
        boolean selected =
            selectedIds.contains(
                scenario.id()
            );

        boolean hovered = contains(
            mouseX,
            mouseY,
            listX + 2,
            rowY,
            listWidth - 4,
            ROW_HEIGHT - 1
        );

        graphics.fill(
            listX + 2,
            rowY,
            listX + listWidth - 2,
            rowY + ROW_HEIGHT - 1,
            selected
                ? 0xFF2A2237
                : hovered
                    ? 0xFF201C29
                    : 0xFF15121B
        );

        graphics.renderOutline(
            listX + 2,
            rowY,
            listWidth - 4,
            ROW_HEIGHT - 1,
            selected
                ? 0xFF9B79D1
                : hovered
                    ? 0xFF51465D
                    : 0xFF27222F
        );

        int checkboxX = listX + 10;
        int checkboxY = rowY
            + (ROW_HEIGHT - CHECKBOX_SIZE) / 2;

        KarakuriCheckboxRenderer.render(
            graphics,
            checkboxX,
            checkboxY,
            CHECKBOX_SIZE,
            selected,
            0xFF9B79D1
        );

        int textX =
            checkboxX + CHECKBOX_SIZE + 9;

        String channels =
            hanamuramiyu.karakuri.task
                .ScenarioConflictAnalyzer
                .channels(scenario)
                .stream()
                .map(TaskChannel::label)
                .sorted()
                .collect(
                    Collectors.joining(" · ")
                );

        if (channels.isEmpty()) {
            channels = "No input channels";
        }

        graphics.drawString(
            font,
            Component.literal(
                truncate(
                    scenario.name(),
                    listWidth - 52
                )
            ),
            textX,
            rowY + 6,
            0xFFF0EAF4,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(
                    channels,
                    listWidth - 52
                )
            ),
            textX,
            rowY + 21,
            0xFF8F8499,
            false
        );
    }

    private List<Scenario> selectedScenarios() {
        return scenarios.stream()
            .filter(
                scenario -> selectedIds.contains(
                    scenario.id()
                )
            )
            .toList();
    }

    private void selectAll() {
        scenarios.stream()
            .map(Scenario::id)
            .forEach(selectedIds::add);
        updateButtons();
    }

    private void clearSelection() {
        selectedIds.clear();
        updateButtons();
    }

    private void clearSlot() {
        QuickLaunchRegistry.clearSlot(
            slotNumber
        );
        minecraft.setScreen(parent);
    }

    private void saveSlot() {
        List<Scenario> selected =
            selectedScenarios();

        if (
            selected.isEmpty()
                || !QuickLaunchValidation
                    .analyze(selected)
                    .valid()
        ) {
            return;
        }

        QuickLaunchRegistry.saveSlot(
            slotNumber,
            selected.stream()
                .map(Scenario::id)
                .toList()
        );

        minecraft.setScreen(parent);
    }

    private void updateButtons() {
        if (
            selectAllButton == null
                || clearButton == null
                || clearSlotButton == null
                || saveButton == null
        ) {
            return;
        }

        List<Scenario> selected =
            selectedScenarios();

        boolean valid =
            !selected.isEmpty()
                && QuickLaunchValidation
                    .analyze(selected)
                    .valid();

        saveButton.setMessage(
            Component.literal(
                "Save Slot ("
                    + selected.size()
                    + ")"
            )
        );

        saveButton.active = valid;
        selectAllButton.active =
            selectedIds.size() < scenarios.size();
        clearButton.active =
            !selectedIds.isEmpty();
        clearSlotButton.active =
            !QuickLaunchRegistry
                .slot(slotNumber)
                .empty();
    }

    private void clampScroll() {
        int listHeight =
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 5;

        int visibleRows =
            Math.max(1, listHeight / ROW_HEIGHT);

        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            Math.max(
                0,
                scenarios.size() - visibleRows
            )
        );
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int panelWidth() {
        return Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int panelHeight() {
        return Math.min(
            PANEL_MAX_HEIGHT,
            height - PANEL_MARGIN * 2
        );
    }

    private String truncate(
        String value,
        int maximumWidth
    ) {
        if (font.width(value) <= maximumWidth) {
            return value;
        }

        String suffix = "...";
        int end = value.length();

        while (
            end > 0
                && font.width(
                    value.substring(0, end)
                        + suffix
                ) > maximumWidth
        ) {
            end--;
        }

        return value.substring(0, end)
            + suffix;
    }

    private boolean contains(
        double mouseX,
        double mouseY,
        int x,
        int y,
        int width,
        int height
    ) {
        return mouseX >= x
            && mouseX < x + width
            && mouseY >= y
            && mouseY < y + height;
    }
}