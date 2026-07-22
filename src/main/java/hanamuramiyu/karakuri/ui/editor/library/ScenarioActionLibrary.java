package hanamuramiyu.karakuri.ui.editor.library;

import hanamuramiyu.karakuri.ui.editor.ScenarioEditorTheme;
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
    private boolean drawerOpen;
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
            this::selectCategory,
            this::closeDrawer
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
            ScenarioEditorTheme.PANEL
        );

        graphics.renderOutline(
            layout.x(),
            layout.y(),
            layout.width(),
            layout.height(),
            ScenarioEditorTheme.OUTLINE
        );

        graphics.fill(
            layout.x(),
            layout.y(),
            layout.x() + layout.width(),
            layout.y() + 2,
            ScenarioEditorTheme.ACCENT
        );

        if (layout.isToolbar()) {
            int labelX =
                layout.x()
                    + ScenarioActionLibraryLayout.PADDING;
            int labelY = layout.y() + 5;
            int labelWidth =
                layout.toolbarCategoryX()
                    - ScenarioActionLibraryLayout.BUTTON_GAP
                    - labelX;

            graphics.fill(
                labelX,
                labelY,
                labelX + labelWidth,
                labelY
                    + ScenarioActionLibraryLayout.CATEGORY_HEIGHT,
                ScenarioEditorTheme.PANEL_ELEVATED
            );
            graphics.renderOutline(
                labelX,
                labelY,
                labelWidth,
                ScenarioActionLibraryLayout.CATEGORY_HEIGHT,
                ScenarioEditorTheme.OUTLINE
            );

            Component addAction =
                Component.literal("Add Action");

            graphics.drawString(
                font,
                addAction,
                labelX
                    + (labelWidth - font.width(addAction)) / 2,
                labelY
                    + (
                        ScenarioActionLibraryLayout.CATEGORY_HEIGHT
                            - font.lineHeight
                    ) / 2
                    + 1,
                ScenarioEditorTheme.TEXT,
                false
            );
        } else {
            graphics.drawString(
                font,
                Component.literal("Action Library"),
                layout.x()
                    + ScenarioActionLibraryLayout.PADDING,
                layout.y() + 10,
                ScenarioEditorTheme.TEXT,
                false
            );
        }

        if (!layout.isToolbar()) {
            graphics.drawString(
                font,
                Component.literal("Choose a category"),
                layout.x() + ScenarioActionLibraryLayout.PADDING,
                layout.y() + 22,
                ScenarioEditorTheme.TEXT_MUTED,
                false
            );
        }

        if (layout.height() >= 106) {
            graphics.drawString(
                font,
                Component.literal(
                    "Add "
                        + selectedCategory.sectionLabel
                        + " after the selected block"
                ),
                layout.x() + ScenarioActionLibraryLayout.PADDING,
                layout.y() + layout.height() - 13,
                ScenarioEditorTheme.TEXT_MUTED,
                false
            );
        }
    }

    public void renderDrawer(GuiGraphics graphics) {
        if (
            !visible
                || !layout.isToolbar()
                || !drawerOpen
        ) {
            return;
        }

        int drawerY = layout.drawerY();
        int drawerHeight = ScenarioActionLibraryLayout.DRAWER_HEIGHT;

        graphics.fill(
            layout.x(),
            drawerY,
            layout.x() + layout.width(),
            drawerY + drawerHeight,
            ScenarioEditorTheme.PANEL_ELEVATED
        );
        graphics.renderOutline(
            layout.x(),
            drawerY,
            layout.width(),
            drawerHeight,
            ScenarioEditorTheme.OUTLINE_STRONG
        );
        graphics.drawString(
            font,
            Component.literal(selectedCategory.sectionLabel),
            layout.x() + ScenarioActionLibraryLayout.PADDING,
            drawerY
                + (drawerHeight - font.lineHeight) / 2
                + 1,
            ScenarioEditorTheme.TEXT_MUTED,
            false
        );
    }

    private void selectCategory(Category category) {
        if (
            selectedCategory == category
                && drawerOpen
        ) {
            drawerOpen = false;
        } else {
            selectedCategory = category;
            drawerOpen = true;
        }

        updateWidgets();
    }

    private void closeDrawer() {
        if (!layout.isToolbar()) {
            return;
        }

        drawerOpen = false;
        updateWidgets();
    }

    private void updateWidgets() {
        widgets.update(
            visible,
            selectedCategory,
            !layout.isToolbar() || drawerOpen
        );
    }

    enum Category {
        MOVEMENT("Movement", "Move", "Direction actions"),
        TIMING("Timing", "Time", "Flow controls"),
        MOUSE("Mouse", "Mouse", "Mouse input"),
        CAMERA("Camera", "Camera", "Camera direction"),
        BLOCKS("Blocks", "Blocks", "Block actions"),
        INVENTORY("Inventory", "Items", "Inventory actions");

        final String panelLabel;
        final String toolbarLabel;
        final String sectionLabel;

        Category(
            String panelLabel,
            String toolbarLabel,
            String sectionLabel
        ) {
            this.panelLabel = panelLabel;
            this.toolbarLabel = toolbarLabel;
            this.sectionLabel = sectionLabel;
        }
    }
}