package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.quicklaunch.QuickLaunchController;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.ScenarioGroupStartResult;
import hanamuramiyu.karakuri.task.TaskChannel;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskSessionSnapshot;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public final class QuickLaunchConflictScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 620;
    private static final int PANEL_MAX_HEIGHT = 380;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 7;

    private final Screen parent;
    private final int slotNumber;
    private final List<Scenario> scenarios;
    private final List<TaskSessionSnapshot> conflicts;
    private final String channelText;

    public QuickLaunchConflictScreen(
        Screen parent,
        int slotNumber,
        List<Scenario> scenarios,
        ScenarioGroupStartResult result
    ) {
        super(Component.literal("Quick Launch Conflict"));

        this.parent = parent;
        this.slotNumber = slotNumber;
        this.scenarios = List.copyOf(scenarios);
        this.conflicts = result.conflicts();
        this.channelText =
            result.conflictingChannels()
                .stream()
                .map(TaskChannel::label)
                .sorted()
                .collect(
                    Collectors.joining(", ")
                );
    }

    @Override
    protected void init() {
        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int contentWidth =
            panelWidth - CONTENT_MARGIN * 2;
        int buttonY =
            panelY
                + panelHeight
                - CONTENT_MARGIN
                - BUTTON_HEIGHT;
        int buttonWidth =
            (contentWidth - BUTTON_GAP * 3) / 4;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Cancel"),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.SECONDARY
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + buttonWidth
                    + BUTTON_GAP,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    panelWidth < 520
                        ? "Manage"
                        : "Running Sessions"
                ),
                () -> QuickLaunchController
                    .openRunningSessions(
                        this,
                        minecraft
                    ),
                KarakuriButton.Style.GHOST
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (buttonWidth + BUTTON_GAP) * 2,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    panelWidth < 520
                        ? "Compatible"
                        : "Start Compatible"
                ),
                this::startCompatible,
                KarakuriButton.Style.SUCCESS
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (buttonWidth + BUTTON_GAP) * 3,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    panelWidth < 520
                        ? "Stop & Start"
                        : "Stop Conflicts & Start"
                ),
                this::stopConflictsAndStart,
                KarakuriButton.Style.DANGER
            )
        );
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
            0xFFE66777
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelBottom,
            0xFFE66777
        );

        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 15,
            0xFFFFE9ED,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                "Quick Slot "
                    + slotNumber
                    + " cannot start"
            ),
            panelX + CONTENT_MARGIN,
            panelY + 39,
            0xFFF6F2FA,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                conflicts.size() == 1
                    ? "1 active scenario uses the same controls."
                    : conflicts.size()
                        + " active scenarios use the same controls."
            ),
            panelX + CONTENT_MARGIN,
            panelY + 57,
            0xFFB7AFBF,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                channelText.isBlank()
                    ? "A selected scenario is already active"
                    : "Conflicting: "
                        + channelText
            ),
            panelX + CONTENT_MARGIN,
            panelY + 75,
            0xFFE66777,
            false
        );

        int rowY = panelY + 102;
        int maximumRows = Math.max(
            1,
            (
                panelHeight
                    - 166
                    - BUTTON_HEIGHT
            ) / 30
        );

        for (
            int index = 0;
            index < Math.min(
                maximumRows,
                conflicts.size()
            );
            index++
        ) {
            TaskSessionSnapshot conflict =
                conflicts.get(index);

            graphics.fill(
                panelX + CONTENT_MARGIN,
                rowY,
                panelRight - CONTENT_MARGIN,
                rowY + 25,
                0xFF211A24
            );
            graphics.renderOutline(
                panelX + CONTENT_MARGIN,
                rowY,
                panelWidth - CONTENT_MARGIN * 2,
                25,
                0xFF5A3941
            );

            graphics.drawString(
                font,
                Component.literal(
                    conflict.name()
                        + " · "
                        + conflict.groupName()
                ),
                panelX + CONTENT_MARGIN + 8,
                rowY + 8,
                0xFFE8E2ED,
                false
            );

            rowY += 30;
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

    private void stopConflictsAndStart() {
        TaskManager.stopSessions(
            conflicts.stream()
                .map(TaskSessionSnapshot::id)
                .toList(),
            minecraft
        );

        ScenarioGroupStartResult result =
            TaskManager.startScenarioGroup(
                "Quick Slot " + slotNumber,
                scenarios,
                1,
                minecraft
            );

        if (
            result.status()
                == ScenarioGroupStartResult
                    .Status.STARTED
        ) {
            QuickLaunchController.notify(
                minecraft,
                QuickLaunchController.startedMessage(
                    slotNumber,
                    scenarios.size()
                )
            );

            minecraft.setScreen(parent);
            return;
        }

        QuickLaunchController.handleStartResult(
            parent,
            slotNumber,
            scenarios,
            result,
            minecraft
        );
    }

    private void startCompatible() {
        List<Scenario> compatible =
            QuickLaunchController
                .compatibleScenarios(
                    scenarios
                );

        if (compatible.isEmpty()) {
            QuickLaunchController.notify(
                minecraft,
                "No compatible scenarios can start"
            );
            return;
        }

        ScenarioGroupStartResult result =
            TaskManager.startScenarioGroup(
                "Quick Slot " + slotNumber,
                compatible,
                1,
                minecraft
            );

        if (
            result.status()
                == ScenarioGroupStartResult
                    .Status.STARTED
        ) {
            QuickLaunchController.notify(
                minecraft,
                "Started "
                    + compatible.size()
                    + " of "
                    + scenarios.size()
                    + " scenarios from Quick Slot "
                    + slotNumber
            );

            minecraft.setScreen(parent);
            return;
        }

        QuickLaunchController.handleStartResult(
            parent,
            slotNumber,
            compatible,
            result,
            minecraft
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
}