package hanamuramiyu.karakuri.scenario.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.DepositItemsStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
import hanamuramiyu.karakuri.scenario.model.JumpMode;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.JumpStopMode;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MouseInputMode;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MouseStopMode;
import hanamuramiyu.karakuri.scenario.model.MoveDirection;
import hanamuramiyu.karakuri.scenario.model.MoveMode;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.RepeatMode;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.RestockItemsStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.StorageTransferAmountMode;
import hanamuramiyu.karakuri.scenario.model.StorageTransferItemMode;
import hanamuramiyu.karakuri.scenario.model.StorageTransferOptions;
import hanamuramiyu.karakuri.scenario.model.StorageTransferSpeed;
import hanamuramiyu.karakuri.scenario.model.WaitStep;

import java.util.ArrayList;
import java.util.List;

final class ScenarioStepJsonCodec {
    ScenarioStep read(
        JsonElement element
    ) {
        JsonObjectReader values =
            JsonObjectReader.from(
                element,
                "Scenario step"
            );

        String type =
            values.requiredString("type");

        return switch (type) {
            case "camera" ->
                readCameraStep(
                    values,
                    values.requiredInt(
                        "durationTicks"
                    )
                );

            case "deposit_items" ->
                readDepositItemsStep(values);

            case "hotbar" ->
                readHotbarStep(values);

            case "inventory_slot" ->
                readInventorySlotStep(values);

            case "jump" ->
                readJumpStep(
                    values,
                    values.requiredInt(
                        "durationTicks"
                    )
                );

            case "move" ->
                readMoveStep(
                    values,
                    values.requiredInt(
                        "durationTicks"
                    )
                );

            case "mouse" ->
                readMouseStep(
                    values,
                    values.requiredInt(
                        "durationTicks"
                    )
                );

            case "repeat" ->
                readRepeatStep(values);

            case "restock_items" ->
                readRestockItemsStep(values);

            case "walk_forward" ->
                new MoveStep(
                    MoveDirection.FORWARD,
                    values.requiredInt(
                        "durationTicks"
                    )
                );

            case "wait" ->
                new WaitStep(
                    values.requiredInt(
                        "durationTicks"
                    )
                );

            default ->
                throw new JsonParseException(
                    "Unknown scenario step type: "
                        + type
                );
        };
    }

    JsonObject write(
        ScenarioStep step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Scenario step must not be null"
            );
        }

        return switch (step) {
            case CameraStep cameraStep ->
                writeCameraStep(cameraStep);

            case DepositItemsStep depositItemsStep ->
                writeDepositItemsStep(depositItemsStep);

            case HotbarStep hotbarStep ->
                writeHotbarStep(hotbarStep);

            case InventorySlotStep inventorySlotStep ->
                writeInventorySlotStep(inventorySlotStep);

            case JumpStep jumpStep ->
                writeJumpStep(jumpStep);

            case MoveStep moveStep ->
                writeMoveStep(moveStep);

            case MouseStep mouseStep ->
                writeMouseStep(mouseStep);

            case RepeatStep repeatStep ->
                writeRepeatStep(repeatStep);

            case RestockItemsStep restockItemsStep ->
                writeRestockItemsStep(restockItemsStep);

            case WaitStep waitStep ->
                writeWaitStep(waitStep);
        };
    }

    private CameraStep readCameraStep(
        JsonObjectReader values,
        int durationTicks
    ) {
        return new CameraStep(
            CameraDirection.fromId(
                values.requiredString(
                    "direction"
                )
            ),
            CameraMotion.fromId(
                values.optionalString(
                    "motion",
                    "smooth"
                )
            ),
            values.optionalInt(
                "angleDegrees",
                CameraStep.DEFAULT_ANGLE_DEGREES
            ),
            durationTicks
        );
    }

    private DepositItemsStep readDepositItemsStep(
        JsonObjectReader values
    ) {
        return new DepositItemsStep(
            new StorageTransferOptions(
                values.optionalString(
                    "storageGroupId",
                    DepositItemsStep.UNASSIGNED_GROUP_ID
                ),
                StorageTransferItemMode.fromId(
                    values.optionalString(
                        "itemMode",
                        StorageTransferItemMode.GROUP_FILTER.id()
                    )
                ),
                readTransferItemIds(values, null),
                StorageTransferAmountMode.fromId(
                    values.optionalString(
                        "amountMode",
                        StorageTransferAmountMode.ALL.id()
                    )
                ),
                values.optionalInt(
                    "amount",
                    StorageTransferOptions.DEFAULT_AMOUNT
                ),
                StorageTransferSpeed.fromId(
                    values.optionalString(
                        "speed",
                        StorageTransferSpeed.FAST.id()
                    )
                ),
                values.optionalBoolean(
                    "includeHotbar",
                    DepositItemsStep.DEFAULT_INCLUDE_HOTBAR
                )
            )
        );
    }

    private RestockItemsStep readRestockItemsStep(
        JsonObjectReader values
    ) {
        String legacyItemId = values.optionalString(
            "itemId",
            RestockItemsStep.UNASSIGNED_ITEM_ID
        );

        return new RestockItemsStep(
            new StorageTransferOptions(
                values.optionalString(
                    "storageGroupId",
                    RestockItemsStep.UNASSIGNED_GROUP_ID
                ),
                StorageTransferItemMode.fromId(
                    values.optionalString(
                        "itemMode",
                        StorageTransferItemMode.SELECTED_ITEMS.id()
                    )
                ),
                readTransferItemIds(
                    values,
                    legacyItemId
                ),
                StorageTransferAmountMode.fromId(
                    values.optionalString(
                        "amountMode",
                        StorageTransferAmountMode.TARGET.id()
                    )
                ),
                values.optionalInt(
                    "amount",
                    values.optionalInt(
                        "targetAmount",
                        RestockItemsStep.DEFAULT_TARGET_AMOUNT
                    )
                ),
                StorageTransferSpeed.fromId(
                    values.optionalString(
                        "speed",
                        StorageTransferSpeed.CONTROLLED.id()
                    )
                ),
                values.optionalBoolean(
                    "includeHotbar",
                    values.optionalBoolean(
                        "countHotbar",
                        RestockItemsStep.DEFAULT_COUNT_HOTBAR
                    )
                )
            )
        );
    }

    private HotbarStep readHotbarStep(
        JsonObjectReader values
    ) {
        return new HotbarStep(
            values.optionalInt(
                "slot",
                HotbarStep.DEFAULT_SLOT
            )
        );
    }

    private InventorySlotStep readInventorySlotStep(
        JsonObjectReader values
    ) {
        return new InventorySlotStep(
            values.optionalInt(
                "inventorySlot",
                InventorySlotStep.DEFAULT_INVENTORY_SLOT
            ),
            values.optionalInt(
                "hotbarSlot",
                InventorySlotStep.DEFAULT_HOTBAR_SLOT
            )
        );
    }

    private JumpStep readJumpStep(
        JsonObjectReader values,
        int durationTicks
    ) {
        return new JumpStep(
            JumpMode.fromId(
                values.optionalString(
                    "mode",
                    "single"
                )
            ),
            JumpStopMode.fromId(
                values.optionalString(
                    "stopMode",
                    "duration"
                )
            ),
            durationTicks,
            values.optionalInt(
                "jumpCount",
                JumpStep.DEFAULT_JUMP_COUNT
            )
        );
    }

    private MoveStep readMoveStep(
        JsonObjectReader values,
        int durationTicks
    ) {
        return new MoveStep(
            MoveDirection.fromId(
                values.requiredString(
                    "direction"
                )
            ),
            MoveMode.fromId(
                values.optionalString(
                    "mode",
                    "walk"
                )
            ),
            values.optionalBoolean(
                "jumping",
                false
            ),
            durationTicks
        );
    }

    private MouseStep readMouseStep(
        JsonObjectReader values,
        int durationTicks
    ) {
        return new MouseStep(
            MouseAction.fromId(
                values.requiredString(
                    "action"
                )
            ),
            MouseInputMode.fromId(
                values.optionalString(
                    "inputMode",
                    "hold"
                )
            ),
            MouseStopMode.fromId(
                values.optionalString(
                    "stopMode",
                    "duration"
                )
            ),
            durationTicks,
            values.optionalInt(
                "clicksPerSecondHalfSteps",
                MouseStep.DEFAULT_CPS_HALF_STEPS
            ),
            values.optionalInt(
                "clickCount",
                MouseStep.DEFAULT_CLICK_COUNT
            )
        );
    }

    private RepeatStep readRepeatStep(
        JsonObjectReader values
    ) {
        JsonArray stepArray =
            values.requiredArray("steps");

        List<ScenarioStep> steps =
            new ArrayList<>();

        for (JsonElement stepElement : stepArray) {
            steps.add(read(stepElement));
        }

        return new RepeatStep(
            RepeatMode.fromId(
                values.optionalString(
                    "mode",
                    "count"
                )
            ),
            values.optionalInt(
                "repeatCount",
                RepeatStep.DEFAULT_REPEAT_COUNT
            ),
            steps
        );
    }

    private JsonObject writeCameraStep(
        CameraStep step
    ) {
        JsonObject object =
            createTimedStepObject(
                "camera",
                step.durationTicks()
            );

        object.addProperty(
            "direction",
            step.direction().id()
        );

        object.addProperty(
            "motion",
            step.motion().id()
        );

        object.addProperty(
            "angleDegrees",
            step.angleDegrees()
        );

        return object;
    }

    private JsonObject writeDepositItemsStep(
        DepositItemsStep step
    ) {
        JsonObject object =
            createTimedStepObject(
                "deposit_items",
                step.durationTicks()
            );

        writeTransferOptions(object, step.options());
        return object;
    }

    private JsonObject writeRestockItemsStep(
        RestockItemsStep step
    ) {
        JsonObject object =
            createTimedStepObject(
                "restock_items",
                step.durationTicks()
            );

        writeTransferOptions(object, step.options());
        return object;
    }

    private List<String> readTransferItemIds(
        JsonObjectReader values,
        String legacyItemId
    ) {
        JsonArray itemIds = values.optionalArray("itemIds");

        if (itemIds == null) {
            return legacyItemId == null
                || legacyItemId.isBlank()
                    ? List.of()
                    : List.of(legacyItemId);
        }

        List<String> result = new ArrayList<>();

        for (JsonElement itemId : itemIds) {
            if (
                !itemId.isJsonPrimitive()
                    || !itemId
                        .getAsJsonPrimitive()
                        .isString()
            ) {
                throw new JsonParseException(
                    "Storage transfer item ID must be a string"
                );
            }

            result.add(itemId.getAsString());
        }

        return List.copyOf(result);
    }

    private void writeTransferOptions(
        JsonObject object,
        StorageTransferOptions options
    ) {
        object.addProperty(
            "storageGroupId",
            options.storageGroupId()
        );
        object.addProperty(
            "itemMode",
            options.itemMode().id()
        );

        JsonArray itemIds = new JsonArray();

        for (String itemId : options.itemIds()) {
            itemIds.add(itemId);
        }

        object.add("itemIds", itemIds);
        object.addProperty(
            "amountMode",
            options.amountMode().id()
        );
        object.addProperty("amount", options.amount());
        object.addProperty("speed", options.speed().id());
        object.addProperty(
            "includeHotbar",
            options.includeHotbar()
        );
    }

    private JsonObject writeHotbarStep(
        HotbarStep step
    ) {
        JsonObject object =
            createTimedStepObject(
                "hotbar",
                step.durationTicks()
            );

        object.addProperty(
            "slot",
            step.slot()
        );

        return object;
    }

    private JsonObject writeInventorySlotStep(
        InventorySlotStep step
    ) {
        JsonObject object =
            createTimedStepObject(
                "inventory_slot",
                step.durationTicks()
            );

        object.addProperty(
            "inventorySlot",
            step.inventorySlot()
        );

        object.addProperty(
            "hotbarSlot",
            step.hotbarSlot()
        );

        return object;
    }

    private JsonObject writeJumpStep(
        JumpStep step
    ) {
        JsonObject object =
            createTimedStepObject(
                "jump",
                step.durationTicks()
            );

        object.addProperty(
            "mode",
            step.mode().id()
        );

        object.addProperty(
            "stopMode",
            step.stopMode().id()
        );

        object.addProperty(
            "jumpCount",
            step.jumpCount()
        );

        return object;
    }

    private JsonObject writeMoveStep(
        MoveStep step
    ) {
        JsonObject object =
            createTimedStepObject(
                "move",
                step.durationTicks()
            );

        object.addProperty(
            "direction",
            step.direction().id()
        );

        object.addProperty(
            "mode",
            step.mode().id()
        );

        object.addProperty(
            "jumping",
            step.jumping()
        );

        return object;
    }

    private JsonObject writeMouseStep(
        MouseStep step
    ) {
        JsonObject object =
            createTimedStepObject(
                "mouse",
                step.durationTicks()
            );

        object.addProperty(
            "action",
            step.action().id()
        );

        object.addProperty(
            "inputMode",
            step.inputMode().id()
        );

        object.addProperty(
            "stopMode",
            step.stopMode().id()
        );

        object.addProperty(
            "clicksPerSecondHalfSteps",
            step.clicksPerSecondHalfSteps()
        );

        object.addProperty(
            "clickCount",
            step.clickCount()
        );

        return object;
    }

    private JsonObject writeRepeatStep(
        RepeatStep step
    ) {
        JsonObject object =
            createStepObject("repeat");

        object.addProperty(
            "mode",
            step.mode().id()
        );

        object.addProperty(
            "repeatCount",
            step.repeatCount()
        );

        JsonArray stepArray =
            new JsonArray();

        for (ScenarioStep child : step.steps()) {
            stepArray.add(write(child));
        }

        object.add(
            "steps",
            stepArray
        );

        return object;
    }

    private JsonObject writeWaitStep(
        WaitStep step
    ) {
        return createTimedStepObject(
            "wait",
            step.durationTicks()
        );
    }

    private JsonObject createTimedStepObject(
        String type,
        int durationTicks
    ) {
        JsonObject object =
            createStepObject(type);

        object.addProperty(
            "durationTicks",
            durationTicks
        );

        return object;
    }

    private JsonObject createStepObject(
        String type
    ) {
        JsonObject object =
            new JsonObject();

        object.addProperty(
            "type",
            type
        );

        return object;
    }
}