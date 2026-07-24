package hanamuramiyu.karakuri.scenario.model;

import java.util.List;
import java.util.Objects;

public record RestockItemsStep(
    StorageTransferOptions options
) implements ScenarioStep {
    public static final String UNASSIGNED_GROUP_ID =
        StorageTransferOptions.UNASSIGNED_GROUP_ID;
    public static final String UNASSIGNED_ITEM_ID = "";
    public static final int MIN_TARGET_AMOUNT =
        StorageTransferOptions.MIN_AMOUNT;
    public static final int MAX_TARGET_AMOUNT =
        StorageTransferOptions.MAX_AMOUNT;
    public static final int DEFAULT_TARGET_AMOUNT =
        StorageTransferOptions.DEFAULT_AMOUNT;
    public static final boolean DEFAULT_COUNT_HOTBAR = true;

    public RestockItemsStep {
        options = Objects.requireNonNull(
            options,
            "Restock transfer options must not be null"
        );

        if (
            !options.amountMode().validFor(
                StorageTransferDirection.WITHDRAW
            )
        ) {
            throw new IllegalArgumentException(
                "Restock amount mode is not valid for withdrawals"
            );
        }
    }

    public RestockItemsStep(
        String storageGroupId,
        String itemId,
        int targetAmount,
        boolean countHotbar
    ) {
        this(
            StorageTransferOptions.restockDefaults(
                storageGroupId,
                itemId,
                targetAmount,
                countHotbar
            )
        );
    }

    @Override
    public int durationTicks() {
        return 1;
    }

    @Override
    public String label() {
        return switch (options.amountMode()) {
            case ALL -> "Take all matching items";
            case UP_TO -> "Take up to " + options.amount();
            case TARGET -> "Restock items to " + options.amount();
            case KEEP -> throw new IllegalStateException(
                "Restock step cannot use keep mode"
            );
        };
    }

    @Override
    public <T> T accept(
        ScenarioStepVisitor<T> visitor
    ) {
        return visitor.visit(this);
    }

    public String storageGroupId() {
        return options.storageGroupId();
    }

    public StorageTransferItemMode itemMode() {
        return options.itemMode();
    }

    public List<String> itemIds() {
        return options.itemIds();
    }

    public String itemId() {
        return options.itemIds().isEmpty()
            ? UNASSIGNED_ITEM_ID
            : options.itemIds().getFirst();
    }

    public StorageTransferAmountMode amountMode() {
        return options.amountMode();
    }

    public int amount() {
        return options.amount();
    }

    public int targetAmount() {
        return options.amount();
    }

    public StorageTransferSpeed speed() {
        return options.speed();
    }

    public boolean includeHotbar() {
        return options.includeHotbar();
    }

    public boolean countHotbar() {
        return options.includeHotbar();
    }

    public boolean hasAssignedGroup() {
        return options.hasAssignedGroup();
    }

    public boolean hasAssignedItem() {
        return options.itemMode()
            == StorageTransferItemMode.GROUP_FILTER
            || options.hasSelectedItems();
    }

    public RestockItemsStep withSelection(
        StorageTransferOptions updatedOptions
    ) {
        return new RestockItemsStep(updatedOptions);
    }
}