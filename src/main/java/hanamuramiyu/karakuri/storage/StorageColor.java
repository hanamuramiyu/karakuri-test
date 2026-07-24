package hanamuramiyu.karakuri.storage;

public enum StorageColor {
    PURPLE("purple", "Purple", 0xFFB38AE8),
    BLUE("blue", "Blue", 0xFF6FA8FF),
    CYAN("cyan", "Cyan", 0xFF67C7E8),
    GREEN("green", "Green", 0xFF61D394),
    YELLOW("yellow", "Yellow", 0xFFF1D06E),
    ORANGE("orange", "Orange", 0xFFF0A35E),
    RED("red", "Red", 0xFFE66777),
    PINK("pink", "Pink", 0xFFE88AC7);

    private final String id;
    private final String label;
    private final int color;

    StorageColor(
        String id,
        String label,
        int color
    ) {
        this.id = id;
        this.label = label;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public int color() {
        return color;
    }

    public int fillColor() {
        return color & 0x00FFFFFF | 0x44000000;
    }

    public StorageColor next() {
        StorageColor[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static StorageColor fromId(
        String id
    ) {
        for (StorageColor color : values()) {
            if (color.id.equals(id)) {
                return color;
            }
        }

        throw new IllegalArgumentException(
            "Unknown storage color: " + id
        );
    }
}