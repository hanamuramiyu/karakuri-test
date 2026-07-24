package hanamuramiyu.karakuri.scenario.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record StorageTransferOptions(
    String storageGroupId,
    StorageTransferItemMode itemMode,
    List<String> itemIds,
    StorageTransferAmountMode amountMode,
    int amount,
    StorageTransferSpeed speed,
    boolean includeHotbar
) {
    public static final String UNASSIGNED_GROUP_ID = "";
    public static final int MIN_AMOUNT = 1;
    public static final int MAX_AMOUNT = 2304;
    public static final int DEFAULT_AMOUNT = 64;

    public StorageTransferOptions {
        storageGroupId = normalizeGroupId(storageGroupId);
        itemMode = Objects.requireNonNull(
            itemMode,
            "Storage transfer item mode must not be null"
        );
        itemIds = normalizeItemIds(itemIds);
        amountMode = Objects.requireNonNull(
            amountMode,
            "Storage transfer amount mode must not be null"
        );
        speed = Objects.requireNonNull(
            speed,
            "Storage transfer speed must not be null"
        );

        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            throw new IllegalArgumentException(
                "Storage transfer amount must be between 1 and 2304"
            );
        }
    }

    public static StorageTransferOptions depositDefaults(
        String storageGroupId,
        boolean includeHotbar
    ) {
        return new StorageTransferOptions(
            storageGroupId,
            StorageTransferItemMode.GROUP_FILTER,
            List.of(),
            StorageTransferAmountMode.ALL,
            DEFAULT_AMOUNT,
            StorageTransferSpeed.FAST,
            includeHotbar
        );
    }

    public static StorageTransferOptions restockDefaults(
        String storageGroupId,
        String itemId,
        int targetAmount,
        boolean includeHotbar
    ) {
        List<String> itemIds = itemId == null
            || itemId.isBlank()
                ? List.of()
                : List.of(itemId);

        return new StorageTransferOptions(
            storageGroupId,
            StorageTransferItemMode.SELECTED_ITEMS,
            itemIds,
            StorageTransferAmountMode.TARGET,
            targetAmount,
            StorageTransferSpeed.CONTROLLED,
            includeHotbar
        );
    }

    public boolean hasAssignedGroup() {
        return !storageGroupId.isEmpty();
    }

    public boolean hasSelectedItems() {
        return !itemIds.isEmpty();
    }

    public StorageTransferOptions withSelection(
        String updatedStorageGroupId,
        StorageTransferItemMode updatedItemMode,
        List<String> updatedItemIds,
        StorageTransferAmountMode updatedAmountMode,
        int updatedAmount,
        StorageTransferSpeed updatedSpeed,
        boolean updatedIncludeHotbar
    ) {
        return new StorageTransferOptions(
            updatedStorageGroupId,
            updatedItemMode,
            updatedItemIds,
            updatedAmountMode,
            updatedAmount,
            updatedSpeed,
            updatedIncludeHotbar
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

    private static List<String> normalizeItemIds(
        List<String> values
    ) {
        if (values == null) {
            throw new IllegalArgumentException(
                "Storage transfer item IDs must not be null"
            );
        }

        LinkedHashSet<String> normalized =
            new LinkedHashSet<>();

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }

            String itemId = value
                .trim()
                .toLowerCase(Locale.ROOT);

            if (
                itemId.indexOf(':') <= 0
                    || itemId.endsWith(":")
            ) {
                throw new IllegalArgumentException(
                    "Storage transfer item ID must be namespaced: "
                        + value
                );
            }

            normalized.add(itemId);
        }

        return List.copyOf(normalized);
    }
}