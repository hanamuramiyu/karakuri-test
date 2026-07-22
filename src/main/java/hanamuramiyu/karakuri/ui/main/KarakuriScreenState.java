package hanamuramiyu.karakuri.ui.main;

import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.composite.RepeatTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class KarakuriScreenState {
    private static final int MAX_SCENARIO_NAME_LENGTH = 64;

    private List<Scenario> scenarios;
    private int selectedScenarioIndex;
    private String searchQuery = "";
    private ScenarioSortMode sortMode = ScenarioSortMode.LIBRARY_ORDER;
    private ExecutionMode executionMode = ExecutionMode.ONCE;

    public KarakuriScreenState() {
        scenarios = ScenarioLibrary.getScenarios();
        normalizeSelection();
    }

    public Scenario selectedScenario() {
        normalizeSelection();

        if (!isSelectedScenarioVisible()) {
            return null;
        }

        return scenarios.get(selectedScenarioIndex);
    }

    public int selectedScenarioIndex() {
        normalizeSelection();
        return selectedScenarioIndex;
    }

    public int selectedVisibleIndex() {
        List<ScenarioEntry> visible = visibleScenarios();

        for (int index = 0; index < visible.size(); index++) {
            if (visible.get(index).libraryIndex() == selectedScenarioIndex) {
                return index;
            }
        }

        return -1;
    }

    public int scenarioCount() {
        return scenarios.size();
    }

    public int visibleScenarioCount() {
        return visibleScenarios().size();
    }

    public boolean hasScenario() {
        return !scenarios.isEmpty();
    }

    public boolean hasVisibleScenario() {
        return !visibleScenarios().isEmpty();
    }

    public String searchQuery() {
        return searchQuery;
    }

    public ScenarioSortMode sortMode() {
        return sortMode;
    }

    public List<ScenarioEntry> visibleScenarios() {
        String normalizedQuery = searchQuery
            .trim()
            .toLowerCase(Locale.ROOT);

        List<ScenarioEntry> visible = new ArrayList<>();

        for (int index = 0; index < scenarios.size(); index++) {
            Scenario scenario = scenarios.get(index);

            if (
                normalizedQuery.isEmpty()
                    || scenario.name()
                        .toLowerCase(Locale.ROOT)
                        .contains(normalizedQuery)
            ) {
                visible.add(
                    new ScenarioEntry(index, scenario)
                );
            }
        }

        visible.sort(comparatorFor(sortMode));
        return List.copyOf(visible);
    }

    public String executionModeLabel() {
        return executionMode.label;
    }

    public int repeatCount() {
        return executionMode.repeatCount;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery == null
            ? ""
            : searchQuery;
        ensureVisibleSelection();
    }

    public void setSortMode(ScenarioSortMode sortMode) {
        if (sortMode == null) {
            throw new IllegalArgumentException(
                "Scenario sort mode must not be null"
            );
        }

        this.sortMode = sortMode;
        ensureVisibleSelection();
    }

    public void selectScenarioIndex(int libraryIndex) {
        if (libraryIndex < 0 || libraryIndex >= scenarios.size()) {
            return;
        }

        selectedScenarioIndex = libraryIndex;
    }

    public void selectVisibleScenario(int visibleIndex) {
        List<ScenarioEntry> visible = visibleScenarios();

        if (visibleIndex < 0 || visibleIndex >= visible.size()) {
            return;
        }

        selectedScenarioIndex = visible.get(visibleIndex).libraryIndex();
    }

    public void selectVisibleOffset(int offset) {
        List<ScenarioEntry> visible = visibleScenarios();

        if (visible.size() <= 1) {
            return;
        }

        int currentVisibleIndex = selectedVisibleIndex();
        int updatedVisibleIndex = Math.floorMod(
            Math.max(0, currentVisibleIndex) + offset,
            visible.size()
        );

        selectedScenarioIndex = visible
            .get(updatedVisibleIndex)
            .libraryIndex();
    }

    public void cycleExecutionMode() {
        executionMode = executionMode.next();
    }

    public String duplicateSelectedScenario() {
        Scenario source = selectedScenario();

        if (source == null) {
            return null;
        }

        String duplicateName = createDuplicateName(source.name());

        ScenarioLibrary.add(
            new Scenario(
                duplicateName,
                source.steps()
            )
        );

        refreshSelected(duplicateName);
        return duplicateName;
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
        ensureVisibleSelection();
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
        ensureVisibleSelection();
    }

    private Comparator<ScenarioEntry> comparatorFor(
        ScenarioSortMode sortMode
    ) {
        Comparator<ScenarioEntry> byName = Comparator.comparing(
            entry -> entry.scenario().name(),
            String.CASE_INSENSITIVE_ORDER
        );

        return switch (sortMode) {
            case LIBRARY_ORDER -> Comparator.comparingInt(
                ScenarioEntry::libraryIndex
            );
            case NAME_ASCENDING -> byName
                .thenComparingInt(ScenarioEntry::libraryIndex);
            case NAME_DESCENDING -> byName
                .reversed()
                .thenComparingInt(ScenarioEntry::libraryIndex);
            case MOST_ACTIONS -> Comparator
                .comparingInt(
                    (ScenarioEntry entry) -> entry
                        .scenario()
                        .steps()
                        .size()
                )
                .reversed()
                .thenComparing(byName);
            case FEWEST_ACTIONS -> Comparator
                .comparingInt(
                    (ScenarioEntry entry) -> entry
                        .scenario()
                        .steps()
                        .size()
                )
                .thenComparing(byName);
        };
    }

    private boolean isSelectedScenarioVisible() {
        for (ScenarioEntry entry : visibleScenarios()) {
            if (entry.libraryIndex() == selectedScenarioIndex) {
                return true;
            }
        }

        return false;
    }

    private void ensureVisibleSelection() {
        List<ScenarioEntry> visible = visibleScenarios();

        if (visible.isEmpty()) {
            return;
        }

        for (ScenarioEntry entry : visible) {
            if (entry.libraryIndex() == selectedScenarioIndex) {
                return;
            }
        }

        selectedScenarioIndex = visible.getFirst().libraryIndex();
    }

    private String createDuplicateName(String sourceName) {
        int copyNumber = 1;

        while (true) {
            String suffix = copyNumber == 1
                ? " Copy"
                : " Copy " + copyNumber;

            int maximumBaseLength =
                MAX_SCENARIO_NAME_LENGTH - suffix.length();

            String baseName = sourceName.length() <= maximumBaseLength
                ? sourceName
                : sourceName
                    .substring(0, maximumBaseLength)
                    .stripTrailing();

            String candidate = baseName + suffix;

            if (!ScenarioLibrary.containsName(candidate, -1)) {
                return candidate;
            }

            copyNumber++;
        }
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

    public record ScenarioEntry(
        int libraryIndex,
        Scenario scenario
    ) {
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