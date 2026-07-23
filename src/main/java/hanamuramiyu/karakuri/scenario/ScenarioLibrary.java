package hanamuramiyu.karakuri.scenario;

import hanamuramiyu.karakuri.scenario.model.Scenario;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static void reload() {
        scenarios = ScenarioRepository.load();
        initialized = true;
    }

    public static void save(List<Scenario> updatedScenarios) {
        if (updatedScenarios == null) {
            throw new IllegalArgumentException(
                "Scenario library must not be null"
            );
        }

        validateUniqueIdentities(updatedScenarios);

        scenarios = List.copyOf(updatedScenarios);
        ScenarioRepository.save(scenarios);
        initialized = true;
    }

    public static void add(Scenario scenario) {
        if (scenario == null) {
            throw new IllegalArgumentException(
                "Scenario must not be null"
            );
        }

        List<Scenario> updatedScenarios = new ArrayList<>(
            getScenarios()
        );

        updatedScenarios.add(scenario);
        save(updatedScenarios);
    }

    public static void replace(
        int index,
        Scenario scenario
    ) {
        if (scenario == null) {
            throw new IllegalArgumentException(
                "Scenario must not be null"
            );
        }

        List<Scenario> updatedScenarios = new ArrayList<>(
            getScenarios()
        );

        validateIndex(index, updatedScenarios.size());

        Scenario existing =
            updatedScenarios.get(index);

        updatedScenarios.set(
            index,
            new Scenario(
                existing.id(),
                scenario.name(),
                scenario.steps()
            )
        );

        save(updatedScenarios);
    }

    public static void delete(int index) {
        List<Scenario> updatedScenarios = new ArrayList<>(
            getScenarios()
        );

        validateIndex(index, updatedScenarios.size());
        updatedScenarios.remove(index);
        save(updatedScenarios);
    }

    public static Scenario findById(
        String scenarioId
    ) {
        if (
            scenarioId == null
                || scenarioId.isBlank()
        ) {
            return null;
        }

        for (Scenario scenario : getScenarios()) {
            if (scenario.id().equals(scenarioId)) {
                return scenario;
            }
        }

        return null;
    }

    public static int indexOfId(
        String scenarioId
    ) {
        if (
            scenarioId == null
                || scenarioId.isBlank()
        ) {
            return -1;
        }

        for (
            int index = 0;
            index < getScenarios().size();
            index++
        ) {
            if (
                getScenarios()
                    .get(index)
                    .id()
                    .equals(scenarioId)
            ) {
                return index;
            }
        }

        return -1;
    }

    public static boolean containsName(
        String name,
        int excludedIndex
    ) {
        String normalizedName = name.trim();

        for (
            int index = 0;
            index < getScenarios().size();
            index++
        ) {
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

    private static void validateUniqueIdentities(
        List<Scenario> scenarios
    ) {
        Set<String> identities = new HashSet<>();

        for (Scenario scenario : scenarios) {
            if (scenario == null) {
                throw new IllegalArgumentException(
                    "Scenario library must not contain null"
                );
            }

            if (!identities.add(scenario.id())) {
                throw new IllegalArgumentException(
                    "Duplicate scenario ID: "
                        + scenario.id()
                );
            }
        }
    }

    private static void validateIndex(
        int index,
        int size
    ) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                "Scenario index is out of bounds: " + index
            );
        }
    }
}