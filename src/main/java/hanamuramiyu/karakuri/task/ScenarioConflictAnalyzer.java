package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;

import java.util.EnumSet;
import java.util.Set;

public final class ScenarioConflictAnalyzer {
    private ScenarioConflictAnalyzer() {
    }

    public static Set<TaskChannel> channels(
        Scenario scenario
    ) {
        if (scenario == null) {
            throw new IllegalArgumentException(
                "Scenario must not be null"
            );
        }

        EnumSet<TaskChannel> channels =
            EnumSet.noneOf(TaskChannel.class);

        for (ScenarioStep step : scenario.steps()) {
            collect(step, channels);
        }

        return Set.copyOf(channels);
    }

    public static Set<TaskChannel> conflicts(
        Set<TaskChannel> first,
        Set<TaskChannel> second
    ) {
        EnumSet<TaskChannel> conflicts =
            EnumSet.noneOf(TaskChannel.class);

        conflicts.addAll(first);
        conflicts.retainAll(second);

        return Set.copyOf(conflicts);
    }

    private static void collect(
        ScenarioStep step,
        EnumSet<TaskChannel> channels
    ) {
        switch (step) {
            case MoveStep moveStep -> {
                channels.add(TaskChannel.MOVEMENT);

                if (moveStep.jumping()) {
                    channels.add(TaskChannel.JUMP);
                }
            }

            case JumpStep ignored ->
                channels.add(TaskChannel.JUMP);

            case CameraStep ignored ->
                channels.add(TaskChannel.CAMERA);

            case MouseStep mouseStep ->
                channels.add(
                    mouseStep.action()
                        == MouseAction.LEFT_CLICK
                            ? TaskChannel.LEFT_MOUSE
                            : TaskChannel.RIGHT_MOUSE
                );

            case HotbarStep ignored ->
                channels.add(TaskChannel.HOTBAR);

            case InventorySlotStep ignored ->
                channels.add(TaskChannel.HOTBAR);

            case RepeatStep repeatStep -> {
                for (
                    ScenarioStep nestedStep :
                    repeatStep.steps()
                ) {
                    collect(nestedStep, channels);
                }
            }

            case WaitStep ignored -> {
            }
        }
    }
}