package hanamuramiyu.karakuri.scenario.model;

public enum JumpMode {
    SINGLE("single", "Single"),
    HOLD("hold", "Hold"),
    REPEAT("repeat", "Repeat");

    private final String id;
    private final String label;

    JumpMode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static JumpMode fromId(String id) {
        for (JumpMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
            "Unknown jump mode: " + id
        );
    }
}
