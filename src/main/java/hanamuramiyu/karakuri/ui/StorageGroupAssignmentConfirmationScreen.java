package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class StorageGroupAssignmentConfirmationScreen extends Screen {
    private static final int PANEL_WIDTH = 500;
    private static final int PANEL_HEIGHT = 190;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 24;

    private final Screen parent;
    private final String storageName;
    private final String currentGroups;
    private final String targetGroup;
    private final Runnable assignAction;

    public StorageGroupAssignmentConfirmationScreen(
        Screen parent,
        String storageName,
        String currentGroups,
        String targetGroup,
        Runnable assignAction
    ) {
        super(Component.literal("Assign Storage to Another Group"));
        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        this.storageName = Objects.requireNonNull(
            storageName,
            "Storage name must not be null"
        );
        this.currentGroups = Objects.requireNonNull(
            currentGroups,
            "Current storage groups must not be null"
        );
        this.targetGroup = Objects.requireNonNull(
            targetGroup,
            "Target storage group must not be null"
        );
        this.assignAction = Objects.requireNonNull(
            assignAction,
            "Storage assignment action must not be null"
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
                Component.literal("Assign Group"),
                this::assign,
                KarakuriButton.Style.SUCCESS
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
            0xFF806546
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + PANEL_HEIGHT,
            0xFFE3B865
        );

        drawCenteredText(
            graphics,
            title,
            panelY + 18,
            0xFFFFF4DE
        );
        drawCenteredText(
            graphics,
            fit(
                "\"" + storageName + "\" is already assigned to "
                    + currentGroups
            ),
            panelY + 52,
            0xFFF4F0F6
        );
        drawCenteredText(
            graphics,
            fit(
                "Also assign it to \"" + targetGroup + "\"?"
            ),
            panelY + 74,
            0xFFFFE7B4
        );
        drawCenteredText(
            graphics,
            Component.literal(
                "Future automation rules from both groups may target this container."
            ),
            panelY + 98,
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

    private void assign() {
        assignAction.run();
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

    private Component fit(
        String value
    ) {
        int maximumWidth =
            panelWidth() - CONTENT_MARGIN * 2;

        if (font.width(value) <= maximumWidth) {
            return Component.literal(value);
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

        return Component.literal(
            value.substring(0, end) + suffix
        );
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