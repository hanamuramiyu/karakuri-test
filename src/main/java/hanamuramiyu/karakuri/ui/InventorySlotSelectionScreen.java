package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class InventorySlotSelectionScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 228;
    private static final int PANEL_MARGIN = 12;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_COLUMNS = 9;
    private static final int GRID_WIDTH = SLOT_SIZE * GRID_COLUMNS;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_HEIGHT = 22;

    private final Screen parent;
    private final BiConsumer<Integer, Integer> selectionAction;

    private int inventorySlot;
    private int hotbarSlot;

    public InventorySlotSelectionScreen(
        Screen parent,
        int inventorySlot,
        int hotbarSlot,
        BiConsumer<Integer, Integer> selectionAction
    ) {
        super(Component.literal("Select Inventory Slot"));
        this.parent = Objects.requireNonNull(
            parent,
            "Parent screen must not be null"
        );
        this.selectionAction = Objects.requireNonNull(
            selectionAction,
            "Selection action must not be null"
        );
        this.inventorySlot = inventorySlot;
        this.hotbarSlot = hotbarSlot;
    }

    @Override
    protected void init() {
        int buttonWidth = (panelWidth() - 32 - BUTTON_GAP) / 2;
        int buttonY = panelY() + PANEL_HEIGHT - 32;

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX() + 16,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Cancel"),
                this::onClose,
                KarakuriButton.Style.SECONDARY
            )
        );

        addRenderableWidget(
            new KarakuriButton(
                font,
                panelX() + 16 + buttonWidth + BUTTON_GAP,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                Component.literal("Apply Selection"),
                this::applySelection,
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
        graphics.fill(
            0,
            0,
            width,
            height,
            0xD0100E16
        );

        int panelX = panelX();
        int panelY = panelY();
        int gridX = gridX();
        int mainGridY = mainGridY();
        int sourceHotbarY = sourceHotbarY();
        int targetHotbarY = targetHotbarY();

        graphics.fill(
            panelX,
            panelY,
            panelX + panelWidth(),
            panelY + PANEL_HEIGHT,
            0xFF191620
        );
        graphics.renderOutline(
            panelX,
            panelY,
            panelWidth(),
            PANEL_HEIGHT,
            0xFF8063AA
        );
        graphics.fill(
            panelX,
            panelY,
            panelX + 4,
            panelY + PANEL_HEIGHT,
            0xFFB38AE8
        );

        drawCentered(
            graphics,
            title,
            panelY + 10,
            0xFFF4F0F7
        );

        graphics.drawString(
            font,
            Component.literal("Source inventory slot"),
            gridX,
            mainGridY - 14,
            0xFFB9AEBE,
            false
        );

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < GRID_COLUMNS; column++) {
                int slot = 9 + row * GRID_COLUMNS + column;
                renderInventorySlot(
                    graphics,
                    slot,
                    gridX + column * SLOT_SIZE,
                    mainGridY + row * SLOT_SIZE,
                    mouseX,
                    mouseY
                );
            }
        }

        for (int column = 0; column < GRID_COLUMNS; column++) {
            renderInventorySlot(
                graphics,
                column,
                gridX + column * SLOT_SIZE,
                sourceHotbarY,
                mouseX,
                mouseY
            );
        }

        graphics.drawString(
            font,
            Component.literal(
                "Source: "
                    + InventorySlotStep.inventorySlotLabel(
                        inventorySlot
                    )
            ),
            gridX,
            sourceHotbarY + SLOT_SIZE + 7,
            0xFFE8D26A,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Working hotbar slot"),
            gridX,
            targetHotbarY - 14,
            0xFFB9AEBE,
            false
        );

        for (int column = 0; column < GRID_COLUMNS; column++) {
            renderTargetSlot(
                graphics,
                column,
                gridX + column * SLOT_SIZE,
                targetHotbarY,
                mouseX,
                mouseY
            );
        }

        graphics.drawString(
            font,
            Component.literal(
                "Target: Hotbar "
                    + (hotbarSlot + 1)
            ),
            gridX,
            targetHotbarY + SLOT_SIZE + 7,
            0xFF67C7E8,
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
        if (event.button() == 0) {
            int source = sourceSlotAt(
                event.x(),
                event.y()
            );

            if (source >= 0) {
                inventorySlot = source;
                return true;
            }

            int target = targetSlotAt(
                event.x(),
                event.y()
            );

            if (target >= 0) {
                hotbarSlot = target;
                return true;
            }
        }

        return super.mouseClicked(event, doubled);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void applySelection() {
        selectionAction.accept(
            inventorySlot,
            hotbarSlot
        );
        minecraft.setScreen(parent);
    }

    private void renderInventorySlot(
        GuiGraphics graphics,
        int slot,
        int x,
        int y,
        int mouseX,
        int mouseY
    ) {
        boolean selected = slot == inventorySlot;
        boolean hovered = contains(
            mouseX,
            mouseY,
            x,
            y,
            SLOT_SIZE,
            SLOT_SIZE
        );

        renderSlotFrame(
            graphics,
            x,
            y,
            selected,
            hovered,
            0xFFE8D26A
        );

        ItemStack stack = stack(slot);

        if (!stack.isEmpty()) {
            graphics.renderItem(
                stack,
                x + 1,
                y + 1
            );
            graphics.renderItemDecorations(
                font,
                stack,
                x + 1,
                y + 1
            );
        }
    }

    private void renderTargetSlot(
        GuiGraphics graphics,
        int slot,
        int x,
        int y,
        int mouseX,
        int mouseY
    ) {
        boolean selected = slot == hotbarSlot;
        boolean hovered = contains(
            mouseX,
            mouseY,
            x,
            y,
            SLOT_SIZE,
            SLOT_SIZE
        );

        renderSlotFrame(
            graphics,
            x,
            y,
            selected,
            hovered,
            0xFF67C7E8
        );

        Component number = Component.literal(
            Integer.toString(slot + 1)
        );

        graphics.drawString(
            font,
            number,
            x + (SLOT_SIZE - font.width(number)) / 2,
            y + (SLOT_SIZE - font.lineHeight) / 2 + 1,
            selected ? 0xFFF4F0F7 : 0xFFB9AEBE,
            false
        );
    }

    private void renderSlotFrame(
        GuiGraphics graphics,
        int x,
        int y,
        boolean selected,
        boolean hovered,
        int accentColor
    ) {
        graphics.fill(
            x,
            y,
            x + SLOT_SIZE,
            y + SLOT_SIZE,
            selected
                ? 0xFF2A2434
                : hovered
                    ? 0xFF221E2A
                    : 0xFF100E16
        );
        graphics.renderOutline(
            x,
            y,
            SLOT_SIZE,
            SLOT_SIZE,
            selected
                ? accentColor
                : hovered
                    ? 0xFF756682
                    : 0xFF393243
        );
    }

    private ItemStack stack(
        int slot
    ) {
        return minecraft.player == null
            ? ItemStack.EMPTY
            : minecraft.player
                .getInventory()
                .getItem(slot);
    }

    private int sourceSlotAt(
        double mouseX,
        double mouseY
    ) {
        int column = (int) ((mouseX - gridX()) / SLOT_SIZE);

        if (column < 0 || column >= GRID_COLUMNS) {
            return -1;
        }

        int mainRow = (int) ((mouseY - mainGridY()) / SLOT_SIZE);

        if (
            mouseX >= gridX()
                && mouseX < gridX() + GRID_WIDTH
                && mainRow >= 0
                && mainRow < 3
                && mouseY >= mainGridY()
                && mouseY < mainGridY() + SLOT_SIZE * 3
        ) {
            return 9 + mainRow * GRID_COLUMNS + column;
        }

        if (
            contains(
                mouseX,
                mouseY,
                gridX(),
                sourceHotbarY(),
                GRID_WIDTH,
                SLOT_SIZE
            )
        ) {
            return column;
        }

        return -1;
    }

    private int targetSlotAt(
        double mouseX,
        double mouseY
    ) {
        if (
            !contains(
                mouseX,
                mouseY,
                gridX(),
                targetHotbarY(),
                GRID_WIDTH,
                SLOT_SIZE
            )
        ) {
            return -1;
        }

        return (int) ((mouseX - gridX()) / SLOT_SIZE);
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

    private int gridX() {
        return panelX() + (panelWidth() - GRID_WIDTH) / 2;
    }

    private int mainGridY() {
        return panelY() + 38;
    }

    private int sourceHotbarY() {
        return mainGridY() + SLOT_SIZE * 3 + 4;
    }

    private int targetHotbarY() {
        return sourceHotbarY() + SLOT_SIZE + 38;
    }

    private void drawCentered(
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