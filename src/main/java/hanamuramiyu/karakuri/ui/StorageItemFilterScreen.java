package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.storage.StorageGroup;
import hanamuramiyu.karakuri.storage.StorageItemFilter;
import hanamuramiyu.karakuri.storage.StorageRegistry;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class StorageItemFilterScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 920;
    private static final int PANEL_MAX_HEIGHT = 570;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 82;
    private static final int FOOTER_HEIGHT = 72;
    private static final int COLUMN_GAP = 10;
    private static final int SELECTED_WIDTH = 330;
    private static final int SLOT_SIZE = 20;
    private static final int SELECTED_ROW_HEIGHT = 30;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 6;
    private static final int SCROLL_STEP = 3;

    private final Screen parent;
    private final String groupId;
    private final Set<String> selectedItemIds =
        new LinkedHashSet<>();
    private final Map<String, ItemEntry> entriesById =
        new LinkedHashMap<>();

    private List<ItemEntry> catalog = List.of();
    private List<ItemEntry> filteredCatalog = List.of();
    private EditBox searchField;
    private KarakuriButton clearButton;
    private KarakuriButton saveButton;
    private int catalogScrollRows;
    private int selectedScroll;
    private String hoveredLabel;

    public StorageItemFilterScreen(
        Screen parent,
        String groupId
    ) {
        super(Component.literal("Storage Item Filter"));
        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        this.groupId = Objects.requireNonNull(
            groupId,
            "Storage group ID must not be null"
        );

        StorageGroup group =
            StorageRegistry.findGroup(groupId);

        if (group == null) {
            throw new IllegalArgumentException(
                "Unknown storage group ID: " + groupId
            );
        }

        selectedItemIds.addAll(
            group.itemFilter().itemIds()
        );
    }

    @Override
    protected void init() {
        rebuildCatalog();

        int panelX = panelX();
        int panelY = panelY();
        int contentWidth =
            panelWidth() - CONTENT_MARGIN * 2;

        searchField = new EditBox(
            font,
            panelX + CONTENT_MARGIN,
            panelY + 48,
            contentWidth,
            22,
            Component.literal("Search items")
        );
        searchField.setMaxLength(96);
        searchField.setHint(
            Component.literal("Search item name or ID")
        );
        searchField.setResponder(value -> {
            catalogScrollRows = 0;
            rebuildFilteredCatalog();
        });
        addRenderableWidget(searchField);
        setInitialFocus(searchField);

        int buttonWidth =
            (contentWidth - BUTTON_GAP * 3) / 4;
        int buttonY =
            panelY
                + panelHeight()
                - CONTENT_MARGIN
                - BUTTON_HEIGHT;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Add Held Item"),
                this::addHeldItem,
                KarakuriButton.Style.PRIMARY
            )
        );

        clearButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + buttonWidth
                    + BUTTON_GAP,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Clear Filter"),
                this::clearFilter,
                KarakuriButton.Style.DANGER
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (buttonWidth + BUTTON_GAP) * 2,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Cancel"),
                this::onClose,
                KarakuriButton.Style.SECONDARY
            )
        );

        saveButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (buttonWidth + BUTTON_GAP) * 3,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Save Filter"),
                this::saveFilter,
                KarakuriButton.Style.SUCCESS
            )
        );

        rebuildFilteredCatalog();
        updateButtons();
    }

    @Override
    public void tick() {
        updateButtons();
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        hoveredLabel = null;

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int contentX = panelX + CONTENT_MARGIN;
        int contentY = panelY + HEADER_HEIGHT;
        int contentWidth =
            panelWidth - CONTENT_MARGIN * 2;
        int contentHeight =
            panelHeight
                - HEADER_HEIGHT
                - FOOTER_HEIGHT;
        int selectedWidth = Math.min(
            SELECTED_WIDTH,
            Math.max(180, contentWidth / 3)
        );
        int catalogWidth =
            contentWidth - selectedWidth - COLUMN_GAP;
        int selectedX =
            contentX + catalogWidth + COLUMN_GAP;

        graphics.fill(0, 0, width, height, 0xD0100E16);
        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + panelHeight,
            0xFF181620
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            0xFF6F5A91
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + panelHeight,
            0xFF9B79D1
        );
        graphics.fill(
            panelX + 4,
            panelY,
            panelX + panelWidth,
            panelY + HEADER_HEIGHT - 4,
            0xFF1C1824
        );

        StorageGroup group =
            StorageRegistry.findGroup(groupId);
        String groupName = group == null
            ? "Unknown Group"
            : group.name();

        graphics.drawString(
            font,
            Component.literal(
                "Item Filter · " + groupName
            ),
            panelX + CONTENT_MARGIN,
            panelY + 15,
            0xFFF6F2FA,
            false
        );
        graphics.drawString(
            font,
            Component.literal(
                "Accept selected items · click an item to toggle it"
            ),
            panelX + CONTENT_MARGIN,
            panelY + 31,
            0xFF9E94A7,
            false
        );

        renderFrame(
            graphics,
            contentX,
            contentY,
            catalogWidth,
            contentHeight,
            "Item Catalog · "
                + filteredCatalog.size()
        );
        renderFrame(
            graphics,
            selectedX,
            contentY,
            selectedWidth,
            contentHeight,
            "Selected Items · "
                + selectedItemIds.size()
        );

        renderCatalog(
            graphics,
            contentX + 1,
            contentY + 27,
            catalogWidth - 2,
            contentHeight - 28,
            mouseX,
            mouseY
        );
        renderSelectedItems(
            graphics,
            selectedX + 1,
            contentY + 27,
            selectedWidth - 2,
            contentHeight - 28,
            mouseX,
            mouseY
        );

        renderStatus(
            graphics,
            panelX + CONTENT_MARGIN,
            panelY + panelHeight - FOOTER_HEIGHT + 5,
            contentWidth
        );

        super.render(graphics, mouseX, mouseY, delta);
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

        Bounds catalogBounds = catalogBounds();
        Bounds selectedBounds = selectedBounds();

        if (
            catalogBounds.contains(event.x(), event.y())
        ) {
            ItemEntry entry = catalogEntryAt(
                event.x(),
                event.y()
            );

            if (entry != null) {
                toggleItem(entry.id());
                return true;
            }
        }

        if (
            selectedBounds.contains(event.x(), event.y())
        ) {
            ItemEntry entry = selectedEntryAt(event.y());

            if (entry != null) {
                selectedItemIds.remove(entry.id());
                clampScrolls();
                updateButtons();
                return true;
            }
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
        int amount =
            (int) Math.signum(verticalAmount)
                * SCROLL_STEP;

        if (catalogBounds().contains(mouseX, mouseY)) {
            catalogScrollRows -= amount;
            clampScrolls();
            return true;
        }

        if (selectedBounds().contains(mouseX, mouseY)) {
            selectedScroll -= amount;
            clampScrolls();
            return true;
        }

        return super.mouseScrolled(
            mouseX,
            mouseY,
            horizontalAmount,
            verticalAmount
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

    private void rebuildCatalog() {
        List<ItemEntry> entries = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();

            if (stack.isEmpty()) {
                continue;
            }

            String id = BuiltInRegistries.ITEM
                .getKey(item)
                .toString();
            String name = stack.getHoverName().getString();
            ItemEntry entry =
                new ItemEntry(id, name, stack);
            entries.add(entry);
            entriesById.put(id, entry);
        }

        entries.sort(
            Comparator.comparing(
                ItemEntry::name,
                String.CASE_INSENSITIVE_ORDER
            ).thenComparing(ItemEntry::id)
        );
        catalog = List.copyOf(entries);
    }

    private void rebuildFilteredCatalog() {
        if (searchField == null) {
            filteredCatalog = catalog;
            return;
        }

        String query = searchField
            .getValue()
            .trim()
            .toLowerCase(Locale.ROOT);

        filteredCatalog = query.isEmpty()
            ? catalog
            : catalog.stream()
                .filter(
                    entry ->
                        entry.name()
                            .toLowerCase(Locale.ROOT)
                            .contains(query)
                            || entry.id()
                                .toLowerCase(Locale.ROOT)
                                .contains(query)
                )
                .toList();

        clampScrolls();
    }

    private void renderFrame(
        GuiGraphics graphics,
        int x,
        int y,
        int frameWidth,
        int frameHeight,
        String label
    ) {
        graphics.fill(
            x,
            y,
            x + frameWidth,
            y + frameHeight,
            0xFF100E16
        );
        graphics.renderOutline(
            x,
            y,
            frameWidth,
            frameHeight,
            0xFF393243
        );
        graphics.fill(
            x + 1,
            y + 1,
            x + frameWidth - 1,
            y + 25,
            0xFF17141F
        );
        graphics.drawString(
            font,
            Component.literal(
                truncate(label, frameWidth - 18)
            ),
            x + 9,
            y + 9,
            0xFFECE6F1,
            false
        );
    }

    private void renderCatalog(
        GuiGraphics graphics,
        int x,
        int y,
        int areaWidth,
        int areaHeight,
        int mouseX,
        int mouseY
    ) {
        int columns = catalogColumns();
        int rows = catalogVisibleRows();
        int firstIndex =
            catalogScrollRows * columns;
        int end = Math.min(
            filteredCatalog.size(),
            firstIndex + rows * columns
        );

        graphics.enableScissor(
            x,
            y,
            x + areaWidth,
            y + areaHeight
        );

        for (int index = firstIndex; index < end; index++) {
            int localIndex = index - firstIndex;
            int column = localIndex % columns;
            int row = localIndex / columns;
            int slotX = x + 6 + column * SLOT_SIZE;
            int slotY = y + 6 + row * SLOT_SIZE;
            ItemEntry entry = filteredCatalog.get(index);
            boolean selected =
                selectedItemIds.contains(entry.id());
            boolean hovered = contains(
                mouseX,
                mouseY,
                slotX,
                slotY,
                SLOT_SIZE,
                SLOT_SIZE
            );

            graphics.fill(
                slotX,
                slotY,
                slotX + SLOT_SIZE,
                slotY + SLOT_SIZE,
                selected
                    ? 0xFF2C2440
                    : hovered
                        ? 0xFF241F2E
                        : 0xFF17141F
            );
            graphics.renderOutline(
                slotX,
                slotY,
                SLOT_SIZE,
                SLOT_SIZE,
                selected
                    ? 0xFFB38AE8
                    : hovered
                        ? 0xFF756682
                        : 0xFF393243
            );
            graphics.renderItem(
                entry.stack(),
                slotX + 2,
                slotY + 2
            );

            if (selected) {
                graphics.fill(
                    slotX + SLOT_SIZE - 5,
                    slotY + 2,
                    slotX + SLOT_SIZE - 2,
                    slotY + 5,
                    0xFF61D394
                );
            }

            if (hovered) {
                hoveredLabel =
                    entry.name() + " · " + entry.id();
            }
        }

        graphics.disableScissor();
    }

    private void renderSelectedItems(
        GuiGraphics graphics,
        int x,
        int y,
        int areaWidth,
        int areaHeight,
        int mouseX,
        int mouseY
    ) {
        List<ItemEntry> selected = selectedEntries();

        if (selected.isEmpty()) {
            Component heading =
                Component.literal("No items selected");
            Component detail =
                Component.literal(
                    "This group currently accepts nothing"
                );
            int centerY = y + areaHeight / 2;

            graphics.drawString(
                font,
                heading,
                x + (areaWidth - font.width(heading)) / 2,
                centerY - 10,
                0xFFECE6F1,
                false
            );
            graphics.drawString(
                font,
                detail,
                x + Math.max(
                    8,
                    (areaWidth - font.width(detail)) / 2
                ),
                centerY + 7,
                0xFF81778A,
                false
            );
            return;
        }

        int visibleRows =
            Math.max(1, areaHeight / SELECTED_ROW_HEIGHT);
        int end = Math.min(
            selected.size(),
            selectedScroll + visibleRows + 1
        );

        graphics.enableScissor(
            x,
            y,
            x + areaWidth,
            y + areaHeight
        );

        for (
            int index = selectedScroll;
            index < end;
            index++
        ) {
            int rowY =
                y + (index - selectedScroll)
                    * SELECTED_ROW_HEIGHT;
            ItemEntry entry = selected.get(index);
            boolean hovered = contains(
                mouseX,
                mouseY,
                x + 2,
                rowY,
                areaWidth - 4,
                SELECTED_ROW_HEIGHT - 1
            );

            graphics.fill(
                x + 2,
                rowY,
                x + areaWidth - 2,
                rowY + SELECTED_ROW_HEIGHT - 1,
                hovered ? 0xFF241F2E : 0xFF15121B
            );
            graphics.renderOutline(
                x + 2,
                rowY,
                areaWidth - 4,
                SELECTED_ROW_HEIGHT - 1,
                hovered ? 0xFF756682 : 0xFF302A39
            );

            if (!entry.stack().isEmpty()) {
                graphics.renderItem(
                    entry.stack(),
                    x + 7,
                    rowY + 6
                );
            }

            graphics.drawString(
                font,
                Component.literal(
                    truncate(entry.name(), areaWidth - 56)
                ),
                x + 30,
                rowY + 6,
                0xFFF0EAF4,
                false
            );
            graphics.drawString(
                font,
                Component.literal(
                    truncate(entry.id(), areaWidth - 56)
                ),
                x + 30,
                rowY + 18,
                0xFF92889B,
                false
            );

            if (hovered) {
                hoveredLabel =
                    "Click to remove · "
                        + entry.name()
                        + " · "
                        + entry.id();
            }
        }

        graphics.disableScissor();
    }

    private void renderStatus(
        GuiGraphics graphics,
        int x,
        int y,
        int availableWidth
    ) {
        StorageItemFilter filter =
            currentFilter();
        List<StorageGroup> overlaps =
            StorageRegistry.overlappingGroups(
                groupId,
                filter
            );

        String status;

        if (!overlaps.isEmpty()) {
            status = "Warning: shared storage filters overlap with "
                + overlaps.stream()
                    .map(
                        group -> group.name()
                            + " ("
                            + filter.overlapCount(
                                group.itemFilter()
                            )
                            + ")"
                    )
                    .collect(Collectors.joining(", "));
        } else if (hoveredLabel != null) {
            status = hoveredLabel;
        } else if (selectedItemIds.isEmpty()) {
            status =
                "Select catalog items or add the item held in your main hand";
        } else {
            status = selectedItemIds.size()
                + (selectedItemIds.size() == 1
                    ? " item selected"
                    : " items selected");
        }

        graphics.drawString(
            font,
            Component.literal(
                truncate(status, availableWidth)
            ),
            x,
            y,
            overlaps.isEmpty()
                ? 0xFF9E94A7
                : 0xFFF1C36E,
            false
        );
    }

    private void addHeldItem() {
        if (
            minecraft.player == null
                || minecraft.player
                    .getMainHandItem()
                    .isEmpty()
        ) {
            return;
        }

        ItemStack stack =
            minecraft.player.getMainHandItem();
        String itemId = BuiltInRegistries.ITEM
            .getKey(stack.getItem())
            .toString();

        selectedItemIds.add(itemId);
        clampScrolls();
        updateButtons();
    }

    private void clearFilter() {
        selectedItemIds.clear();
        selectedScroll = 0;
        updateButtons();
    }

    private void saveFilter() {
        StorageGroup group =
            StorageRegistry.findGroup(groupId);

        if (group == null) {
            minecraft.setScreen(parent);
            return;
        }

        StorageRegistry.replaceGroup(
            group.withItemFilter(currentFilter())
        );
        minecraft.setScreen(parent);
    }

    private void toggleItem(
        String itemId
    ) {
        if (!selectedItemIds.remove(itemId)) {
            selectedItemIds.add(itemId);
        }

        clampScrolls();
        updateButtons();
    }

    private StorageItemFilter currentFilter() {
        return new StorageItemFilter(
            List.copyOf(selectedItemIds)
        );
    }

    private List<ItemEntry> selectedEntries() {
        List<ItemEntry> result = new ArrayList<>();

        for (String itemId : selectedItemIds) {
            ItemEntry entry = entriesById.get(itemId);

            if (entry == null) {
                result.add(
                    new ItemEntry(
                        itemId,
                        itemId,
                        ItemStack.EMPTY
                    )
                );
            } else {
                result.add(entry);
            }
        }

        return List.copyOf(result);
    }

    private ItemEntry catalogEntryAt(
        double mouseX,
        double mouseY
    ) {
        Bounds bounds = catalogBounds();
        int localX =
            (int) mouseX - bounds.x() - 6;
        int localY =
            (int) mouseY - bounds.y() - 6;

        if (localX < 0 || localY < 0) {
            return null;
        }

        int columns = catalogColumns();
        int column = localX / SLOT_SIZE;
        int row = localY / SLOT_SIZE;

        if (
            column < 0
                || column >= columns
                || row < 0
                || row >= catalogVisibleRows()
        ) {
            return null;
        }

        int index =
            catalogScrollRows * columns
                + row * columns
                + column;

        return index >= 0
            && index < filteredCatalog.size()
                ? filteredCatalog.get(index)
                : null;
    }

    private ItemEntry selectedEntryAt(
        double mouseY
    ) {
        Bounds bounds = selectedBounds();
        int row =
            ((int) mouseY - bounds.y())
                / SELECTED_ROW_HEIGHT;
        int index = selectedScroll + row;
        List<ItemEntry> selected = selectedEntries();

        return index >= 0 && index < selected.size()
            ? selected.get(index)
            : null;
    }

    private void updateButtons() {
        if (clearButton == null || saveButton == null) {
            return;
        }

        clearButton.active = !selectedItemIds.isEmpty();
        saveButton.active =
            StorageRegistry.findGroup(groupId) != null;
    }

    private void clampScrolls() {
        int columns = catalogColumns();
        int totalRows = Math.ceilDiv(
            filteredCatalog.size(),
            columns
        );
        catalogScrollRows = Math.clamp(
            catalogScrollRows,
            0,
            Math.max(
                0,
                totalRows - catalogVisibleRows()
            )
        );

        int selectedVisibleRows = Math.max(
            1,
            selectedBounds().height()
                / SELECTED_ROW_HEIGHT
        );
        selectedScroll = Math.clamp(
            selectedScroll,
            0,
            Math.max(
                0,
                selectedItemIds.size()
                    - selectedVisibleRows
            )
        );
    }

    private Bounds catalogBounds() {
        int contentX = panelX() + CONTENT_MARGIN;
        int contentY = panelY() + HEADER_HEIGHT + 27;
        int contentWidth =
            panelWidth() - CONTENT_MARGIN * 2;
        int selectedWidth = Math.min(
            SELECTED_WIDTH,
            Math.max(180, contentWidth / 3)
        );
        int catalogWidth =
            contentWidth - selectedWidth - COLUMN_GAP;

        return new Bounds(
            contentX + 1,
            contentY,
            catalogWidth - 2,
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 28
        );
    }

    private Bounds selectedBounds() {
        int contentX = panelX() + CONTENT_MARGIN;
        int contentY = panelY() + HEADER_HEIGHT + 27;
        int contentWidth =
            panelWidth() - CONTENT_MARGIN * 2;
        int selectedWidth = Math.min(
            SELECTED_WIDTH,
            Math.max(180, contentWidth / 3)
        );
        int catalogWidth =
            contentWidth - selectedWidth - COLUMN_GAP;

        return new Bounds(
            contentX
                + catalogWidth
                + COLUMN_GAP
                + 1,
            contentY,
            selectedWidth - 2,
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 28
        );
    }

    private int catalogColumns() {
        return Math.max(
            1,
            (catalogBounds().width() - 12)
                / SLOT_SIZE
        );
    }

    private int catalogVisibleRows() {
        return Math.max(
            1,
            (catalogBounds().height() - 12)
                / SLOT_SIZE
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

    private record ItemEntry(
        String id,
        String name,
        ItemStack stack
    ) {
        private ItemEntry {
            Objects.requireNonNull(id, "Item ID must not be null");
            Objects.requireNonNull(name, "Item name must not be null");
            Objects.requireNonNull(stack, "Item stack must not be null");
        }
    }

    private record Bounds(
        int x,
        int y,
        int width,
        int height
    ) {
        private boolean contains(
            double mouseX,
            double mouseY
        ) {
            return StorageItemFilterScreen.contains(
                mouseX,
                mouseY,
                x,
                y,
                width,
                height
            );
        }
    }
}