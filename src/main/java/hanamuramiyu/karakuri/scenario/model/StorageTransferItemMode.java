package hanamuramiyu.karakuri.scenario.model;

public enum StorageTransferItemMode {
    GROUP_FILTER("group_filter", "Entire group filter"),
    SELECTED_ITEMS("selected_items", "Selected items");

    private final String id;
    private final String label;

    StorageTransferItemMode(
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

    public StorageTransferItemMode next() {
        StorageTransferItemMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static StorageTransferItemMode fromId(
        String id
    ) {
        for (StorageTransferItemMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
            "Unknown storage transfer item mode: " + id
        );
    }
}