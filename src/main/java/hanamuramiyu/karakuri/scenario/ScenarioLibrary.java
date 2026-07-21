package hanamuramiyu.karakuri.scenario;

import java.util.ArrayList;
import java.util.List;

public final class ScenarioLibrary {
    private static List<Scenario> scenarios = List.of();
    private static boolean initialized;

    private ScenarioLibrary() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        scenarios = ScenarioRepository.load();
        initialized = true;
    }

    public static List<Scenario> getScenarios() {
        initialize();
        return scenarios;
    }

    public static Scenario getFirst() {
        return getScenarios().getFirst();
    }

    public static void reload() {
        scenarios = ScenarioRepository.load();
        initialized = true;
    }

    public static void save(List<Scenario> updatedScenarios) {
        if (updatedScenarios == null || updatedScenarios.isEmpty()) {
            throw new IllegalArgumentException(
                "Scenario library must not be empty"
            );
        }

        scenarios = List.copyOf(updatedScenarios);
        ScenarioRepository.save(scenarios);
        initialized = true;
    }

    public static void add(Scenario scenario) {
        List<Scenario> updatedScenarios = new ArrayList<>(
            getScenarios()
        );

        updatedScenarios.add(scenario);
        save(updatedScenarios);
    }

    public static void replace(int index, Scenario scenario) {
        List<Scenario> updatedScenarios = new ArrayList<>(
            getScenarios()
        );

        if (index < 0 || index >= updatedScenarios.size()) {
            throw new IndexOutOfBoundsException(
                "Scenario index is out of bounds: " + index
            );
        }

        updatedScenarios.set(index, scenario);
        save(updatedScenarios);
    }

    public static boolean containsName(
        String name,
        int excludedIndex
    ) {
        String normalizedName = name.trim();

        for (int index = 0; index < getScenarios().size(); index++) {
            if (index == excludedIndex) {
                continue;
            }

            if (
                getScenarios()
                    .get(index)
                    .name()
                    .equalsIgnoreCase(normalizedName)
            ) {
                return true;
            }
        }

        return false;
    }
}