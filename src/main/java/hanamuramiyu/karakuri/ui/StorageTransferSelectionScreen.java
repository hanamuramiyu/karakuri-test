package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.StorageTransferAmountMode;
import hanamuramiyu.karakuri.scenario.model.StorageTransferDirection;
import hanamuramiyu.karakuri.scenario.model.StorageTransferItemMode;
import hanamuramiyu.karakuri.scenario.model.StorageTransferOptions;
import hanamuramiyu.karakuri.scenario.model.StorageTransferSpeed;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class StorageTransferSelectionScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 860;
    private static final int PANEL_MAX_HEIGHT = 560;
    private static final int PANEL_MARGIN = 8;
    private static final int CONTENT_MARGIN = 14;
    private static final int HEADER_HEIGHT = 62;
    private static final int FOOTER_HEIGHT = 142;
    private static final int COLUMN_GAP = 10;
    private static final int GROUP_ROW_HEIGHT = 38;
    private static final int ITEM_ROW_HEIGHT = 38;
    private static final int CHECKBOX_SIZE = 14;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 7;
    private static final int SCROLL_STEP = 3;

    private final Screen parent;
    private final StorageTransferDirection direction;
    private final SelectionAction selectionAction;

    private List<StorageGroup> groups = List.of();
    private List<ItemEntry> items = List.of();
    private final Set<String> selectedItemIds =
        new LinkedHashSet<>();
    private String selectedGroupId;
    private StorageTransferItemMode itemMode;
    private StorageTransferAmountMode amountMode;
    private int amount;
    private StorageTransferSpeed speed;
    private boolean includeHotbar;
    private int groupScroll;
    private int itemScroll;
    private boolean amountValid = true;
    private EditBox amountField;
    private KarakuriButton itemModeButton;
    private KarakuriButton amountModeButton;
    private KarakuriButton speedButton;
    private KarakuriButton applyButton;

    public StorageTransferSelectionScreen(
        Screen parent,
        StorageTransferDirection direction,
        StorageTransferOptions options,
        SelectionAction selectionAction
    ) {
        super(
            Component.literal(
                "Configure " + direction.label()
            )
        );
        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        this.direction = Objects.requireNonNull(
            direction,
            "Storage transfer direction must not be null"
        );
        this.selectionAction = Objects.requireNonNull(
            selectionAction,
            "Selection action must not be null"
        );

        StorageTransferOptions initial =
            Objects.requireNonNull(
                options,
                "Storage transfer options must not be null"
            );

        selectedGroupId = initial.storageGroupId();
        itemMode = initial.itemMode();
        selectedItemIds.addAll(initial.itemIds());
        amountMode = initial.amountMode();
        amount = initial.amount();
        speed = initial.speed();
        includeHotbar = initial.includeHotbar();
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
        int contentWidth = panelWidth - CONTENT_MARGIN * 2;
        int footerY = panelY + panelHeight() - FOOTER_HEIGHT;
        int modeButtonWidth = Math.max(
            130,
            (contentWidth - BUTTON_GAP * 3 - 180) / 3
        );

        itemModeButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX + CONTENT_MARGIN,
                footerY + 10,
                modeButtonWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::cycleItemMode,
                KarakuriButton.Style.SECONDARY
            )
        );

        amountModeButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + modeButtonWidth
                    + BUTTON_GAP,
                footerY + 10,
                modeButtonWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::cycleAmountMode,
                KarakuriButton.Style.SECONDARY
            )
        );

        speedButton = addRenderableWidget(
            new KarakuriButton(
                font,
                panelX
                    + CONTENT_MARGIN
                    + (modeButtonWidth + BUTTON_GAP) * 2,
                footerY + 10,
                modeButtonWidth,
                BUTTON_HEIGHT,
                Component.empty(),
                this::cycleSpeed,
                KarakuriButton.Style.SECONDARY
            )
        );

        int amountX = panelX
            + CONTENT_MARGIN
            + 104;

        amountField = new EditBox(
            font,
            amountX,
            footerY + 46,
            76,
            22,
            Component.literal("Transfer amount")
        );
        amountField.setMaxLength(4);
        amountField.setValue(Integer.toString(amount));
        amountField.setResponder(this::onAmountChanged);
        addRenderableWidget(amountField);

        addRenderableWidget(
            new KarakuriButton(
                font,
                amountX - 34,
                footerY + 45,
                28,
                BUTTON_HEIGHT,
                Component.literal("-"),
                () -> adjustAmount(-1),
                KarakuriButton.Style.GHOST
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                amountX + amountField.getWidth() + 6,
                footerY + 45,
                28,
                BUTTON_HEIGHT,
                Component.literal("+"),
                () -> adjustAmount(1),
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
            contentWidth * 34 / 100,
            160,
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
            Component.literal(headerDescription()),
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
            itemMode == StorageTransferItemMode.GROUP_FILTER
                ? "Filtered Items · Entire Filter"
                : "Filtered Items · Select Multiple"
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

        graphics.drawString(
            font,
            Component.literal("Amount"),
            contentX,
            footerY + 52,
            amountMode.usesAmount()
                ? 0xFF9F95A8
                : 0xFF625B68,
            false
        );

        int hotbarX = panelX
            + panelWidth
            - CONTENT_MARGIN
            - 230;
        int hotbarY = footerY + 49;

        KarakuriCheckboxRenderer.render(
            graphics,
            hotbarX,
            hotbarY,
            CHECKBOX_SIZE,
            includeHotbar,
            0xFFB38AE8
        );
        graphics.drawString(
            font,
            Component.literal(hotbarLabel()),
            hotbarX + CHECKBOX_SIZE + 8,
            hotbarY + 3,
            0xFFEAE4EE,
            false
        );

        String status = statusText();
        graphics.drawString(
            font,
            Component.literal(
                truncate(status, contentWidth)
            ),
            contentX,
            footerY + 78,
            configurationValid()
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
            if (
                itemMode
                    == StorageTransferItemMode.SELECTED_ITEMS
            ) {
                int row = (int) (
                    (event.y() - itemBounds.y())
                        / ITEM_ROW_HEIGHT
                );
                int index = itemScroll + row;

                if (index >= 0 && index < items.size()) {
                    String itemId = items.get(index).id();

                    if (!selectedItemIds.add(itemId)) {
                        selectedItemIds.remove(itemId);
                    }

                    updateButtons();
                }
            }

            return true;
        }

        int footerY = panelY()
            + panelHeight()
            - FOOTER_HEIGHT;
        int hotbarX = panelX()
            + panelWidth()
            - CONTENT_MARGIN
            - 230;

        if (
            contains(
                event.x(),
                event.y(),
                hotbarX,
                footerY + 43,
                230,
                28
            )
        ) {
            includeHotbar = !includeHotbar;
            updateButtons();
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
        int scroll = (int) Math.signum(verticalAmount)
            * SCROLL_STEP;

        if (groupBounds().contains(mouseX, mouseY)) {
            groupScroll -= scroll;
            clampScrolls();
            return true;
        }

        if (itemBounds().contains(mouseX, mouseY)) {
            itemScroll -= scroll;
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

    private void cycleItemMode() {
        itemMode = itemMode.next();
        normalizeItemSelection();
        updateButtons();
    }

    private void cycleAmountMode() {
        amountMode = amountMode.nextFor(direction);
        updateButtons();
    }

    private void cycleSpeed() {
        speed = speed.next();
        updateButtons();
    }

    private void onAmountChanged(
        String value
    ) {
        try {
            int parsed = Integer.parseInt(value);
            amountValid = parsed
                >= StorageTransferOptions.MIN_AMOUNT
                && parsed
                    <= StorageTransferOptions.MAX_AMOUNT;

            if (amountValid) {
                amount = parsed;
            }
        } catch (NumberFormatException exception) {
            amountValid = false;
        }

        updateButtons();
    }

    private void adjustAmount(
        int direction
    ) {
        amount = Math.clamp(
            amount + direction,
            StorageTransferOptions.MIN_AMOUNT,
            StorageTransferOptions.MAX_AMOUNT
        );
        amountValid = true;
        amountField.setValue(Integer.toString(amount));
        updateButtons();
    }

    private void applySelection() {
        if (!configurationValid()) {
            return;
        }

        selectionAction.apply(
            new StorageTransferOptions(
                selectedGroupId,
                itemMode,
                selectedItemIds.stream().toList(),
                amountMode,
                amount,
                speed,
                includeHotbar
            )
        );
        minecraft.setScreen(parent);
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

            result.add(
                entry == null
                    ? new ItemEntry(
                        itemId,
                        itemId,
                        ItemStack.EMPTY
                    )
                    : entry
            );
        }

        items = List.copyOf(result);
    }

    private void normalizeGroupSelection() {
        boolean exists = groups.stream()
            .anyMatch(
                group -> group.id().equals(selectedGroupId)
            );

        if (!exists) {
            selectedGroupId = groups.isEmpty()
                ? null
                : groups.getFirst().id();
        }
    }

    private void normalizeItemSelection() {
        Set<String> available = items.stream()
            .map(ItemEntry::id)
            .collect(
                java.util.stream.Collectors.toCollection(
                    LinkedHashSet::new
                )
            );

        selectedItemIds.retainAll(available);

        if (
            itemMode == StorageTransferItemMode.SELECTED_ITEMS
                && selectedItemIds.isEmpty()
                && !items.isEmpty()
        ) {
            selectedItemIds.add(items.getFirst().id());
        }
    }

    private StorageGroup selectedGroup() {
        return selectedGroupId == null
            ? null
            : StorageRegistry.findGroup(selectedGroupId);
    }

    private boolean configurationValid() {
        StorageGroup group = selectedGroup();

        if (
            group == null
                || group.itemFilter().emptyFilter()
        ) {
            return false;
        }

        if (
            itemMode == StorageTransferItemMode.SELECTED_ITEMS
                && selectedItemIds.isEmpty()
        ) {
            return false;
        }

        return !amountMode.usesAmount() || amountValid;
    }

    private String statusText() {
        StorageGroup group = selectedGroup();

        if (group == null) {
            return "Create and enable a storage group first";
        }

        if (group.itemFilter().emptyFilter()) {
            return "Add at least one item to the selected group filter";
        }

        if (
            itemMode == StorageTransferItemMode.SELECTED_ITEMS
                && selectedItemIds.isEmpty()
        ) {
            return "Select at least one filtered item";
        }

        if (amountMode.usesAmount() && !amountValid) {
            return "Amount must be between 1 and 2304";
        }

        String itemsText = itemMode
            == StorageTransferItemMode.GROUP_FILTER
                ? group.itemFilter().itemIds().size()
                    + " filtered item types"
                : selectedItemIds.size()
                    + " selected item types";

        return direction == StorageTransferDirection.DEPOSIT
            ? depositStatus(itemsText)
            : withdrawStatus(itemsText);
    }

    private String depositStatus(
        String itemsText
    ) {
        return switch (amountMode) {
            case ALL -> "Deposit all matching " + itemsText;
            case UP_TO -> "Deposit up to "
                + amount
                + " of each of "
                + itemsText;
            case KEEP -> "Keep "
                + amount
                + " of each type and deposit the excess from "
                + itemsText;
            case TARGET -> throw new IllegalStateException(
                "Deposit screen cannot use target mode"
            );
        };
    }

    private String withdrawStatus(
        String itemsText
    ) {
        return switch (amountMode) {
            case ALL -> "Take all available " + itemsText;
            case UP_TO -> "Take up to "
                + amount
                + " of each of "
                + itemsText;
            case TARGET -> "Restock each of "
                + itemsText
                + " to "
                + amount;
            case KEEP -> throw new IllegalStateException(
                "Restock screen cannot use keep mode"
            );
        };
    }

    private String headerDescription() {
        return direction == StorageTransferDirection.DEPOSIT
            ? "Choose what the player gives to the targeted storage"
            : "Choose what the player takes from the targeted storage";
    }

    private String hotbarLabel() {
        return direction == StorageTransferDirection.DEPOSIT
            ? "Include hotbar"
            : "Count and fill hotbar";
    }

    private void updateButtons() {
        if (
            itemModeButton == null
                || amountModeButton == null
                || speedButton == null
                || amountField == null
                || applyButton == null
        ) {
            return;
        }

        itemModeButton.setMessage(
            Component.literal(
                itemMode == StorageTransferItemMode.GROUP_FILTER
                    ? "Items: Entire Filter"
                    : "Items: Selected ("
                        + selectedItemIds.size()
                        + ")"
            )
        );
        amountModeButton.setMessage(
            Component.literal(
                "Amount: " + amountModeLabel()
            )
        );
        speedButton.setMessage(
            Component.literal("Speed: " + speed.label())
        );
        amountField.active = amountMode.usesAmount();
        applyButton.active = configurationValid();
    }

    private String amountModeLabel() {
        return switch (direction) {
            case DEPOSIT -> switch (amountMode) {
                case ALL -> "All";
                case UP_TO -> "Up To";
                case KEEP -> "Keep";
                case TARGET -> "Target";
            };
            case WITHDRAW -> switch (amountMode) {
                case ALL -> "Take All";
                case UP_TO -> "Take Up To";
                case TARGET -> "Target";
                case KEEP -> "Keep";
            };
        };
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
            boolean selected = itemMode
                == StorageTransferItemMode.GROUP_FILTER
                    || selectedItemIds.contains(entry.id());
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

            int itemX = x + 8;

            if (
                itemMode
                    == StorageTransferItemMode.SELECTED_ITEMS
            ) {
                KarakuriCheckboxRenderer.render(
                    graphics,
                    itemX,
                    rowY + 12,
                    CHECKBOX_SIZE,
                    selectedItemIds.contains(entry.id()),
                    0xFFB38AE8
                );
                itemX += CHECKBOX_SIZE + 7;
            }

            if (!entry.stack().isEmpty()) {
                graphics.renderItem(
                    entry.stack(),
                    itemX,
                    rowY + 10
                );
            }

            int textX = itemX + 23;
            graphics.drawString(
                font,
                Component.literal(
                    truncate(
                        entry.name(),
                        areaWidth - (textX - x) - 8
                    )
                ),
                textX,
                rowY + 6,
                0xFFF0EAF4,
                false
            );
            graphics.drawString(
                font,
                Component.literal(
                    truncate(
                        entry.id(),
                        areaWidth - (textX - x) - 8
                    )
                ),
                textX,
                rowY + 22,
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

    private Bounds groupBounds() {
        int contentWidth = panelWidth()
            - CONTENT_MARGIN * 2;
        int groupWidth = Math.clamp(
            contentWidth * 34 / 100,
            160,
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
            contentWidth * 34 / 100,
            160,
            300
        );

        return new Bounds(
            panelX()
                + CONTENT_MARGIN
                + groupWidth
                + COLUMN_GAP
                + 1,
            panelY() + HEADER_HEIGHT + 27,
            contentWidth - groupWidth - COLUMN_GAP - 2,
            panelHeight()
                - HEADER_HEIGHT
                - FOOTER_HEIGHT
                - 28
        );
    }

    private int panelX() {
        return (width - panelWidth()) / 2;
    }

    private int panelY() {
        return (height - panelHeight()) / 2;
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

    @FunctionalInterface
    public interface SelectionAction {
        void apply(StorageTransferOptions options);
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
        boolean contains(
            double mouseX,
            double mouseY
        ) {
            return StorageTransferSelectionScreen.contains(
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