package hanamuramiyu.karakuri.task.factory;

import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.RepeatMode;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioStepVisitor;
import hanamuramiyu.karakuri.scenario.model.WaitStep;
import hanamuramiyu.karakuri.task.ClientTask;
import hanamuramiyu.karakuri.task.action.CameraTask;
import hanamuramiyu.karakuri.task.action.HotbarTask;
import hanamuramiyu.karakuri.task.action.InventorySlotTask;
import hanamuramiyu.karakuri.task.action.JumpTask;
import hanamuramiyu.karakuri.task.action.MouseButtonTask;
import hanamuramiyu.karakuri.task.action.MoveTask;
import hanamuramiyu.karakuri.task.action.WaitTask;
import hanamuramiyu.karakuri.task.composite.RepeatTask;
import hanamuramiyu.karakuri.task.composite.SequenceTask;

import java.util.List;

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
        InventorySlotStep step
    ) {
        return new InventorySlotTask(step);
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
        RepeatStep step
    ) {
        int repeatCount =
            step.mode() == RepeatMode.FOREVER
                ? RepeatTask.INFINITE
                : step.repeatCount();

        return new RepeatTask(
            () -> createSequence(step.steps()),
            repeatCount
        );
    }

    @Override
    public ClientTask visit(
        WaitStep step
    ) {
        return new WaitTask(
            step.durationTicks()
        );
    }

    private ClientTask createSequence(
        List<ScenarioStep> steps
    ) {
        return new SequenceTask(
            steps.stream()
                .map(this::create)
                .toList()
        );
    }
}