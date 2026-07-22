package hanamuramiyu.karakuri.ui.editor.workflow;

import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;
import java.util.function.IntConsumer;

final class ScenarioWorkflowInteraction {
    private static final int DRAG_THRESHOLD = 4;

    private List<ScenarioStep> steps;
    private final ScenarioWorkflowViewport viewport;
    private final IntConsumer selectionListener;
    private final Runnable contentEditStartedListener;
    private final Runnable contentListener;

    private int selectedIndex;
    private int pressedIndex = -1;
    private double dragStartX;
    private double dragMouseX;
    private double dragOffsetX;
    private boolean dragging;
    private boolean editStarted;

    ScenarioWorkflowInteraction(
        List<ScenarioStep> steps,
        ScenarioWorkflowViewport viewport,
        IntConsumer selectionListener,
        Runnable contentEditStartedListener,
        Runnable contentListener
    ) {
        this.steps = steps;
        this.viewport = viewport;
        this.selectionListener = selectionListener;
        this.contentEditStartedListener =
            contentEditStartedListener;
        this.contentListener = contentListener;
    }

    void setSteps(
        List<ScenarioStep> steps
    ) {
        this.steps = steps;
        selectedIndex = 0;
        pressedIndex = -1;
        dragging = false;
        editStarted = false;
        viewport.resetScroll();
        ensureSelectedVisible();
    }

    void setBounds(
        int x,
        int y,
        int width,
        int height
    ) {
        viewport.setBounds(
            x,
            y,
            width,
            height,
            steps.size()
        );

        ensureSelectedVisible();
    }

    boolean mouseClicked(
        MouseButtonEvent event
    ) {
        if (
            event.button() != 0
                || !viewport.contains(
                    event.x(),
                    event.y()
                )
        ) {
            return false;
        }

        int index = viewport.findCardIndex(
            event.x(),
            event.y(),
            steps.size()
        );

        if (index < 0) {
            return false;
        }

        select(index);

        pressedIndex = index;
        dragStartX = event.x();
        dragMouseX = event.x();
        dragOffsetX =
            event.x()
                - viewport.cardX(index);
        dragging = false;
        editStarted = false;

        return true;
    }

    boolean mouseDragged(
        MouseButtonEvent event
    ) {
        if (
            pressedIndex < 0
                || event.button() != 0
        ) {
            return false;
        }

        dragMouseX = event.x();

        if (!dragging) {
            dragging = Math.abs(
                event.x() - dragStartX
            ) >= DRAG_THRESHOLD;

            if (dragging) {
                editStarted = true;
                contentEditStartedListener.run();
            }
        }

        if (!dragging) {
            return true;
        }

        viewport.autoScroll(
            event.x(),
            steps.size()
        );

        int targetIndex =
            viewport.dragTargetIndex(
                event.x(),
                steps.size()
            );

        if (targetIndex != pressedIndex) {
            ScenarioStep step =
                steps.remove(pressedIndex);

            steps.add(targetIndex, step);

            pressedIndex = targetIndex;
            selectedIndex = targetIndex;
        }

        return true;
    }

    boolean mouseReleased() {
        boolean handled =
            pressedIndex >= 0;

        boolean commitEdit =
            editStarted;

        pressedIndex = -1;
        dragging = false;
        editStarted = false;

        if (commitEdit) {
            contentListener.run();
        }

        return handled;
    }

    boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        if (!viewport.contains(mouseX, mouseY)) {
            return false;
        }

        int index = viewport.findCardIndex(
            mouseX,
            mouseY,
            steps.size()
        );

        if (
            index >= 0
                && verticalAmount != 0.0
        ) {
            select(index);

            adjustSelectedPrimaryValue(
                verticalAmount > 0.0
                    ? 1
                    : -1
            );

            return true;
        }

        double amount =
            verticalAmount != 0.0
                ? verticalAmount
                : horizontalAmount;

        viewport.scrollBy(
            amount,
            steps.size()
        );

        return true;
    }

    void setSelectedIndex(int index) {
        selectedIndex = Math.clamp(
            index,
            0,
            steps.size() - 1
        );

        ensureSelectedVisible();
    }

    int selectedIndex() {
        selectedIndex = Math.clamp(
            selectedIndex,
            0,
            steps.size() - 1
        );

        return selectedIndex;
    }

    boolean dragging() {
        return dragging;
    }

    int pressedIndex() {
        return pressedIndex;
    }

    int draggedCardX() {
        return (int) Math.round(
            dragMouseX - dragOffsetX
        );
    }

    private void select(int index) {
        selectedIndex = Math.clamp(
            index,
            0,
            steps.size() - 1
        );

        ensureSelectedVisible();

        selectionListener.accept(
            selectedIndex
        );
    }

    private void adjustSelectedPrimaryValue(
        int direction
    ) {
        ScenarioStep step =
            steps.get(selectedIndex());

        ScenarioStep updatedStep =
            ScenarioStepValueAdjuster
                .adjustPrimaryValue(
                    step,
                    direction
                );

        if (updatedStep.equals(step)) {
            return;
        }

        contentEditStartedListener.run();

        steps.set(
            selectedIndex,
            updatedStep
        );

        contentListener.run();
    }

    private void ensureSelectedVisible() {
        viewport.ensureIndexVisible(
            selectedIndex,
            steps.size()
        );
    }
}