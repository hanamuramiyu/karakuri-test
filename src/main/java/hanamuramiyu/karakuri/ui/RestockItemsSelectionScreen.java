package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.RestockItemsStep;
import hanamuramiyu.karakuri.storage.StorageGroup;
import hanamuramiyu.karakuri.storage.StorageRegistry;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import hanamuramiyu.karakuri.ui.widget.KarakuriCheckboxRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RestockItemsSelectionScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 760;
    private static final int PANEL_MAX_HEIGHT = 500;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 62;
    private static final int FOOTER_HEIGHT = 104;
    private static final int COLUMN_GAP = 10;
    private static final int GROUP_ROW_HEIGHT = 38;
    private static final int ITEM_ROW_HEIGHT = 36;
    private static final int CHECKBOX_SIZE = 14;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 7;
    private static final int SCROLL_STEP = 3;

    private final Screen parent;
    private final SelectionAction selectionAction;

    private List<StorageGroup> groups = List.of();
    private List<ItemEntry> items = List.of();
    private String selectedGroupId;
    private String selectedItemId;
    private int targetAmount;
    private boolean countHotbar;
    private int groupScroll;
    private int itemScroll;
    private EditBox amountField;
    private KarakuriButton applyButton;
    private boolean amountValid = true;

    public RestockItemsSelectionScreen(
        Screen parent,
        String selectedGroupId,
        String selectedItemId,
        int targetAmount,
        boolean countHotbar,
        SelectionAction selectionAction
    ) {
        super(Component.literal("Configure Restock Items"));
        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        this.selectionAction = Objects.requireNonNull(
            selectionAction,
            "Selection action must not be null"
        );
        this.selectedGroupId = selectedGroupId;
        this.selectedItemId = selectedItemId;
        this.targetAmount = targetAmount;
        this.countHotbar = countHotbar;
    }

    @Override
    protected void init() {
        groups = StorageRegistry.groups()
            .stream()
            .filter(StorageGroup::enabled)
            .toList();

        normalizeGroupSelection();
        rebuildItems();
        normalizeItemSelection();

        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int footerY = panelY + panelHeight() - FOOTER_HEIGHT;
        int contentWidth = panelWidth - CONTENT_MARGIN * 2;

        int plusX = panelX
            + panelWidth
            - CONTENT_MARGIN
            - 28;
        int amountX = plusX - 6 - 72;

        amountField = new EditBox(
            font,
            amountX,
            footerY + 12,
            72,
            22,
            Component.literal("Target amount")
        );
        amountField.setMaxLength(4);
        amountField.setValue(Integer.toString(targetAmount));
        amountField.setResponder(this::onAmountChanged);
        addRenderableWidget(amountField);

        addRenderableWidget(
            new KarakuriButton(
                font,
                amountField.getX() - 34,
                footerY + 11,
                28,
                BUTTON_HEIGHT,
                Component.literal("-"),
                () -> adjustTarget(-1),
                KarakuriButton.Style.GHOST
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                amountField.getX() + amountField.getWidth() + 6,
                footerY + 11,
                28,
                BUTTON_HEIGHT,
                Component.literal("+"),
                () -> adjustTarget(1),
                KarakuriButton.Style.GHOST
            )
        );

        int buttonWidth = (contentWidth - BUTTON_GAP) / 2;
        int buttonY = panelY + panelHeight() - 34;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
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
                panelX
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

        clampScrolls();
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
        int panelX = panelX();
        int panelY = panelY();
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int contentX = panelX + CONTENT_MARGIN;
        int contentY = panelY + HEADER_HEIGHT;
        int contentWidth = panelWidth - CONTENT_MARGIN * 2;
        int contentHeight = panelHeight
            - HEADER_HEIGHT
            - FOOTER_HEIGHT;
        int groupWidth = Math.clamp(
            contentWidth * 42 / 100,
            140,
            300
        );
        int itemX = contentX + groupWidth + COLUMN_GAP;
        int itemWidth = contentWidth - groupWidth - COLUMN_GAP;
        int footerY = panelY + panelHeight - FOOTER_HEIGHT;

        graphics.fill(0, 0, width, height, 0xD0100E16);
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
        graphics.fill(
            panelX + 4,
            panelY,
            panelX + panelWidth,
            panelY + HEADER_HEIGHT - 4,
            0xFF1C1824
        );

        graphics.drawString(
            font,
            title,
            panelX + CONTENT_MARGIN,
            panelY + 13,
            0xFFF4F0F7,
            false
        );
        graphics.drawString(
            font,
            Component.literal(
                "Choose one filtered item and the amount to keep"
            ),
            panelX + CONTENT_MARGIN,
            panelY + 32,
            0xFF9F95A8,
            false
        );

        renderFrame(
            graphics,
            contentX,
            contentY,
            groupWidth,
            contentHeight,
            "Storage Groups"
        );
        renderFrame(
            graphics,
            itemX,
            contentY,
            itemWidth,
            contentHeight,
            "Filtered Items"
        );

        renderGroups(
            graphics,
            mouseX,
            mouseY,
            contentX + 1,
            contentY + 27,
            groupWidth - 2,
            contentHeight - 28
        );
        renderItems(
            graphics,
            mouseX,
            mouseY,
            itemX + 1,
            contentY + 27,
            itemWidth - 2,
            contentHeight - 28
        );

        KarakuriCheckboxRenderer.render(
            graphics,
            contentX,
            footerY + 13,
            CHECKBOX_SIZE,
            countHotbar,
            0xFFB38AE8
        );
        graphics.drawString(
            font,
            Component.literal("Count and fill hotbar"),
            contentX + CHECKBOX_SIZE + 8,
            footerY + 16,
            0xFFEAE4EE,
            false
        );
        graphics.drawString(
            font,
            Component.literal("Target amount"),
            amountField.getX() - 116,
            footerY + 18,
            0xFF9F95A8,
            false
        );

        String status = statusText();
        graphics.drawString(
            font,
            Component.literal(
                truncate(status, contentWidth)
            ),
            contentX,
            footerY + 45,
            amountValid
                ? 0xFF9F95A8
                : 0xFFE66777,
            false
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

        Bounds groupBounds = groupBounds();

        if (groupBounds.contains(event.x(), event.y())) {
            int row = (int) (
                (event.y() - groupBounds.y())
                    / GROUP_ROW_HEIGHT
            );
            int index = groupScroll + row;

            if (index >= 0 && index < groups.size()) {
                selectedGroupId = groups.get(index).id();
                itemScroll = 0;
                rebuildItems();
                normalizeItemSelection();
                clampScrolls();
                updateButtons();
            }

            return true;
        }

        Bounds itemBounds = itemBounds();

        if (itemBounds.contains(event.x(), event.y())) {
            int row = (int) (
                (event.y() - itemBounds.y())
                    / ITEM_ROW_HEIGHT
            );
            int index = itemScroll + row;

            if (index >= 0 && index < items.size()) {
                selectedItemId = items.get(index).id();
                updateButtons();
            }

            return true;
        }

        int footerY = panelY()
            + panelHeight()
            - FOOTER_HEIGHT;

        if (
            contains(
                event.x(),
                event.y(),
                panelX() + CONTENT_MARGIN,
                footerY + 9,
                240,
                28
            )
        ) {
            countHotbar = !countHotbar;
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
        int amount = (int) Math.signum(verticalAmount)
            * SCROLL_STEP;

        if (groupBounds().contains(mouseX, mouseY)) {
            groupScroll -= amount;
            clampScrolls();
            return true;
        }

        if (itemBounds().contains(mouseX, mouseY)) {
            itemScroll -= amount;
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

    private void rebuildItems() {
        StorageGroup group = selectedGroup();

        if (group == null) {
            items = List.of();
            return;
        }

        Map<String, ItemEntry> entries =
            new LinkedHashMap<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();

            if (stack.isEmpty()) {
                continue;
            }

            String id = BuiltInRegistries.ITEM
                .getKey(item)
                .toString();

            if (!group.itemFilter().accepts(id)) {
                continue;
            }

            entries.put(
                id,
                new ItemEntry(
                    id,
                    stack.getHoverName().getString(),
                    stack
                )
            );
        }

        List<ItemEntry> result = new ArrayList<>();

        for (String itemId : group.itemFilter().itemIds()) {
            ItemEntry entry = entries.get(itemId);

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

        items = List.copyOf(result);
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
            Component.literal(label),
            x + 9,
            y + 9,
            0xFFECE6F1,
            false
        );
    }

    private void renderGroups(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        int x,
        int y,
        int areaWidth,
        int areaHeight
    ) {
        if (groups.isEmpty()) {
            drawEmptyMessage(
                graphics,
                x,
                y,
                areaWidth,
                areaHeight,
                "No enabled storage groups"
            );
            return;
        }

        int visibleRows = Math.max(
            1,
            areaHeight / GROUP_ROW_HEIGHT
        );
        int end = Math.min(
            groups.size(),
            groupScroll + visibleRows + 1
        );

        graphics.enableScissor(
            x,
            y,
            x + areaWidth,
            y + areaHeight
        );

        for (int index = groupScroll; index < end; index++) {
            StorageGroup group = groups.get(index);
            int rowY = y
                + (index - groupScroll)
                    * GROUP_ROW_HEIGHT;
            boolean selected = group.id()
                .equals(selectedGroupId);
            boolean hovered = contains(
                mouseX,
                mouseY,
                x + 2,
                rowY,
                areaWidth - 4,
                GROUP_ROW_HEIGHT - 1
            );

            graphics.fill(
                x + 2,
                rowY,
                x + areaWidth - 2,
                rowY + GROUP_ROW_HEIGHT - 1,
                selected
                    ? 0xFF2A2237
                    : hovered
                        ? 0xFF201C29
                        : 0xFF15121B
            );
            graphics.renderOutline(
                x + 2,
                rowY,
                areaWidth - 4,
                GROUP_ROW_HEIGHT - 1,
                selected
                    ? group.color().color()
                    : hovered
                        ? 0xFF51465D
                        : 0xFF27222F
            );
            graphics.fill(
                x + 8,
                rowY + 7,
                x + 12,
                rowY + GROUP_ROW_HEIGHT - 8,
                group.color().color()
            );
            graphics.drawString(
                font,
                Component.literal(
                    truncate(group.name(), areaWidth - 34)
                ),
                x + 20,
                rowY + 6,
                0xFFF0EAF4,
                false
            );

            int itemCount = group.itemFilter()
                .itemIds()
                .size();
            graphics.drawString(
                font,
                Component.literal(
                    itemCount
                        + (itemCount == 1
                            ? " filtered item"
                            : " filtered items")
                ),
                x + 20,
                rowY + 21,
                itemCount == 0
                    ? 0xFFE66777
                    : 0xFF9F95A8,
                false
            );
        }

        graphics.disableScissor();
    }

    private void renderItems(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        int x,
        int y,
        int areaWidth,
        int areaHeight
    ) {
        if (items.isEmpty()) {
            drawEmptyMessage(
                graphics,
                x,
                y,
                areaWidth,
                areaHeight,
                "Selected group has no filtered items"
            );
            return;
        }

        int visibleRows = Math.max(
            1,
            areaHeight / ITEM_ROW_HEIGHT
        );
        int end = Math.min(
            items.size(),
            itemScroll + visibleRows + 1
        );

        graphics.enableScissor(
            x,
            y,
            x + areaWidth,
            y + areaHeight
        );

        for (int index = itemScroll; index < end; index++) {
            ItemEntry entry = items.get(index);
            int rowY = y
                + (index - itemScroll)
                    * ITEM_ROW_HEIGHT;
            boolean selected = entry.id()
                .equals(selectedItemId);
            boolean hovered = contains(
                mouseX,
                mouseY,
                x + 2,
                rowY,
                areaWidth - 4,
                ITEM_ROW_HEIGHT - 1
            );

            graphics.fill(
                x + 2,
                rowY,
                x + areaWidth - 2,
                rowY + ITEM_ROW_HEIGHT - 1,
                selected
                    ? 0xFF2A2237
                    : hovered
                        ? 0xFF201C29
                        : 0xFF15121B
            );
            graphics.renderOutline(
                x + 2,
                rowY,
                areaWidth - 4,
                ITEM_ROW_HEIGHT - 1,
                selected
                    ? 0xFFB38AE8
                    : hovered
                        ? 0xFF51465D
                        : 0xFF27222F
            );

            if (!entry.stack().isEmpty()) {
                graphics.renderItem(
                    entry.stack(),
                    x + 8,
                    rowY + 9
                );
            }

            graphics.drawString(
                font,
                Component.literal(
                    truncate(entry.name(), areaWidth - 48)
                ),
                x + 31,
                rowY + 6,
                0xFFF0EAF4,
                false
            );
            graphics.drawString(
                font,
                Component.literal(
                    truncate(entry.id(), areaWidth - 48)
                ),
                x + 31,
                rowY + 21,
                0xFF92889B,
                false
            );
        }

        graphics.disableScissor();
    }

    private void drawEmptyMessage(
        GuiGraphics graphics,
        int x,
        int y,
        int areaWidth,
        int areaHeight,
        String message
    ) {
        Component text = Component.literal(message);
        graphics.drawString(
            font,
            text,
            x + Math.max(
                8,
                (areaWidth - font.width(text)) / 2
            ),
            y + areaHeight / 2 - 4,
            0xFFE66777,
            false
        );
    }

    private void onAmountChanged(
        String value
    ) {
        try {
            int parsed = Integer.parseInt(value);
            amountValid = parsed
                >= RestockItemsStep.MIN_TARGET_AMOUNT
                && parsed
                    <= RestockItemsStep.MAX_TARGET_AMOUNT;

            if (amountValid) {
                targetAmount = parsed;
            }
        } catch (NumberFormatException exception) {
            amountValid = false;
        }

        updateButtons();
    }

    private void adjustTarget(
        int direction
    ) {
        targetAmount = Math.clamp(
            targetAmount + direction,
            RestockItemsStep.MIN_TARGET_AMOUNT,
            RestockItemsStep.MAX_TARGET_AMOUNT
        );
        amountValid = true;
        amountField.setValue(Integer.toString(targetAmount));
        updateButtons();
    }

    private void applySelection() {
        if (
            selectedGroupId == null
                || selectedItemId == null
                || !amountValid
        ) {
            return;
        }

        selectionAction.apply(
            selectedGroupId,
            selectedItemId,
            targetAmount,
            countHotbar
        );
        minecraft.setScreen(parent);
    }

    private void normalizeGroupSelection() {
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

    private void normalizeItemSelection() {
        boolean selectedExists = items.stream()
            .anyMatch(
                item -> item.id().equals(selectedItemId)
            );

        if (!selectedExists) {
            selectedItemId = items.isEmpty()
                ? null
                : items.getFirst().id();
        }
    }

    private StorageGroup selectedGroup() {
        return selectedGroupId == null
            ? null
            : StorageRegistry.findGroup(selectedGroupId);
    }

    private ItemEntry selectedItem() {
        if (selectedItemId == null) {
            return null;
        }

        return items.stream()
            .filter(item -> item.id().equals(selectedItemId))
            .findFirst()
            .orElse(null);
    }

    private String statusText() {
        if (!amountValid) {
            return "Target amount must be between 1 and 2304";
        }

        StorageGroup group = selectedGroup();
        ItemEntry item = selectedItem();

        if (group == null) {
            return "Create and enable a storage group first";
        }

        if (item == null) {
            return "Add at least one item to the selected group filter";
        }

        return "Keep "
            + targetAmount
            + " "
            + item.name()
            + (countHotbar
                ? " across inventory and hotbar"
                : " in the main inventory");
    }

    private void updateButtons() {
        if (applyButton == null) {
            return;
        }

        applyButton.active = selectedGroupId != null
            && selectedItemId != null
            && amountValid;
    }

    private void clampScrolls() {
        groupScroll = Math.clamp(
            groupScroll,
            0,
            Math.max(
                0,
                groups.size() - visibleGroupRows()
            )
        );
        itemScroll = Math.clamp(
            itemScroll,
            0,
            Math.max(
                0,
                items.size() - visibleItemRows()
            )
        );
    }

    private Bounds groupBounds() {
        int contentWidth = panelWidth()
            - CONTENT_MARGIN * 2;
        int groupWidth = Math.clamp(
            contentWidth * 42 / 100,
            140,
            300
        );

        return new Bounds(
            panelX() + CONTENT_MARGIN + 1,
            panelY() + HEADER_HEIGHT + 27,
            groupWidth - 2,
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 28
        );
    }

    private Bounds itemBounds() {
        int contentWidth = panelWidth()
            - CONTENT_MARGIN * 2;
        int groupWidth = Math.clamp(
            contentWidth * 42 / 100,
            140,
            300
        );
        int itemWidth = contentWidth
            - groupWidth
            - COLUMN_GAP;

        return new Bounds(
            panelX()
                + CONTENT_MARGIN
                + groupWidth
                + COLUMN_GAP
                + 1,
            panelY() + HEADER_HEIGHT + 27,
            itemWidth - 2,
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 28
        );
    }

    private int visibleGroupRows() {
        return Math.max(
            1,
            groupBounds().height() / GROUP_ROW_HEIGHT
        );
    }

    private int visibleItemRows() {
        return Math.max(
            1,
            itemBounds().height() / ITEM_ROW_HEIGHT
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

    private boolean contains(
        double mouseX,
        double mouseY,
        int x,
        int y,
        int areaWidth,
        int areaHeight
    ) {
        return mouseX >= x
            && mouseX < x + areaWidth
            && mouseY >= y
            && mouseY < y + areaHeight;
    }

    @FunctionalInterface
    public interface SelectionAction {
        void apply(
            String storageGroupId,
            String itemId,
            int targetAmount,
            boolean countHotbar
        );
    }

    private record ItemEntry(
        String id,
        String name,
        ItemStack stack
    ) {
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
            return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
        }
    }
}