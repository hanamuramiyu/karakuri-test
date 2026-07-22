package hanamuramiyu.karakuri.ui.editor.library;

import java.util.Objects;

public record ScenarioActionLibraryLayout(
    int x,
    int y,
    int width,
    int height,
    Mode mode
) {
    static final int PADDING = 8;
    static final int BUTTON_HEIGHT = 22;
    static final int BUTTON_GAP = 4;

    public ScenarioActionLibraryLayout {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be greater than zero");
        }

        if (height <= 0) {
            throw new IllegalArgumentException("Height must be greater than zero");
        }

        Objects.requireNonNull(mode, "Layout mode must not be null");
    }

    int contentX() {
        return x + PADDING;
    }

    int contentWidth() {
        return width - PADDING * 2;
    }

    int sidebarActionY() {
        return Math.min(
            y + 214,
            y + height - 74
        );
    }

    int sidebarDividerY() {
        return Math.min(
            y + 192,
            y + height - 94
        );
    }

    boolean isSidebar() {
        return mode == Mode.SIDEBAR;
    }

    public enum Mode {
        SIDEBAR,
        HORIZONTAL
    }
}