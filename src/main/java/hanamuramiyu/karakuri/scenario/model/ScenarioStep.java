package hanamuramiyu.karakuri.scenario.model;

public sealed interface ScenarioStep permits
    CameraStep,
    HotbarStep,
    JumpStep,
    MoveStep,
    MouseStep,
    WaitStep {
    int durationTicks();

    String label();

    default boolean isInfinite() {
        return false;
    }

    static void validateDuration(int durationTicks) {
        if (durationTicks <= 0) {
            throw new IllegalArgumentException(
                "Duration must be greater than zero"
            );
        }
    }
}
