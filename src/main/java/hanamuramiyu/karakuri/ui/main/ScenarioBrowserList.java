package hanamuramiyu.karakuri.ui.main;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskSessionSnapshot;
import hanamuramiyu.karakuri.task.TaskStatus;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

public final class ScenarioBrowserList {
    private static final int HEADER_HEIGHT = 24;
    private static final int ROW_HEIGHT = 30;
    private static final int SCROLL_STEP = 3;

    private final Font font;
    private final KarakuriScreenState state;
    private final IntConsumer selectionAction;
    private final Runnable openAction;

    private int x;
    private int y;
    private int width;
    private int height;
    private int scrollOffset;

    public ScenarioBrowserList(
        Font font,
        KarakuriScreenState state,
        IntConsumer selectionAction,
        Runnable openAction
    ) {
        this.font = Objects.requireNonNull(
            font,
            "Font must not be null"
        );
        this.state = Objects.requireNonNull(
            state,
            "Screen state must not be null"
        );
        this.selectionAction = Objects.requireNonNull(
            selectionAction,
            "Selection action must not be null"
        );
        this.openAction = Objects.requireNonNull(
            openAction,
            "Open action must not be null"
        );
    }

    public void setBounds(
        int x,
        int y,
        int width,
        int height
    ) {
        this.x = x;
        this.y = y;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        clampScroll();
    }

    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY
    ) {
        graphics.fill(
            x,
            y,
            x + width,
            y + height,
            0xFF100E16
        );
        graphics.renderOutline(
            x,
            y,
            width,
            height,
            0xFF393243
        );

        renderHeader(graphics);

        List<KarakuriScreenState.ScenarioEntry> visible =
            state.visibleScenarios();

        if (visible.isEmpty()) {
            renderEmptyState(graphics);
            return;
        }

        int listTop = y + HEADER_HEIGHT;
        int listBottom = y + height - 1;

        graphics.enableScissor(
            x + 1,
            listTop,
            x + width - 1,
            listBottom
        );

        int visibleRows = visibleRowCapacity();
        int endIndex = Math.min(
            visible.size(),
            scrollOffset + visibleRows + 1
        );

        for (int index = scrollOffset; index < endIndex; index++) {
            int rowY = listTop + (index - scrollOffset) * ROW_HEIGHT;

            renderRow(
                graphics,
                visible.get(index),
                rowY,
                mouseX,
                mouseY
            );
        }

        graphics.disableScissor();
        renderScrollIndicator(graphics, visible.size());
    }

    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (
            event.button() != 0
                || !contains(event.x(), event.y())
        ) {
            return false;
        }

        int listTop = y + HEADER_HEIGHT;

        if (event.y() < listTop) {
            return true;
        }

        int rowIndex = scrollOffset
            + (int) ((event.y() - listTop) / ROW_HEIGHT);
        List<KarakuriScreenState.ScenarioEntry> visible =
            state.visibleScenarios();

        if (rowIndex < 0 || rowIndex >= visible.size()) {
            return true;
        }

        selectionAction.accept(
            visible.get(rowIndex).libraryIndex()
        );

        if (doubled) {
            openAction.run();
        }

        return true;
    }

    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double verticalAmount
    ) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }

        int previousOffset = scrollOffset;

        if (verticalAmount > 0) {
            scrollOffset -= SCROLL_STEP;
        } else if (verticalAmount < 0) {
            scrollOffset += SCROLL_STEP;
        }

        clampScroll();
        return previousOffset != scrollOffset;
    }

    public void ensureSelectedVisible() {
        int selectedVisibleIndex = state.selectedVisibleIndex();

        if (selectedVisibleIndex < 0) {
            clampScroll();
            return;
        }

        int capacity = visibleRowCapacity();

        if (selectedVisibleIndex < scrollOffset) {
            scrollOffset = selectedVisibleIndex;
        } else if (selectedVisibleIndex >= scrollOffset + capacity) {
            scrollOffset = selectedVisibleIndex - capacity + 1;
        }

        clampScroll();
    }

    public void resetScroll() {
        scrollOffset = 0;
        ensureSelectedVisible();
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.fill(
            x + 1,
            y + 1,
            x + width - 1,
            y + HEADER_HEIGHT,
            0xFF17141F
        );

        Component title = Component.literal("Scenarios");
        Component count = Component.literal(
            state.visibleScenarioCount()
                + " / "
                + state.scenarioCount()
        );

        graphics.drawString(
            font,
            title,
            x + 8,
            y + 8,
            0xFFE8E2ED,
            false
        );
        graphics.drawString(
            font,
            count,
            x + width - 8 - font.width(count),
            y + 8,
            0xFF8F8499,
            false
        );
    }

    private void renderRow(
        GuiGraphics graphics,
        KarakuriScreenState.ScenarioEntry entry,
        int rowY,
        int mouseX,
        int mouseY
    ) {
        Scenario scenario = entry.scenario();
        boolean selected = entry.libraryIndex()
            == state.selectedScenarioIndex();

        TaskSessionSnapshot activeSession =
            TaskManager.findScenarioSessionById(
                scenario.id()
            );

        boolean hovered = contains(
            mouseX,
            mouseY,
            x + 2,
            rowY,
            width - 4,
            ROW_HEIGHT
        );

        int background = selected
            ? 0xFF2A2237
            : activeSession != null
                ? 0xFF18231F
                : hovered
                    ? 0xFF201C29
                    : 0xFF15121B;

        int outline = selected
            ? 0xFF9B79D1
            : activeSession != null
                ? activeSession.status()
                    == TaskStatus.RUNNING
                        ? 0xFF3F8D68
                        : 0xFF96763C
                : hovered
                    ? 0xFF51465D
                    : 0xFF27222F;

        graphics.fill(
            x + 2,
            rowY,
            x + width - 2,
            rowY + ROW_HEIGHT - 1,
            background
        );
        graphics.renderOutline(
            x + 2,
            rowY,
            width - 4,
            ROW_HEIGHT - 1,
            outline
        );

        if (
            selected
                || activeSession != null
        ) {
            int markerColor =
                activeSession == null
                    ? 0xFFB38AE8
                    : activeSession.status()
                        == TaskStatus.RUNNING
                            ? 0xFF61D394
                            : 0xFFF1C36E;

            graphics.fill(
                x + 2,
                rowY,
                x + 5,
                rowY + ROW_HEIGHT - 1,
                markerColor
            );
        }

        String actionLabel = scenario.steps().size() == 1
            ? "1 action"
            : scenario.steps().size() + " actions";
        int metadataWidth = font.width(actionLabel);
        int nameWidth = Math.max(
            20,
            width - metadataWidth - 34
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(scenario.name(), nameWidth)
            ),
            x + 11,
            rowY + 6,
            selected ? 0xFFF6F1FA : 0xFFE3DCE8,
            false
        );
        graphics.drawString(
            font,
            Component.literal(actionLabel),
            x + width - 11 - metadataWidth,
            rowY + 6,
            0xFF9A90A4,
            false
        );

        String stateLabel =
            activeSession == null
                ? selected
                    ? "Selected"
                    : "Click to select"
                : activeSession.status()
                    == TaskStatus.RUNNING
                        ? "Running"
                        : "Paused";

        int stateColor =
            activeSession == null
                ? selected
                    ? 0xFFB38AE8
                    : 0xFF716879
                : activeSession.status()
                    == TaskStatus.RUNNING
                        ? 0xFF61D394
                        : 0xFFF1C36E;

        graphics.drawString(
            font,
            Component.literal(stateLabel),
            x + 11,
            rowY + 18,
            stateColor,
            false
        );
    }

    private void renderEmptyState(GuiGraphics graphics) {
        Component title = Component.literal(
            state.hasScenario()
                ? "No matching scenarios"
                : "No scenarios yet"
        );
        Component description = Component.literal(
            state.hasScenario()
                ? "Try another search."
                : "Create your first workflow below."
        );
        int centerY = y + HEADER_HEIGHT
            + Math.max(20, (height - HEADER_HEIGHT) / 2);

        graphics.drawString(
            font,
            title,
            x + (width - font.width(title)) / 2,
            centerY - 9,
            0xFFECE6F1,
            false
        );
        graphics.drawString(
            font,
            description,
            x + (width - font.width(description)) / 2,
            centerY + 8,
            0xFF81778A,
            false
        );
    }

    private void renderScrollIndicator(
        GuiGraphics graphics,
        int rowCount
    ) {
        int capacity = visibleRowCapacity();

        if (rowCount <= capacity) {
            return;
        }

        int trackTop = y + HEADER_HEIGHT + 3;
        int trackHeight = height - HEADER_HEIGHT - 6;
        int thumbHeight = Math.max(
            12,
            trackHeight * capacity / rowCount
        );
        int maxOffset = Math.max(1, rowCount - capacity);
        int thumbY = trackTop
            + (trackHeight - thumbHeight)
                * scrollOffset
                / maxOffset;

        graphics.fill(
            x + width - 3,
            trackTop,
            x + width - 2,
            trackTop + trackHeight,
            0xFF302A38
        );
        graphics.fill(
            x + width - 4,
            thumbY,
            x + width - 1,
            thumbY + thumbHeight,
            0xFF8F6FC0
        );
    }

    private int visibleRowCapacity() {
        return Math.max(
            1,
            (height - HEADER_HEIGHT) / ROW_HEIGHT
        );
    }

    private void clampScroll() {
        int maximumOffset = Math.max(
            0,
            state.visibleScenarioCount()
                - visibleRowCapacity()
        );

        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            maximumOffset
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
                    value.substring(0, end) + suffix
                ) > maximumWidth
        ) {
            end--;
        }

        return value.substring(0, end) + suffix;
    }

    private boolean contains(
        double mouseX,
        double mouseY
    ) {
        return contains(
            mouseX,
            mouseY,
            x,
            y,
            width,
            height
        );
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