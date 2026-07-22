package hanamuramiyu.karakuri.task.factory;

import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioStepVisitor;
import hanamuramiyu.karakuri.scenario.model.WaitStep;
import hanamuramiyu.karakuri.task.ClientTask;
import hanamuramiyu.karakuri.task.action.CameraTask;
import hanamuramiyu.karakuri.task.action.HotbarTask;
import hanamuramiyu.karakuri.task.action.JumpTask;
import hanamuramiyu.karakuri.task.action.MouseButtonTask;
import hanamuramiyu.karakuri.task.action.MoveTask;
import hanamuramiyu.karakuri.task.action.WaitTask;

final class ScenarioStepTaskCreator
    implements ScenarioStepVisitor<ClientTask> {
    ClientTask create(
        ScenarioStep step
    ) {
        return step.accept(this);
    }

    @Override
    public ClientTask visit(
        CameraStep step
    ) {
        return new CameraTask(step);
    }

    @Override
    public ClientTask visit(
        HotbarStep step
    ) {
        return new HotbarTask(step);
    }

    @Override
    public ClientTask visit(
        JumpStep step
    ) {
        return new JumpTask(step);
    }

    @Override
    public ClientTask visit(
        MoveStep step
    ) {
        return new MoveTask(step);
    }

    @Override
    public ClientTask visit(
        MouseStep step
    ) {
        return new MouseButtonTask(step);
    }

    @Override
    public ClientTask visit(
        WaitStep step
    ) {
        return new WaitTask(
            step.durationTicks()
        );
    }
}