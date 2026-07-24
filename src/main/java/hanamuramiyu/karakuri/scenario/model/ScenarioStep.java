package hanamuramiyu.karakuri.scenario.model;

public sealed interface ScenarioStep permits
    CameraStep,
    HotbarStep,
    InventorySlotStep,
    JumpStep,
    MoveStep,
    MouseStep,
    RepeatStep,
    WaitStep {
    int durationTicks();

    String label();

    <T> T accept(ScenarioStepVisitor<T> visitor);

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