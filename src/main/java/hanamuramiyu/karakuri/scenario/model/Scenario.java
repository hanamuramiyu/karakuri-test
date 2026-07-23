package hanamuramiyu.karakuri.scenario.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record Scenario(
    String id,
    String name,
    List<ScenarioStep> steps
) {
    public Scenario(
        String name,
        List<ScenarioStep> steps
    ) {
        this(
            UUID.randomUUID().toString(),
            name,
            steps
        );
    }

    public Scenario {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "Scenario ID must not be blank"
            );
        }

        try {
            id = UUID.fromString(
                id.trim()
            ).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "Scenario ID must be a UUID: " + id,
                exception
            );
        }

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

        validateSequence(
            steps,
            "Scenario"
        );
    }

    public Scenario withIdentity(
        String updatedId
    ) {
        return new Scenario(
            updatedId,
            name,
            steps
        );
    }

    public Scenario withName(
        String updatedName
    ) {
        return new Scenario(
            id,
            updatedName,
            steps
        );
    }

    public Scenario withSteps(
        List<ScenarioStep> updatedSteps
    ) {
        return new Scenario(
            id,
            name,
            updatedSteps
        );
    }

    public Scenario copyWithNewIdentity() {
        return new Scenario(
            name,
            steps
        );
    }

    private static void validateSequence(
        List<ScenarioStep> steps,
        String owner
    ) {
        if (steps.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                owner + " steps must not contain null"
            );
        }

        for (
            int index = 0;
            index < steps.size() - 1;
            index++
        ) {
            if (steps.get(index).isInfinite()) {
                throw new IllegalArgumentException(
                    "An infinite step must be the final step of its group"
                );
            }
        }

        for (ScenarioStep step : steps) {
            if (step instanceof RepeatStep repeatStep) {
                validateSequence(
                    repeatStep.steps(),
                    "Repeat group"
                );
            }
        }
    }
}