package hanamuramiyu.karakuri.ui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class KarakuriButton extends AbstractWidget {
    private final Font font;
    private final Runnable onPress;
    private final Style style;

    public KarakuriButton(
        Font font,
        int x,
        int y,
        int width,
        int height,
        Component message,
        Runnable onPress,
        Style style
    ) {
        super(x, y, width, height, message);

        this.font = Objects.requireNonNull(
            font,
            "Font must not be null"
        );

        this.onPress = Objects.requireNonNull(
            onPress,
            "Button action must not be null"
        );

        this.style = Objects.requireNonNull(
            style,
            "Button style must not be null"
        );
    }

    @Override
    protected void renderWidget(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        boolean highlighted = isHoveredOrFocused();

        int backgroundColor = active
            ? style.backgroundColor(highlighted)
            : 0xFF1A1820;

        int outlineColor = active
            ? style.outlineColor(highlighted)
            : 0xFF36313D;

        int textColor = active
            ? style.textColor()
            : 0xFF68616F;

        graphics.fill(
            getX(),
            getY(),
            getX() + width,
            getY() + height,
            backgroundColor
        );

        graphics.renderOutline(
            getX(),
            getY(),
            width,
            height,
            outlineColor
        );

        if (style.accentColor() != 0) {
            graphics.fill(
                getX(),
                getY(),
                getX() + 3,
                getY() + height,
                active
                    ? style.accentColor()
                    : 0xFF36313D
            );
        }

        int textX = getX()
            + (width - font.width(getMessage())) / 2;

        int textY = getY()
            + (height - font.lineHeight) / 2
            + 1;

        graphics.drawString(
            font,
            getMessage(),
            textX,
            textY,
            textColor,
            false
        );
    }

    @Override
    public void onClick(
        MouseButtonEvent event,
        boolean doubled
    ) {
        if (active) {
            onPress.run();
        }
    }

    @Override
    protected void updateWidgetNarration(
        NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }

    public enum Style {
        PRIMARY(
            0xFF302640,
            0xFF433255,
            0xFF8F6FC0,
            0xFFB38AE8,
            0xFFF7F3FC
        ),
        SECONDARY(
            0xFF211E29,
            0xFF2B2635,
            0xFF484052,
            0xFF746480,
            0xFFE4DEE9
        ),
        SUCCESS(
            0xFF1D3029,
            0xFF254235,
            0xFF3B785B,
            0xFF61D394,
            0xFFF1FFF7
        ),
        DANGER(
            0xFF342026,
            0xFF48272F,
            0xFF75404C,
            0xFFE66777,
            0xFFFFF1F3
        ),
        GHOST(
            0x00181720,
            0xFF24212B,
            0x003B3544,
            0xFF5C5269,
            0xFFD3CBDD
        );

        private final int backgroundColor;
        private final int hoveredBackgroundColor;
        private final int outlineColor;
        private final int hoveredOutlineColor;
        private final int textColor;

        Style(
            int backgroundColor,
            int hoveredBackgroundColor,
            int outlineColor,
            int hoveredOutlineColor,
            int textColor
        ) {
            this.backgroundColor = backgroundColor;
            this.hoveredBackgroundColor = hoveredBackgroundColor;
            this.outlineColor = outlineColor;
            this.hoveredOutlineColor = hoveredOutlineColor;
            this.textColor = textColor;
        }

        private int backgroundColor(boolean highlighted) {
            return highlighted
                ? hoveredBackgroundColor
                : backgroundColor;
        }

        private int outlineColor(boolean highlighted) {
            return highlighted
                ? hoveredOutlineColor
                : outlineColor;
        }

        private int textColor() {
            return textColor;
        }

        private int accentColor() {
            return switch (this) {
                case PRIMARY -> 0xFFB38AE8;
                case SUCCESS -> 0xFF61D394;
                case DANGER -> 0xFFE66777;
                case SECONDARY, GHOST -> 0;
            };
        }
    }
}