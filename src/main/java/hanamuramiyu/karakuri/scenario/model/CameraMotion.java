package hanamuramiyu.karakuri.scenario.model;

public enum CameraMotion {
    INSTANT("instant", "Instant"),
    SMOOTH("smooth", "Smooth");

    private final String id;
    private final String label;

    CameraMotion(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public static CameraMotion fromId(String id) {
        for (CameraMotion motion : values()) {
            if (motion.id.equals(id)) {
                return motion;
            }
        }

        throw new IllegalArgumentException(
            "Unknown camera motion: " + id
        );
    }
}
