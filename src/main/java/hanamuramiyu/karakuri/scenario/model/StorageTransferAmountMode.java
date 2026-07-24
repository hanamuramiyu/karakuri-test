package hanamuramiyu.karakuri.scenario.model;

public enum StorageTransferAmountMode {
    ALL("all", "All matching"),
    UP_TO("up_to", "Up to amount"),
    KEEP("keep", "Keep amount"),
    TARGET("target", "Restock to target");

    private final String id;
    private final String label;

    StorageTransferAmountMode(
        String id,
        String label
    ) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public boolean usesAmount() {
        return this != ALL;
    }

    public boolean validFor(
        StorageTransferDirection direction
    ) {
        return switch (direction) {
            case DEPOSIT -> this != TARGET;
            case WITHDRAW -> this != KEEP;
        };
    }

    public StorageTransferAmountMode nextFor(
        StorageTransferDirection direction
    ) {
        StorageTransferAmountMode candidate = this;

        do {
            candidate = values()[
                (candidate.ordinal() + 1) % values().length
            ];
        } while (!candidate.validFor(direction));

        return candidate;
    }

    public static StorageTransferAmountMode fromId(
        String id
    ) {
        for (StorageTransferAmountMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
            "Unknown storage transfer amount mode: " + id
        );
    }
}