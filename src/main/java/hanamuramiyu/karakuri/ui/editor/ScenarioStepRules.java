package hanamuramiyu.karakuri.ui.editor;

import hanamuramiyu.karakuri.scenario.model.CameraMotion;
import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.JumpMode;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.JumpStopMode;
import hanamuramiyu.karakuri.scenario.model.MouseInputMode;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MouseStopMode;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;

public final class ScenarioStepRules {
    private ScenarioStepRules() {
    }

    public static boolean usesCps(
        ScenarioStep step
    ) {
        return step instanceof MouseStep mouseStep
            && mouseStep.inputMode()
                == MouseInputMode.CLICK;
    }

    public static boolean usesAngle(
        ScenarioStep step
    ) {
        return step instanceof CameraStep;
    }

    public static boolean usesHotbarSlot(
        ScenarioStep step
    ) {
        return step instanceof HotbarStep;
    }

    public static boolean usesDuration(
        ScenarioStep step
    ) {
        if (step instanceof HotbarStep) {
            return false;
        }

        if (step instanceof JumpStep jumpStep) {
            return jumpStep.mode()
                != JumpMode.SINGLE
                && jumpStep.stopMode()
                    == JumpStopMode.DURATION;
        }

        if (step instanceof MouseStep mouseStep) {
            return mouseStep.stopMode()
                == MouseStopMode.DURATION;
        }

        if (step instanceof CameraStep cameraStep) {
            return cameraStep.motion()
                == CameraMotion.SMOOTH;
        }

        return true;
    }

    public static boolean usesCount(
        ScenarioStep step
    ) {
        if (step instanceof JumpStep jumpStep) {
            return jumpStep.mode()
                == JumpMode.REPEAT
                && jumpStep.stopMode()
                    == JumpStopMode.JUMP_COUNT;
        }

        return step instanceof MouseStep mouseStep
            && mouseStep.inputMode()
                == MouseInputMode.CLICK
            && mouseStep.stopMode()
                == MouseStopMode.CLICK_COUNT;
    }

    public static boolean usesPrimaryValue(
        ScenarioStep step
    ) {
        return usesDuration(step)
            || usesCount(step)
            || usesHotbarSlot(step);
    }
}