package hanamuramiyu.karakuri.task;

public enum TaskStatus {
    IDLE("Idle"),
    RUNNING("Running"),
    PAUSED("Paused");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}