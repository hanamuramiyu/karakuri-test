package hanamuramiyu.karakuri.scenario.persistence;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hanamuramiyu.karakuri.scenario.Scenario;

final class ScenarioStepJsonCodec {
    Scenario.Step read(
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
                new Scenario.MoveStep(
                    Scenario.MoveDirection.FORWARD,
                    durationTicks
                );

            case "wait" ->
                new Scenario.WaitStep(
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
        Scenario.Step step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Scenario step must not be null"
            );
        }

        return switch (step) {
            case Scenario.CameraStep cameraStep ->
                writeCameraStep(cameraStep);

            case Scenario.HotbarStep hotbarStep ->
                writeHotbarStep(hotbarStep);

            case Scenario.JumpStep jumpStep ->
                writeJumpStep(jumpStep);

            case Scenario.MoveStep moveStep ->
                writeMoveStep(moveStep);

            case Scenario.MouseStep mouseStep ->
                writeMouseStep(mouseStep);

            case Scenario.WaitStep waitStep ->
                writeWaitStep(waitStep);
        };
    }

    private Scenario.CameraStep readCameraStep(
        JsonObjectReader values,
        int durationTicks
    ) {
        return new Scenario.CameraStep(
            Scenario.CameraDirection.fromId(
                values.requiredString(
                    "direction"
                )
            ),
            Scenario.CameraMotion.fromId(
                values.optionalString(
                    "motion",
                    "smooth"
                )
            ),
            values.optionalInt(
                "angleDegrees",
                Scenario.CameraStep
                    .DEFAULT_ANGLE_DEGREES
            ),
            durationTicks
        );
    }

    private Scenario.HotbarStep readHotbarStep(
        JsonObjectReader values
    ) {
        return new Scenario.HotbarStep(
            values.optionalInt(
                "slot",
                Scenario.HotbarStep.DEFAULT_SLOT
            )
        );
    }

    private Scenario.JumpStep readJumpStep(
        JsonObjectReader values,
        int durationTicks
    ) {
        return new Scenario.JumpStep(
            Scenario.JumpMode.fromId(
                values.optionalString(
                    "mode",
                    "single"
                )
            ),
            Scenario.JumpStopMode.fromId(
                values.optionalString(
                    "stopMode",
                    "duration"
                )
            ),
            durationTicks,
            values.optionalInt(
                "jumpCount",
                Scenario.JumpStep
                    .DEFAULT_JUMP_COUNT
            )
        );
    }

    private Scenario.MoveStep readMoveStep(
        JsonObjectReader values,
        int durationTicks
    ) {
        return new Scenario.MoveStep(
            Scenario.MoveDirection.fromId(
                values.requiredString(
                    "direction"
                )
            ),
            Scenario.MoveMode.fromId(
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

    private Scenario.MouseStep readMouseStep(
        JsonObjectReader values,
        int durationTicks
    ) {
        return new Scenario.MouseStep(
            Scenario.MouseAction.fromId(
                values.requiredString(
                    "action"
                )
            ),
            Scenario.MouseInputMode.fromId(
                values.optionalString(
                    "inputMode",
                    "hold"
                )
            ),
            Scenario.MouseStopMode.fromId(
                values.optionalString(
                    "stopMode",
                    "duration"
                )
            ),
            durationTicks,
            values.optionalInt(
                "clicksPerSecondHalfSteps",
                Scenario.MouseStep
                    .DEFAULT_CPS_HALF_STEPS
            ),
            values.optionalInt(
                "clickCount",
                Scenario.MouseStep
                    .DEFAULT_CLICK_COUNT
            )
        );
    }

    private JsonObject writeCameraStep(
        Scenario.CameraStep step
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
        Scenario.HotbarStep step
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
        Scenario.JumpStep step
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
        Scenario.MoveStep step
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
        Scenario.MouseStep step
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
        Scenario.WaitStep step
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