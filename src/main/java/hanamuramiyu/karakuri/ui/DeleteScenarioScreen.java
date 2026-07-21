package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.Scenario;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DeleteScenarioScreen extends Screen {
    private static final int PANEL_WIDTH = 380;
    private static final int PANEL_HEIGHT = 176;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 24;

    private final KarakuriScreen parent;
    private final int scenarioIndex;
    private final Scenario scenario;

    public DeleteScenarioScreen(
        KarakuriScreen parent,
        int scenarioIndex,
        Scenario scenario
    ) {
        super(Component.literal("Delete Scenario"));

        this.parent = parent;
        this.scenarioIndex = scenarioIndex;
        this.scenario = scenario;
    }

    @Override
    protected void init() {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int contentWidth =
            getPanelWidth()
                - CONTENT_MARGIN * 2;

        int buttonWidth =
            (contentWidth - BUTTON_GAP) / 2;

        KarakuriButton deleteButton =
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                panelY
                    + PANEL_HEIGHT
                    - CONTENT_MARGIN
                    - BUTTON_HEIGHT,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal(
                    "Delete Permanently"
                ),
                this::deleteScenario,
                KarakuriButton.Style.DANGER
            );

        KarakuriButton cancelButton =
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + buttonWidth
                    + BUTTON_GAP,
                panelY
                    + PANEL_HEIGHT
                    - CONTENT_MARGIN
                    - BUTTON_HEIGHT,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Cancel"),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.SECONDARY
            );

        addRenderableWidget(deleteButton);
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();

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
            0xFF87505D
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
            panelY + 18,
            0xFFFFF1F3
        );

        drawCenteredText(
            graphics,
            Component.literal(
                "\"" + scenario.name() + "\""
            ),
            panelY + 50,
            0xFFF4F0F6
        );

        drawCenteredText(
            graphics,
            Component.literal(
                "Its .karakuri file will be removed."
            ),
            panelY + 76,
            0xFFB9AEBE
        );

        drawCenteredText(
            graphics,
            Component.literal(
                "This action cannot be undone."
            ),
            panelY + 92,
            0xFFE18B97
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

    private void deleteScenario() {
        ScenarioLibrary.delete(scenarioIndex);
        parent.refreshAfterDeletion(
            scenarioIndex
        );
        minecraft.setScreen(parent);
    }

    private int getPanelWidth() {
        return Math.min(
            PANEL_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int getPanelX() {
        return (
            width - getPanelWidth()
        ) / 2;
    }

    private int getPanelY() {
        return (
            height - PANEL_HEIGHT
        ) / 2;
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
            (
                width
                    - font.width(text)
            ) / 2,
            y,
            color,
            false
        );
    }
}