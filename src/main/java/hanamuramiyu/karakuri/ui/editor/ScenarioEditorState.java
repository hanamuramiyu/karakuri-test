package hanamuramiyu.karakuri.ui.editor;

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
import hanamuramiyu.karakuri.scenario.model.RepeatMode;
import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;

import java.util.ArrayList;
import java.util.List;

public final class ScenarioEditorState {
    public static final int DURATION_STEP_TICKS = 10;
    public static final int CAMERA_ANGLE_STEP = 5;
    public static final int MIN_DURATION_TICKS = 1;
    public static final int MAX_DURATION_TICKS = 72000;
    public static final int DEFAULT_MOVE_DURATION_TICKS = 40;
    public static final int DEFAULT_WAIT_DURATION_TICKS = 20;
    public static final int DEFAULT_MOUSE_DURATION_TICKS = 20;

    private final int scenarioIndex;
    private final String initialName;
    private final List<ScenarioStep> rootSteps;
    private final List<GroupContext> groupPath =
        new ArrayList<>();

    private List<ScenarioStep> activeSteps;
    private int selectedIndex;

    private ScenarioEditorState(
        int scenarioIndex,
        String initialName,
        List<ScenarioStep> steps
    ) {
        this.scenarioIndex = scenarioIndex;
        this.initialName = initialName;
        this.rootSteps = steps;
        this.activeSteps = steps;
    }

    public static ScenarioEditorState create(
        int scenarioIndex,
        Scenario scenario
    ) {
        if (scenario == null) {
            return new ScenarioEditorState(
                scenarioIndex,
                "New Scenario",
                new ArrayList<>(
                    List.of(
                        new MoveStep(
                            MoveDirection.FORWARD,
                            DEFAULT_MOVE_DURATION_TICKS
                        )
                    )
                )
            );
        }

        return new ScenarioEditorState(
            scenarioIndex,
            scenario.name(),
            new ArrayList<>(scenario.steps())
        );
    }

    public int scenarioIndex() {
        return scenarioIndex;
    }

    public String initialName() {
        return initialName;
    }

    public List<ScenarioStep> steps() {
        return activeSteps;
    }

    public int size() {
        return activeSteps.size();
    }

    public int selectedIndex() {
        selectedIndex = Math.clamp(
            selectedIndex,
            0,
            activeSteps.size() - 1
        );

        return selectedIndex;
    }

    public ScenarioStep selectedStep() {
        return activeSteps.get(selectedIndex());
    }

    public void select(
        int index
    ) {
        selectedIndex = Math.clamp(
            index,
            0,
            activeSteps.size() - 1
        );
    }

    public boolean isInsideGroup() {
        return !groupPath.isEmpty();
    }

    public int groupDepth() {
        return groupPath.size();
    }

    public String workflowLabel() {
        return isInsideGroup()
            ? "Group L" + groupDepth()
            : "Root";
    }

    public void insertMoveStep(
        MoveDirection direction
    ) {
        insertAfterSelected(
            new MoveStep(
                direction,
                DEFAULT_MOVE_DURATION_TICKS
            )
        );
    }

    public void insertJumpStep() {
        insertAfterSelected(
            new JumpStep(
                JumpMode.SINGLE,
                JumpStopMode.DURATION,
                JumpStep.DEFAULT_DURATION_TICKS,
                JumpStep.DEFAULT_JUMP_COUNT
            )
        );
    }

    public void insertWaitStep() {
        insertAfterSelected(
            new WaitStep(
                DEFAULT_WAIT_DURATION_TICKS
            )
        );
    }

    public void insertMouseStep(
        MouseAction action
    ) {
        insertAfterSelected(
            new MouseStep(
                action,
                MouseInputMode.HOLD,
                MouseStopMode.DURATION,
                DEFAULT_MOUSE_DURATION_TICKS,
                MouseStep.DEFAULT_CPS_HALF_STEPS,
                MouseStep.DEFAULT_CLICK_COUNT
            )
        );
    }

    public void insertCameraStep(
        CameraDirection direction
    ) {
        insertAfterSelected(
            new CameraStep(
                direction,
                CameraMotion.SMOOTH,
                CameraStep.DEFAULT_ANGLE_DEGREES,
                CameraStep.DEFAULT_DURATION_TICKS
            )
        );
    }

    public void insertHotbarStep() {
        insertAfterSelected(
            new HotbarStep(
                HotbarStep.DEFAULT_SLOT
            )
        );
    }

    public void wrapSelectedInRepeatGroup() {
        replaceSelected(
            new RepeatStep(
                RepeatMode.COUNT,
                RepeatStep.DEFAULT_REPEAT_COUNT,
                List.of(selectedStep())
            )
        );
    }

    public boolean enterSelectedGroup() {
        if (!(selectedStep() instanceof RepeatStep repeatStep)) {
            return false;
        }

        commitHierarchy();

        groupPath.add(
            new GroupContext(
                activeSteps,
                selectedIndex()
            )
        );

        activeSteps =
            new ArrayList<>(
                repeatStep.steps()
            );

        selectedIndex = 0;
        return true;
    }

    public boolean exitGroup() {
        if (groupPath.isEmpty()) {
            return false;
        }

        commitHierarchy();

        GroupContext context =
            groupPath.remove(
                groupPath.size() - 1
            );

        activeSteps = context.parentSteps();
        selectedIndex = context.groupIndex();
        return true;
    }

    public boolean canMoveSelectedOutOfGroup() {
        return isInsideGroup();
    }

    public boolean moveSelectedOutOfGroup() {
        if (!isInsideGroup()) {
            return false;
        }

        ScenarioStep movedStep =
            activeSteps.remove(selectedIndex());

        GroupContext context =
            groupPath.remove(
                groupPath.size() - 1
            );

        List<ScenarioStep> parentSteps =
            context.parentSteps();

        int groupIndex =
            context.groupIndex();

        if (activeSteps.isEmpty()) {
            parentSteps.set(
                groupIndex,
                movedStep
            );

            activeSteps = parentSteps;
            selectedIndex = groupIndex;
        } else {
            RepeatStep group =
                (RepeatStep) parentSteps.get(
                    groupIndex
                );

            parentSteps.set(
                groupIndex,
                group.withSteps(activeSteps)
            );

            parentSteps.add(
                groupIndex + 1,
                movedStep
            );

            activeSteps = parentSteps;
            selectedIndex = groupIndex + 1;
        }

        commitHierarchy();
        return true;
    }

    public boolean canIncludePreviousStep() {
        if (!(selectedStep() instanceof RepeatStep group)) {
            return false;
        }

        int index = selectedIndex();

        if (index <= 0) {
            return false;
        }

        ScenarioStep candidate =
            activeSteps.get(index - 1);

        return !candidate.isInfinite()
            || group.steps().isEmpty();
    }

    public boolean includePreviousStep() {
        if (!canIncludePreviousStep()) {
            return false;
        }

        int groupIndex = selectedIndex();
        ScenarioStep candidate =
            activeSteps.remove(groupIndex - 1);

        groupIndex--;

        RepeatStep group =
            (RepeatStep) activeSteps.get(
                groupIndex
            );

        List<ScenarioStep> children =
            new ArrayList<>();

        children.add(candidate);
        children.addAll(group.steps());

        activeSteps.set(
            groupIndex,
            group.withSteps(children)
        );

        selectedIndex = groupIndex;
        commitHierarchy();
        return true;
    }

    public boolean canIncludeNextStep() {
        if (!(selectedStep() instanceof RepeatStep group)) {
            return false;
        }

        int index = selectedIndex();

        if (index >= activeSteps.size() - 1) {
            return false;
        }

        return !group.steps()
            .getLast()
            .isInfinite();
    }

    public boolean includeNextStep() {
        if (!canIncludeNextStep()) {
            return false;
        }

        int groupIndex = selectedIndex();
        RepeatStep group =
            (RepeatStep) activeSteps.get(
                groupIndex
            );

        ScenarioStep candidate =
            activeSteps.remove(
                groupIndex + 1
            );

        List<ScenarioStep> children =
            new ArrayList<>(
                group.steps()
            );

        children.add(candidate);

        activeSteps.set(
            groupIndex,
            group.withSteps(children)
        );

        commitHierarchy();
        return true;
    }

    public void commitCanvasChanges() {
        commitHierarchy();
    }

    public void duplicateSelectedStep() {
        insertAfterSelected(selectedStep());
    }

    public boolean deleteSelectedStep() {
        if (activeSteps.size() <= 1) {
            return false;
        }

        activeSteps.remove(selectedIndex());

        select(
            Math.min(
                selectedIndex,
                activeSteps.size() - 1
            )
        );

        commitHierarchy();
        return true;
    }

    public void setMoveDirection(
        MoveDirection direction
    ) {
        if (selectedStep() instanceof MoveStep step) {
            replaceSelected(
                step.withDirection(direction)
            );
        }
    }

    public void setMoveMode(
        MoveMode mode
    ) {
        if (selectedStep() instanceof MoveStep step) {
            replaceSelected(
                step.withMode(mode)
            );
        }
    }

    public void toggleMoveJumping() {
        if (selectedStep() instanceof MoveStep step) {
            replaceSelected(
                step.withJumping(!step.jumping())
            );
        }
    }

    public void setJumpMode(
        JumpMode mode
    ) {
        if (selectedStep() instanceof JumpStep step) {
            replaceSelected(
                step.withMode(mode)
            );
        }
    }

    public void setJumpStopMode(
        JumpStopMode stopMode
    ) {
        if (selectedStep() instanceof JumpStep step) {
            replaceSelected(
                step.withStopMode(stopMode)
            );
        }
    }

    public void setRepeatMode(
        RepeatMode mode
    ) {
        if (selectedStep() instanceof RepeatStep step) {
            replaceSelected(
                step.withMode(mode)
            );
        }
    }

    public void setCameraDirection(
        CameraDirection direction
    ) {
        if (selectedStep() instanceof CameraStep step) {
            replaceSelected(
                step.withDirection(direction)
            );
        }
    }

    public void setCameraMotion(
        CameraMotion motion
    ) {
        if (selectedStep() instanceof CameraStep step) {
            replaceSelected(
                step.withMotion(motion)
            );
        }
    }

    public void setMouseAction(
        MouseAction action
    ) {
        if (selectedStep() instanceof MouseStep step) {
            replaceSelected(
                step.withAction(action)
            );
        }
    }

    public void setMouseInputMode(
        MouseInputMode inputMode
    ) {
        if (selectedStep() instanceof MouseStep step) {
            replaceSelected(
                step.withInputMode(inputMode)
            );
        }
    }

    public void setMouseStopMode(
        MouseStopMode stopMode
    ) {
        if (selectedStep() instanceof MouseStep step) {
            replaceSelected(
                step.withStopMode(stopMode)
            );
        }
    }

    public void changeCps(
        int direction
    ) {
        if (!(selectedStep() instanceof MouseStep step)) {
            return;
        }

        replaceSelected(
            step.withClicksPerSecondHalfSteps(
                Math.clamp(
                    step.clicksPerSecondHalfSteps()
                        + direction,
                    MouseStep.MIN_CPS_HALF_STEPS,
                    MouseStep.MAX_CPS_HALF_STEPS
                )
            )
        );
    }

    public void changeAngle(
        int direction
    ) {
        if (!(selectedStep() instanceof CameraStep step)) {
            return;
        }

        replaceSelected(
            step.withAngleDegrees(
                Math.clamp(
                    step.angleDegrees()
                        + direction
                        * CAMERA_ANGLE_STEP,
                    CameraStep.MIN_ANGLE_DEGREES,
                    CameraStep.MAX_ANGLE_DEGREES
                )
            )
        );
    }

    public void changePrimaryValue(
        int direction
    ) {
        ScenarioStep step = selectedStep();

        if (step instanceof HotbarStep hotbarStep) {
            replaceSelected(
                hotbarStep.withSlot(
                    Math.clamp(
                        hotbarStep.slot()
                            + direction,
                        HotbarStep.MIN_SLOT,
                        HotbarStep.MAX_SLOT
                    )
                )
            );

            return;
        }

        if (
            step instanceof RepeatStep repeatStep
                && ScenarioStepRules.usesCount(step)
        ) {
            replaceSelected(
                repeatStep.withRepeatCount(
                    Math.clamp(
                        repeatStep.repeatCount()
                            + direction,
                        RepeatStep.MIN_REPEAT_COUNT,
                        RepeatStep.MAX_REPEAT_COUNT
                    )
                )
            );

            return;
        }

        if (
            step instanceof JumpStep jumpStep
                && ScenarioStepRules.usesCount(step)
        ) {
            replaceSelected(
                jumpStep.withJumpCount(
                    Math.clamp(
                        jumpStep.jumpCount()
                            + direction,
                        JumpStep.MIN_JUMP_COUNT,
                        JumpStep.MAX_JUMP_COUNT
                    )
                )
            );

            return;
        }

        if (
            step instanceof MouseStep mouseStep
                && ScenarioStepRules.usesCount(step)
        ) {
            replaceSelected(
                mouseStep.withClickCount(
                    Math.clamp(
                        mouseStep.clickCount()
                            + direction,
                        MouseStep.MIN_CLICK_COUNT,
                        MouseStep.MAX_CLICK_COUNT
                    )
                )
            );

            return;
        }

        if (!ScenarioStepRules.usesDuration(step)) {
            return;
        }

        setDurationTicks(
            Math.clamp(
                step.durationTicks()
                    + direction
                    * DURATION_STEP_TICKS,
                MIN_DURATION_TICKS,
                MAX_DURATION_TICKS
            )
        );
    }

    public void setDurationTicks(
        int durationTicks
    ) {
        ScenarioStep step = selectedStep();

        replaceSelected(
            switch (step) {
                case CameraStep cameraStep ->
                    cameraStep.withDurationTicks(
                        durationTicks
                    );
                case HotbarStep hotbarStep ->
                    hotbarStep;
                case JumpStep jumpStep ->
                    jumpStep.withDurationTicks(
                        durationTicks
                    );
                case MoveStep moveStep ->
                    moveStep.withDurationTicks(
                        durationTicks
                    );
                case MouseStep mouseStep ->
                    mouseStep.withDurationTicks(
                        durationTicks
                    );
                case RepeatStep repeatStep ->
                    repeatStep;
                case WaitStep waitStep ->
                    new WaitStep(durationTicks);
            }
        );
    }

    public void setCount(
        int count
    ) {
        ScenarioStep step = selectedStep();

        if (step instanceof JumpStep jumpStep) {
            replaceSelected(
                jumpStep.withJumpCount(count)
            );
        } else if (step instanceof MouseStep mouseStep) {
            replaceSelected(
                mouseStep.withClickCount(count)
            );
        } else if (step instanceof RepeatStep repeatStep) {
            replaceSelected(
                repeatStep.withRepeatCount(count)
            );
        }
    }

    public void setAngle(
        int angle
    ) {
        if (selectedStep() instanceof CameraStep step) {
            replaceSelected(
                step.withAngleDegrees(angle)
            );
        }
    }

    public boolean hasInfiniteStepBeforeEnd() {
        commitHierarchy();
        return hasInvalidInfiniteOrder(rootSteps);
    }

    public boolean selectedGroupHasInfiniteStepBeforeEnd() {
        return selectedStep() instanceof RepeatStep repeatStep
            && hasInvalidInfiniteOrder(
                repeatStep.steps()
            );
    }

    public Scenario toScenario(
        String name
    ) {
        commitHierarchy();
        return new Scenario(name, rootSteps);
    }

    private void insertAfterSelected(
        ScenarioStep step
    ) {
        int insertIndex = selectedIndex() + 1;

        activeSteps.add(insertIndex, step);
        selectedIndex = insertIndex;
        commitHierarchy();
    }

    private void replaceSelected(
        ScenarioStep step
    ) {
        activeSteps.set(selectedIndex(), step);
        commitHierarchy();
    }

    private void commitHierarchy() {
        List<ScenarioStep> children =
            activeSteps;

        for (
            int index = groupPath.size() - 1;
            index >= 0;
            index--
        ) {
            GroupContext context =
                groupPath.get(index);

            RepeatStep group =
                (RepeatStep) context.parentSteps()
                    .get(context.groupIndex());

            context.parentSteps().set(
                context.groupIndex(),
                group.withSteps(children)
            );

            children = context.parentSteps();
        }
    }

    private boolean hasInvalidInfiniteOrder(
        List<ScenarioStep> steps
    ) {
        for (
            int index = 0;
            index < steps.size() - 1;
            index++
        ) {
            if (steps.get(index).isInfinite()) {
                return true;
            }
        }

        for (ScenarioStep step : steps) {
            if (
                step instanceof RepeatStep repeatStep
                    && hasInvalidInfiniteOrder(
                        repeatStep.steps()
                    )
            ) {
                return true;
            }
        }

        return false;
    }

    private record GroupContext(
        List<ScenarioStep> parentSteps,
        int groupIndex
    ) {
    }
}