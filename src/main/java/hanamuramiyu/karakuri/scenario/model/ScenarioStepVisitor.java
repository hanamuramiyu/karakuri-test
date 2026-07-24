package hanamuramiyu.karakuri.scenario.model;

public interface ScenarioStepVisitor<T> {
    T visit(CameraStep step);

    T visit(DepositItemsStep step);

    T visit(HotbarStep step);

    T visit(InventorySlotStep step);

    T visit(JumpStep step);

    T visit(MoveStep step);

    T visit(MouseStep step);

    T visit(RepeatStep step);

    T visit(RestockItemsStep step);

    T visit(WaitStep step);
}
