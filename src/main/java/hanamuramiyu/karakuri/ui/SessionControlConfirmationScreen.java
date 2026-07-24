package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class SessionControlConfirmationScreen extends Screen {
    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 184;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 24;

    private final Screen parent;
    private final Action action;
    private final int count;
    private final String targetLabel;
    private final Runnable confirmAction;

    public SessionControlConfirmationScreen(
        Screen parent,
        Action action,
        int count,
        String targetLabel,
        Runnable confirmAction
    ) {
        super(Component.literal(title(action)));

        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        this.action = Objects.requireNonNull(
            action,
            "Session control action must not be null"
        );

        if (count <= 0) {
            throw new IllegalArgumentException(
                "Affected session count must be positive"
            );
        }

        this.count = count;
        this.targetLabel = targetLabel == null
            ? ""
            : targetLabel.trim();
        this.confirmAction = Objects.requireNonNull(
            confirmAction,
            "Confirmation action must not be null"
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
                Component.literal(confirmLabel()),
                this::confirm,
                confirmStyle()
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
            panelY + PANEL_HEIGHT,
            0xFF191620
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            PANEL_HEIGHT,
            outlineColor()
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + PANEL_HEIGHT,
            accentColor()
        );

        drawCenteredText(
            graphics,
            title,
            panelY + 19,
            titleColor()
        );
        drawCenteredText(
            graphics,
            Component.literal(question()),
            panelY + 52,
            0xFFF4F0F6
        );
        drawCenteredText(
            graphics,
            Component.literal(detail()),
            panelY + 76,
            0xFFB9AEBE
        );
        drawCenteredText(
            graphics,
            Component.literal(warning()),
            panelY + 96,
            warningColor()
        );

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

    private void confirm() {
        confirmAction.run();
    }

    private String question() {
        String scenarios = count == 1
            ? "1 scenario"
            : count + " scenarios";

        return switch (action) {
            case PAUSE_SELECTED ->
                "Pause selected " + scenarios + "?";
            case STOP_SELECTED ->
                "Stop selected " + scenarios + "?";
            case EMERGENCY_STOP_ALL ->
                "Stop all " + scenarios + "?";
            case PAUSE_QUICK_SLOT ->
                "Pause "
                    + scenarios
                    + " from "
                    + targetLabel
                    + "?";
            case STOP_QUICK_SLOT ->
                "Stop "
                    + scenarios
                    + " from "
                    + targetLabel
                    + "?";
        };
    }

    private String detail() {
        return switch (action) {
            case PAUSE_SELECTED,
                 PAUSE_QUICK_SLOT ->
                "Their controlled inputs will be released.";
            case STOP_SELECTED,
                 STOP_QUICK_SLOT ->
                "Their current execution progress will be discarded.";
            case EMERGENCY_STOP_ALL ->
                "Every Karakuri-controlled input will be released.";
        };
    }

    private String warning() {
        return switch (action) {
            case PAUSE_SELECTED,
                 PAUSE_QUICK_SLOT ->
                "You can resume them later.";
            case STOP_SELECTED,
                 STOP_QUICK_SLOT,
                 EMERGENCY_STOP_ALL ->
                "This action cannot be undone.";
        };
    }

    private String confirmLabel() {
        return switch (action) {
            case PAUSE_SELECTED -> "Pause Selected";
            case STOP_SELECTED -> "Stop Selected";
            case EMERGENCY_STOP_ALL -> "Emergency Stop All";
            case PAUSE_QUICK_SLOT -> "Pause Slot";
            case STOP_QUICK_SLOT -> "Stop Slot";
        };
    }

    private KarakuriButton.Style confirmStyle() {
        return isPauseAction()
            ? KarakuriButton.Style.SECONDARY
            : KarakuriButton.Style.DANGER;
    }

    private int outlineColor() {
        return isPauseAction()
            ? 0xFF806546
            : 0xFF87505D;
    }

    private int accentColor() {
        return isPauseAction()
            ? 0xFFE3B865
            : 0xFFE66777;
    }

    private int titleColor() {
        return isPauseAction()
            ? 0xFFFFF4DE
            : 0xFFFFF1F3;
    }

    private int warningColor() {
        return isPauseAction()
            ? 0xFFE3B865
            : 0xFFE18B97;
    }

    private boolean isPauseAction() {
        return action == Action.PAUSE_SELECTED
            || action == Action.PAUSE_QUICK_SLOT;
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

    private static String title(Action action) {
        return switch (action) {
            case PAUSE_SELECTED,
                 PAUSE_QUICK_SLOT ->
                "Confirm Pause";
            case STOP_SELECTED,
                 STOP_QUICK_SLOT ->
                "Confirm Stop";
            case EMERGENCY_STOP_ALL ->
                "Confirm Emergency Stop";
        };
    }

    public enum Action {
        PAUSE_SELECTED,
        STOP_SELECTED,
        EMERGENCY_STOP_ALL,
        PAUSE_QUICK_SLOT,
        STOP_QUICK_SLOT
    }
}