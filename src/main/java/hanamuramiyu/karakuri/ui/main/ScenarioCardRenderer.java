package hanamuramiyu.karakuri.ui.main;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class ScenarioCardRenderer {
    private static final int MAX_VISIBLE_STEPS = 12;

    private final Font font;

    public ScenarioCardRenderer(Font font) {
        this.font = Objects.requireNonNull(font, "Font must not be null");
    }

    public void render(
        GuiGraphics graphics,
        int x,
        int y,
        int width,
        int height,
        Scenario scenario,
        int selectedScenarioIndex,
        int scenarioCount,
        boolean compactLayout
    ) {
        graphics.fill(
            x,
            y,
            x + width,
            y + height,
            0xFF100E16
        );

        graphics.renderOutline(
            x,
            y,
            width,
            height,
            0xFF393243
        );

        if (scenario == null) {
            renderEmptyState(
                graphics,
                x,
                y,
                width,
                height,
                compactLayout
            );
            return;
        }

        renderScenario(
            graphics,
            x,
            y,
            width,
            height,
            scenario,
            selectedScenarioIndex,
            scenarioCount
        );
    }

    private void renderEmptyState(
        GuiGraphics graphics,
        int x,
        int y,
        int width,
        int height,
        boolean compactLayout
    ) {
        Component title = Component.literal("No scenarios");
        Component description = Component.literal(
            compactLayout
                ? "Create a workflow"
                : "Create a workflow to begin."
        );

        int centerY = y + height / 2;

        graphics.drawString(
            font,
            title,
            x + (width - font.width(title)) / 2,
            centerY - 12,
            0xFFF0EBF4,
            false
        );

        graphics.drawString(
            font,
            description,
            x + (width - font.width(description)) / 2,
            centerY + 8,
            0xFF8F8499,
            false
        );
    }

    private void renderScenario(
        GuiGraphics graphics,
        int x,
        int y,
        int width,
        int height,
        Scenario scenario,
        int selectedScenarioIndex,
        int scenarioCount
    ) {
        Component position = Component.literal(
            (selectedScenarioIndex + 1) + " / " + scenarioCount
        );

        graphics.drawString(
            font,
            Component.literal(scenario.name()),
            x + 46,
            y + 14,
            0xFFF4F0F7,
            false
        );

        graphics.drawString(
            font,
            position,
            x + width - 46 - font.width(position),
            y + 14,
            0xFF8F8499,
            false
        );

        int availableStepHeight = height - 50;
        int maxVisibleByHeight = Math.max(1, availableStepHeight / 16);
        int visibleStepCount = Math.min(
            scenario.steps().size(),
            Math.min(MAX_VISIBLE_STEPS, maxVisibleByHeight)
        );

        for (int index = 0; index < visibleStepCount; index++) {
            ScenarioStep step = scenario.steps().get(index);

            graphics.drawString(
                font,
                Component.literal((index + 1) + ". " + step.label()),
                x + 46,
                y + 38 + index * 16,
                0xFFC4BACB,
                false
            );
        }

        if (scenario.steps().size() <= visibleStepCount) {
            return;
        }

        int remainingSteps = scenario.steps().size() - visibleStepCount;
        int moreY = Math.min(
            y + height - 16,
            y + 38 + visibleStepCount * 16
        );

        graphics.drawString(
            font,
            Component.literal("+ " + remainingSteps + " more"),
            x + 46,
            moreY,
            0xFF81768A,
            false
        );
    }
}