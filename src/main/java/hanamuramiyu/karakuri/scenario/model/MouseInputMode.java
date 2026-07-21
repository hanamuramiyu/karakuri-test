package hanamuramiyu.karakuri.scenario.model;

public enum MouseInputMode {
    HOLD("hold", "Hold"),
    CLICK("click", "Click");

    private final String id;
    private final String label;

    MouseInputMode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static MouseInputMode fromId(String id) {
        for (MouseInputMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
            "Unknown mouse input mode: " + id
        );
    }
}
