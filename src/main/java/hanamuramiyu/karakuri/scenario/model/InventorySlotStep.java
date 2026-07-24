package hanamuramiyu.karakuri.scenario.model;

public record InventorySlotStep(
    int inventorySlot,
    int hotbarSlot
) implements ScenarioStep {
    public static final int MIN_INVENTORY_SLOT = 0;
    public static final int MAX_INVENTORY_SLOT = 35;
    public static final int DEFAULT_INVENTORY_SLOT = 9;
    public static final int MIN_HOTBAR_SLOT = 0;
    public static final int MAX_HOTBAR_SLOT = 8;
    public static final int DEFAULT_HOTBAR_SLOT = 0;

    public InventorySlotStep {
        if (
            inventorySlot < MIN_INVENTORY_SLOT
                || inventorySlot > MAX_INVENTORY_SLOT
        ) {
            throw new IllegalArgumentException(
                "Inventory slot must be between 0 and 35"
            );
        }

        if (
            hotbarSlot < MIN_HOTBAR_SLOT
                || hotbarSlot > MAX_HOTBAR_SLOT
        ) {
            throw new IllegalArgumentException(
                "Hotbar slot must be between 0 and 8"
            );
        }
    }

    @Override
    public int durationTicks() {
        return 1;
    }

    @Override
    public String label() {
        return "Use "
            + inventorySlotLabel(inventorySlot)
            + " in hotbar slot "
            + (hotbarSlot + 1);
    }

    @Override
    public <T> T accept(
        ScenarioStepVisitor<T> visitor
    ) {
        return visitor.visit(this);
    }

    public InventorySlotStep withSelection(
        int updatedInventorySlot,
        int updatedHotbarSlot
    ) {
        return new InventorySlotStep(
            updatedInventorySlot,
            updatedHotbarSlot
        );
    }

    public static String inventorySlotLabel(
        int inventorySlot
    ) {
        if (
            inventorySlot < MIN_INVENTORY_SLOT
                || inventorySlot > MAX_INVENTORY_SLOT
        ) {
            throw new IllegalArgumentException(
                "Inventory slot must be between 0 and 35"
            );
        }

        return inventorySlot <= MAX_HOTBAR_SLOT
            ? "Hotbar " + (inventorySlot + 1)
            : "Inventory " + (inventorySlot - 8);
    }
}