package hanamuramiyu.karakuri.ui.main;

import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.composite.RepeatTask;

import java.util.List;

public final class KarakuriScreenState {
    private List<Scenario> scenarios;
    private int selectedScenarioIndex;
    private ExecutionMode executionMode = ExecutionMode.ONCE;

    public KarakuriScreenState() {
        scenarios = ScenarioLibrary.getScenarios();
        normalizeSelection();
    }

    public Scenario selectedScenario() {
        normalizeSelection();

        if (scenarios.isEmpty()) {
            return null;
        }

        return scenarios.get(selectedScenarioIndex);
    }

    public int selectedScenarioIndex() {
        normalizeSelection();
        return selectedScenarioIndex;
    }

    public int scenarioCount() {
        return scenarios.size();
    }

    public boolean hasScenario() {
        return !scenarios.isEmpty();
    }

    public boolean hasMultipleScenarios() {
        return scenarios.size() > 1;
    }

    public String executionModeLabel() {
        return executionMode.label;
    }

    public int repeatCount() {
        return executionMode.repeatCount;
    }

    public void selectScenario(int offset) {
        if (scenarios.size() <= 1) {
            return;
        }

        selectedScenarioIndex = Math.floorMod(
            selectedScenarioIndex + offset,
            scenarios.size()
        );
    }

    public void cycleExecutionMode() {
        executionMode = executionMode.next();
    }

    public void reload() {
        Scenario selectedScenario = selectedScenario();
        String selectedName = selectedScenario == null
            ? null
            : selectedScenario.name();

        ScenarioLibrary.reload();
        refreshSelected(selectedName);
    }

    public void refreshSelected(String selectedScenarioName) {
        scenarios = ScenarioLibrary.getScenarios();
        selectedScenarioIndex = findScenarioIndex(selectedScenarioName);
        normalizeSelection();
    }

    public void refreshAfterDeletion(int deletedIndex) {
        scenarios = ScenarioLibrary.getScenarios();

        if (scenarios.isEmpty()) {
            selectedScenarioIndex = 0;
            return;
        }

        selectedScenarioIndex = Math.min(
            deletedIndex,
            scenarios.size() - 1
        );
    }

    private int findScenarioIndex(String scenarioName) {
        if (scenarioName == null) {
            return 0;
        }

        for (int index = 0; index < scenarios.size(); index++) {
            if (scenarios.get(index).name().equals(scenarioName)) {
                return index;
            }
        }

        return 0;
    }

    private void normalizeSelection() {
        if (scenarios.isEmpty()) {
            selectedScenarioIndex = 0;
            return;
        }

        selectedScenarioIndex = Math.clamp(
            selectedScenarioIndex,
            0,
            scenarios.size() - 1
        );
    }

    private enum ExecutionMode {
        ONCE("Once", 1),
        LOOP("Loop", RepeatTask.INFINITE);

        private final String label;
        private final int repeatCount;

        ExecutionMode(String label, int repeatCount) {
            this.label = label;
            this.repeatCount = repeatCount;
        }

        private ExecutionMode next() {
            return this == ONCE ? LOOP : ONCE;
        }
    }
}