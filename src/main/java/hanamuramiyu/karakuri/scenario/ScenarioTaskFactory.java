package hanamuramiyu.karakuri.scenario;

import hanamuramiyu.karakuri.task.ClientTask;
import hanamuramiyu.karakuri.task.SequenceTask;
import hanamuramiyu.karakuri.task.WaitTask;
import hanamuramiyu.karakuri.task.WalkForwardTask;

import java.util.List;

public final class ScenarioTaskFactory {
    private ScenarioTaskFactory() {
    }

    public static ClientTask create(Scenario scenario) {
        List<ClientTask> tasks = scenario.steps()
            .stream()
            .map(ScenarioTaskFactory::createStepTask)
            .toList();

        return new SequenceTask(tasks);
    }

    private static ClientTask createStepTask(Scenario.Step step) {
        return switch (step) {
            case Scenario.WalkForwardStep walkForwardStep ->
                new WalkForwardTask(walkForwardStep.durationTicks());
            case Scenario.WaitStep waitStep ->
                new WaitTask(waitStep.durationTicks());
        };
    }
}