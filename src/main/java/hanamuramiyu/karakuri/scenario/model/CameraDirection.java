package hanamuramiyu.karakuri.scenario.model;

public enum CameraDirection {
    LEFT("left", "Turn Left"),
    RIGHT("right", "Turn Right"),
    UP("up", "Look Up"),
    DOWN("down", "Look Down");

    private final String id;
    private final String label;

    CameraDirection(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static CameraDirection fromId(String id) {
        for (CameraDirection direction : values()) {
            if (direction.id.equals(id)) {
                return direction;
            }
        }

        throw new IllegalArgumentException(
            "Unknown camera direction: " + id
        );
    }
}
