package hanamuramiyu.karakuri.ui.widget;

import net.minecraft.client.gui.GuiGraphics;

public final class KarakuriCheckboxRenderer {
    private KarakuriCheckboxRenderer() {
    }

    public static void render(
        GuiGraphics graphics,
        int x,
        int y,
        int size,
        boolean selected,
        int selectedColor
    ) {
        graphics.fill(
            x,
            y,
            x + size,
            y + size,
            selected ? selectedColor : 0xFF100E16
        );
        graphics.renderOutline(
            x,
            y,
            size,
            size,
            selected ? 0xFFF3EAFB : 0xFF5A5063
        );

        if (!selected) {
            return;
        }

        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int color = 0xFFF8F4FB;

        graphics.fill(
            centerX - 4,
            centerY,
            centerX - 2,
            centerY + 2,
            color
        );
        graphics.fill(
            centerX - 2,
            centerY + 2,
            centerX,
            centerY + 4,
            color
        );
        graphics.fill(
            centerX,
            centerY,
            centerX + 2,
            centerY + 3,
            color
        );
        graphics.fill(
            centerX + 1,
            centerY - 2,
            centerX + 3,
            centerY + 1,
            color
        );
        graphics.fill(
            centerX + 3,
            centerY - 4,
            centerX + 5,
            centerY - 1,
            color
        );
    }
}