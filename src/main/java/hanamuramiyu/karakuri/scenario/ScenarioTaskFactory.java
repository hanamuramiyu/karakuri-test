package hanamuramiyu.karakuri.scenario;

import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;
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
        ScenarioStep step
    ) {
        return switch (step) {
            case CameraStep cameraStep ->
                new CameraTask(cameraStep);

            case HotbarStep hotbarStep ->
                new HotbarTask(hotbarStep);

            case JumpStep jumpStep ->
                new JumpTask(jumpStep);

            case MoveStep moveStep ->
                new MoveTask(moveStep);

            case MouseStep mouseStep ->
                new MouseButtonTask(mouseStep);

            case WaitStep waitStep ->
                new WaitTask(
                    waitStep.durationTicks()
                );
        };
    }
}
