package hanamuramiyu.karakuri.scenario.model;

import java.util.Locale;
import java.util.UUID;

public record RestockItemsStep(
    String storageGroupId,
    String itemId,
    int targetAmount,
    boolean countHotbar
) implements ScenarioStep {
    public static final String UNASSIGNED_GROUP_ID = "";
    public static final String UNASSIGNED_ITEM_ID = "";
    public static final int MIN_TARGET_AMOUNT = 1;
    public static final int MAX_TARGET_AMOUNT = 2304;
    public static final int DEFAULT_TARGET_AMOUNT = 64;
    public static final boolean DEFAULT_COUNT_HOTBAR = true;

    public RestockItemsStep {
        storageGroupId = normalizeGroupId(storageGroupId);
        itemId = normalizeItemId(itemId);

        if (
            targetAmount < MIN_TARGET_AMOUNT
                || targetAmount > MAX_TARGET_AMOUNT
        ) {
            throw new IllegalArgumentException(
                "Restock target amount must be between 1 and 2304"
            );
        }
    }

    @Override
    public int durationTicks() {
        return 1;
    }

    @Override
    public String label() {
        return "Restock item to " + targetAmount;
    }

    @Override
    public <T> T accept(
        ScenarioStepVisitor<T> visitor
    ) {
        return visitor.visit(this);
    }

    public boolean hasAssignedGroup() {
        return !storageGroupId.isEmpty();
    }

    public boolean hasAssignedItem() {
        return !itemId.isEmpty();
    }

    public RestockItemsStep withSelection(
        String updatedStorageGroupId,
        String updatedItemId,
        int updatedTargetAmount,
        boolean updatedCountHotbar
    ) {
        return new RestockItemsStep(
            updatedStorageGroupId,
            updatedItemId,
            updatedTargetAmount,
            updatedCountHotbar
        );
    }

    private static String normalizeGroupId(
        String value
    ) {
        if (value == null || value.isBlank()) {
            return UNASSIGNED_GROUP_ID;
        }

        try {
            return UUID.fromString(value.trim())
                .toString()
                .toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "Storage group ID must be a UUID: " + value,
                exception
            );
        }
    }

    private static String normalizeItemId(
        String value
    ) {
        if (value == null || value.isBlank()) {
            return UNASSIGNED_ITEM_ID;
        }

        String normalized = value
            .trim()
            .toLowerCase(Locale.ROOT);

        if (
            normalized.indexOf(':') <= 0
                || normalized.endsWith(":")
        ) {
            throw new IllegalArgumentException(
                "Restock item ID must be namespaced: " + value
            );
        }

        return normalized;
    }
}