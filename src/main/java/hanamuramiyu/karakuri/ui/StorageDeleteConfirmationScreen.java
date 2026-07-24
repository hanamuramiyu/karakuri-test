package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class StorageDeleteConfirmationScreen extends Screen {
    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 172;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 24;

    private final Screen parent;
    private final String detail;
    private final String actionLabel;
    private final String question;
    private final Runnable action;

    public StorageDeleteConfirmationScreen(
        Screen parent,
        String title,
        String targetName,
        String detail,
        Runnable action
    ) {
        this(
            parent,
            title,
            targetName,
            detail,
            "Delete",
            "Delete \"" + targetName + "\"?",
            action
        );
    }

    public StorageDeleteConfirmationScreen(
        Screen parent,
        String title,
        String targetName,
        String detail,
        String actionLabel,
        String question,
        Runnable action
    ) {
        super(Component.literal(title));
        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        Objects.requireNonNull(
            targetName,
            "Storage confirmation target must not be null"
        );
        this.detail = Objects.requireNonNull(
            detail,
            "Storage confirmation detail must not be null"
        );
        this.actionLabel = Objects.requireNonNull(
            actionLabel,
            "Storage confirmation action label must not be null"
        );
        this.question = Objects.requireNonNull(
            question,
            "Storage confirmation question must not be null"
        );
        this.action = Objects.requireNonNull(
            action,
            "Storage confirmation action must not be null"
        );
    }

    @Override
    protected void init() {
        int panelX = panelX();
        int panelY = panelY();
        int contentWidth =
            panelWidth() - CONTENT_MARGIN * 2;
        int buttonWidth =
            (contentWidth - BUTTON_GAP) / 2;
        int buttonY =
            panelY
                + PANEL_HEIGHT
                - CONTENT_MARGIN
                - BUTTON_HEIGHT;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal(actionLabel),
                this::confirm,
                KarakuriButton.Style.DANGER
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
                Component.literal("Cancel"),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.SECONDARY
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

        graphics.fill(0, 0, width, height, 0xD0100E16);
        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + PANEL_HEIGHT,
            0xFF191620
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            PANEL_HEIGHT,
            0xFF7A3F4A
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + PANEL_HEIGHT,
            0xFFE66777
        );

        drawCenteredText(
            graphics,
            title,
            panelY + 20,
            0xFFFFE9ED
        );
        drawCenteredText(
            graphics,
            Component.literal(question),
            panelY + 54,
            0xFFF4F0F6
        );
        drawCenteredText(
            graphics,
            Component.literal(detail),
            panelY + 78,
            0xFFB9AEBE
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

    private void confirm() {
        action.run();
        minecraft.setScreen(parent);
    }

    private int panelWidth() {
        return Math.min(
            PANEL_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - PANEL_HEIGHT) / 2;
    }

    private void drawCenteredText(
        GuiGraphics graphics,
        Component text,
        int y,
        int color
    ) {
        graphics.drawString(
            font,
            text,
            (width - font.width(text)) / 2,
            y,
            color,
            false
        );
    }
}