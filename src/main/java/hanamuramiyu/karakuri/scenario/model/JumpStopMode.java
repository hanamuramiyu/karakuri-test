package hanamuramiyu.karakuri.scenario.model;

public enum JumpStopMode {
    DURATION("duration", "Time"),
    JUMP_COUNT("jump_count", "Jumps"),
    MANUAL("manual", "Manual");

    private final String id;
    private final String label;

    JumpStopMode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static JumpStopMode fromId(String id) {
        for (JumpStopMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
            "Unknown jump stop mode: " + id
        );
    }
}
