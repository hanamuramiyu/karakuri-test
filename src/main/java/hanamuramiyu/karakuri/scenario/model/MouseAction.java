package hanamuramiyu.karakuri.scenario.model;

public enum MouseAction {
    LEFT_CLICK("left_click", "Left Click"),
    RIGHT_CLICK("right_click", "Right Click");

    private final String id;
    private final String label;

    MouseAction(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static MouseAction fromId(String id) {
        for (MouseAction action : values()) {
            if (action.id.equals(id)) {
                return action;
            }
        }

        throw new IllegalArgumentException(
            "Unknown mouse action: " + id
        );
    }
}
