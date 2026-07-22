package hanamuramiyu.karakuri.ui.editor.library;

import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public final class ScenarioActionLibrary {
    private final Font font;
    private final ScenarioActionLibraryLayout layout;
    private final ScenarioActionLibraryWidgets widgets;

    private Category selectedCategory = Category.MOVEMENT;
    private boolean visible = true;

    public ScenarioActionLibrary(
        Font font,
        ScenarioActionLibraryLayout layout,
        ScenarioActionLibraryActions actions
    ) {
        this.font = Objects.requireNonNull(font, "Font must not be null");
        this.layout = Objects.requireNonNull(layout, "Layout must not be null");

        widgets = new ScenarioActionLibraryWidgets(
            font,
            layout,
            Objects.requireNonNull(actions, "Actions must not be null"),
            this::selectCategory
        );
        updateWidgets();
    }

    public List<KarakuriButton> widgets() {
        return widgets.all();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        updateWidgets();
    }

    public void render(GuiGraphics graphics) {
        if (!visible) {
            return;
        }

        graphics.fill(
            layout.x(),
            layout.y(),
            layout.x() + layout.width(),
            layout.y() + layout.height(),
            0xFF121018
        );

        graphics.renderOutline(
            layout.x(),
            layout.y(),
            layout.width(),
            layout.height(),
            0xFF393243
        );

        graphics.fill(
            layout.x(),
            layout.y(),
            layout.x() + 3,
            layout.y() + layout.height(),
            0xFF776092
        );

        graphics.drawString(
            font,
            Component.literal("Action Library"),
            layout.x() + ScenarioActionLibraryLayout.PADDING,
            layout.y() + 9,
            0xFFF1ECF5,
            false
        );

        if (layout.isSidebar()) {
            renderSidebarHeader(graphics);
        }

        if (layout.height() >= 118) {
            graphics.drawString(
                font,
                Component.literal(
                    layout.isSidebar()
                        ? "Inserted after selected block"
                        : "Inserted after the selected block"
                ),
                layout.x() + ScenarioActionLibraryLayout.PADDING,
                layout.y() + layout.height() - 14,
                0xFF716A79,
                false
            );
        }
    }

    private void renderSidebarHeader(GuiGraphics graphics) {
        graphics.drawString(
            font,
            Component.literal("Categories"),
            layout.x() + ScenarioActionLibraryLayout.PADDING,
            layout.y() + 20,
            0xFF81778A,
            false
        );

        int dividerY = layout.sidebarDividerY();

        graphics.fill(
            layout.x() + ScenarioActionLibraryLayout.PADDING,
            dividerY,
            layout.x() + layout.width() - ScenarioActionLibraryLayout.PADDING,
            dividerY + 1,
            0xFF302B37
        );

        graphics.drawString(
            font,
            Component.literal("Add " + selectedCategory.sectionLabel),
            layout.x() + ScenarioActionLibraryLayout.PADDING,
            dividerY + 8,
            0xFF9E94A8,
            false
        );
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        updateWidgets();
    }

    private void updateWidgets() {
        widgets.update(visible, selectedCategory);
    }

    enum Category {
        MOVEMENT("Movement", "Move", "Movement"),
        TIMING("Timing", "Time", "Timing"),
        MOUSE("Mouse", "Mouse", "Mouse"),
        CAMERA("Camera", "Camera", "Camera"),
        BLOCKS("Blocks  ·  Soon", "Blocks", "Blocks"),
        INVENTORY("Inventory", "Inventory", "Hotbar");

        final String sidebarLabel;
        final String horizontalLabel;
        final String sectionLabel;

        Category(
            String sidebarLabel,
            String horizontalLabel,
            String sectionLabel
        ) {
            this.sidebarLabel = sidebarLabel;
            this.horizontalLabel = horizontalLabel;
            this.sectionLabel = sectionLabel;
        }
    }
}