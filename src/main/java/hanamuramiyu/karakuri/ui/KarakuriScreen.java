package hanamuramiyu.karakuri.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class KarakuriScreen extends Screen {
    private static final int PANEL_HEIGHT = 210;
    private static final int PANEL_MAX_WIDTH = 360;
    private static final int PANEL_MARGIN = 20;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;
    private ExecutionStatus status = ExecutionStatus.IDLE;

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
        int buttonY = panelY + PANEL_HEIGHT - CONTENT_MARGIN - BUTTON_HEIGHT;

        startButton = Button.builder(
            Component.literal("Start"),
            button -> setStatus(ExecutionStatus.RUNNING)
        ).bounds(
            panelX + CONTENT_MARGIN,
            buttonY,
            buttonWidth,
            BUTTON_HEIGHT
        ).build();

        pauseButton = Button.builder(
            Component.literal("Pause"),
            button -> setStatus(ExecutionStatus.PAUSED)
        ).bounds(
            panelX + CONTENT_MARGIN + buttonWidth + BUTTON_GAP,
            buttonY,
            buttonWidth,
            BUTTON_HEIGHT
        ).build();

        stopButton = Button.builder(
            Component.literal("Stop"),
            button -> setStatus(ExecutionStatus.IDLE)
        ).bounds(
            panelX + CONTENT_MARGIN + (buttonWidth + BUTTON_GAP) * 2,
            buttonY,
            buttonWidth,
            BUTTON_HEIGHT
        ).build();

        addRenderableWidget(startButton);
        addRenderableWidget(pauseButton);
        addRenderableWidget(stopButton);

        updateButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();

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
            Component.literal("Current session"),
            panelX + CONTENT_MARGIN,
            panelY + 72,
            0xFFF4F4F7,
            false
        );
        graphics.drawString(
            font,
            Component.literal("Controls only the active account"),
            panelX + CONTENT_MARGIN,
            panelY + 90,
            0xFF9999AA,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Status"),
            panelX + CONTENT_MARGIN,
            panelY + 122,
            0xFF9999AA,
            false
        );
        graphics.drawString(
            font,
            Component.literal(status.label()),
            panelX + CONTENT_MARGIN,
            panelY + 140,
            status.color(),
            false
        );

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void setStatus(ExecutionStatus status) {
        this.status = status;
        updateButtons();
    }

    private void updateButtons() {
        if (startButton == null || pauseButton == null || stopButton == null) {
            return;
        }

        startButton.active = status != ExecutionStatus.RUNNING;
        pauseButton.active = status == ExecutionStatus.RUNNING;
        stopButton.active = status != ExecutionStatus.IDLE;
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

    private enum ExecutionStatus {
        IDLE("Idle", 0xFFB7B7C5),
        RUNNING("Running", 0xFF73D69C),
        PAUSED("Paused", 0xFFF2C66D);

        private final String label;
        private final int color;

        ExecutionStatus(String label, int color) {
            this.label = label;
            this.color = color;
        }

        private String label() {
            return label;
        }

        private int color() {
            return color;
        }
    }
}