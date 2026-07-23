package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class UnsavedChangesScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 176;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 6;
    private static final int BUTTON_HEIGHT = 24;

    private final ScenarioEditorScreen editor;
    private final String scenarioName;

    public UnsavedChangesScreen(
        ScenarioEditorScreen editor,
        String scenarioName
    ) {
        super(Component.literal("Save Changes"));
        this.editor = editor;
        this.scenarioName = scenarioName;
    }

    @Override
    protected void init() {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int contentWidth =
            getPanelWidth()
                - CONTENT_MARGIN * 2;
        int buttonWidth =
            (
                contentWidth
                    - BUTTON_GAP * 2
            ) / 3;
        int buttonY =
            panelY
                + PANEL_HEIGHT
                - CONTENT_MARGIN
                - BUTTON_HEIGHT;

        KarakuriButton saveButton =
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Save"),
                editor::saveAndCloseFromPrompt,
                KarakuriButton.Style.SUCCESS
            );

        KarakuriButton discardButton =
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + buttonWidth
                    + BUTTON_GAP,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Discard"),
                editor::discardAndClose,
                KarakuriButton.Style.DANGER
            );

        KarakuriButton cancelButton =
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (
                        buttonWidth
                            + BUTTON_GAP
                    ) * 2,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Cancel"),
                () -> minecraft.setScreen(editor),
                KarakuriButton.Style.SECONDARY
            );

        saveButton.active = editor.canSaveFromPrompt();

        addRenderableWidget(saveButton);
        addRenderableWidget(discardButton);
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
            panelY + 20,
            0xFFFFF4DE
        );
        drawCenteredText(
            graphics,
            Component.literal(
                "Changes to \"" + scenarioName + "\" are unsaved."
            ),
            panelY + 56,
            0xFFF4F0F6
        );
        drawCenteredText(
            graphics,
            Component.literal(
                "Save them before leaving the editor?"
            ),
            panelY + 78,
            0xFFB9AEBE
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
        minecraft.setScreen(editor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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