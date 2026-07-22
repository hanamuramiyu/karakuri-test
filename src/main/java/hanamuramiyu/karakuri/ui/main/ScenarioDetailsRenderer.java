package hanamuramiyu.karakuri.ui.main;

import hanamuramiyu.karakuri.scenario.model.RepeatStep;
import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.scenario.model.ScenarioFormat;
import hanamuramiyu.karakuri.scenario.model.ScenarioStep;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public final class ScenarioDetailsRenderer {
    private static final int MAX_VISIBLE_STEPS = 10;

    private final Font font;

    public ScenarioDetailsRenderer(Font font) {
        this.font = Objects.requireNonNull(
            font,
            "Font must not be null"
        );
    }

    public void render(
        GuiGraphics graphics,
        int x,
        int y,
        int width,
        int height,
        Scenario scenario,
        int selectedPosition,
        int visibleCount
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
            renderEmptyState(graphics, x, y, width, height);
            return;
        }

        renderScenario(
            graphics,
            x,
            y,
            width,
            height,
            scenario,
            selectedPosition,
            visibleCount
        );
    }

    private void renderScenario(
        GuiGraphics graphics,
        int x,
        int y,
        int width,
        int height,
        Scenario scenario,
        int selectedPosition,
        int visibleCount
    ) {
        Component position = Component.literal(
            selectedPosition >= 0
                ? (selectedPosition + 1) + " of " + visibleCount
                : ""
        );

        graphics.fill(
            x + 1,
            y + 1,
            x + width - 1,
            y + 43,
            0xFF17141F
        );
        graphics.fill(
            x + 1,
            y + 1,
            x + 4,
            y + 43,
            0xFFB38AE8
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(scenario.name(), width - 120)
            ),
            x + 12,
            y + 10,
            0xFFF6F1FA,
            false
        );
        graphics.drawString(
            font,
            position,
            x + width - 12 - font.width(position),
            y + 10,
            0xFF8F8499,
            false
        );
        graphics.drawString(
            font,
            Component.literal(summaryLabel(scenario)),
            x + 12,
            y + 26,
            0xFFA99EB2,
            false
        );

        int statisticsY = y + 55;
        int columnWidth = Math.max(80, (width - 36) / 3);

        renderStatistic(
            graphics,
            x + 12,
            statisticsY,
            columnWidth,
            "Actions",
            Integer.toString(countActions(scenario.steps()))
        );
        renderStatistic(
            graphics,
            x + 18 + columnWidth,
            statisticsY,
            columnWidth,
            "Groups",
            Integer.toString(countGroups(scenario.steps()))
        );
        renderStatistic(
            graphics,
            x + 24 + columnWidth * 2,
            statisticsY,
            Math.max(80, width - 36 - columnWidth * 2),
            "Duration",
            durationLabel(scenario)
        );

        int previewY = statisticsY + 48;

        graphics.drawString(
            font,
            Component.literal("Workflow Preview"),
            x + 12,
            previewY,
            0xFFBEB3C7,
            false
        );

        int availableHeight = height - (previewY - y) - 16;
        int maxByHeight = Math.max(1, availableHeight / 17);
        int visibleStepCount = Math.min(
            scenario.steps().size(),
            Math.min(MAX_VISIBLE_STEPS, maxByHeight)
        );

        for (int index = 0; index < visibleStepCount; index++) {
            ScenarioStep step = scenario.steps().get(index);
            int stepY = previewY + 18 + index * 17;

            graphics.fill(
                x + 12,
                stepY - 3,
                x + width - 12,
                stepY + 11,
                index % 2 == 0
                    ? 0xFF15121B
                    : 0xFF18151E
            );
            graphics.drawString(
                font,
                Component.literal(
                    (index + 1)
                        + ".  "
                        + truncate(
                            step.label(),
                            width - 54
                        )
                ),
                x + 18,
                stepY,
                0xFFC9BECE,
                false
            );
        }

        if (scenario.steps().size() > visibleStepCount) {
            int remaining = scenario.steps().size() - visibleStepCount;

            graphics.drawString(
                font,
                Component.literal(
                    "+ " + remaining + " more top-level actions"
                ),
                x + 18,
                Math.min(
                    y + height - 14,
                    previewY + 18 + visibleStepCount * 17
                ),
                0xFF81768A,
                false
            );
        }
    }

    private void renderStatistic(
        GuiGraphics graphics,
        int x,
        int y,
        int width,
        String label,
        String value
    ) {
        graphics.fill(
            x,
            y,
            x + width,
            y + 34,
            0xFF17141F
        );
        graphics.renderOutline(
            x,
            y,
            width,
            34,
            0xFF302A38
        );
        graphics.drawString(
            font,
            Component.literal(label),
            x + 7,
            y + 6,
            0xFF81778A,
            false
        );
        graphics.drawString(
            font,
            Component.literal(truncate(value, width - 14)),
            x + 7,
            y + 19,
            0xFFE6DFEA,
            false
        );
    }

    private void renderEmptyState(
        GuiGraphics graphics,
        int x,
        int y,
        int width,
        int height
    ) {
        Component title = Component.literal("Nothing selected");
        Component description = Component.literal(
            "Choose a scenario from the list."
        );
        int centerY = y + height / 2;

        graphics.drawString(
            font,
            title,
            x + (width - font.width(title)) / 2,
            centerY - 10,
            0xFFECE6F1,
            false
        );
        graphics.drawString(
            font,
            description,
            x + (width - font.width(description)) / 2,
            centerY + 8,
            0xFF81778A,
            false
        );
    }

    private String summaryLabel(Scenario scenario) {
        int topLevelActions = scenario.steps().size();
        int nestedActions = countActions(scenario.steps());

        if (nestedActions == topLevelActions) {
            return topLevelActions == 1
                ? "1 top-level action"
                : topLevelActions + " top-level actions";
        }

        return topLevelActions
            + " top-level  ·  "
            + nestedActions
            + " total";
    }

    private String durationLabel(Scenario scenario) {
        ScenarioStep lastStep = scenario.steps().getLast();

        if (lastStep.isInfinite()) {
            return "Until stopped";
        }

        long duration = 0;

        for (ScenarioStep step : scenario.steps()) {
            duration += step.durationTicks();
        }

        return ScenarioFormat.formatDuration(
            (int) Math.min(Integer.MAX_VALUE, duration)
        );
    }

    private int countActions(List<ScenarioStep> steps) {
        int count = 0;

        for (ScenarioStep step : steps) {
            count++;

            if (step instanceof RepeatStep repeatStep) {
                count += countActions(repeatStep.steps());
            }
        }

        return count;
    }

    private int countGroups(List<ScenarioStep> steps) {
        int count = 0;

        for (ScenarioStep step : steps) {
            if (step instanceof RepeatStep repeatStep) {
                count++;
                count += countGroups(repeatStep.steps());
            }
        }

        return count;
    }

    private String truncate(
        String value,
        int maximumWidth
    ) {
        if (font.width(value) <= maximumWidth) {
            return value;
        }

        String suffix = "...";
        int end = value.length();

        while (
            end > 0
                && font.width(
                    value.substring(0, end) + suffix
                ) > maximumWidth
        ) {
            end--;
        }

        return value.substring(0, end) + suffix;
    }
}