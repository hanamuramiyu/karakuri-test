package hanamuramiyu.karakuri.scenario.model;

import java.util.Locale;
import java.util.UUID;

public record DepositItemsStep(
    String storageGroupId,
    boolean includeHotbar
) implements ScenarioStep {
    public static final String UNASSIGNED_GROUP_ID = "";
    public static final boolean DEFAULT_INCLUDE_HOTBAR = false;

    public DepositItemsStep {
        storageGroupId = normalizeGroupId(storageGroupId);
    }

    @Override
    public int durationTicks() {
        return 1;
    }

    @Override
    public String label() {
        return "Deposit matching items";
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

    public DepositItemsStep withSelection(
        String updatedStorageGroupId,
        boolean updatedIncludeHotbar
    ) {
        return new DepositItemsStep(
            updatedStorageGroupId,
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
}