package hanamuramiyu.karakuri.ui.editor;

public final class ScenarioEditorPreferences {
    private static String compactTab = "workflow";
    private static String actionCategory = "movement";

    private ScenarioEditorPreferences() {
    }

    public static String compactTab() {
        return compactTab;
    }

    public static void setCompactTab(
        String updatedCompactTab
    ) {
        if (updatedCompactTab == null) {
            throw new IllegalArgumentException(
                "Compact tab must not be null"
            );
        }

        compactTab = updatedCompactTab;
    }

    public static String actionCategory() {
        return actionCategory;
    }

    public static void setActionCategory(
        String updatedActionCategory
    ) {
        if (updatedActionCategory == null) {
            throw new IllegalArgumentException(
                "Action category must not be null"
            );
        }

        actionCategory = updatedActionCategory;
    }
}