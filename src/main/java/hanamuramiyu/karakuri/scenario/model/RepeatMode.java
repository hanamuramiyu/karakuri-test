package hanamuramiyu.karakuri.scenario.model;

public enum RepeatMode {
    COUNT("count", "Count"),
    FOREVER("forever", "Forever");

    private final String id;
    private final String label;

    RepeatMode(
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

    public static RepeatMode fromId(
        String id
    ) {
        for (RepeatMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
            "Unknown repeat mode: " + id
        );
    }
}