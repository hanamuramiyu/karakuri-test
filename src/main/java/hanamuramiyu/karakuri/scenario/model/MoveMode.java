package hanamuramiyu.karakuri.scenario.model;

public enum MoveMode {
    WALK("walk", "Walk"),
    SPRINT("sprint", "Sprint"),
    SNEAK("sneak", "Sneak");

    private final String id;
    private final String label;

    MoveMode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static MoveMode fromId(String id) {
        for (MoveMode mode : values()) {
            if (mode.id.equals(id)) {
                return mode;
            }
        }

        throw new IllegalArgumentException(
            "Unknown movement mode: " + id
        );
    }
}
