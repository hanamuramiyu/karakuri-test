package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.task.ClientTask;
import hanamuramiyu.karakuri.task.RepeatTask;
import hanamuramiyu.karakuri.task.SequenceTask;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.task.TaskStatus;
import hanamuramiyu.karakuri.task.WaitTask;
import hanamuramiyu.karakuri.task.WalkForwardTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class KarakuriScreen extends Screen {
    private static final int PANEL_HEIGHT = 280;
    private static final int PANEL_MAX_WIDTH = 360;
    private static final int PANEL_MARGIN = 20;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int WALK_DURATION_TICKS = 40;
    private static final int WAIT_DURATION_TICKS = 20;

    private final Screen parent;

    private ExecutionMode executionMode = ExecutionMode.ONCE;

    private Button modeButton;
    private Button startButton;
    private Button pauseButton;
    private Button stopButton;

    public KarakuriScreen(Screen parent) {
        super(Component.literal("Karakuri"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int contentWidth = getPanelWidth() - CONTENT_MARGIN * 2;
        int buttonWidth = (contentWidth - BUTTON_GAP * 2) / 3;
        int modeButtonY = panelY + 214;
        int controlButtonY = panelY + 244;

        modeButton = Button.builder(
            Component.empty(),
            button -> {
                executionMode = executionMode.next();
                updateButtons();
            }
        ).bounds(
            panelX + CONTENT_MARGIN,
            modeButtonY,
            contentWidth,
            BUTTON_HEIGHT
        ).build();

        startButton = Button.builder(
            Component.literal("Start"),
            button -> startOrResume()
        ).bounds(
            panelX + CONTENT_MARGIN,
            controlButtonY,
            buttonWidth,
            BUTTON_HEIGHT
        ).build();

        pauseButton = Button.builder(
            Component.literal("Pause"),
            button -> TaskManager.pause(minecraft)
        ).bounds(
            panelX + CONTENT_MARGIN + buttonWidth + BUTTON_GAP,
            controlButtonY,
            buttonWidth,
            BUTTON_HEIGHT
        ).build();

        stopButton = Button.builder(
            Component.literal("Stop"),
            button -> TaskManager.stop(minecraft)
        ).bounds(
            panelX + CONTENT_MARGIN + (buttonWidth + BUTTON_GAP) * 2,
            controlButtonY,
            buttonWidth,
            BUTTON_HEIGHT
        ).build();

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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();
        TaskStatus status = TaskManager.getStatus();

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

        drawCenteredText(graphics, title, panelY + 18, 0xFFF4F4F7);
        drawCenteredText(
            graphics,
            Component.literal("Programmable player automation"),
            panelY + 38,
            0xFF9999AA
        );

        graphics.drawString(
            font,
            Component.literal("Test sequence"),
            panelX + CONTENT_MARGIN,
            panelY + 70,
            0xFF9999AA,
            false
        );
        graphics.drawString(
            font,
            Component.literal("Walk 2s, wait 1s, walk 2s"),
            panelX + CONTENT_MARGIN,
            panelY + 88,
            0xFFF4F4F7,
            false
        );
        graphics.drawString(
            font,
            Component.literal(executionMode.description()),
            panelX + CONTENT_MARGIN,
            panelY + 106,
            0xFFF4F4F7,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Current session"),
            panelX + CONTENT_MARGIN,
            panelY + 136,
            0xFF9999AA,
            false
        );
        graphics.drawString(
            font,
            Component.literal("Controls only the active account"),
            panelX + CONTENT_MARGIN,
            panelY + 154,
            0xFFF4F4F7,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Status"),
            panelX + CONTENT_MARGIN,
            panelY + 178,
            0xFF9999AA,
            false
        );
        graphics.drawString(
            font,
            Component.literal(status.label()),
            panelX + CONTENT_MARGIN,
            panelY + 196,
            getStatusColor(status),
            false
        );

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

    private void startOrResume() {
        if (TaskManager.getStatus() == TaskStatus.PAUSED) {
            TaskManager.resume(minecraft);
            return;
        }

        TaskManager.start(
            new RepeatTask(
                KarakuriScreen::createTestSequence,
                executionMode.repeatCount()
            ),
            minecraft
        );
    }

    private static ClientTask createTestSequence() {
        return new SequenceTask(
            List.of(
                new WalkForwardTask(WALK_DURATION_TICKS),
                new WaitTask(WAIT_DURATION_TICKS),
                new WalkForwardTask(WALK_DURATION_TICKS)
            )
        );
    }

    private void updateButtons() {
        if (
            modeButton == null
                || startButton == null
                || pauseButton == null
                || stopButton == null
        ) {
            return;
        }

        TaskStatus status = TaskManager.getStatus();

        modeButton.setMessage(
            Component.literal("Mode: " + executionMode.label())
        );
        modeButton.active = status == TaskStatus.IDLE;

        startButton.setMessage(
            Component.literal(status == TaskStatus.PAUSED ? "Resume" : "Start")
        );
        startButton.active = status != TaskStatus.RUNNING;
        pauseButton.active = status == TaskStatus.RUNNING;
        stopButton.active = status != TaskStatus.IDLE;
    }

    private int getStatusColor(TaskStatus status) {
        return switch (status) {
            case IDLE -> 0xFFB7B7C5;
            case RUNNING -> 0xFF73D69C;
            case PAUSED -> 0xFFF2C66D;
        };
    }

    private int getPanelWidth() {
        return Math.min(PANEL_MAX_WIDTH, width - PANEL_MARGIN * 2);
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
        graphics.drawString(font, text, x, y, color, false);
    }

    private enum ExecutionMode {
        ONCE("Once", "Run the sequence one time", 1),
        LOOP("Loop", "Repeat until stopped", RepeatTask.INFINITE);

        private final String label;
        private final String description;
        private final int repeatCount;

        ExecutionMode(String label, String description, int repeatCount) {
            this.label = label;
            this.description = description;
            this.repeatCount = repeatCount;
        }

        private String label() {
            return label;
        }

        private String description() {
            return description;
        }

        private int repeatCount() {
            return repeatCount;
        }

        private ExecutionMode next() {
            return this == ONCE ? LOOP : ONCE;
        }
    }
}