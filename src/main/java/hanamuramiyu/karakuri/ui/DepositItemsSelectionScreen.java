package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.storage.StorageGroup;
import hanamuramiyu.karakuri.storage.StorageRegistry;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import hanamuramiyu.karakuri.ui.widget.KarakuriCheckboxRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class DepositItemsSelectionScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 560;
    private static final int PANEL_MAX_HEIGHT = 420;
    private static final int PANEL_MARGIN = 10;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 58;
    private static final int FOOTER_HEIGHT = 78;
    private static final int ROW_HEIGHT = 38;
    private static final int CHECKBOX_SIZE = 14;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 8;
    private static final int SCROLL_STEP = 3;

    private final Screen parent;
    private final BiConsumer<String, Boolean> selectionAction;

    private List<StorageGroup> groups = List.of();
    private String selectedGroupId;
    private boolean includeHotbar;
    private int scrollOffset;
    private KarakuriButton applyButton;

    public DepositItemsSelectionScreen(
        Screen parent,
        String selectedGroupId,
        boolean includeHotbar,
        BiConsumer<String, Boolean> selectionAction
    ) {
        super(Component.literal("Configure Deposit Items"));
        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        this.selectionAction = Objects.requireNonNull(
            selectionAction,
            "Selection action must not be null"
        );
        this.selectedGroupId = selectedGroupId;
        this.includeHotbar = includeHotbar;
    }

    @Override
    protected void init() {
        groups = StorageRegistry.groups()
            .stream()
            .filter(StorageGroup::enabled)
            .toList();

        normalizeSelection();

        int contentWidth = panelWidth() - CONTENT_MARGIN * 2;
        int buttonWidth = (contentWidth - BUTTON_GAP) / 2;
        int buttonY = panelY() + panelHeight() - 34;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX() + CONTENT_MARGIN,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Cancel"),
                this::onClose,
                KarakuriButton.Style.SECONDARY
            )
        );

        applyButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX()
                    + CONTENT_MARGIN
                    + buttonWidth
                    + BUTTON_GAP,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Apply"),
                this::applySelection,
                KarakuriButton.Style.PRIMARY
            )
        );

        clampScroll();
        updateButtons();
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
        int listX = panelX + CONTENT_MARGIN;
        int listY = panelY + HEADER_HEIGHT;
        int listWidth = panelWidth - CONTENT_MARGIN * 2;
        int listHeight = listHeight();

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
            panelY + panelHeight,
            0xFF191620
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            0xFF8063AA
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + panelHeight,
            0xFFB38AE8
        );

        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 12,
            0xFFF4F0F7,
            false
        );
        graphics.drawString(
            font,
            Component.literal(
                "Choose the registered storage group used by this action"
            ),
            panelX + CONTENT_MARGIN,
            panelY + 31,
            0xFF9F95A8,
            false
        );

        graphics.fill(
            listX,
            listY,
            listX + listWidth,
            listY + listHeight,
            0xFF100E16
        );
        graphics.renderOutline(
            listX,
            listY,
            listWidth,
            listHeight,
            0xFF393243
        );

        renderGroups(
            graphics,
            mouseX,
            mouseY,
            listX,
            listY,
            listWidth,
            listHeight
        );

        int optionY = panelY
            + panelHeight
            - FOOTER_HEIGHT
            + 6;

        KarakuriCheckboxRenderer.render(
            graphics,
            listX,
            optionY,
            CHECKBOX_SIZE,
            includeHotbar,
            0xFFB38AE8
        );

        graphics.drawString(
            font,
            Component.literal("Include hotbar"),
            listX + CHECKBOX_SIZE + 8,
            optionY + 3,
            0xFFEAE4EE,
            false
        );
        graphics.drawString(
            font,
            Component.literal(
                includeHotbar
                    ? "Main inventory and hotbar will be deposited"
                    : "Hotbar is protected"
            ),
            listX + CHECKBOX_SIZE + 8,
            optionY + 16,
            includeHotbar
                ? 0xFFF0B96A
                : 0xFF74D69B,
            false
        );

        super.render(
            graphics,
            mouseX,
            mouseY,
            delta
        );
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (super.mouseClicked(event, doubled)) {
            return true;
        }

        if (event.button() != 0) {
            return false;
        }

        int listX = panelX() + CONTENT_MARGIN;
        int listY = panelY() + HEADER_HEIGHT;
        int listWidth = panelWidth() - CONTENT_MARGIN * 2;
        int listHeight = listHeight();

        if (
            contains(
                event.x(),
                event.y(),
                listX,
                listY,
                listWidth,
                listHeight
            )
        ) {
            int index = scrollOffset
                + (int) ((event.y() - listY) / ROW_HEIGHT);

            if (index >= 0 && index < groups.size()) {
                selectedGroupId = groups.get(index).id();
                updateButtons();
            }

            return true;
        }

        int optionY = panelY()
            + panelHeight()
            - FOOTER_HEIGHT
            + 6;

        if (
            contains(
                event.x(),
                event.y(),
                listX,
                optionY,
                listWidth,
                30
            )
        ) {
            includeHotbar = !includeHotbar;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        int listX = panelX() + CONTENT_MARGIN;
        int listY = panelY() + HEADER_HEIGHT;
        int listWidth = panelWidth() - CONTENT_MARGIN * 2;
        int listHeight = listHeight();

        if (
            !contains(
                mouseX,
                mouseY,
                listX,
                listY,
                listWidth,
                listHeight
            )
        ) {
            return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
            );
        }

        scrollOffset -=
            (int) Math.signum(verticalAmount)
                * SCROLL_STEP;
        clampScroll();
        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderGroups(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        int listX,
        int listY,
        int listWidth,
        int listHeight
    ) {
        if (groups.isEmpty()) {
            graphics.drawCenteredString(
                font,
                Component.literal("No enabled storage groups"),
                listX + listWidth / 2,
                listY + listHeight / 2 - 10,
                0xFFE66777
            );
            graphics.drawCenteredString(
                font,
                Component.literal(
                    "Create one in Tools > Storage Manager"
                ),
                listX + listWidth / 2,
                listY + listHeight / 2 + 6,
                0xFF9F95A8
            );
            return;
        }

        graphics.enableScissor(
            listX + 1,
            listY + 1,
            listX + listWidth - 1,
            listY + listHeight - 1
        );

        int visibleRows = Math.max(
            1,
            listHeight / ROW_HEIGHT
        );
        int endIndex = Math.min(
            groups.size(),
            scrollOffset + visibleRows + 1
        );

        for (
            int index = scrollOffset;
            index < endIndex;
            index++
        ) {
            StorageGroup group = groups.get(index);
            int rowY = listY
                + (index - scrollOffset) * ROW_HEIGHT;
            boolean selected = group.id()
                .equals(selectedGroupId);
            boolean hovered = contains(
                mouseX,
                mouseY,
                listX + 2,
                rowY,
                listWidth - 4,
                ROW_HEIGHT - 1
            );

            graphics.fill(
                listX + 2,
                rowY,
                listX + listWidth - 2,
                rowY + ROW_HEIGHT - 1,
                selected
                    ? 0xFF2A2237
                    : hovered
                        ? 0xFF201C29
                        : 0xFF15121B
            );
            graphics.renderOutline(
                listX + 2,
                rowY,
                listWidth - 4,
                ROW_HEIGHT - 1,
                selected
                    ? group.color().color()
                    : hovered
                        ? 0xFF51465D
                        : 0xFF27222F
            );

            graphics.fill(
                listX + 8,
                rowY + 7,
                listX + 12,
                rowY + ROW_HEIGHT - 8,
                group.color().color()
            );

            graphics.drawString(
                font,
                Component.literal(
                    truncate(group.name(), listWidth - 48)
                ),
                listX + 20,
                rowY + 6,
                0xFFF0EAF4,
                false
            );

            int markerCount = StorageRegistry
                .markersForGroup(group)
                .size();
            int itemCount = group.itemFilter()
                .itemIds()
                .size();

            graphics.drawString(
                font,
                Component.literal(
                    itemCount
                        + (itemCount == 1 ? " item" : " items")
                        + " · "
                        + markerCount
                        + (markerCount == 1
                            ? " storage"
                            : " storages")
                ),
                listX + 20,
                rowY + 21,
                itemCount == 0
                    ? 0xFFE66777
                    : 0xFF9F95A8,
                false
            );
        }

        graphics.disableScissor();
    }

    private void applySelection() {
        if (selectedGroupId == null) {
            return;
        }

        selectionAction.accept(
            selectedGroupId,
            includeHotbar
        );
        minecraft.setScreen(parent);
    }

    private void normalizeSelection() {
        boolean selectedExists = groups.stream()
            .anyMatch(
                group -> group.id().equals(selectedGroupId)
            );

        if (!selectedExists) {
            selectedGroupId = groups.isEmpty()
                ? null
                : groups.getFirst().id();
        }
    }

    private void updateButtons() {
        if (applyButton != null) {
            applyButton.active = selectedGroupId != null;
        }
    }

    private void clampScroll() {
        int visibleRows = Math.max(
            1,
            listHeight() / ROW_HEIGHT
        );

        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            Math.max(0, groups.size() - visibleRows)
        );
    }

    private int panelWidth() {
        return Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_MARGIN * 2
        );
    }

    private int panelHeight() {
        return Math.min(
            PANEL_MAX_HEIGHT,
            height - PANEL_MARGIN * 2
        );
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
    }

    private int listHeight() {
        return panelHeight()
            - HEADER_HEIGHT
            - FOOTER_HEIGHT;
    }

    private String truncate(
        String value,
        int maximumWidth
    ) {
        if (font.width(value) <= maximumWidth) {
            return value;
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

        return value.substring(0, end) + suffix;
    }

    private static boolean contains(
        double mouseX,
        double mouseY,
        int x,
        int y,
        int width,
        int height
    ) {
        return mouseX >= x
            && mouseX < x + width
            && mouseY >= y
            && mouseY < y + height;
    }
}