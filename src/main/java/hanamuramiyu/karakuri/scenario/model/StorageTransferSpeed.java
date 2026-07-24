package hanamuramiyu.karakuri.scenario.model;

public enum StorageTransferSpeed {
    FAST("fast", "Fast"),
    CONTROLLED("controlled", "Controlled");

    private final String id;
    private final String label;

    StorageTransferSpeed(
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

    public StorageTransferSpeed next() {
        StorageTransferSpeed[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static StorageTransferSpeed fromId(
        String id
    ) {
        for (StorageTransferSpeed speed : values()) {
            if (speed.id.equals(id)) {
                return speed;
            }
        }

        throw new IllegalArgumentException(
            "Unknown storage transfer speed: " + id
        );
    }
}