package hanamuramiyu.karakuri.scenario;

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
            throw new IllegalArgumentException("Scenario library must not be empty");
        }

        scenarios = List.copyOf(updatedScenarios);
        ScenarioRepository.save(scenarios);
        initialized = true;
    }
}