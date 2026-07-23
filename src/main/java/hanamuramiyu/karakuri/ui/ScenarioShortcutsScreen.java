package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.ui.editor.ScenarioEditorTheme;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ScenarioShortcutsScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 620;
    private static final int PANEL_MAX_HEIGHT = 250;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 44;
    private static final int FOOTER_HEIGHT = 36;
    private static final int GROUP_GAP = 8;
    private static final int GROUP_HEADER_HEIGHT = 24;
    private static final int BUTTON_HEIGHT = 24;

    private static final List<ShortcutGroup> GROUPS = List.of(
        new ShortcutGroup(
            "FILE & HISTORY",
            "FILE",
            ScenarioEditorTheme.ACCENT,
            List.of(
                new Shortcut("Ctrl+S", "Save", "Save"),
                new Shortcut("Ctrl+Shift+S", "Save As", "Save As"),
                new Shortcut("Ctrl+Z", "Undo", "Undo"),
                new Shortcut("Ctrl+Y", "Redo", "Redo")
            )
        ),
        new ShortcutGroup(
            "EDITING",
            "EDIT",
            0xFF67C7E8,
            List.of(
                new Shortcut("Ctrl+C", "Copy", "Copy"),
                new Shortcut("Ctrl+X", "Cut", "Cut"),
                new Shortcut("Ctrl+V", "Paste", "Paste"),
                new Shortcut("Ctrl+D", "Duplicate", "Duplicate"),
                new Shortcut("Delete", "Delete", "Delete")
            )
        ),
        new ShortcutGroup(
            "WORKFLOW",
            "FLOW",
            ScenarioEditorTheme.WARNING,
            List.of(
                new Shortcut(
                    "Alt+← / →",
                    "Move",
                    "Move"
                ),
                new Shortcut("Enter", "Open group", "Open group"),
                new Shortcut(
                    "Backspace",
                    "Parent group",
                    "Parent group"
                ),
                new Shortcut(
                    "Esc",
                    "Close / leave",
                    "Close / leave"
                )
            )
        )
    );

    private final ScenarioEditorScreen editor;

    public ScenarioShortcutsScreen(
        ScenarioEditorScreen editor
    ) {
        super(Component.literal("Keyboard Shortcuts"));
        this.editor = editor;
    }

    @Override
    protected void init() {
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();
        int buttonWidth = isCompactLayout() ? 104 : 132;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + panelWidth
                    - CONTENT_MARGIN
                    - buttonWidth,
                panelY
                    + getPanelHeight()
                    - 6
                    - BUTTON_HEIGHT,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Back to Editor"),
                () -> minecraft.setScreen(editor),
                KarakuriButton.Style.PRIMARY
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
        int panelX = getPanelX();
        int panelY = getPanelY();
        int panelWidth = getPanelWidth();
        int panelHeight = getPanelHeight();
        int panelRight = panelX + panelWidth;
        int panelBottom = panelY + panelHeight;

        graphics.fill(
            0,
            0,
            width,
            height,
            ScenarioEditorTheme.SCREEN_OVERLAY
        );
        graphics.fill(
            panelX + 3,
            panelY + 3,
            panelRight + 3,
            panelBottom + 3,
            0x80000000
        );
        graphics.fill(
            panelX,
            panelY,
            panelRight,
            panelBottom,
            ScenarioEditorTheme.SHELL
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            ScenarioEditorTheme.OUTLINE_STRONG
        );
        graphics.fill(
            panelX,
            panelY,
            panelRight,
            panelY + 3,
            ScenarioEditorTheme.ACCENT
        );

        renderHeader(
            graphics,
            panelX,
            panelY,
            panelWidth
        );

        int footerY = panelBottom - FOOTER_HEIGHT;
        int contentX = panelX + CONTENT_MARGIN;
        int contentY = panelY + HEADER_HEIGHT + 4;
        int contentWidth = panelWidth - CONTENT_MARGIN * 2;
        int contentHeight = footerY - contentY - 5;
        int columnGap = isCompactLayout() ? 5 : GROUP_GAP;
        int columnWidth = (
            contentWidth
                - columnGap * (GROUPS.size() - 1)
        ) / GROUPS.size();

        for (
            int index = 0;
            index < GROUPS.size();
            index++
        ) {
            renderShortcutGroup(
                graphics,
                GROUPS.get(index),
                contentX + index * (columnWidth + columnGap),
                contentY,
                columnWidth,
                contentHeight
            );
        }

        renderFooter(
            graphics,
            panelX,
            footerY,
            panelWidth,
            panelBottom
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

    private void renderHeader(
        GuiGraphics graphics,
        int panelX,
        int panelY,
        int panelWidth
    ) {
        int contentX = panelX + CONTENT_MARGIN;
        int badgeY = panelY + 11;

        graphics.fill(
            panelX + 1,
            panelY + 3,
            panelX + panelWidth - 1,
            panelY + HEADER_HEIGHT,
            ScenarioEditorTheme.TOOLBAR
        );
        graphics.fill(
            contentX,
            badgeY,
            contentX + 20,
            badgeY + 20,
            0xFF302640
        );
        graphics.renderOutline(
            contentX,
            badgeY,
            20,
            20,
            ScenarioEditorTheme.ACCENT
        );
        drawCenteredInBox(
            graphics,
            Component.literal("K"),
            contentX,
            badgeY,
            20,
            20,
            ScenarioEditorTheme.TEXT
        );

        graphics.drawString(
            font,
            title,
            contentX + 28,
            panelY + 10,
            ScenarioEditorTheme.TEXT,
            false
        );
        graphics.drawString(
            font,
            Component.literal("Scenario editor commands"),
            contentX + 28,
            panelY + 24,
            ScenarioEditorTheme.TEXT_SECONDARY,
            false
        );

        Component count = Component.literal("13 commands");
        graphics.drawString(
            font,
            count,
            panelX
                + panelWidth
                - CONTENT_MARGIN
                - font.width(count),
            panelY + 17,
            ScenarioEditorTheme.TEXT_MUTED,
            false
        );
        graphics.fill(
            panelX + CONTENT_MARGIN,
            panelY + HEADER_HEIGHT - 1,
            panelX + panelWidth - CONTENT_MARGIN,
            panelY + HEADER_HEIGHT,
            ScenarioEditorTheme.DIVIDER
        );
    }

    private void renderShortcutGroup(
        GuiGraphics graphics,
        ShortcutGroup group,
        int x,
        int y,
        int groupWidth,
        int groupHeight
    ) {
        graphics.fill(
            x + 2,
            y + 2,
            x + groupWidth + 2,
            y + groupHeight + 2,
            0x50000000
        );
        graphics.fill(
            x,
            y,
            x + groupWidth,
            y + groupHeight,
            ScenarioEditorTheme.PANEL
        );
        graphics.renderOutline(
            x,
            y,
            groupWidth,
            groupHeight,
            ScenarioEditorTheme.OUTLINE
        );
        graphics.fill(
            x,
            y,
            x + groupWidth,
            y + 2,
            group.accentColor()
        );
        graphics.drawString(
            font,
            Component.literal(
                isCompactLayout()
                    ? group.compactTitle()
                    : group.title()
            ),
            x + 7,
            y + 8,
            group.accentColor(),
            false
        );

        Component count = Component.literal(
            Integer.toString(group.shortcuts().size())
        );
        graphics.drawString(
            font,
            count,
            x + groupWidth - 7 - font.width(count),
            y + 8,
            ScenarioEditorTheme.TEXT_MUTED,
            false
        );

        int rowY = y + GROUP_HEADER_HEIGHT;
        int rowHeight = Math.min(
            24,
            Math.max(
                21,
                (groupHeight - GROUP_HEADER_HEIGHT - 5) / 5
            )
        );

        for (
            int index = 0;
            index < group.shortcuts().size();
            index++
        ) {
            renderShortcutRow(
                graphics,
                group.shortcuts().get(index),
                group.accentColor(),
                x + 5,
                rowY + index * rowHeight,
                groupWidth - 10,
                rowHeight,
                index % 2 == 1
            );
        }
    }

    private void renderShortcutRow(
        GuiGraphics graphics,
        Shortcut shortcut,
        int accentColor,
        int x,
        int y,
        int rowWidth,
        int rowHeight,
        boolean alternate
    ) {
        graphics.fill(
            x,
            y,
            x + rowWidth,
            y + rowHeight,
            alternate
                ? 0xFF16131C
                : ScenarioEditorTheme.PANEL
        );

        if (isCompactLayout()) {
            renderCompactShortcut(
                graphics,
                shortcut,
                accentColor,
                x,
                y,
                rowWidth,
                rowHeight
            );
        } else {
            renderWideShortcut(
                graphics,
                shortcut,
                accentColor,
                x,
                y,
                rowWidth,
                rowHeight
            );
        }

        graphics.fill(
            x,
            y + rowHeight - 1,
            x + rowWidth,
            y + rowHeight,
            ScenarioEditorTheme.DIVIDER
        );
    }

    private void renderWideShortcut(
        GuiGraphics graphics,
        Shortcut shortcut,
        int accentColor,
        int x,
        int y,
        int rowWidth,
        int rowHeight
    ) {
        int keyWidth = Math.min(
            rowWidth - 12,
            Math.max(
                30,
                font.width(shortcut.key()) + 10
            )
        );
        int keyHeight = 13;
        int keyY = y + (rowHeight - keyHeight) / 2;

        renderKeycap(
            graphics,
            shortcut.key(),
            x + 3,
            keyY,
            keyWidth,
            keyHeight,
            accentColor
        );

        graphics.drawString(
            font,
            Component.literal(shortcut.action()),
            x + keyWidth + 8,
            y + (rowHeight - font.lineHeight) / 2 + 1,
            ScenarioEditorTheme.TEXT,
            false
        );
    }

    private void renderCompactShortcut(
        GuiGraphics graphics,
        Shortcut shortcut,
        int accentColor,
        int x,
        int y,
        int rowWidth,
        int rowHeight
    ) {
        int keyWidth = Math.min(
            rowWidth - 4,
            Math.max(
                28,
                font.width(shortcut.key()) + 8
            )
        );
        int keyHeight = 10;
        int keyX = x + (rowWidth - keyWidth) / 2;

        renderKeycap(
            graphics,
            shortcut.key(),
            keyX,
            y + 1,
            keyWidth,
            keyHeight,
            accentColor
        );

        Component action = Component.literal(
            shortcut.compactAction()
        );
        graphics.drawString(
            font,
            action,
            x + (rowWidth - font.width(action)) / 2,
            y + rowHeight - font.lineHeight - 1,
            ScenarioEditorTheme.TEXT_SECONDARY,
            false
        );
    }

    private void renderKeycap(
        GuiGraphics graphics,
        String key,
        int x,
        int y,
        int keyWidth,
        int keyHeight,
        int accentColor
    ) {
        graphics.fill(
            x + 1,
            y + 2,
            x + keyWidth + 1,
            y + keyHeight + 2,
            0x60000000
        );
        graphics.fill(
            x,
            y,
            x + keyWidth,
            y + keyHeight,
            ScenarioEditorTheme.PANEL_ELEVATED
        );
        graphics.renderOutline(
            x,
            y,
            keyWidth,
            keyHeight,
            accentColor
        );

        Component keyText = Component.literal(key);
        graphics.drawString(
            font,
            keyText,
            x + (keyWidth - font.width(keyText)) / 2,
            y + (keyHeight - font.lineHeight) / 2 + 1,
            ScenarioEditorTheme.TEXT,
            false
        );
    }

    private void renderFooter(
        GuiGraphics graphics,
        int panelX,
        int footerY,
        int panelWidth,
        int panelBottom
    ) {
        graphics.fill(
            panelX + 1,
            footerY,
            panelX + panelWidth - 1,
            panelBottom - 1,
            ScenarioEditorTheme.TOOLBAR
        );
        graphics.fill(
            panelX + CONTENT_MARGIN,
            footerY,
            panelX + panelWidth - CONTENT_MARGIN,
            footerY + 1,
            ScenarioEditorTheme.DIVIDER
        );

        int hintY = footerY + 11;
        renderKeycap(
            graphics,
            "Esc",
            panelX + CONTENT_MARGIN,
            hintY - 2,
            28,
            14,
            ScenarioEditorTheme.OUTLINE_STRONG
        );
        graphics.drawString(
            font,
            Component.literal("Return to editor"),
            panelX + CONTENT_MARGIN + 36,
            hintY + 1,
            ScenarioEditorTheme.TEXT_SECONDARY,
            false
        );
    }

    private int getPanelWidth() {
        return Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int getPanelHeight() {
        return Math.min(
            PANEL_MAX_HEIGHT,
            height - PANEL_MARGIN * 2
        );
    }

    private int getPanelX() {
        return (width - getPanelWidth()) / 2;
    }

    private int getPanelY() {
        return (height - getPanelHeight()) / 2;
    }

    private boolean isCompactLayout() {
        return getPanelWidth() < 430;
    }

    private void drawCenteredInBox(
        GuiGraphics graphics,
        Component text,
        int x,
        int y,
        int boxWidth,
        int boxHeight,
        int color
    ) {
        graphics.drawString(
            font,
            text,
            x + (boxWidth - font.width(text)) / 2,
            y + (boxHeight - font.lineHeight) / 2 + 1,
            color,
            false
        );
    }

    private record ShortcutGroup(
        String title,
        String compactTitle,
        int accentColor,
        List<Shortcut> shortcuts
    ) {
    }

    private record Shortcut(
        String key,
        String action,
        String compactAction
    ) {
    }
}