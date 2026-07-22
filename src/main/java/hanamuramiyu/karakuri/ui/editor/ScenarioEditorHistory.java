package hanamuramiyu.karakuri.ui.editor;

import hanamuramiyu.karakuri.scenario.model.ScenarioStep;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

final class ScenarioEditorHistory {
    private static final int MAX_ENTRIES = 100;

    private final Deque<Snapshot> undoStack =
        new ArrayDeque<>();
    private final Deque<Snapshot> redoStack =
        new ArrayDeque<>();

    boolean canUndo() {
        return !undoStack.isEmpty();
    }

    boolean canRedo() {
        return !redoStack.isEmpty();
    }

    void record(
        Snapshot before
    ) {
        Objects.requireNonNull(
            before,
            "History snapshot must not be null"
        );

        undoStack.push(before);

        while (undoStack.size() > MAX_ENTRIES) {
            undoStack.removeLast();
        }

        redoStack.clear();
    }

    Snapshot undo(
        Snapshot current
    ) {
        if (undoStack.isEmpty()) {
            return null;
        }

        redoStack.push(
            Objects.requireNonNull(
                current,
                "Current snapshot must not be null"
            )
        );

        return undoStack.pop();
    }

    Snapshot redo(
        Snapshot current
    ) {
        if (redoStack.isEmpty()) {
            return null;
        }

        undoStack.push(
            Objects.requireNonNull(
                current,
                "Current snapshot must not be null"
            )
        );

        return redoStack.pop();
    }

    record Snapshot(
        String name,
        List<ScenarioStep> rootSteps,
        List<Integer> groupPath,
        int selectedIndex
    ) {
        Snapshot {
            name = Objects.requireNonNull(
                name,
                "Scenario name must not be null"
            );
            rootSteps = List.copyOf(
                Objects.requireNonNull(
                    rootSteps,
                    "Root steps must not be null"
                )
            );
            groupPath = List.copyOf(
                Objects.requireNonNull(
                    groupPath,
                    "Group path must not be null"
                )
            );
        }

        boolean sameDocument(
            Snapshot other
        ) {
            return other != null
                && name.equals(other.name)
                && rootSteps.equals(other.rootSteps);
        }
    }
}