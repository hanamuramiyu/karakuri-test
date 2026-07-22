package hanamuramiyu.karakuri.ui.editor.workflow;

import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import hanamuramiyu.karakuri.ui.editor.ScenarioStepPresentation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.IntConsumer;

public final class ScenarioWorkflowCanvas {
    private final Font font;
    private List<ScenarioStep> steps;
    private final ScenarioWorkflowViewport viewport;
    private final ScenarioWorkflowInteraction interaction;

    public ScenarioWorkflowCanvas(
        Font font,
        List<ScenarioStep> steps,
        IntConsumer selectionListener,
        Runnable contentListener
    ) {
        this.font = font;
        this.steps = steps;
        this.viewport =
            new ScenarioWorkflowViewport();
        this.interaction =
            new ScenarioWorkflowInteraction(
                steps,
                viewport,
                selectionListener,
                contentListener
            );
    }


    public void setSteps(
        List<ScenarioStep> steps
    ) {
        this.steps = steps;
        interaction.setSteps(steps);
    }

    public void setBounds(
        int x,
        int y,
        int width,
        int height
    ) {
        interaction.setBounds(
            x,
            y,
            width,
            height
        );
    }

    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        Component footer,
        int footerColor
    ) {
        graphics.fill(
            viewport.x(),
            viewport.y(),
            viewport.x() + viewport.width(),
            viewport.y() + viewport.height(),
            0xFF100F17
        );

        graphics.renderOutline(
            viewport.x(),
            viewport.y(),
            viewport.width(),
            viewport.height(),
            0xFF393246
        );

        graphics.enableScissor(
            viewport.x(),
            viewport.y(),
            viewport.x() + viewport.width(),
            viewport.y() + viewport.height()
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
            viewport.x() + 8,
            viewport.y()
                + viewport.height()
                - 11,
            footerColor,
            false
        );
    }

    public boolean mouseClicked(
        MouseButtonEvent event
    ) {
        return interaction.mouseClicked(event);
    }

    public boolean mouseDragged(
        MouseButtonEvent event
    ) {
        return interaction.mouseDragged(event);
    }

    public boolean mouseReleased() {
        return interaction.mouseReleased();
    }

    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        return interaction.mouseScrolled(
            mouseX,
            mouseY,
            horizontalAmount,
            verticalAmount
        );
    }

    public void setSelectedIndex(int index) {
        interaction.setSelectedIndex(index);
    }

    public int getSelectedIndex() {
        return interaction.selectedIndex();
    }

    private void renderConnections(
        GuiGraphics graphics
    ) {
        int centerY =
            viewport.cardY()
                + ScenarioWorkflowViewport
                    .CARD_HEIGHT / 2;

        for (
            int index = 0;
            index < steps.size() - 1;
            index++
        ) {
            int startX =
                viewport.cardX(index)
                    + viewport.cardWidth();

            int endX =
                viewport.cardX(index + 1);

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
        int cardY = viewport.cardY();
        int hoveredIndex =
            viewport.findCardIndex(
                mouseX,
                mouseY,
                steps.size()
            );

        for (
            int index = 0;
            index < steps.size();
            index++
        ) {
            int cardX =
                viewport.cardX(index);

            if (
                interaction.dragging()
                    && index
                        == interaction
                            .pressedIndex()
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
                index
                    == interaction
                        .selectedIndex(),
                index == hoveredIndex,
                false
            );
        }

        if (
            interaction.dragging()
                && interaction.pressedIndex()
                    >= 0
        ) {
            int pressedIndex =
                interaction.pressedIndex();

            renderCard(
                graphics,
                steps.get(pressedIndex),
                pressedIndex,
                interaction.draggedCardX(),
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
            viewport.cardWidth();

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
            cardY
                + ScenarioWorkflowViewport
                    .CARD_HEIGHT,
            background
        );

        graphics.renderOutline(
            cardX,
            cardY,
            cardWidth,
            ScenarioWorkflowViewport.CARD_HEIGHT,
            outline
        );

        int accent =
            ScenarioStepPresentation
                .workflowAccentColor(step);

        graphics.fill(
            cardX,
            cardY,
            cardX + 4,
            cardY
                + ScenarioWorkflowViewport
                    .CARD_HEIGHT,
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
                ScenarioStepPresentation
                    .workflowIcon(step)
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
                ScenarioStepPresentation
                    .workflowTitle(step)
            ),
            cardX + 38,
            cardY + 10,
            0xFFF4F4F7,
            false
        );

        graphics.drawString(
            font,
            Component.literal(
                ScenarioStepPresentation
                    .workflowSubtitle(step)
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
            viewport.cardWidth();

        graphics.fill(
            cardX,
            cardY,
            cardX + cardWidth,
            cardY
                + ScenarioWorkflowViewport
                    .CARD_HEIGHT,
            0x552A2432
        );

        graphics.renderOutline(
            cardX,
            cardY,
            cardWidth,
            ScenarioWorkflowViewport.CARD_HEIGHT,
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
}