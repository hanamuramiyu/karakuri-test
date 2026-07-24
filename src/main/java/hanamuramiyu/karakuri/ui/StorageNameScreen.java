package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.Consumer;

public final class StorageNameScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 176;
    private static final int PANEL_MARGIN = 12;
    private static final int CONTENT_MARGIN = 16;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 24;

    private final Screen parent;
    private final String fieldLabel;
    private final String initialName;
    private final String saveLabel;
    private final Consumer<String> saveAction;

    private EditBox nameField;
    private KarakuriButton saveButton;
    private String errorMessage;

    public StorageNameScreen(
        Screen parent,
        String title,
        String fieldLabel,
        String initialName,
        String saveLabel,
        Consumer<String> saveAction
    ) {
        super(Component.literal(title));
        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        this.fieldLabel = Objects.requireNonNull(
            fieldLabel,
            "Storage name field label must not be null"
        );
        this.initialName = initialName == null
            ? ""
            : initialName;
        this.saveLabel = Objects.requireNonNull(
            saveLabel,
            "Storage save label must not be null"
        );
        this.saveAction = Objects.requireNonNull(
            saveAction,
            "Storage name action must not be null"
        );
    }

    @Override
    protected void init() {
        int panelX = panelX();
        int panelY = panelY();
        int contentWidth =
            panelWidth() - CONTENT_MARGIN * 2;

        nameField = new EditBox(
            font,
            panelX + CONTENT_MARGIN,
            panelY + 58,
            contentWidth,
            22,
            Component.literal(fieldLabel)
        );
        nameField.setMaxLength(64);
        nameField.setValue(initialName);
        nameField.setResponder(value -> {
            errorMessage = null;
            updateButton();
        });
        addRenderableWidget(nameField);
        setInitialFocus(nameField);

        int buttonWidth =
            (contentWidth - BUTTON_GAP) / 2;
        int buttonY =
            panelY
                + PANEL_HEIGHT
                - CONTENT_MARGIN
                - BUTTON_HEIGHT;

        saveButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal(saveLabel),
                this::save,
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
            save();
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
            Component.literal(fieldLabel),
            panelX + CONTENT_MARGIN,
            panelY + 44,
            0xFFB9AEBE,
            false
        );

        String validation = validationMessage();

        if (validation != null) {
            graphics.drawString(
                font,
                Component.literal(validation),
                panelX + CONTENT_MARGIN,
                panelY + 88,
                0xFFE66777,
                false
            );
        }

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

    private void save() {
        String validation = validationMessage();

        if (validation != null) {
            return;
        }

        try {
            saveAction.accept(nameField.getValue().trim());
        } catch (RuntimeException exception) {
            errorMessage = exception.getMessage() == null
                ? "Could not save the name"
                : exception.getMessage();
            updateButton();
        }
    }

    private void updateButton() {
        if (saveButton != null) {
            saveButton.active = validationMessage() == null;
        }
    }

    private String validationMessage() {
        if (errorMessage != null) {
            return errorMessage;
        }

        if (
            nameField == null
                || nameField.getValue().isBlank()
        ) {
            return "Name must not be blank";
        }

        return null;
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