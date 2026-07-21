package hanamuramiyu.karakuri.scenario.persistence;

import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
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
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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

        int durationTicks =
            values.requiredInt(
                "durationTicks"
            );

        return switch (type) {
            case "camera" ->
                readCameraStep(
                    values,
                    durationTicks
                );

            case "hotbar" ->
                readHotbarStep(values);

            case "jump" ->
                readJumpStep(
                    values,
                    durationTicks
                );

            case "move" ->
                readMoveStep(
                    values,
                    durationTicks
                );

            case "mouse" ->
                readMouseStep(
                    values,
                    durationTicks
                );

            case "walk_forward" ->
                new MoveStep(
                    MoveDirection.FORWARD,
                    durationTicks
                );

            case "wait" ->
                new WaitStep(
                    durationTicks
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

            case HotbarStep hotbarStep ->
                writeHotbarStep(hotbarStep);

            case JumpStep jumpStep ->
                writeJumpStep(jumpStep);

            case MoveStep moveStep ->
                writeMoveStep(moveStep);

            case MouseStep mouseStep ->
                writeMouseStep(mouseStep);

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
                CameraStep
                    .DEFAULT_ANGLE_DEGREES
            ),
            durationTicks
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
                JumpStep
                    .DEFAULT_JUMP_COUNT
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
                MouseStep
                    .DEFAULT_CPS_HALF_STEPS
            ),
            values.optionalInt(
                "clickCount",
                MouseStep
                    .DEFAULT_CLICK_COUNT
            )
        );
    }

    private JsonObject writeCameraStep(
        CameraStep step
    ) {
        JsonObject object =
            createStepObject(
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

    private JsonObject writeHotbarStep(
        HotbarStep step
    ) {
        JsonObject object =
            createStepObject(
                "hotbar",
                step.durationTicks()
            );

        object.addProperty(
            "slot",
            step.slot()
        );

        return object;
    }

    private JsonObject writeJumpStep(
        JumpStep step
    ) {
        JsonObject object =
            createStepObject(
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
            createStepObject(
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
            createStepObject(
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

    private JsonObject writeWaitStep(
        WaitStep step
    ) {
        return createStepObject(
            "wait",
            step.durationTicks()
        );
    }

    private JsonObject createStepObject(
        String type,
        int durationTicks
    ) {
        JsonObject object =
            new JsonObject();

        object.addProperty(
            "type",
            type
        );

        object.addProperty(
            "durationTicks",
            durationTicks
        );

        return object;
    }
}
