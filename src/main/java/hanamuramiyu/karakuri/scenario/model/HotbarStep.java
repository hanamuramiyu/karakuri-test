package hanamuramiyu.karakuri.scenario.model;

public record HotbarStep(int slot) implements ScenarioStep {
    public static final int MIN_SLOT = 0;
    public static final int MAX_SLOT = 8;
    public static final int DEFAULT_SLOT = 0;

    public HotbarStep {
        if (slot < MIN_SLOT || slot > MAX_SLOT) {
            throw new IllegalArgumentException(
                "Hotbar slot must be between 0 and 8"
            );
        }
    }

    @Override
    public int durationTicks() {
        return 1;
    }

    @Override
    public String label() {
        return "Select hotbar slot " + (slot + 1);
    }

    @Override
    public <T> T accept(
        ScenarioStepVisitor<T> visitor
    ) {
        return visitor.visit(this);
    }

    public HotbarStep withSlot(int updatedSlot) {
        return new HotbarStep(updatedSlot);
    }
}