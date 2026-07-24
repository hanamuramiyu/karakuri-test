package hanamuramiyu.karakuri.scenario.model;

import java.util.List;
import java.util.Objects;

public record DepositItemsStep(
    StorageTransferOptions options
) implements ScenarioStep {
    public static final String UNASSIGNED_GROUP_ID =
        StorageTransferOptions.UNASSIGNED_GROUP_ID;
    public static final boolean DEFAULT_INCLUDE_HOTBAR = false;

    public DepositItemsStep {
        options = Objects.requireNonNull(
            options,
            "Deposit transfer options must not be null"
        );

        if (
            !options.amountMode().validFor(
                StorageTransferDirection.DEPOSIT
            )
        ) {
            throw new IllegalArgumentException(
                "Deposit amount mode is not valid for deposits"
            );
        }
    }

    public DepositItemsStep(
        String storageGroupId,
        boolean includeHotbar
    ) {
        this(
            StorageTransferOptions.depositDefaults(
                storageGroupId,
                includeHotbar
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
            case ALL -> "Deposit matching items";
            case UP_TO -> "Deposit up to " + options.amount();
            case KEEP -> "Deposit and keep " + options.amount();
            case TARGET -> throw new IllegalStateException(
                "Deposit step cannot use target mode"
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

    public StorageTransferAmountMode amountMode() {
        return options.amountMode();
    }

    public int amount() {
        return options.amount();
    }

    public StorageTransferSpeed speed() {
        return options.speed();
    }

    public boolean includeHotbar() {
        return options.includeHotbar();
    }

    public boolean hasAssignedGroup() {
        return options.hasAssignedGroup();
    }

    public DepositItemsStep withSelection(
        StorageTransferOptions updatedOptions
    ) {
        return new DepositItemsStep(updatedOptions);
    }
}