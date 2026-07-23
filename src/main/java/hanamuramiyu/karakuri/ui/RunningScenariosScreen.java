package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.task.TaskChannel;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskSessionSnapshot;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RunningScenariosScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 720;
    private static final int PANEL_MAX_HEIGHT = 520;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 70;
    private static final int FOOTER_HEIGHT = 44;
    private static final int ROW_HEIGHT = 40;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 6;
    private static final int CHECKBOX_SIZE = 14;
    private static final int SCROLL_STEP = 3;

    private final Screen parent;
    private final OpenMode openMode;
    private final Set<Long> selectedIds =
        new LinkedHashSet<>();

    private List<TaskSessionSnapshot> sessions =
        List.of();

    private int scrollOffset;
    private boolean selectionInitialized;

    private KarakuriButton selectAllButton;
    private KarakuriButton clearButton;
    private KarakuriButton emergencyButton;
    private KarakuriButton pauseButton;
    private KarakuriButton resumeButton;
    private KarakuriButton stopButton;

    public RunningScenariosScreen(
        Screen parent,
        OpenMode openMode
    ) {
        super(
            Component.literal(
                switch (openMode) {
                    case MANAGE ->
                        "Running Sessions";
                    case PAUSE ->
                        "Pause Running Scenarios";
                    case STOP ->
                        "Stop Running Scenarios";
                }
            )
        );

        this.parent = parent;
        this.openMode = openMode;
    }

    @Override
    protected void init() {
        refreshSessions();

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int contentWidth =
            panelWidth - CONTENT_MARGIN * 2;
        int footerY =
            panelY + panelHeight - FOOTER_HEIGHT;
        int buttonWidth =
            (contentWidth - BUTTON_GAP * 3) / 4;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                footerY + 10,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Back"),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.SECONDARY
            )
        );

        pauseButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + buttonWidth
                    + BUTTON_GAP,
                footerY + 10,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::pauseSelected,
                KarakuriButton.Style.SECONDARY
            )
        );

        resumeButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (buttonWidth + BUTTON_GAP) * 2,
                footerY + 10,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::resumeSelected,
                KarakuriButton.Style.SUCCESS
            )
        );

        stopButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (buttonWidth + BUTTON_GAP) * 3,
                footerY + 10,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::stopSelected,
                KarakuriButton.Style.DANGER
            )
        );

        createHeaderButtons(
            panelX,
            panelY,
            panelWidth
        );

        updateButtons();
    }

    @Override
    public void tick() {
        refreshSessions();
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
                - 6;

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
            panelY + 13,
            0xFFF6F2FA,
            false
        );

        Component summary = Component.literal(
            selectedIds.size()
                + " selected · "
                + TaskManager.runningCount()
                + " running · "
                + TaskManager.pausedCount()
                + " paused"
        );

        graphics.drawString(
            font,
            summary,
            panelX + CONTENT_MARGIN,
            panelY + 30,
            0xFF8F8499,
            false
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
                - 6;

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

        int index =
            scrollOffset
                + (int) (
                    (event.y() - listY)
                        / ROW_HEIGHT
                );

        if (
            index < 0
                || index >= sessions.size()
        ) {
            return true;
        }

        long sessionId =
            sessions.get(index).id();

        if (!selectedIds.add(sessionId)) {
            selectedIds.remove(sessionId);
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
                - 6;

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

        int visibleRows =
            Math.max(1, listHeight / ROW_HEIGHT);

        int maximumOffset = Math.max(
            0,
            sessions.size() - visibleRows
        );

        scrollOffset = Math.clamp(
            scrollOffset
                - (int) Math.signum(verticalAmount)
                    * SCROLL_STEP,
            0,
            maximumOffset
        );

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

    private void createHeaderButtons(
        int panelX,
        int panelY,
        int panelWidth
    ) {
        int emergencyWidth =
            panelWidth < 470 ? 82 : 138;
        int clearWidth =
            panelWidth < 470 ? 46 : 62;
        int allWidth =
            panelWidth < 470 ? 46 : 72;
        int buttonsY = panelY + 42;
        int emergencyX =
            panelX
                + panelWidth
                - CONTENT_MARGIN
                - emergencyWidth;

        emergencyButton = addRenderableWidget(
            new KarakuriButton(
                font,
                emergencyX,
                buttonsY,
                emergencyWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    panelWidth < 470
                        ? "Stop All"
                        : "Emergency Stop All"
                ),
                this::emergencyStopAll,
                KarakuriButton.Style.DANGER
            )
        );

        clearButton = addRenderableWidget(
            new KarakuriButton(
                font,
                emergencyX
                    - BUTTON_GAP
                    - clearWidth,
                buttonsY,
                clearWidth,
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
                emergencyX
                    - BUTTON_GAP * 2
                    - clearWidth
                    - allWidth,
                buttonsY,
                allWidth,
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
    }

    private void refreshSessions() {
        sessions = TaskManager.sessions();

        Set<Long> activeIds = sessions.stream()
            .map(TaskSessionSnapshot::id)
            .collect(Collectors.toSet());

        selectedIds.retainAll(activeIds);

        if (!selectionInitialized) {
            switch (openMode) {
                case PAUSE -> sessions.stream()
                    .filter(
                        session -> session.status()
                            == TaskStatus.RUNNING
                    )
                    .map(TaskSessionSnapshot::id)
                    .forEach(selectedIds::add);

                case STOP -> sessions.stream()
                    .map(TaskSessionSnapshot::id)
                    .forEach(selectedIds::add);

                case MANAGE -> {
                }
            }

            selectionInitialized = true;
        }

        int visibleRows = Math.max(
            1,
            (
                panelHeight()
                    - HEADER_HEIGHT
                    - FOOTER_HEIGHT
                    - 6
            ) / ROW_HEIGHT
        );

        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            Math.max(
                0,
                sessions.size() - visibleRows
            )
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
        if (sessions.isEmpty()) {
            graphics.drawCenteredString(
                font,
                Component.literal(
                    "No scenarios are running"
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
            sessions.size(),
            scrollOffset + visibleRows + 1
        );

        for (
            int index = scrollOffset;
            index < endIndex;
            index++
        ) {
            int rowY =
                listY
                    + (index - scrollOffset)
                        * ROW_HEIGHT;

            renderRow(
                graphics,
                sessions.get(index),
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
        TaskSessionSnapshot session,
        int rowY,
        int listX,
        int listWidth,
        int mouseX,
        int mouseY
    ) {
        boolean selected =
            selectedIds.contains(session.id());

        boolean hovered = contains(
            mouseX,
            mouseY,
            listX + 2,
            rowY,
            listWidth - 4,
            ROW_HEIGHT - 1
        );

        int background = selected
            ? 0xFF2A2237
            : hovered
                ? 0xFF201C29
                : 0xFF15121B;

        graphics.fill(
            listX + 2,
            rowY,
            listX + listWidth - 2,
            rowY + ROW_HEIGHT - 1,
            background
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
        int checkboxY =
            rowY
                + (ROW_HEIGHT - CHECKBOX_SIZE) / 2;

        graphics.fill(
            checkboxX,
            checkboxY,
            checkboxX + CHECKBOX_SIZE,
            checkboxY + CHECKBOX_SIZE,
            selected
                ? 0xFF9B79D1
                : 0xFF100E16
        );

        graphics.renderOutline(
            checkboxX,
            checkboxY,
            CHECKBOX_SIZE,
            CHECKBOX_SIZE,
            selected
                ? 0xFFCDB1F2
                : 0xFF5A5063
        );

        if (selected) {
            graphics.drawString(
                font,
                Component.literal("✓"),
                checkboxX + 3,
                checkboxY + 3,
                0xFFF8F4FB,
                false
            );
        }

        int textX =
            checkboxX + CHECKBOX_SIZE + 9;
        int statusColor =
            session.status()
                == TaskStatus.RUNNING
                    ? 0xFF61D394
                    : 0xFFF1C36E;

        String statusText =
            session.status().label();

        int statusWidth =
            font.width(statusText);
        int availableNameWidth = Math.max(
            30,
            listWidth
                - (textX - listX)
                - statusWidth
                - 22
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(
                    session.name(),
                    availableNameWidth
                )
            ),
            textX,
            rowY + 7,
            0xFFF0EAF4,
            false
        );

        graphics.drawString(
            font,
            Component.literal(statusText),
            listX
                + listWidth
                - statusWidth
                - 10,
            rowY + 7,
            statusColor,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(
                    channelText(session),
                    listWidth
                        - (textX - listX)
                        - 18
                )
            ),
            textX,
            rowY + 22,
            0xFF8F8499,
            false
        );
    }

    private String channelText(
        TaskSessionSnapshot session
    ) {
        String channels =
            session.channels().isEmpty()
                ? "No input channels"
                : session.channels()
                    .stream()
                    .map(TaskChannel::label)
                    .sorted()
                    .collect(
                        Collectors.joining(" · ")
                    );

        String details =
            session.groupName().equals(session.name())
                ? channels
                : session.groupName()
                    + " · "
                    + channels;

        if (
            session.repeatCount()
                == hanamuramiyu.karakuri.task.composite.RepeatTask.INFINITE
        ) {
            return details + " · Forever";
        }

        if (session.repeatCount() > 1) {
            return details
                + " · "
                + session.repeatCount()
                + "x";
        }

        return details;
    }

    private void updateButtons() {
        if (
            pauseButton == null
                || resumeButton == null
                || stopButton == null
                || selectAllButton == null
                || clearButton == null
                || emergencyButton == null
        ) {
            return;
        }

        int selectedCount = selectedIds.size();

        boolean hasRunning =
            sessions.stream().anyMatch(
                session ->
                    selectedIds.contains(
                        session.id()
                    )
                        && session.status()
                            == TaskStatus.RUNNING
            );

        boolean hasPaused =
            sessions.stream().anyMatch(
                session ->
                    selectedIds.contains(
                        session.id()
                    )
                        && session.status()
                            == TaskStatus.PAUSED
            );

        pauseButton.setMessage(
            Component.literal(
                shortActionLabel(
                    "Pause Selected",
                    "Pause",
                    selectedCount
                )
            )
        );

        resumeButton.setMessage(
            Component.literal(
                shortActionLabel(
                    "Resume Selected",
                    "Resume",
                    selectedCount
                )
            )
        );

        stopButton.setMessage(
            Component.literal(
                shortActionLabel(
                    "Stop Selected",
                    "Stop",
                    selectedCount
                )
            )
        );

        pauseButton.active = hasRunning;
        resumeButton.active = hasPaused;
        stopButton.active = selectedCount > 0;
        selectAllButton.active =
            selectedCount < sessions.size();
        clearButton.active = selectedCount > 0;
        emergencyButton.active = !sessions.isEmpty();
    }

    private String shortActionLabel(
        String wide,
        String compact,
        int selectedCount
    ) {
        return panelWidth() < 560
            ? compact + " (" + selectedCount + ")"
            : wide + " (" + selectedCount + ")";
    }

    private void selectAll() {
        sessions.stream()
            .map(TaskSessionSnapshot::id)
            .forEach(selectedIds::add);
        updateButtons();
    }

    private void clearSelection() {
        selectedIds.clear();
        updateButtons();
    }

    private void pauseSelected() {
        List<Long> affectedIds = sessions.stream()
            .filter(
                session ->
                    selectedIds.contains(
                        session.id()
                    )
                        && session.status()
                            == TaskStatus.RUNNING
            )
            .map(TaskSessionSnapshot::id)
            .toList();

        TaskManager.pauseSessions(
            affectedIds,
            minecraft
        );

        showFeedback(
            pausedMessage(
                affectedIds.size()
            ),
            true
        );

        refreshSessions();
        updateButtons();
    }

    private void resumeSelected() {
        List<Long> affectedIds = sessions.stream()
            .filter(
                session ->
                    selectedIds.contains(
                        session.id()
                    )
                        && session.status()
                            == TaskStatus.PAUSED
            )
            .map(TaskSessionSnapshot::id)
            .toList();

        TaskManager.resumeSessions(
            affectedIds,
            minecraft
        );

        showFeedback(
            resumedMessage(
                affectedIds.size()
            ),
            true
        );

        refreshSessions();
        updateButtons();
    }

    private void stopSelected() {
        int count = selectedIds.size();

        TaskManager.stopSessions(
            new ArrayList<>(selectedIds),
            minecraft
        );

        showFeedback(
            stoppedMessage(count),
            true
        );

        selectedIds.clear();
        refreshSessions();
        updateButtons();
    }

    private void emergencyStopAll() {
        int count = TaskManager.activeCount();
        TaskManager.stop(minecraft);

        showFeedback(
            count == 1
                ? "Stopped 1 scenario"
                : "Stopped " + count + " scenarios",
            true
        );

        selectedIds.clear();
        refreshSessions();
        updateButtons();
    }

    private void showFeedback(
        String message,
        boolean success
    ) {
        if (parent instanceof KarakuriScreen screen) {
            screen.showFeedback(
                message,
                success
            );
            return;
        }

        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                Component.literal(message),
                true
            );
        }
    }

    private String pausedMessage(int count) {
        return count == 1
            ? "Paused 1 scenario"
            : "Paused " + count + " scenarios";
    }

    private String resumedMessage(int count) {
        return count == 1
            ? "Resumed 1 scenario"
            : "Resumed " + count + " scenarios";
    }

    private String stoppedMessage(int count) {
        return count == 1
            ? "Stopped 1 scenario"
            : "Stopped " + count + " scenarios";
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

    public enum OpenMode {
        MANAGE,
        PAUSE,
        STOP
    }
}