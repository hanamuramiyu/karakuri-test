package hanamuramiyu.karakuri.scenario.model;

public enum MoveDirection {
    FORWARD("forward", "Forward"),
    BACKWARD("backward", "Backward"),
    LEFT("left", "Left"),
    RIGHT("right", "Right");

    private final String id;
    private final String label;

    MoveDirection(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public MoveDirection next() {
        MoveDirection[] directions = values();
        return directions[(ordinal() + 1) % directions.length];
    }

    public static MoveDirection fromId(String id) {
        for (MoveDirection direction : values()) {
            if (direction.id.equals(id)) {
                return direction;
            }
        }

        throw new IllegalArgumentException(
            "Unknown movement direction: " + id
        );
    }
}
