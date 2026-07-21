package hanamuramiyu.karakuri.scenario.model;

import java.util.List;

public record Scenario(String name, List<ScenarioStep> steps) {
    public Scenario {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Scenario name must not be blank"
            );
        }

        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException(
                "Scenario must contain at least one step"
            );
        }

        name = name.trim();
        steps = List.copyOf(steps);

        for (int index = 0; index < steps.size() - 1; index++) {
            if (steps.get(index).isInfinite()) {
                throw new IllegalArgumentException(
                    "An infinite step must be the final scenario step"
                );
            }
        }
    }
}
