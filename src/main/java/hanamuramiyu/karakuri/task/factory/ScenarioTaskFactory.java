package hanamuramiyu.karakuri.task.factory;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.task.ClientTask;
import hanamuramiyu.karakuri.task.composite.SequenceTask;

import java.util.List;

public final class ScenarioTaskFactory {
    private static final ScenarioStepTaskCreator STEP_TASK_CREATOR =
        new ScenarioStepTaskCreator();

    private ScenarioTaskFactory() {
    }

    public static ClientTask create(
        Scenario scenario
    ) {
        List<ClientTask> tasks =
            scenario.steps()
                .stream()
                .map(
                    STEP_TASK_CREATOR::create
                )
                .toList();

        return new SequenceTask(tasks);
    }

    public static ClientTask createStep(
        ScenarioStep step
    ) {
        return STEP_TASK_CREATOR.create(step);
    }
}