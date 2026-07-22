package hanamuramiyu.karakuri.ui.main;

public enum ScenarioSortMode {
    LIBRARY_ORDER("Library order"),
    NAME_ASCENDING("Name A-Z"),
    NAME_DESCENDING("Name Z-A"),
    MOST_ACTIONS("Most actions"),
    FEWEST_ACTIONS("Fewest actions");

    private final String label;

    ScenarioSortMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}