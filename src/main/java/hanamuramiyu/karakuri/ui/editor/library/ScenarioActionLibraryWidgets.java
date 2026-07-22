package hanamuramiyu.karakuri.ui.editor.library;

import hanamuramiyu.karakuri.scenario.model.CameraDirection;
import hanamuramiyu.karakuri.scenario.model.MouseAction;
import hanamuramiyu.karakuri.scenario.model.MoveDirection;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

final class ScenarioActionLibraryWidgets {
    private final Font font;
    private final ScenarioActionLibraryLayout layout;
    private final ScenarioActionLibraryActions actions;
    private final Consumer<ScenarioActionLibrary.Category> categoryAction;
    private final Runnable actionExecuted;
    private final EnumMap<ScenarioActionLibrary.Category, KarakuriButton> categoryButtons =
        new EnumMap<>(ScenarioActionLibrary.Category.class);
    private final EnumMap<Action, KarakuriButton> actionButtons =
        new EnumMap<>(Action.class);
    private final List<KarakuriButton> widgets;

    ScenarioActionLibraryWidgets(
        Font font,
        ScenarioActionLibraryLayout layout,
        ScenarioActionLibraryActions actions,
        Consumer<ScenarioActionLibrary.Category> categoryAction,
        Runnable actionExecuted
    ) {
        this.font = font;
        this.layout = layout;
        this.actions = actions;
        this.categoryAction = categoryAction;
        this.actionExecuted = actionExecuted;

        if (layout.isToolbar()) {
            createToolbarWidgets();
        } else {
            createPanelWidgets();
        }

        categoryButtons.get(ScenarioActionLibrary.Category.BLOCKS).active = false;

        List<KarakuriButton> orderedWidgets = new ArrayList<>();
        for (ScenarioActionLibrary.Category category : ScenarioActionLibrary.Category.values()) {
            orderedWidgets.add(categoryButtons.get(category));
        }
        for (Action action : Action.values()) {
            orderedWidgets.add(actionButtons.get(action));
        }
        widgets = List.copyOf(orderedWidgets);
    }

    List<KarakuriButton> all() {
        return widgets;
    }

    void update(
        boolean visible,
        ScenarioActionLibrary.Category selectedCategory,
        boolean drawerOpen
    ) {
        for (KarakuriButton widget : widgets) {
            widget.visible = visible;
        }

        if (!visible) {
            return;
        }

        for (ScenarioActionLibrary.Category category : ScenarioActionLibrary.Category.values()) {
            KarakuriButton button = categoryButtons.get(category);
            button.setStyle(
                drawerOpen && category == selectedCategory
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.GHOST
            );
        }

        for (Action action : Action.values()) {
            KarakuriButton button = actionButtons.get(action);
            button.visible = drawerOpen && action.category == selectedCategory;
            button.setStyle(KarakuriButton.Style.SECONDARY);
        }
    }

    private void createToolbarWidgets() {
        int categoryX = layout.toolbarCategoryX();
        int categoryWidth = layout.toolbarCategoryWidth();

        for (ScenarioActionLibrary.Category category : ScenarioActionLibrary.Category.values()) {
            createCategoryButton(
                category,
                categoryX
                    + category.ordinal()
                    * (
                        categoryWidth
                            + ScenarioActionLibraryLayout.BUTTON_GAP
                    ),
                layout.y() + 5,
                categoryWidth,
                category.toolbarLabel,
                KarakuriButton.TextAlignment.CENTER
            );
        }

        int actionX =
            layout.contentX()
                + getToolbarDrawerLabelWidth();

        int actionWidth =
            layout.x()
                + layout.width()
                - ScenarioActionLibraryLayout.PADDING
                - actionX;

        createActionButtons(
            actionX,
            layout.drawerButtonY(),
            actionWidth
        );
    }


    private int getToolbarDrawerLabelWidth() {
        int labelWidth = 0;

        for (ScenarioActionLibrary.Category category : ScenarioActionLibrary.Category.values()) {
            labelWidth = Math.max(
                labelWidth,
                font.width(
                    Component.literal(
                        category.sectionLabel
                    )
                )
            );
        }

        return labelWidth + 16;
    }

    private void createPanelWidgets() {
        int x = layout.contentX();
        int width = layout.contentWidth();
        int categoryWidth =
            (
                width
                    - ScenarioActionLibraryLayout.BUTTON_GAP * 2
            ) / 3;

        for (ScenarioActionLibrary.Category category : ScenarioActionLibrary.Category.values()) {
            int column = category.ordinal() % 3;
            int row = category.ordinal() / 3;

            createCategoryButton(
                category,
                x
                    + column
                    * (
                        categoryWidth
                            + ScenarioActionLibraryLayout.BUTTON_GAP
                    ),
                layout.y() + 35 + row * 22,
                categoryWidth,
                category.panelLabel,
                KarakuriButton.TextAlignment.CENTER
            );
        }

        createActionButtons(
            x,
            layout.actionY(),
            width
        );
    }

    private void createActionButtons(
        int x,
        int y,
        int width
    ) {
        int movementWidth =
            (
                width
                    - ScenarioActionLibraryLayout.BUTTON_GAP * 4
            ) / 5;

        createActionButton(Action.FORWARD, x, y, movementWidth, "Forward", () -> actions.moveAction().accept(MoveDirection.FORWARD));
        createActionButton(Action.BACKWARD, x + movementWidth + ScenarioActionLibraryLayout.BUTTON_GAP, y, movementWidth, "Back", () -> actions.moveAction().accept(MoveDirection.BACKWARD));
        createActionButton(Action.LEFT, x + (movementWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 2, y, movementWidth, "Left", () -> actions.moveAction().accept(MoveDirection.LEFT));
        createActionButton(Action.RIGHT, x + (movementWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 3, y, movementWidth, "Right", () -> actions.moveAction().accept(MoveDirection.RIGHT));
        createActionButton(Action.JUMP, x + (movementWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 4, y, movementWidth, "Jump", actions.jumpAction());

        int halfWidth =
            (
                width
                    - ScenarioActionLibraryLayout.BUTTON_GAP
            ) / 2;

        createActionButton(Action.WAIT, x, y, halfWidth, "Wait", actions.waitAction());
        createActionButton(Action.REPEAT, x + halfWidth + ScenarioActionLibraryLayout.BUTTON_GAP, y, halfWidth, "Repeat Group", actions.repeatAction());
        createActionButton(Action.LEFT_CLICK, x, y, halfWidth, "Left Click", () -> actions.mouseAction().accept(MouseAction.LEFT_CLICK));
        createActionButton(Action.RIGHT_CLICK, x + halfWidth + ScenarioActionLibraryLayout.BUTTON_GAP, y, halfWidth, "Right Click", () -> actions.mouseAction().accept(MouseAction.RIGHT_CLICK));

        int cameraWidth =
            (
                width
                    - ScenarioActionLibraryLayout.BUTTON_GAP * 3
            ) / 4;

        createActionButton(Action.CAMERA_LEFT, x, y, cameraWidth, "Left", () -> actions.cameraAction().accept(CameraDirection.LEFT));
        createActionButton(Action.CAMERA_RIGHT, x + cameraWidth + ScenarioActionLibraryLayout.BUTTON_GAP, y, cameraWidth, "Right", () -> actions.cameraAction().accept(CameraDirection.RIGHT));
        createActionButton(Action.CAMERA_UP, x + (cameraWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 2, y, cameraWidth, "Up", () -> actions.cameraAction().accept(CameraDirection.UP));
        createActionButton(Action.CAMERA_DOWN, x + (cameraWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 3, y, cameraWidth, "Down", () -> actions.cameraAction().accept(CameraDirection.DOWN));
        createActionButton(Action.HOTBAR, x, y, width, "Select Hotbar Slot", actions.hotbarAction());
    }

    private void createCategoryButton(
        ScenarioActionLibrary.Category category,
        int x,
        int y,
        int width,
        String label,
        KarakuriButton.TextAlignment alignment
    ) {
        Runnable action = category == ScenarioActionLibrary.Category.BLOCKS
            ? () -> {
            }
            : () -> categoryAction.accept(category);

        categoryButtons.put(
            category,
            createButton(
                x,
                y,
                width,
                ScenarioActionLibraryLayout.CATEGORY_HEIGHT,
                label,
                action,
                alignment
            )
        );
    }

    private void createActionButton(
        Action action,
        int x,
        int y,
        int width,
        String label,
        Runnable callback
    ) {
        actionButtons.put(
            action,
            createButton(
                x,
                y,
                width,
                ScenarioActionLibraryLayout.ACTION_HEIGHT,
                label,
                () -> {
                    callback.run();
                    actionExecuted.run();
                },
                KarakuriButton.TextAlignment.CENTER
            )
        );
    }

    private KarakuriButton createButton(
        int x,
        int y,
        int width,
        int height,
        String label,
        Runnable action,
        KarakuriButton.TextAlignment alignment
    ) {
        KarakuriButton button = new KarakuriButton(
            font,
            x,
            y,
            width,
            height,
            Component.literal(label),
            action,
            KarakuriButton.Style.GHOST
        );

        button.setTextAlignment(alignment);
        return button;
    }

    private enum Action {
        FORWARD(ScenarioActionLibrary.Category.MOVEMENT),
        BACKWARD(ScenarioActionLibrary.Category.MOVEMENT),
        LEFT(ScenarioActionLibrary.Category.MOVEMENT),
        RIGHT(ScenarioActionLibrary.Category.MOVEMENT),
        JUMP(ScenarioActionLibrary.Category.MOVEMENT),
        WAIT(ScenarioActionLibrary.Category.TIMING),
        REPEAT(ScenarioActionLibrary.Category.TIMING),
        LEFT_CLICK(ScenarioActionLibrary.Category.MOUSE),
        RIGHT_CLICK(ScenarioActionLibrary.Category.MOUSE),
        CAMERA_LEFT(ScenarioActionLibrary.Category.CAMERA),
        CAMERA_RIGHT(ScenarioActionLibrary.Category.CAMERA),
        CAMERA_UP(ScenarioActionLibrary.Category.CAMERA),
        CAMERA_DOWN(ScenarioActionLibrary.Category.CAMERA),
        HOTBAR(ScenarioActionLibrary.Category.INVENTORY);

        private final ScenarioActionLibrary.Category category;

        Action(ScenarioActionLibrary.Category category) {
            this.category = category;
        }
    }
}