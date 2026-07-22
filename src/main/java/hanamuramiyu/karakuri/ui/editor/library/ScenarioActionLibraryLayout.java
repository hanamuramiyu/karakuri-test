package hanamuramiyu.karakuri.ui.editor.library;

import java.util.Objects;

public record ScenarioActionLibraryLayout(
    int x,
    int y,
    int width,
    int height,
    Mode mode
) {
    static final int PADDING = 6;
    static final int CATEGORY_HEIGHT = 20;
    static final int ACTION_HEIGHT = 20;
    static final int DRAWER_HEIGHT = 32;
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

    boolean isToolbar() {
        return mode == Mode.TOOLBAR;
    }

    int toolbarCategoryX() {
        return x + 86;
    }

    int toolbarCategoryWidth() {
        int availableWidth =
            x + width
                - PADDING
                - toolbarCategoryX();

        return Math.max(
            42,
            (
                availableWidth
                    - BUTTON_GAP * 5
            ) / 6
        );
    }

    int actionY() {
        return isToolbar()
            ? y + height + 34
            : y + 82;
    }

    int drawerY() {
        return actionY() - 2;
    }

    int drawerButtonY() {
        return drawerY()
            + (DRAWER_HEIGHT - ACTION_HEIGHT) / 2;
    }

    public enum Mode {
        TOOLBAR,
        PANEL
    }
}