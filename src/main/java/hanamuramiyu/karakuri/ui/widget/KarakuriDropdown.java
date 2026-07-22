package hanamuramiyu.karakuri.ui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class KarakuriDropdown<T> extends AbstractWidget {
    private static final int OPTION_GAP = 2;

    private final Font font;
    private final List<Option<T>> options;
    private final Consumer<T> selectionAction;
    private final Runnable expandedChanged;

    private T value;
    private String labelPrefix = "";
    private boolean expanded;

    public KarakuriDropdown(
        Font font,
        int x,
        int y,
        int width,
        int height,
        List<Option<T>> options,
        T initialValue,
        Consumer<T> selectionAction,
        Runnable expandedChanged
    ) {
        super(
            x,
            y,
            width,
            height,
            Component.literal("")
        );

        this.font = Objects.requireNonNull(
            font,
            "Font must not be null"
        );
        this.options = List.copyOf(
            Objects.requireNonNull(
                options,
                "Dropdown options must not be null"
            )
        );

        if (this.options.isEmpty()) {
            throw new IllegalArgumentException(
                "Dropdown must contain at least one option"
            );
        }

        this.selectionAction = Objects.requireNonNull(
            selectionAction,
            "Selection action must not be null"
        );
        this.expandedChanged = Objects.requireNonNull(
            expandedChanged,
            "Expanded-state listener must not be null"
        );

        setValue(initialValue);
    }

    public void setValue(T value) {
        Option<T> option = findOption(value);
        this.value = option.value();
        updateMessage(option);
    }

    public void setLabelPrefix(String labelPrefix) {
        this.labelPrefix = Objects.requireNonNull(
            labelPrefix,
            "Dropdown label prefix must not be null"
        );
        updateMessage(findOption(value));
    }

    public T value() {
        return value;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void collapse() {
        expanded = false;
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) {
            return;
        }

        this.expanded = expanded;
        expandedChanged.run();
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
            containsBase(mouseX, mouseY),
            true,
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

        int optionY = getY() + height + OPTION_GAP;

        for (Option<T> option : options) {
            boolean selected = Objects.equals(
                option.value(),
                value
            );

            renderEntry(
                graphics,
                getX(),
                optionY,
                width,
                height,
                Component.literal(option.label()),
                true,
                contains(
                    mouseX,
                    mouseY,
                    getX(),
                    optionY,
                    width,
                    height
                ),
                selected,
                false
            );

            optionY += height;
        }
    }

    @Override
    public boolean mouseClicked(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (
            !visible
                || event.button() != 0
        ) {
            return false;
        }

        if (
            active
                && containsBase(
                    event.x(),
                    event.y()
                )
        ) {
            setExpanded(!expanded);
            return true;
        }

        if (!expanded) {
            return false;
        }

        int optionY = getY() + height + OPTION_GAP;

        for (Option<T> option : options) {
            if (
                contains(
                    event.x(),
                    event.y(),
                    getX(),
                    optionY,
                    width,
                    height
                )
            ) {
                value = option.value();
                updateMessage(option);
                expanded = false;
                selectionAction.accept(value);
                expandedChanged.run();
                return true;
            }

            optionY += height;
        }

        expanded = false;
        expandedChanged.run();
        return true;
    }

    @Override
    protected void updateWidgetNarration(
        NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }

    private void renderEntry(
        GuiGraphics graphics,
        int x,
        int y,
        int width,
        int height,
        Component text,
        boolean enabled,
        boolean hovered,
        boolean accented,
        boolean showArrow
    ) {
        int background = enabled
            ? hovered
                ? 0xFF2B2635
                : 0xFF17151D
            : 0xFF18161D;

        int outline = enabled
            ? accented
                ? 0xFF9B79D1
                : hovered
                    ? 0xFF746480
                    : 0xFF484052
            : 0xFF332F39;

        int textColor = enabled
            ? 0xFFE8E2ED
            : 0xFF66606B;

        graphics.fill(
            x,
            y,
            x + width,
            y + height,
            background
        );
        graphics.renderOutline(
            x,
            y,
            width,
            height,
            outline
        );

        if (accented) {
            graphics.fill(
                x,
                y,
                x + 3,
                y + height,
                enabled
                    ? 0xFFB38AE8
                    : 0xFF332F39
            );
        }

        int textY =
            y + (height - font.lineHeight) / 2 + 1;

        graphics.drawString(
            font,
            text,
            x + 9,
            textY,
            textColor,
            false
        );

        if (showArrow) {
            Component arrow = Component.literal(
                expanded ? "▴" : "▾"
            );

            graphics.drawString(
                font,
                arrow,
                x + width - 9 - font.width(arrow),
                textY,
                textColor,
                false
            );
        }
    }


    private void updateMessage(Option<T> option) {
        setMessage(
            Component.literal(
                labelPrefix
                    + option.label()
            )
        );
    }

    private Option<T> findOption(T value) {
        for (Option<T> option : options) {
            if (Objects.equals(option.value(), value)) {
                return option;
            }
        }

        throw new IllegalArgumentException(
            "Unknown dropdown value: " + value
        );
    }

    private boolean containsBase(
        double mouseX,
        double mouseY
    ) {
        return contains(
            mouseX,
            mouseY,
            getX(),
            getY(),
            width,
            height
        );
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

    public record Option<T>(
        T value,
        String label
    ) {
        public Option {
            Objects.requireNonNull(
                value,
                "Dropdown option value must not be null"
            );
            Objects.requireNonNull(
                label,
                "Dropdown option label must not be null"
            );
        }
    }
}