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
    private final EnumMap<ScenarioActionLibrary.Category, KarakuriButton> categoryButtons =
        new EnumMap<>(ScenarioActionLibrary.Category.class);
    private final EnumMap<Action, KarakuriButton> actionButtons =
        new EnumMap<>(Action.class);
    private final List<KarakuriButton> widgets;

    ScenarioActionLibraryWidgets(
        Font font,
        ScenarioActionLibraryLayout layout,
        ScenarioActionLibraryActions actions,
        Consumer<ScenarioActionLibrary.Category> categoryAction
    ) {
        this.font = font;
        this.layout = layout;
        this.actions = actions;
        this.categoryAction = categoryAction;

        if (layout.isSidebar()) {
            createSidebarWidgets();
        } else {
            createHorizontalWidgets();
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
        ScenarioActionLibrary.Category selectedCategory
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
                category == selectedCategory
                    ? KarakuriButton.Style.PRIMARY
                    : KarakuriButton.Style.GHOST
            );
        }

        for (Action action : Action.values()) {
            KarakuriButton button = actionButtons.get(action);
            button.visible = action.category == selectedCategory;
            button.setStyle(KarakuriButton.Style.SECONDARY);
        }
    }

    private void createSidebarWidgets() {
        int x = layout.contentX();
        int width = layout.contentWidth();
        int categoryY = layout.y() + 30;

        for (ScenarioActionLibrary.Category category : ScenarioActionLibrary.Category.values()) {
            createCategoryButton(
                category,
                x,
                categoryY + category.ordinal() * 26,
                width,
                category.sidebarLabel,
                KarakuriButton.TextAlignment.LEFT
            );
        }

        int actionY = layout.sidebarActionY();
        int halfWidth = (width - ScenarioActionLibraryLayout.BUTTON_GAP) / 2;
        int thirdWidth = (width - ScenarioActionLibraryLayout.BUTTON_GAP * 2) / 3;

        createActionButton(Action.FORWARD, x, actionY, thirdWidth, "Forward", () -> actions.moveAction().accept(MoveDirection.FORWARD));
        createActionButton(Action.BACKWARD, x + thirdWidth + ScenarioActionLibraryLayout.BUTTON_GAP, actionY, thirdWidth, "Back", () -> actions.moveAction().accept(MoveDirection.BACKWARD));
        createActionButton(Action.JUMP, x + (thirdWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 2, actionY, thirdWidth, "Jump", actions.jumpAction());
        createActionButton(Action.LEFT, x, actionY + 26, halfWidth, "Left", () -> actions.moveAction().accept(MoveDirection.LEFT));
        createActionButton(Action.RIGHT, x + halfWidth + ScenarioActionLibraryLayout.BUTTON_GAP, actionY + 26, halfWidth, "Right", () -> actions.moveAction().accept(MoveDirection.RIGHT));
        createActionButton(Action.WAIT, x, actionY, width, "Wait", actions.waitAction());
        createActionButton(Action.LEFT_CLICK, x, actionY, halfWidth, "Left Click", () -> actions.mouseAction().accept(MouseAction.LEFT_CLICK));
        createActionButton(Action.RIGHT_CLICK, x + halfWidth + ScenarioActionLibraryLayout.BUTTON_GAP, actionY, halfWidth, "Right Click", () -> actions.mouseAction().accept(MouseAction.RIGHT_CLICK));
        createActionButton(Action.CAMERA_LEFT, x, actionY, halfWidth, "Turn Left", () -> actions.cameraAction().accept(CameraDirection.LEFT));
        createActionButton(Action.CAMERA_RIGHT, x + halfWidth + ScenarioActionLibraryLayout.BUTTON_GAP, actionY, halfWidth, "Turn Right", () -> actions.cameraAction().accept(CameraDirection.RIGHT));
        createActionButton(Action.CAMERA_UP, x, actionY + 26, halfWidth, "Look Up", () -> actions.cameraAction().accept(CameraDirection.UP));
        createActionButton(Action.CAMERA_DOWN, x + halfWidth + ScenarioActionLibraryLayout.BUTTON_GAP, actionY + 26, halfWidth, "Look Down", () -> actions.cameraAction().accept(CameraDirection.DOWN));
        createActionButton(Action.HOTBAR, x, actionY, width, "Select Hotbar Slot", actions.hotbarAction());
    }

    private void createHorizontalWidgets() {
        int x = layout.contentX();
        int width = layout.contentWidth();
        int categoryWidth = (width - ScenarioActionLibraryLayout.BUTTON_GAP * 2) / 3;

        for (ScenarioActionLibrary.Category category : ScenarioActionLibrary.Category.values()) {
            int column = category.ordinal() % 3;
            int row = category.ordinal() / 3;

            createCategoryButton(
                category,
                x + column * (categoryWidth + ScenarioActionLibraryLayout.BUTTON_GAP),
                layout.y() + 28 + row * 26,
                categoryWidth,
                category.horizontalLabel,
                KarakuriButton.TextAlignment.CENTER
            );
        }

        int actionY = layout.y() + 84;
        int movementWidth = (width - ScenarioActionLibraryLayout.BUTTON_GAP * 4) / 5;

        createActionButton(Action.FORWARD, x, actionY, movementWidth, "Forward", () -> actions.moveAction().accept(MoveDirection.FORWARD));
        createActionButton(Action.BACKWARD, x + movementWidth + ScenarioActionLibraryLayout.BUTTON_GAP, actionY, movementWidth, "Back", () -> actions.moveAction().accept(MoveDirection.BACKWARD));
        createActionButton(Action.LEFT, x + (movementWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 2, actionY, movementWidth, "Left", () -> actions.moveAction().accept(MoveDirection.LEFT));
        createActionButton(Action.RIGHT, x + (movementWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 3, actionY, movementWidth, "Right", () -> actions.moveAction().accept(MoveDirection.RIGHT));
        createActionButton(Action.JUMP, x + (movementWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 4, actionY, movementWidth, "Jump", actions.jumpAction());
        createActionButton(Action.WAIT, x, actionY, width, "Wait", actions.waitAction());

        int halfWidth = (width - ScenarioActionLibraryLayout.BUTTON_GAP) / 2;
        createActionButton(Action.LEFT_CLICK, x, actionY, halfWidth, "Left Click", () -> actions.mouseAction().accept(MouseAction.LEFT_CLICK));
        createActionButton(Action.RIGHT_CLICK, x + halfWidth + ScenarioActionLibraryLayout.BUTTON_GAP, actionY, halfWidth, "Right Click", () -> actions.mouseAction().accept(MouseAction.RIGHT_CLICK));

        int cameraWidth = (width - ScenarioActionLibraryLayout.BUTTON_GAP * 3) / 4;
        createActionButton(Action.CAMERA_LEFT, x, actionY, cameraWidth, "Left", () -> actions.cameraAction().accept(CameraDirection.LEFT));
        createActionButton(Action.CAMERA_RIGHT, x + cameraWidth + ScenarioActionLibraryLayout.BUTTON_GAP, actionY, cameraWidth, "Right", () -> actions.cameraAction().accept(CameraDirection.RIGHT));
        createActionButton(Action.CAMERA_UP, x + (cameraWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 2, actionY, cameraWidth, "Up", () -> actions.cameraAction().accept(CameraDirection.UP));
        createActionButton(Action.CAMERA_DOWN, x + (cameraWidth + ScenarioActionLibraryLayout.BUTTON_GAP) * 3, actionY, cameraWidth, "Down", () -> actions.cameraAction().accept(CameraDirection.DOWN));
        createActionButton(Action.HOTBAR, x, actionY, width, "Select Hotbar Slot", actions.hotbarAction());
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
            createButton(x, y, width, label, action, alignment)
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
                label,
                callback,
                KarakuriButton.TextAlignment.CENTER
            )
        );
    }

    private KarakuriButton createButton(
        int x,
        int y,
        int width,
        String label,
        Runnable action,
        KarakuriButton.TextAlignment alignment
    ) {
        KarakuriButton button = new KarakuriButton(
            font,
            x,
            y,
            width,
            ScenarioActionLibraryLayout.BUTTON_HEIGHT,
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