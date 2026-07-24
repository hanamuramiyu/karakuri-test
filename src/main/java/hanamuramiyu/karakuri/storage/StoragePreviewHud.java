package hanamuramiyu.karakuri.storage;

import hanamuramiyu.karakuri.KarakuriClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class StoragePreviewHud {
    private static final int PANEL_X = 10;
    private static final int PANEL_Y = 10;
    private static final int PANEL_MAX_WIDTH = 360;
    private static final int HEADER_HEIGHT = 44;
    private static final int ROW_HEIGHT = 42;
    private static final int PADDING = 10;
    private static final int MAX_VISIBLE_ROWS = 7;

    private StoragePreviewHud() {
    }

    public static void render(
        GuiGraphics graphics,
        DeltaTracker tickCounter
    ) {
        Minecraft client = Minecraft.getInstance();

        if (
            !StoragePreviewController.active()
                || client.player == null
                || client.level == null
                || client.screen != null
        ) {
            return;
        }

        Font font = client.font;
        List<StoragePreviewTarget> targets =
            StoragePreviewController.targets();
        int availableWidth =
            client.getWindow().getGuiScaledWidth()
                - PANEL_X * 2;
        int panelWidth = Math.min(
            PANEL_MAX_WIDTH,
            Math.max(190, availableWidth)
        );
        int availableHeight =
            client.getWindow().getGuiScaledHeight()
                - PANEL_Y * 2
                - HEADER_HEIGHT
                - PADDING;
        int capacity = Math.max(
            1,
            Math.min(
                MAX_VISIBLE_ROWS,
                availableHeight / ROW_HEIGHT
            )
        );
        int visibleRows = Math.min(
            targets.size(),
            capacity
        );
        int panelHeight =
            HEADER_HEIGHT
                + visibleRows * ROW_HEIGHT
                + PADDING;

        graphics.fill(
            PANEL_X,
            PANEL_Y,
            PANEL_X + panelWidth,
            PANEL_Y + panelHeight,
            0xD9181620
        );
        graphics.renderOutline(
            PANEL_X,
            PANEL_Y,
            panelWidth,
            panelHeight,
            0xFF6F5A91
        );
        graphics.fill(
            PANEL_X,
            PANEL_Y,
            PANEL_X + 4,
            PANEL_Y + panelHeight,
            0xFFB38AE8
        );

        graphics.drawString(
            font,
            Component.literal("Storage Preview"),
            PANEL_X + PADDING,
            PANEL_Y + 8,
            0xFFF6F2FA,
            false
        );

        Component returnHint = Component.empty()
            .append(
                KarakuriClient
                    .openMenuKey()
                    .getTranslatedKeyMessage()
            )
            .append(" to manage storages");
        graphics.drawString(
            font,
            truncate(
                font,
                returnHint,
                panelWidth - PADDING * 2
            ),
            PANEL_X + PADDING,
            PANEL_Y + 25,
            0xFFB9AEBE,
            false
        );

        for (int index = 0; index < visibleRows; index++) {
            renderRow(
                graphics,
                font,
                client,
                targets.get(index),
                PANEL_Y
                    + HEADER_HEIGHT
                    + index * ROW_HEIGHT,
                panelWidth
            );
        }

        if (targets.size() > visibleRows) {
            graphics.drawString(
                font,
                Component.literal(
                    "+ "
                        + (targets.size() - visibleRows)
                        + " more"
                ),
                PANEL_X + PADDING,
                PANEL_Y + panelHeight - 11,
                0xFF8F8499,
                false
            );
        }
    }

    private static void renderRow(
        GuiGraphics graphics,
        Font font,
        Minecraft client,
        StoragePreviewTarget target,
        int y,
        int panelWidth
    ) {
        StorageMarker marker = target.marker();
        StoragePreviewController.PreviewStatus status =
            StoragePreviewController.status(
                client,
                marker
            );
        int color = target.primaryGroup().color().color();

        graphics.fill(
            PANEL_X + 5,
            y + 2,
            PANEL_X + panelWidth - 5,
            y + ROW_HEIGHT - 2,
            0xB015121B
        );
        graphics.fill(
            PANEL_X + 5,
            y + 2,
            PANEL_X + 8,
            y + ROW_HEIGHT - 2,
            color
        );

        Component title = Component.literal(
            target.groupLabel()
                + " · "
                + marker.name()
        );
        String distance = distanceLabel(client, marker);
        int rightWidth = Math.max(
            font.width(distance),
            font.width(status.label())
        );

        graphics.drawString(
            font,
            truncate(
                font,
                title,
                Math.max(
                    50,
                    panelWidth - rightWidth - 40
                )
            ),
            PANEL_X + 14,
            y + 6,
            color,
            false
        );

        String coordinates =
            "X "
                + marker.x()
                + "  Y "
                + marker.y()
                + "  Z "
                + marker.z();
        graphics.drawString(
            font,
            Component.literal(coordinates),
            PANEL_X + 14,
            y + 22,
            0xFFC8BECF,
            false
        );

        graphics.drawString(
            font,
            Component.literal(distance),
            PANEL_X
                + panelWidth
                - PADDING
                - font.width(distance),
            y + 6,
            0xFFECE6F1,
            false
        );
        graphics.drawString(
            font,
            Component.literal(status.label()),
            PANEL_X
                + panelWidth
                - PADDING
                - font.width(status.label()),
            y + 22,
            status.color(),
            false
        );
    }

    private static String distanceLabel(
        Minecraft client,
        StorageMarker marker
    ) {
        double distance =
            StoragePreviewController.distance(
                client,
                marker
            );

        if (distance >= 0.0) {
            return Math.round(distance) + " blocks";
        }

        StoragePreviewController.PreviewStatus status =
            StoragePreviewController.status(
                client,
                marker
            );

        if (
            status
                == StoragePreviewController
                    .PreviewStatus.OTHER_WORLD
        ) {
            return "Different world";
        }

        int separator = marker.dimensionId().indexOf(':');
        return separator >= 0
            ? marker.dimensionId().substring(separator + 1)
            : marker.dimensionId();
    }

    private static Component truncate(
        Font font,
        Component value,
        int maximumWidth
    ) {
        if (font.width(value) <= maximumWidth) {
            return value;
        }

        String plain = value.getString();
        String suffix = "...";
        int end = plain.length();

        while (
            end > 0
                && font.width(
                    plain.substring(0, end) + suffix
                ) > maximumWidth
        ) {
            end--;
        }

        return Component.literal(
            plain.substring(0, end) + suffix
        );
    }
}