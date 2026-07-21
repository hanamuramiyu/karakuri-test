package hanamuramiyu.karakuri.scenario.model;

public enum MouseStopMode {
    DURATION("duration", "Time"),
    CLICK_COUNT("click_count", "Clicks"),
    MANUAL("manual", "Manual");

    private final String id;
    private final String label;

    MouseStopMode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static MouseStopMode fromId(String id) {
        for (MouseStopMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
            "Unknown mouse stop mode: " + id
        );
    }
}
