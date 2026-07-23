package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.ScenarioStartResult;
import hanamuramiyu.karakuri.task.TaskChannel;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskSessionSnapshot;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public final class ScenarioConflictScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 520;
    private static final int PANEL_MAX_HEIGHT = 330;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 7;

    private final KarakuriScreen parent;
    private final Scenario scenario;
    private final int repeatCount;
    private final List<TaskSessionSnapshot> conflicts;
    private final String channelText;

    public ScenarioConflictScreen(
        KarakuriScreen parent,
        Scenario scenario,
        int repeatCount,
        ScenarioStartResult result
    ) {
        super(Component.literal("Scenario Conflict"));

        this.parent = parent;
        this.scenario = scenario;
        this.repeatCount = repeatCount;
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
        int buttonWidth =
            (contentWidth - BUTTON_GAP * 2) / 3;
        int buttonY =
            panelY
                + panelHeight
                - CONTENT_MARGIN
                - BUTTON_HEIGHT;

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
                    panelWidth < 430
                        ? "Manage"
                        : "Manage Running"
                ),
                () -> minecraft.setScreen(
                    new RunningScenariosScreen(
                        parent,
                        RunningScenariosScreen
                            .OpenMode.MANAGE
                    )
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
                    panelWidth < 430
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
                "Cannot start \""
                    + scenario.name()
                    + "\""
            ),
            panelX + CONTENT_MARGIN,
            panelY + 38,
            0xFFF6F2FA,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                conflicts.size() == 1
                    ? "1 running scenario uses the same controls."
                    : conflicts.size()
                        + " running scenarios use the same controls."
            ),
            panelX + CONTENT_MARGIN,
            panelY + 55,
            0xFFB7AFBF,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                "Conflicting: " + channelText
            ),
            panelX + CONTENT_MARGIN,
            panelY + 72,
            0xFFE66777,
            false
        );

        int rowY = panelY + 98;
        int maximumRows = Math.max(
            1,
            (
                panelHeight
                    - 150
                    - BUTTON_HEIGHT
            ) / 24
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
                rowY + 20,
                0xFF211B29
            );

            graphics.fill(
                panelX + CONTENT_MARGIN,
                rowY,
                panelX + CONTENT_MARGIN + 3,
                rowY + 20,
                0xFFE66777
            );

            graphics.drawString(
                font,
                Component.literal(
                    truncate(
                        conflict.name(),
                        panelWidth
                            - CONTENT_MARGIN * 2
                            - 76
                    )
                ),
                panelX + CONTENT_MARGIN + 9,
                rowY + 6,
                0xFFEDE7F1,
                false
            );

            String status =
                conflict.status().label();

            graphics.drawString(
                font,
                Component.literal(status),
                panelRight
                    - CONTENT_MARGIN
                    - font.width(status)
                    - 7,
                rowY + 6,
                conflict.status()
                    == hanamuramiyu.karakuri.task.TaskStatus.RUNNING
                        ? 0xFF61D394
                        : 0xFFF1C36E,
                false
            );

            rowY += 24;
        }

        if (conflicts.size() > maximumRows) {
            graphics.drawString(
                font,
                Component.literal(
                    "+"
                        + (
                            conflicts.size()
                                - maximumRows
                        )
                        + " more"
                ),
                panelX + CONTENT_MARGIN,
                rowY + 2,
                0xFF8F8499,
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

    private void stopConflictsAndStart() {
        TaskManager.stopSessions(
            conflicts.stream()
                .map(TaskSessionSnapshot::id)
                .toList(),
            minecraft
        );

        ScenarioStartResult result =
            TaskManager.startScenario(
                scenario,
                repeatCount,
                minecraft
            );

        if (
            result.status()
                == ScenarioStartResult.Status.STARTED
        ) {
            parent.showFeedback(
                "Started \"" + scenario.name() + "\"",
                true
            );
        } else {
            parent.showFeedback(
                "Could not start \""
                    + scenario.name()
                    + "\"",
                false
            );
        }

        minecraft.setScreen(parent);
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
}