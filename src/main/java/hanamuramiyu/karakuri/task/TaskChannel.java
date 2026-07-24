package hanamuramiyu.karakuri.task;

public enum TaskChannel {
    MOVEMENT("Movement"),
    JUMP("Jump"),
    CAMERA("Camera"),
    LEFT_MOUSE("Left Mouse"),
    RIGHT_MOUSE("Right Mouse"),
    HOTBAR("Hotbar"),
    INVENTORY("Inventory");

    private final String label;

    TaskChannel(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}