package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.ui.editor.ScenarioEditorTheme;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ScenarioShortcutsScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 660;
    private static final int PANEL_MAX_HEIGHT = 300;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 44;
    private static final int TAB_HEIGHT = 24;
    private static final int FOOTER_HEIGHT = 36;
    private static final int GROUP_GAP = 8;
    private static final int GROUP_HEADER_HEIGHT = 24;
    private static final int BUTTON_HEIGHT = 24;

    private static final List<ShortcutGroup> BROWSER_GROUPS = List.of(
        new ShortcutGroup(
            "GENERAL",
            "GENERAL",
            ScenarioEditorTheme.ACCENT,
            List.of(
                new Shortcut("K", "Open Karakuri", "Open"),
                new Shortcut("Esc", "Close screen", "Close"),
                new Shortcut("?", "Open this guide", "Guide")
            )
        ),
        new ShortcutGroup(
            "SCENARIO BROWSER",
            "BROWSE",
            0xFF67C7E8,
            List.of(
                new Shortcut("Ctrl+F", "Search", "Search"),
                new Shortcut("Ctrl+N", "New scenario", "New"),
                new Shortcut("↑ / ↓", "Select scenario", "Select"),
                new Shortcut("Enter", "Start / resume", "Start"),
                new Shortcut("F5", "Reload", "Reload")
            )
        ),
        new ShortcutGroup(
            "SCREEN ACTIONS",
            "ACTIONS",
            ScenarioEditorTheme.WARNING,
            List.of(
                new Shortcut("2× Click", "Edit scenario", "Edit"),
                new Shortcut("More", "Copy, export, delete", "Scenario"),
                new Shortcut("Tools", "Import, folder, reload", "Tools"),
                new Shortcut("Mode", "Once or loop", "Run mode")
            )
        )
    );

    private static final List<ShortcutGroup> EDITOR_GROUPS = List.of(
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
                new Shortcut("Alt+← / →", "Move", "Move"),
                new Shortcut("Enter", "Open group", "Open group"),
                new Shortcut("Backspace", "Parent group", "Parent group"),
                new Shortcut("Esc", "Close / leave", "Close / leave")
            )
        )
    );

    private final Screen parent;
    private Page page;
    private KarakuriButton browserTab;
    private KarakuriButton editorTab;

    public ScenarioShortcutsScreen(Screen parent) {
        super(Component.literal("Karakuri Guide"));
        this.parent = parent;
        this.page = parent instanceof ScenarioEditorScreen
            ? Page.EDITOR
            : Page.BROWSER;
    }

    @Override
    protected void init() {
        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int tabWidth = Math.min(118, (panelWidth - CONTENT_MARGIN * 2 - 6) / 2);
        int tabY = panelY + HEADER_HEIGHT + 2;

        browserTab = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                tabY,
                tabWidth,
                TAB_HEIGHT,
                Component.literal("Browser"),
                () -> setPage(Page.BROWSER),
                KarakuriButton.Style.SECONDARY
            )
        );
        editorTab = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN + tabWidth + 6,
                tabY,
                tabWidth,
                TAB_HEIGHT,
                Component.literal("Editor"),
                () -> setPage(Page.EDITOR),
                KarakuriButton.Style.SECONDARY
            )
        );

        int buttonWidth = panelWidth < 430 ? 112 : 146;
        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + panelWidth - CONTENT_MARGIN - buttonWidth,
                panelY + panelHeight() - 6 - BUTTON_HEIGHT,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal(returnLabel()),
                () -> minecraft.setScreen(parent),
                KarakuriButton.Style.PRIMARY
            )
        );

        updateTabs();
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
        int panelHeight = panelHeight();
        int panelRight = panelX + panelWidth;
        int panelBottom = panelY + panelHeight;

        graphics.fill(0, 0, width, height, ScenarioEditorTheme.SCREEN_OVERLAY);
        graphics.fill(
            panelX + 3,
            panelY + 3,
            panelRight + 3,
            panelBottom + 3,
            0x80000000
        );
        graphics.fill(panelX, panelY, panelRight, panelBottom, ScenarioEditorTheme.SHELL);
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            ScenarioEditorTheme.OUTLINE_STRONG
        );
        graphics.fill(panelX, panelY, panelRight, panelY + 3, ScenarioEditorTheme.ACCENT);

        renderHeader(graphics, panelX, panelY, panelWidth);

        int footerY = panelBottom - FOOTER_HEIGHT;
        int contentX = panelX + CONTENT_MARGIN;
        int contentY = panelY + HEADER_HEIGHT + TAB_HEIGHT + 8;
        int contentWidth = panelWidth - CONTENT_MARGIN * 2;
        int contentHeight = footerY - contentY - 5;
        List<ShortcutGroup> groups = page.groups();
        int columnGap = panelWidth < 430 ? 5 : GROUP_GAP;
        int columnWidth = (
            contentWidth - columnGap * (groups.size() - 1)
        ) / groups.size();

        for (int index = 0; index < groups.size(); index++) {
            renderShortcutGroup(
                graphics,
                groups.get(index),
                contentX + index * (columnWidth + columnGap),
                contentY,
                columnWidth,
                contentHeight
            );
        }

        renderFooter(graphics, panelX, footerY, panelWidth, panelBottom);
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

    private void setPage(Page page) {
        this.page = page;
        updateTabs();
    }

    private void updateTabs() {
        if (browserTab == null || editorTab == null) {
            return;
        }

        browserTab.setStyle(
            page == Page.BROWSER
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.SECONDARY
        );
        editorTab.setStyle(
            page == Page.EDITOR
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.SECONDARY
        );
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
        graphics.fill(contentX, badgeY, contentX + 20, badgeY + 20, 0xFF302640);
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
            Component.literal(page.subtitle()),
            contentX + 28,
            panelY + 24,
            ScenarioEditorTheme.TEXT_SECONDARY,
            false
        );

        Component count = Component.literal(page.commandCount() + " commands");
        graphics.drawString(
            font,
            count,
            panelX + panelWidth - CONTENT_MARGIN - font.width(count),
            panelY + 17,
            ScenarioEditorTheme.TEXT_MUTED,
            false
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
        graphics.fill(x, y, x + groupWidth, y + groupHeight, ScenarioEditorTheme.PANEL);
        graphics.renderOutline(
            x,
            y,
            groupWidth,
            groupHeight,
            ScenarioEditorTheme.OUTLINE
        );
        graphics.fill(x, y, x + groupWidth, y + 2, group.accentColor());
        graphics.drawString(
            font,
            Component.literal(
                panelWidth() < 430 ? group.compactTitle() : group.title()
            ),
            x + 7,
            y + (panelWidth() < 430 ? 5 : 8),
            group.accentColor(),
            false
        );

        Component count = Component.literal(Integer.toString(group.shortcuts().size()));
        graphics.drawString(
            font,
            count,
            x + groupWidth - 7 - font.width(count),
            y + (panelWidth() < 430 ? 5 : 8),
            ScenarioEditorTheme.TEXT_MUTED,
            false
        );

        boolean compact = panelWidth() < 430;
        int groupHeaderHeight = compact ? 17 : GROUP_HEADER_HEIGHT;
        int rowY = y + groupHeaderHeight;
        int rowHeight = Math.min(
            25,
            Math.max(
                compact ? 15 : 20,
                (groupHeight - groupHeaderHeight - 2)
                    / Math.max(1, group.shortcuts().size())
            )
        );

        for (int index = 0; index < group.shortcuts().size(); index++) {
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
            alternate ? 0xFF16131C : ScenarioEditorTheme.PANEL
        );

        if (panelWidth() < 430) {
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
            Math.max(30, font.width(shortcut.key()) + 10)
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
            Math.max(28, font.width(shortcut.key()) + 8)
        );
        int keyHeight = 8;
        int keyX = x + (rowWidth - keyWidth) / 2;

        renderKeycap(
            graphics,
            shortcut.key(),
            keyX,
            y,
            keyWidth,
            keyHeight,
            accentColor
        );

        Component action = Component.literal(shortcut.compactAction());
        graphics.drawString(
            font,
            action,
            x + (rowWidth - font.width(action)) / 2,
            y + Math.max(8, rowHeight - font.lineHeight),
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
        graphics.renderOutline(x, y, keyWidth, keyHeight, accentColor);

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
            Component.literal(returnHint()),
            panelX + CONTENT_MARGIN + 36,
            hintY + 1,
            ScenarioEditorTheme.TEXT_SECONDARY,
            false
        );
    }

    private String returnLabel() {
        return parent instanceof ScenarioEditorScreen
            ? "Back to Editor"
            : "Back to Karakuri";
    }

    private String returnHint() {
        return parent instanceof ScenarioEditorScreen
            ? "Return to editor"
            : "Return to scenario browser";
    }

    private int panelWidth() {
        return Math.min(PANEL_MAX_WIDTH, width - PANEL_MARGIN * 2);
    }

    private int panelHeight() {
        return Math.min(PANEL_MAX_HEIGHT, height - PANEL_MARGIN * 2);
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
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

    private enum Page {
        BROWSER(
            "Scenario browser navigation and actions",
            BROWSER_GROUPS
        ),
        EDITOR(
            "Scenario editor keyboard commands",
            EDITOR_GROUPS
        );

        private final String subtitle;
        private final List<ShortcutGroup> groups;

        Page(
            String subtitle,
            List<ShortcutGroup> groups
        ) {
            this.subtitle = subtitle;
            this.groups = groups;
        }

        private String subtitle() {
            return subtitle;
        }

        private List<ShortcutGroup> groups() {
            return groups;
        }

        private int commandCount() {
            return groups.stream()
                .mapToInt(group -> group.shortcuts().size())
                .sum();
        }
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