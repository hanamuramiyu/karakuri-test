package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.scenario.model.CameraStep;
import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import hanamuramiyu.karakuri.scenario.model.JumpMode;
import hanamuramiyu.karakuri.scenario.model.JumpStep;
import hanamuramiyu.karakuri.scenario.model.JumpStopMode;
import hanamuramiyu.karakuri.scenario.model.MouseInputMode;
import hanamuramiyu.karakuri.scenario.model.MouseStep;
import hanamuramiyu.karakuri.scenario.model.MouseStopMode;
import hanamuramiyu.karakuri.scenario.model.MoveStep;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.scenario.model.WaitStep;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepPresentation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.IntConsumer;

public final class ScenarioWorkflowCanvas {
    private static final int CARD_MIN_WIDTH = 104;
    private static final int CARD_MAX_WIDTH = 132;
    private static final int CARD_HEIGHT = 58;
    private static final int CARD_GAP = 30;
    private static final int DRAG_THRESHOLD = 4;
    private static final int SCROLL_STEP = 36;
    private static final int DURATION_STEP_TICKS = 10;
    private static final int CAMERA_ANGLE_STEP = 5;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS = 72000;

    private final Font font;
    private final List<ScenarioStep> steps;
    private final IntConsumer selectionListener;
    private final Runnable contentListener;

    private int x;
    private int y;
    private int width;
    private int height;
    private int selectedIndex;
    private int scrollOffset;
    private int pressedIndex = -1;
    private double dragStartX;
    private double dragMouseX;
    private double dragOffsetX;
    private boolean dragging;

    public ScenarioWorkflowCanvas(
        Font font,
        List<ScenarioStep> steps,
        IntConsumer selectionListener,
        Runnable contentListener
    ) {
        this.font = font;
        this.steps = steps;
        this.selectionListener = selectionListener;
        this.contentListener = contentListener;
    }

    public void setBounds(
        int x,
        int y,
        int width,
        int height
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            getMaxScrollOffset()
        );

        ensureSelectedVisible();
    }

    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        Component footer,
        int footerColor
    ) {
        graphics.fill(
            x,
            y,
            x + width,
            y + height,
            0xFF100F17
        );

        graphics.renderOutline(
            x,
            y,
            width,
            height,
            0xFF393246
        );

        graphics.enableScissor(
            x,
            y,
            x + width,
            y + height
        );

        renderConnections(graphics);

        renderCards(
            graphics,
            mouseX,
            mouseY
        );

        graphics.disableScissor();

        graphics.drawString(
            font,
            footer,
            x + 8,
            y + height - 11,
            footerColor,
            false
        );
    }

    public boolean mouseClicked(
        MouseButtonEvent event
    ) {
        if (
            event.button() != 0
                || !contains(
                    event.x(),
                    event.y()
                )
        ) {
            return false;
        }

        int index = findCardIndex(
            event.x(),
            event.y()
        );

        if (index < 0) {
            return false;
        }

        select(index);

        pressedIndex = index;
        dragStartX = event.x();
        dragMouseX = event.x();
        dragOffsetX =
            event.x() - getCardX(index);
        dragging = false;

        return true;
    }

    public boolean mouseDragged(
        MouseButtonEvent event
    ) {
        if (
            pressedIndex < 0
                || event.button() != 0
        ) {
            return false;
        }

        dragMouseX = event.x();

        if (!dragging) {
            dragging = Math.abs(
                event.x() - dragStartX
            ) >= DRAG_THRESHOLD;
        }

        if (!dragging) {
            return true;
        }

        autoScroll(event.x());

        int targetIndex =
            getDragTargetIndex(
                event.x()
            );

        if (targetIndex != pressedIndex) {
            ScenarioStep step =
                steps.remove(pressedIndex);

            steps.add(targetIndex, step);

            pressedIndex = targetIndex;
            selectedIndex = targetIndex;

            contentListener.run();
        }

        return true;
    }

    public boolean mouseReleased() {
        boolean handled =
            pressedIndex >= 0;

        pressedIndex = -1;
        dragging = false;

        return handled;
    }

    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }

        int index = findCardIndex(
            mouseX,
            mouseY
        );

        if (
            index >= 0
                && verticalAmount != 0.0
        ) {
            select(index);

            adjustSelectedPrimaryValue(
                verticalAmount > 0.0
                    ? 1
                    : -1
            );

            return true;
        }

        double amount =
            verticalAmount != 0.0
                ? verticalAmount
                : horizontalAmount;

        scrollOffset = Math.clamp(
            scrollOffset
                - (int) Math.round(
                    amount * SCROLL_STEP
                ),
            0,
            getMaxScrollOffset()
        );

        return true;
    }

    public void setSelectedIndex(
        int index
    ) {
        selectedIndex = Math.clamp(
            index,
            0,
            steps.size() - 1
        );

        ensureSelectedVisible();
    }

    public int getSelectedIndex() {
        selectedIndex = Math.clamp(
            selectedIndex,
            0,
            steps.size() - 1
        );

        return selectedIndex;
    }

    private void renderConnections(
        GuiGraphics graphics
    ) {
        int centerY =
            getCardY() + CARD_HEIGHT / 2;

        for (
            int index = 0;
            index < steps.size() - 1;
            index++
        ) {
            int startX =
                getCardX(index)
                    + getCardWidth();

            int endX =
                getCardX(index + 1);

            graphics.fill(
                startX,
                centerY - 1,
                endX,
                centerY + 1,
                0xFF625A70
            );

            graphics.drawString(
                font,
                Component.literal(">"),
                endX - 11,
                centerY - 4,
                0xFF9B91AA,
                false
            );
        }
    }

    private void renderCards(
        GuiGraphics graphics,
        int mouseX,
        int mouseY
    ) {
        int cardY = getCardY();

        for (
            int index = 0;
            index < steps.size();
            index++
        ) {
            int cardX =
                getCardX(index);

            if (
                dragging
                    && index == pressedIndex
            ) {
                renderDropSlot(
                    graphics,
                    cardX,
                    cardY
                );

                continue;
            }

            renderCard(
                graphics,
                steps.get(index),
                index,
                cardX,
                cardY,
                index == selectedIndex,
                isInsideCard(
                    mouseX,
                    mouseY,
                    cardX,
                    cardY
                ),
                false
            );
        }

        if (
            dragging
                && pressedIndex >= 0
        ) {
            renderCard(
                graphics,
                steps.get(pressedIndex),
                pressedIndex,
                (int) Math.round(
                    dragMouseX - dragOffsetX
                ),
                cardY,
                true,
                false,
                true
            );
        }
    }

    private void renderCard(
        GuiGraphics graphics,
        ScenarioStep step,
        int index,
        int cardX,
        int cardY,
        boolean selected,
        boolean hovered,
        boolean ghost
    ) {
        int cardWidth =
            getCardWidth();

        int background =
            selected
                ? 0xFF392D4E
                : hovered
                    ? 0xFF292333
                    : 0xFF211D29;

        int outline =
            selected
                ? 0xFF9B79D1
                : hovered
                    ? 0xFF6F607F
                    : 0xFF484052;

        if (ghost) {
            background = 0xEE45365E;
            outline = 0xFFB68CEB;
        }

        graphics.fill(
            cardX,
            cardY,
            cardX + cardWidth,
            cardY + CARD_HEIGHT,
            background
        );

        graphics.renderOutline(
            cardX,
            cardY,
            cardWidth,
            CARD_HEIGHT,
            outline
        );

        int accent =
            ScenarioStepPresentation.workflowAccentColor(step);

        graphics.fill(
            cardX,
            cardY,
            cardX + 4,
            cardY + CARD_HEIGHT,
            accent
        );

        graphics.fill(
            cardX + 10,
            cardY + 10,
            cardX + 30,
            cardY + 30,
            0xFF16131D
        );

        graphics.renderOutline(
            cardX + 10,
            cardY + 10,
            20,
            20,
            accent
        );

        Component icon =
            Component.literal(
                ScenarioStepPresentation.workflowIcon(step)
            );

        graphics.drawString(
            font,
            icon,
            cardX
                + 20
                - font.width(icon) / 2,
            cardY + 16,
            accent,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                ScenarioStepPresentation.workflowTitle(step)
            ),
            cardX + 38,
            cardY + 10,
            0xFFF4F4F7,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                ScenarioStepPresentation.workflowSubtitle(step)
            ),
            cardX + 38,
            cardY + 26,
            0xFFB8AFC2,
            false
        );

        Component indexText =
            Component.literal(
                "#" + (index + 1)
            );

        graphics.drawString(
            font,
            indexText,
            cardX
                + cardWidth
                - 8
                - font.width(indexText),
            cardY + 42,
            0xFF81798E,
            false
        );

        graphics.drawString(
            font,
            Component.literal("::"),
            cardX + 10,
            cardY + 42,
            0xFF81798E,
            false
        );
    }

    private void renderDropSlot(
        GuiGraphics graphics,
        int cardX,
        int cardY
    ) {
        int cardWidth =
            getCardWidth();

        graphics.fill(
            cardX,
            cardY,
            cardX + cardWidth,
            cardY + CARD_HEIGHT,
            0x552A2432
        );

        graphics.renderOutline(
            cardX,
            cardY,
            cardWidth,
            CARD_HEIGHT,
            0xFF756682
        );

        Component text =
            Component.literal(
                "Drop here"
            );

        graphics.drawString(
            font,
            text,
            cardX
                + (
                    cardWidth
                        - font.width(text)
                ) / 2,
            cardY + 25,
            0xFF9B91AA,
            false
        );
    }

    private void select(
        int index
    ) {
        selectedIndex = Math.clamp(
            index,
            0,
            steps.size() - 1
        );

        ensureSelectedVisible();

        selectionListener.accept(
            selectedIndex
        );
    }

    private void adjustSelectedPrimaryValue(
        int direction
    ) {
        ScenarioStep step =
            steps.get(
                getSelectedIndex()
            );

        ScenarioStep updatedStep =
            switch (step) {
                case CameraStep cameraStep ->
                    cameraStep.withAngleDegrees(
                        Math.clamp(
                            cameraStep.angleDegrees()
                                + direction
                                * CAMERA_ANGLE_STEP,
                            CameraStep
                                .MIN_ANGLE_DEGREES,
                            CameraStep
                                .MAX_ANGLE_DEGREES
                        )
                    );

                case HotbarStep hotbarStep ->
                    hotbarStep.withSlot(
                        Math.clamp(
                            hotbarStep.slot()
                                + direction,
                            HotbarStep
                                .MIN_SLOT,
                            HotbarStep
                                .MAX_SLOT
                        )
                    );

                case JumpStep jumpStep ->
                    adjustJumpStep(
                        jumpStep,
                        direction
                    );

                case MoveStep moveStep ->
                    moveStep.withDurationTicks(
                        Math.clamp(
                            moveStep.durationTicks()
                                + direction
                                * DURATION_STEP_TICKS,
                            MIN_DURATION_TICKS,
                            MAX_DURATION_TICKS
                        )
                    );

                case MouseStep mouseStep ->
                    adjustMouseStep(
                        mouseStep,
                        direction
                    );

                case WaitStep waitStep ->
                    new WaitStep(
                        Math.clamp(
                            waitStep.durationTicks()
                                + direction
                                * DURATION_STEP_TICKS,
                            MIN_DURATION_TICKS,
                            MAX_DURATION_TICKS
                        )
                    );
            };

        if (updatedStep == step) {
            return;
        }

        steps.set(
            selectedIndex,
            updatedStep
        );

        contentListener.run();
    }

    private JumpStep adjustJumpStep(
        JumpStep step,
        int direction
    ) {
        if (
            step.mode()
                == JumpMode.SINGLE
                || step.stopMode()
                    == JumpStopMode.MANUAL
        ) {
            return step;
        }

        if (
            step.mode()
                == JumpMode.REPEAT
                && step.stopMode()
                    == JumpStopMode.JUMP_COUNT
        ) {
            return step.withJumpCount(
                Math.clamp(
                    step.jumpCount() + direction,
                    JumpStep
                        .MIN_JUMP_COUNT,
                    JumpStep
                        .MAX_JUMP_COUNT
                )
            );
        }

        return step.withDurationTicks(
            Math.clamp(
                step.durationTicks()
                    + direction
                    * DURATION_STEP_TICKS,
                MIN_DURATION_TICKS,
                MAX_DURATION_TICKS
            )
        );
    }

    private MouseStep adjustMouseStep(
        MouseStep step,
        int direction
    ) {
        if (
            step.stopMode()
                == MouseStopMode.MANUAL
        ) {
            return step;
        }

        if (
            step.inputMode()
                == MouseInputMode.CLICK
                && step.stopMode()
                    == MouseStopMode.CLICK_COUNT
        ) {
            return step.withClickCount(
                Math.clamp(
                    step.clickCount() + direction,
                    MouseStep
                        .MIN_CLICK_COUNT,
                    MouseStep
                        .MAX_CLICK_COUNT
                )
            );
        }

        return step.withDurationTicks(
            Math.clamp(
                step.durationTicks()
                    + direction
                    * DURATION_STEP_TICKS,
                MIN_DURATION_TICKS,
                MAX_DURATION_TICKS
            )
        );
    }

    private void ensureSelectedVisible() {
        if (
            width <= 0
                || steps.isEmpty()
        ) {
            return;
        }

        int localLeft =
            12
                + selectedIndex
                * (
                    getCardWidth()
                        + CARD_GAP
                );

        int localRight =
            localLeft
                + getCardWidth();

        if (
            localLeft - scrollOffset < 8
        ) {
            scrollOffset =
                localLeft - 8;
        } else if (
            localRight - scrollOffset
                > width - 8
        ) {
            scrollOffset =
                localRight
                    - width
                    + 8;
        }

        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            getMaxScrollOffset()
        );
    }

    private void autoScroll(
        double mouseX
    ) {
        if (mouseX < x + 24) {
            scrollOffset -= 8;
        } else if (
            mouseX > x + width - 24
        ) {
            scrollOffset += 8;
        }

        scrollOffset = Math.clamp(
            scrollOffset,
            0,
            getMaxScrollOffset()
        );
    }

    private int getDragTargetIndex(
        double mouseX
    ) {
        int stride =
            getCardWidth()
                + CARD_GAP;

        double localX =
            mouseX
                - x
                - 12
                + scrollOffset
                + stride / 2.0;

        return Math.clamp(
            (int) Math.floor(
                localX / stride
            ),
            0,
            steps.size() - 1
        );
    }

    private int findCardIndex(
        double mouseX,
        double mouseY
    ) {
        int cardY = getCardY();

        for (
            int index = 0;
            index < steps.size();
            index++
        ) {
            int cardX =
                getCardX(index);

            if (
                isInsideCard(
                    mouseX,
                    mouseY,
                    cardX,
                    cardY
                )
            ) {
                return index;
            }
        }

        return -1;
    }

    private boolean isInsideCard(
        double mouseX,
        double mouseY,
        int cardX,
        int cardY
    ) {
        return mouseX >= cardX
            && mouseX
                < cardX
                    + getCardWidth()
            && mouseY >= cardY
            && mouseY
                < cardY
                    + CARD_HEIGHT;
    }

    private boolean contains(
        double mouseX,
        double mouseY
    ) {
        return mouseX >= x
            && mouseX < x + width
            && mouseY >= y
            && mouseY < y + height;
    }

    private int getCardWidth() {
        return Math.clamp(
            width / 3,
            CARD_MIN_WIDTH,
            CARD_MAX_WIDTH
        );
    }

    private int getCardY() {
        int usableHeight =
            height - 16;

        return y
            + Math.max(
                5,
                (
                    usableHeight
                        - CARD_HEIGHT
                ) / 2
            );
    }

    private int getCardX(
        int index
    ) {
        return x
            + 12
            - scrollOffset
            + index
            * (
                getCardWidth()
                    + CARD_GAP
            );
    }

    private int getMaxScrollOffset() {
        int totalWidth =
            24
                + steps.size()
                * getCardWidth()
                + Math.max(
                    0,
                    steps.size() - 1
                )
                * CARD_GAP;

        return Math.max(
            0,
            totalWidth - width
        );
    }

}