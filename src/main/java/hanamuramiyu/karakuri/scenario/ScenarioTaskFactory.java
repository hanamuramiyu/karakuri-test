package hanamuramiyu.karakuri.scenario;

import hanamuramiyu.karakuri.task.CameraTask;
import hanamuramiyu.karakuri.task.ClientTask;
import hanamuramiyu.karakuri.task.HotbarTask;
import hanamuramiyu.karakuri.task.JumpTask;
import hanamuramiyu.karakuri.task.MouseButtonTask;
import hanamuramiyu.karakuri.task.MoveTask;
import hanamuramiyu.karakuri.task.SequenceTask;
import hanamuramiyu.karakuri.task.WaitTask;

import java.util.List;

public final class ScenarioTaskFactory {
    private ScenarioTaskFactory() {
    }

    public static ClientTask create(
        Scenario scenario
    ) {
        List<ClientTask> tasks =
            scenario.steps()
                .stream()
                .map(
                    ScenarioTaskFactory::createStep
                )
                .toList();

        return new SequenceTask(tasks);
    }

    public static ClientTask createStep(
        Scenario.Step step
    ) {
        return switch (step) {
            case Scenario.CameraStep cameraStep ->
                new CameraTask(cameraStep);

            case Scenario.HotbarStep hotbarStep ->
                new HotbarTask(hotbarStep);

            case Scenario.JumpStep jumpStep ->
                new JumpTask(jumpStep);

            case Scenario.MoveStep moveStep ->
                new MoveTask(moveStep);

            case Scenario.MouseStep mouseStep ->
                new MouseButtonTask(mouseStep);

            case Scenario.WaitStep waitStep ->
                new WaitTask(
                    waitStep.durationTicks()
                );
        };
    }
}