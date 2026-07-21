package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.Scenario;
import hanamuramiyu.karakuri.ui.widget.KarakuriButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public final class ScenarioActionLibrary {
    private static final int PADDING = 8;
    private static final int BUTTON_HEIGHT = 22;
    private static final int BUTTON_GAP = 4;

    private final Font font;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Layout layout;

    private final KarakuriButton movementCategoryButton;
    private final KarakuriButton timingCategoryButton;
    private final KarakuriButton mouseCategoryButton;
    private final KarakuriButton cameraCategoryButton;
    private final KarakuriButton blocksCategoryButton;
    private final KarakuriButton inventoryCategoryButton;

    private final KarakuriButton forwardButton;
    private final KarakuriButton backwardButton;
    private final KarakuriButton leftButton;
    private final KarakuriButton rightButton;

    private final KarakuriButton waitButton;

    private final KarakuriButton leftClickButton;
    private final KarakuriButton rightClickButton;

    private final KarakuriButton cameraLeftButton;
    private final KarakuriButton cameraRightButton;
    private final KarakuriButton cameraUpButton;
    private final KarakuriButton cameraDownButton;

    private final List<KarakuriButton> widgets;

    private Category selectedCategory =
        Category.MOVEMENT;

    private boolean visible = true;

    public ScenarioActionLibrary(
        Font font,
        int x,
        int y,
        int width,
        int height,
        Layout layout,
        Consumer<Scenario.MoveDirection> moveAction,
        Runnable waitAction,
        Consumer<Scenario.MouseAction> mouseAction,
        Consumer<Scenario.CameraDirection> cameraAction
    ) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.layout = layout;

        if (layout == Layout.SIDEBAR) {
            int categoryX = x + PADDING;
            int categoryWidth =
                width - PADDING * 2;
            int categoryY = y + 30;

            movementCategoryButton =
                createButton(
                    categoryX,
                    categoryY,
                    categoryWidth,
                    Component.literal("Movement"),
                    () -> selectCategory(
                        Category.MOVEMENT
                    ),
                    KarakuriButton
                        .TextAlignment.LEFT
                );

            timingCategoryButton =
                createButton(
                    categoryX,
                    categoryY + 26,
                    categoryWidth,
                    Component.literal("Timing"),
                    () -> selectCategory(
                        Category.TIMING
                    ),
                    KarakuriButton
                        .TextAlignment.LEFT
                );

            mouseCategoryButton =
                createButton(
                    categoryX,
                    categoryY + 52,
                    categoryWidth,
                    Component.literal("Mouse"),
                    () -> selectCategory(
                        Category.MOUSE
                    ),
                    KarakuriButton
                        .TextAlignment.LEFT
                );

            cameraCategoryButton =
                createButton(
                    categoryX,
                    categoryY + 78,
                    categoryWidth,
                    Component.literal("Camera"),
                    () -> selectCategory(
                        Category.CAMERA
                    ),
                    KarakuriButton
                        .TextAlignment.LEFT
                );

            blocksCategoryButton =
                createButton(
                    categoryX,
                    categoryY + 104,
                    categoryWidth,
                    Component.literal(
                        "Blocks  ·  Soon"
                    ),
                    () -> {
                    },
                    KarakuriButton
                        .TextAlignment.LEFT
                );

            inventoryCategoryButton =
                createButton(
                    categoryX,
                    categoryY + 130,
                    categoryWidth,
                    Component.literal(
                        "Inventory  ·  Soon"
                    ),
                    () -> {
                    },
                    KarakuriButton
                        .TextAlignment.LEFT
                );

            int actionY = Math.min(
                y + 214,
                y + height - 74
            );

            int actionWidth =
                (
                    categoryWidth
                        - BUTTON_GAP
                ) / 2;

            forwardButton = createButton(
                categoryX,
                actionY,
                actionWidth,
                Component.literal("Forward"),
                () -> moveAction.accept(
                    Scenario.MoveDirection.FORWARD
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            backwardButton = createButton(
                categoryX
                    + actionWidth
                    + BUTTON_GAP,
                actionY,
                actionWidth,
                Component.literal("Backward"),
                () -> moveAction.accept(
                    Scenario.MoveDirection.BACKWARD
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            leftButton = createButton(
                categoryX,
                actionY + 26,
                actionWidth,
                Component.literal("Left"),
                () -> moveAction.accept(
                    Scenario.MoveDirection.LEFT
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            rightButton = createButton(
                categoryX
                    + actionWidth
                    + BUTTON_GAP,
                actionY + 26,
                actionWidth,
                Component.literal("Right"),
                () -> moveAction.accept(
                    Scenario.MoveDirection.RIGHT
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            waitButton = createButton(
                categoryX,
                actionY,
                categoryWidth,
                Component.literal("Wait"),
                waitAction,
                KarakuriButton
                    .TextAlignment.CENTER
            );

            leftClickButton = createButton(
                categoryX,
                actionY,
                actionWidth,
                Component.literal("Left Click"),
                () -> mouseAction.accept(
                    Scenario.MouseAction.LEFT_CLICK
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            rightClickButton = createButton(
                categoryX
                    + actionWidth
                    + BUTTON_GAP,
                actionY,
                actionWidth,
                Component.literal("Right Click"),
                () -> mouseAction.accept(
                    Scenario.MouseAction.RIGHT_CLICK
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            cameraLeftButton = createButton(
                categoryX,
                actionY,
                actionWidth,
                Component.literal("Turn Left"),
                () -> cameraAction.accept(
                    Scenario.CameraDirection.LEFT
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            cameraRightButton = createButton(
                categoryX
                    + actionWidth
                    + BUTTON_GAP,
                actionY,
                actionWidth,
                Component.literal("Turn Right"),
                () -> cameraAction.accept(
                    Scenario.CameraDirection.RIGHT
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            cameraUpButton = createButton(
                categoryX,
                actionY + 26,
                actionWidth,
                Component.literal("Look Up"),
                () -> cameraAction.accept(
                    Scenario.CameraDirection.UP
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            cameraDownButton = createButton(
                categoryX
                    + actionWidth
                    + BUTTON_GAP,
                actionY + 26,
                actionWidth,
                Component.literal("Look Down"),
                () -> cameraAction.accept(
                    Scenario.CameraDirection.DOWN
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );
        } else {
            int availableWidth =
                width - PADDING * 2;

            int categoryWidth =
                (
                    availableWidth
                        - BUTTON_GAP * 2
                ) / 3;

            int categoryX = x + PADDING;
            int firstCategoryY = y + 28;
            int secondCategoryY = y + 54;

            movementCategoryButton =
                createButton(
                    categoryX,
                    firstCategoryY,
                    categoryWidth,
                    Component.literal("Move"),
                    () -> selectCategory(
                        Category.MOVEMENT
                    ),
                    KarakuriButton
                        .TextAlignment.CENTER
                );

            timingCategoryButton =
                createButton(
                    categoryX
                        + categoryWidth
                        + BUTTON_GAP,
                    firstCategoryY,
                    categoryWidth,
                    Component.literal("Time"),
                    () -> selectCategory(
                        Category.TIMING
                    ),
                    KarakuriButton
                        .TextAlignment.CENTER
                );

            mouseCategoryButton =
                createButton(
                    categoryX
                        + (
                            categoryWidth
                                + BUTTON_GAP
                        ) * 2,
                    firstCategoryY,
                    categoryWidth,
                    Component.literal("Mouse"),
                    () -> selectCategory(
                        Category.MOUSE
                    ),
                    KarakuriButton
                        .TextAlignment.CENTER
                );

            cameraCategoryButton =
                createButton(
                    categoryX,
                    secondCategoryY,
                    categoryWidth,
                    Component.literal("Camera"),
                    () -> selectCategory(
                        Category.CAMERA
                    ),
                    KarakuriButton
                        .TextAlignment.CENTER
                );

            blocksCategoryButton =
                createButton(
                    categoryX
                        + categoryWidth
                        + BUTTON_GAP,
                    secondCategoryY,
                    categoryWidth,
                    Component.literal("Blocks"),
                    () -> {
                    },
                    KarakuriButton
                        .TextAlignment.CENTER
                );

            inventoryCategoryButton =
                createButton(
                    categoryX
                        + (
                            categoryWidth
                                + BUTTON_GAP
                        ) * 2,
                    secondCategoryY,
                    categoryWidth,
                    Component.literal("Items"),
                    () -> {
                    },
                    KarakuriButton
                        .TextAlignment.CENTER
                );

            int actionY = y + 84;

            int actionWidth =
                (
                    availableWidth
                        - BUTTON_GAP * 3
                ) / 4;

            forwardButton = createButton(
                categoryX,
                actionY,
                actionWidth,
                Component.literal("Forward"),
                () -> moveAction.accept(
                    Scenario.MoveDirection.FORWARD
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            backwardButton = createButton(
                categoryX
                    + actionWidth
                    + BUTTON_GAP,
                actionY,
                actionWidth,
                Component.literal("Backward"),
                () -> moveAction.accept(
                    Scenario.MoveDirection.BACKWARD
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            leftButton = createButton(
                categoryX
                    + (
                        actionWidth
                            + BUTTON_GAP
                    ) * 2,
                actionY,
                actionWidth,
                Component.literal("Left"),
                () -> moveAction.accept(
                    Scenario.MoveDirection.LEFT
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            rightButton = createButton(
                categoryX
                    + (
                        actionWidth
                            + BUTTON_GAP
                    ) * 3,
                actionY,
                actionWidth,
                Component.literal("Right"),
                () -> moveAction.accept(
                    Scenario.MoveDirection.RIGHT
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            waitButton = createButton(
                categoryX,
                actionY,
                availableWidth,
                Component.literal("Wait"),
                waitAction,
                KarakuriButton
                    .TextAlignment.CENTER
            );

            int halfWidth =
                (
                    availableWidth
                        - BUTTON_GAP
                ) / 2;

            leftClickButton = createButton(
                categoryX,
                actionY,
                halfWidth,
                Component.literal("Left Click"),
                () -> mouseAction.accept(
                    Scenario.MouseAction.LEFT_CLICK
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            rightClickButton = createButton(
                categoryX
                    + halfWidth
                    + BUTTON_GAP,
                actionY,
                halfWidth,
                Component.literal("Right Click"),
                () -> mouseAction.accept(
                    Scenario.MouseAction.RIGHT_CLICK
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            cameraLeftButton = createButton(
                categoryX,
                actionY,
                actionWidth,
                Component.literal("Left"),
                () -> cameraAction.accept(
                    Scenario.CameraDirection.LEFT
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            cameraRightButton = createButton(
                categoryX
                    + actionWidth
                    + BUTTON_GAP,
                actionY,
                actionWidth,
                Component.literal("Right"),
                () -> cameraAction.accept(
                    Scenario.CameraDirection.RIGHT
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            cameraUpButton = createButton(
                categoryX
                    + (
                        actionWidth
                            + BUTTON_GAP
                    ) * 2,
                actionY,
                actionWidth,
                Component.literal("Up"),
                () -> cameraAction.accept(
                    Scenario.CameraDirection.UP
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );

            cameraDownButton = createButton(
                categoryX
                    + (
                        actionWidth
                            + BUTTON_GAP
                    ) * 3,
                actionY,
                actionWidth,
                Component.literal("Down"),
                () -> cameraAction.accept(
                    Scenario.CameraDirection.DOWN
                ),
                KarakuriButton
                    .TextAlignment.CENTER
            );
        }

        blocksCategoryButton.active = false;
        inventoryCategoryButton.active = false;

        widgets = List.of(
            movementCategoryButton,
            timingCategoryButton,
            mouseCategoryButton,
            cameraCategoryButton,
            blocksCategoryButton,
            inventoryCategoryButton,
            forwardButton,
            backwardButton,
            leftButton,
            rightButton,
            waitButton,
            leftClickButton,
            rightClickButton,
            cameraLeftButton,
            cameraRightButton,
            cameraUpButton,
            cameraDownButton
        );

        updateWidgets();
    }

    public List<KarakuriButton> widgets() {
        return widgets;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        updateWidgets();
    }

    public void render(GuiGraphics graphics) {
        if (!visible) {
            return;
        }

        graphics.fill(
            x,
            y,
            x + width,
            y + height,
            0xFF121018
        );

        graphics.renderOutline(
            x,
            y,
            width,
            height,
            0xFF393243
        );

        graphics.fill(
            x,
            y,
            x + 3,
            y + height,
            0xFF776092
        );

        graphics.drawString(
            font,
            Component.literal("Action Library"),
            x + PADDING,
            y + 9,
            0xFFF1ECF5,
            false
        );

        if (layout == Layout.SIDEBAR) {
            graphics.drawString(
                font,
                Component.literal("Categories"),
                x + PADDING,
                y + 20,
                0xFF81778A,
                false
            );

            int dividerY = Math.min(
                y + 192,
                y + height - 94
            );

            graphics.fill(
                x + PADDING,
                dividerY,
                x + width - PADDING,
                dividerY + 1,
                0xFF302B37
            );

            graphics.drawString(
                font,
                Component.literal(
                    "Add "
                        + selectedCategory.label()
                ),
                x + PADDING,
                dividerY + 8,
                0xFF9E94A8,
                false
            );
        }

        if (height >= 118) {
            graphics.drawString(
                font,
                Component.literal(
                    layout == Layout.SIDEBAR
                        ? "Inserted after selected block"
                        : "Inserted after the selected block"
                ),
                x + PADDING,
                y + height - 14,
                0xFF716A79,
                false
            );
        }
    }

    private KarakuriButton createButton(
        int buttonX,
        int buttonY,
        int buttonWidth,
        Component message,
        Runnable action,
        KarakuriButton.TextAlignment alignment
    ) {
        KarakuriButton button =
            new KarakuriButton(
                font,
                buttonX,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT,
                message,
                action,
                KarakuriButton.Style.GHOST
            );

        button.setTextAlignment(alignment);
        return button;
    }

    private void selectCategory(
        Category category
    ) {
        selectedCategory = category;
        updateWidgets();
    }

    private void updateWidgets() {
        for (KarakuriButton widget : widgets) {
            widget.visible = visible;
        }

        if (!visible) {
            return;
        }

        movementCategoryButton.setStyle(
            selectedCategory == Category.MOVEMENT
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.GHOST
        );

        timingCategoryButton.setStyle(
            selectedCategory == Category.TIMING
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.GHOST
        );

        mouseCategoryButton.setStyle(
            selectedCategory == Category.MOUSE
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.GHOST
        );

        cameraCategoryButton.setStyle(
            selectedCategory == Category.CAMERA
                ? KarakuriButton.Style.PRIMARY
                : KarakuriButton.Style.GHOST
        );

        boolean movement =
            selectedCategory == Category.MOVEMENT;

        boolean timing =
            selectedCategory == Category.TIMING;

        boolean mouse =
            selectedCategory == Category.MOUSE;

        boolean camera =
            selectedCategory == Category.CAMERA;

        forwardButton.visible = movement;
        backwardButton.visible = movement;
        leftButton.visible = movement;
        rightButton.visible = movement;

        waitButton.visible = timing;

        leftClickButton.visible = mouse;
        rightClickButton.visible = mouse;

        cameraLeftButton.visible = camera;
        cameraRightButton.visible = camera;
        cameraUpButton.visible = camera;
        cameraDownButton.visible = camera;

        forwardButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        backwardButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        leftButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        rightButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        waitButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        leftClickButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        rightClickButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        cameraLeftButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        cameraRightButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        cameraUpButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );

        cameraDownButton.setStyle(
            KarakuriButton.Style.SECONDARY
        );
    }

    public enum Layout {
        SIDEBAR,
        HORIZONTAL
    }

    private enum Category {
        MOVEMENT("Movement"),
        TIMING("Timing"),
        MOUSE("Mouse"),
        CAMERA("Camera");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }
    }
}