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

    private final KarakuriButton movementCategoryButton;
    private final KarakuriButton timingCategoryButton;
    private final KarakuriButton mouseCategoryButton;
    private final KarakuriButton blocksCategoryButton;
    private final KarakuriButton inventoryCategoryButton;

    private final KarakuriButton forwardButton;
    private final KarakuriButton backwardButton;
    private final KarakuriButton leftButton;
    private final KarakuriButton rightButton;
    private final KarakuriButton waitButton;

    private final List<KarakuriButton> widgets;

    private Category selectedCategory = Category.MOVEMENT;

    public ScenarioActionLibrary(
        Font font,
        int x,
        int y,
        int width,
        int height,
        Consumer<Scenario.MoveDirection> moveAction,
        Runnable waitAction
    ) {
        this.font = font;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        int categoryX = x + PADDING;
        int categoryWidth = width - PADDING * 2;
        int categoryY = y + 30;

        movementCategoryButton = createButton(
            categoryX,
            categoryY,
            categoryWidth,
            Component.literal("Movement"),
            () -> selectCategory(Category.MOVEMENT)
        );

        timingCategoryButton = createButton(
            categoryX,
            categoryY + 26,
            categoryWidth,
            Component.literal("Timing"),
            () -> selectCategory(Category.TIMING)
        );

        mouseCategoryButton = createButton(
            categoryX,
            categoryY + 52,
            categoryWidth,
            Component.literal("Mouse  ·  Soon"),
            () -> {
            }
        );

        blocksCategoryButton = createButton(
            categoryX,
            categoryY + 78,
            categoryWidth,
            Component.literal("Blocks  ·  Soon"),
            () -> {
            }
        );

        inventoryCategoryButton = createButton(
            categoryX,
            categoryY + 104,
            categoryWidth,
            Component.literal("Inventory  ·  Soon"),
            () -> {
            }
        );

        mouseCategoryButton.active = false;
        blocksCategoryButton.active = false;
        inventoryCategoryButton.active = false;

        int actionY = y + 188;
        int actionWidth = (categoryWidth - BUTTON_GAP) / 2;

        forwardButton = createButton(
            categoryX,
            actionY,
            actionWidth,
            Component.literal("Forward"),
            () -> moveAction.accept(
                Scenario.MoveDirection.FORWARD
            )
        );

        backwardButton = createButton(
            categoryX + actionWidth + BUTTON_GAP,
            actionY,
            actionWidth,
            Component.literal("Backward"),
            () -> moveAction.accept(
                Scenario.MoveDirection.BACKWARD
            )
        );

        leftButton = createButton(
            categoryX,
            actionY + 26,
            actionWidth,
            Component.literal("Left"),
            () -> moveAction.accept(
                Scenario.MoveDirection.LEFT
            )
        );

        rightButton = createButton(
            categoryX + actionWidth + BUTTON_GAP,
            actionY + 26,
            actionWidth,
            Component.literal("Right"),
            () -> moveAction.accept(
                Scenario.MoveDirection.RIGHT
            )
        );

        waitButton = createButton(
            categoryX,
            actionY,
            categoryWidth,
            Component.literal("Wait"),
            waitAction
        );

        widgets = List.of(
            movementCategoryButton,
            timingCategoryButton,
            mouseCategoryButton,
            blocksCategoryButton,
            inventoryCategoryButton,
            forwardButton,
            backwardButton,
            leftButton,
            rightButton,
            waitButton
        );

        updateWidgets();
    }

    public List<KarakuriButton> widgets() {
        return widgets;
    }

    public void render(GuiGraphics graphics) {
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
            y + 10,
            0xFFF1ECF5,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Categories"),
            x + PADDING,
            y + 20,
            0xFF81778A,
            false
        );

        graphics.fill(
            x + PADDING,
            y + 166,
            x + width - PADDING,
            y + 167,
            0xFF302B37
        );

        graphics.drawString(
            font,
            Component.literal(
                "Add " + selectedCategory.label()
            ),
            x + PADDING,
            y + 174,
            0xFF9E94A8,
            false
        );

        graphics.drawString(
            font,
            Component.literal("Inserted after selected block"),
            x + PADDING,
            y + height - 18,
            0xFF716A79,
            false
        );
    }

    private KarakuriButton createButton(
        int buttonX,
        int buttonY,
        int buttonWidth,
        Component message,
        Runnable action
    ) {
        KarakuriButton button = new KarakuriButton(
            font,
            buttonX,
            buttonY,
            buttonWidth,
            BUTTON_HEIGHT,
            message,
            action,
            KarakuriButton.Style.GHOST
        );

        button.setTextAlignment(
            KarakuriButton.TextAlignment.LEFT
        );

        return button;
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        updateWidgets();
    }

    private void updateWidgets() {
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

        boolean movement =
            selectedCategory == Category.MOVEMENT;

        forwardButton.visible = movement;
        backwardButton.visible = movement;
        leftButton.visible = movement;
        rightButton.visible = movement;

        waitButton.visible =
            selectedCategory == Category.TIMING;

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
    }

    private enum Category {
        MOVEMENT("Movement"),
        TIMING("Timing");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }
    }
}