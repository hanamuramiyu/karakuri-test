package hanamuramiyu.karakuri.ui.editor.inspector;

public record ScenarioInspectorLayout(
    Mode mode,
    int x,
    int y,
    int width,
    int height
) {
    public ScenarioInspectorLayout {
        if (mode == null) {
            throw new IllegalArgumentException(
                "Inspector layout mode must not be null"
            );
        }

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                "Inspector dimensions must be positive"
            );
        }
    }

    public boolean isWide() {
        return mode == Mode.WIDE;
    }

    public enum Mode {
        WIDE,
        COMPACT
    }
}