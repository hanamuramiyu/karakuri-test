package hanamuramiyu.karakuri.ui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public final class KarakuriMenuButton extends AbstractWidget {
    private static final int OPTION_GAP = 2;

    private final Font font;
    private final List<Item> items;
    private final Direction direction;

    private boolean expanded;
    private int optionWidth;
    private boolean alignOptionsRight;

    public KarakuriMenuButton(
        Font font,
        int x,
        int y,
        int width,
        int height,
        Component message,
        List<Item> items,
        Direction direction
    ) {
        super(x, y, width, height, message);
        this.font = Objects.requireNonNull(font, "Font must not be null");
        this.items = List.copyOf(
            Objects.requireNonNull(items, "Menu items must not be null")
        );
        this.direction = Objects.requireNonNull(
            direction,
            "Menu direction must not be null"
        );

        this.optionWidth = width;

        if (this.items.isEmpty()) {
            throw new IllegalArgumentException(
                "Menu must contain at least one item"
            );
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void collapse() {
        expanded = false;
    }

    public void setOptionWidth(int optionWidth) {
        this.optionWidth = Math.max(width, optionWidth);
    }

    public void setAlignOptionsRight(boolean alignOptionsRight) {
        this.alignOptionsRight = alignOptionsRight;
    }

    @Override
    protected void renderWidget(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        renderEntry(
            graphics,
            getX(),
            getY(),
            width,
            height,
            getMessage(),
            active,
            contains(mouseX, mouseY, getX(), getY(), width, height),
            0xFFE8E2ED,
            true
        );
    }

    public void renderOverlay(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        if (!expanded || !visible) {
            return;
        }

        for (int index = 0; index < items.size(); index++) {
            Item item = items.get(index);
            int itemY = optionY(index);

            renderEntry(
                graphics,
                optionX(),
                itemY,
                optionWidth,
                height,
                Component.literal(item.label()),
                item.enabled(),
                contains(mouseX, mouseY, optionX(), itemY, optionWidth, height),
                item.textColor(),
                false
            );
        }
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (!visible || event.button() != 0) {
            return false;
        }

        if (
            active
                && contains(
                    event.x(),
                    event.y(),
                    getX(),
                    getY(),
                    width,
                    height
                )
        ) {
            expanded = !expanded;
            return true;
        }

        if (!expanded) {
            return false;
        }

        for (int index = 0; index < items.size(); index++) {
            Item item = items.get(index);
            int itemY = optionY(index);

            if (
                contains(
                    event.x(),
                    event.y(),
                    optionX(),
                    itemY,
                    optionWidth,
                    height
                )
            ) {
                expanded = false;

                if (item.enabled()) {
                    item.action().run();
                }

                return true;
            }
        }

        expanded = false;
        return true;
    }

    @Override
    protected void updateWidgetNarration(
        NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }

    private int optionX() {
        return alignOptionsRight
            ? getX() + width - optionWidth
            : getX();
    }

    private int optionY(int index) {
        return direction == Direction.DOWN
            ? getY() + height + OPTION_GAP + index * height
            : getY() - OPTION_GAP - (index + 1) * height;
    }

    private void renderEntry(
        GuiGraphics graphics,
        int x,
        int y,
        int entryWidth,
        int entryHeight,
        Component text,
        boolean enabled,
        boolean hovered,
        int textColor,
        boolean showArrow
    ) {
        int background = enabled
            ? hovered ? 0xFF2B2635 : 0xFF17151D
            : 0xFF18161D;
        int outline = enabled
            ? hovered ? 0xFF8F6FC0 : 0xFF484052
            : 0xFF332F39;

        graphics.fill(
            x,
            y,
            x + entryWidth,
            y + entryHeight,
            background
        );
        graphics.renderOutline(
            x,
            y,
            entryWidth,
            entryHeight,
            outline
        );

        int renderedTextColor = enabled
            ? textColor
            : 0xFF66606B;
        int textY = y + (entryHeight - font.lineHeight) / 2 + 1;

        graphics.drawString(
            font,
            text,
            x + 9,
            textY,
            renderedTextColor,
            false
        );

        if (showArrow) {
            Component arrow = Component.literal(
                expanded
                    ? direction == Direction.DOWN ? "▴" : "▾"
                    : direction == Direction.DOWN ? "▾" : "▴"
            );

            graphics.drawString(
                font,
                arrow,
                x + entryWidth - 9 - font.width(arrow),
                textY,
                renderedTextColor,
                false
            );
        }
    }

    private boolean contains(
        double mouseX,
        double mouseY,
        int x,
        int y,
        int width,
        int height
    ) {
        return mouseX >= x
            && mouseX < x + width
            && mouseY >= y
            && mouseY < y + height;
    }

    public enum Direction {
        UP,
        DOWN
    }

    public record Item(
        String label,
        Runnable action,
        int textColor,
        Enabled enabledState
    ) {
        public Item(
            String label,
            Runnable action
        ) {
            this(
                label,
                action,
                0xFFE8E2ED,
                () -> true
            );
        }

        public Item(
            String label,
            Runnable action,
            int textColor
        ) {
            this(
                label,
                action,
                textColor,
                () -> true
            );
        }

        public Item {
            Objects.requireNonNull(label, "Menu label must not be null");
            Objects.requireNonNull(action, "Menu action must not be null");
            Objects.requireNonNull(
                enabledState,
                "Menu enabled state must not be null"
            );
        }

        public boolean enabled() {
            return enabledState.enabled();
        }
    }

    @FunctionalInterface
    public interface Enabled {
        boolean enabled();
    }
}