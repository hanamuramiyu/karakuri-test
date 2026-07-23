package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public final class SaveScenarioAsScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 168;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 24;

    private final ScenarioEditorScreen editor;
    private final String initialName;
    private final Consumer<String> saveAction;

    private EditBox nameField;
    private KarakuriButton saveButton;

    public SaveScenarioAsScreen(
        ScenarioEditorScreen editor,
        String initialName,
        Consumer<String> saveAction
    ) {
        super(Component.literal("Save Scenario As"));
        this.editor = editor;
        this.initialName = initialName;
        this.saveAction = saveAction;
    }

    @Override
    protected void init() {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int contentWidth =
            getPanelWidth()
                - CONTENT_MARGIN * 2;

        nameField = new EditBox(
            font,
            panelX + CONTENT_MARGIN,
            panelY + 58,
            contentWidth,
            22,
            Component.literal("Scenario name")
        );
        nameField.setMaxLength(64);
        nameField.setValue(initialName);
        nameField.setResponder(value -> updateButton());
        addRenderableWidget(nameField);

        int buttonWidth =
            (contentWidth - BUTTON_GAP) / 2;
        int buttonY =
            panelY
                + PANEL_HEIGHT
                - CONTENT_MARGIN
                - BUTTON_HEIGHT;

        saveButton = new KarakuriButton(
            font,
            panelX + CONTENT_MARGIN,
            buttonY,
            buttonWidth,
            BUTTON_HEIGHT,
            Component.literal("Save As"),
            this::saveScenario,
            KarakuriButton.Style.SUCCESS
        );

        KarakuriButton cancelButton =
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
                () -> minecraft.setScreen(editor),
                KarakuriButton.Style.SECONDARY
            );

        addRenderableWidget(saveButton);
        addRenderableWidget(cancelButton);
        updateButton();
    }

    @Override
    public boolean keyPressed(
        KeyEvent event
    ) {
        if (
            event.key() == GLFW.GLFW_KEY_ENTER
                || event.key() == GLFW.GLFW_KEY_KP_ENTER
        ) {
            saveScenario();
            return true;
        }

        return super.keyPressed(event);
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
            0xFF645174
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + PANEL_HEIGHT,
            0xFFB38AE8
        );

        drawCenteredText(
            graphics,
            title,
            panelY + 20,
            0xFFF6F2FA
        );

        graphics.drawString(
            font,
            Component.literal("New scenario name"),
            panelX + CONTENT_MARGIN,
            panelY + 44,
            0xFFB9AEBE,
            false
        );

        String validationMessage =
            validationMessage();

        if (validationMessage != null) {
            graphics.drawString(
                font,
                Component.literal(validationMessage),
                panelX + CONTENT_MARGIN,
                panelY + 86,
                0xFFE66777,
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
        minecraft.setScreen(editor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void saveScenario() {
        if (validationMessage() != null) {
            return;
        }

        saveAction.accept(
            nameField.getValue().trim()
        );
    }

    private void updateButton() {
        if (saveButton != null) {
            saveButton.active =
                validationMessage() == null;
        }
    }

    private String validationMessage() {
        if (nameField == null) {
            return null;
        }

        String name =
            nameField.getValue().trim();

        if (name.isBlank()) {
            return "Scenario name is required";
        }

        if (ScenarioLibrary.containsName(name, -1)) {
            return "A scenario with this name already exists";
        }

        return null;
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
