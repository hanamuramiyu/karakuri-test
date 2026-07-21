package hanamuramiyu.karakuri.scenario.model;

public record WaitStep(int durationTicks) implements ScenarioStep {
    public WaitStep {
        ScenarioStep.validateDuration(durationTicks);
    }

    @Override
    public String label() {
        return "Wait for "
            + ScenarioFormat.formatDuration(durationTicks);
    }
}
