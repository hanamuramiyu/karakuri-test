package hanamuramiyu.karakuri.ui.editor.library;

import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MoveDirection;

import java.util.Objects;
import java.util.function.Consumer;

public record ScenarioActionLibraryActions(
    Consumer<MoveDirection> moveAction,
    Runnable jumpAction,
    Runnable waitAction,
    Runnable repeatAction,
    Consumer<MouseAction> mouseAction,
    Consumer<CameraDirection> cameraAction,
    Runnable hotbarAction,
    Runnable inventorySlotAction,
    Runnable depositItemsAction
) {
    public ScenarioActionLibraryActions {
        Objects.requireNonNull(moveAction, "Move action must not be null");
        Objects.requireNonNull(jumpAction, "Jump action must not be null");
        Objects.requireNonNull(waitAction, "Wait action must not be null");
        Objects.requireNonNull(repeatAction, "Repeat action must not be null");
        Objects.requireNonNull(mouseAction, "Mouse action must not be null");
        Objects.requireNonNull(cameraAction, "Camera action must not be null");
        Objects.requireNonNull(hotbarAction, "Hotbar action must not be null");
        Objects.requireNonNull(inventorySlotAction, "Inventory slot action must not be null");
        Objects.requireNonNull(depositItemsAction, "Deposit items action must not be null");
    }
}