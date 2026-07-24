package hanamuramiyu.karakuri.ui;

import hanamuramiyu.karakuri.storage.StorageMarker;
import hanamuramiyu.karakuri.storage.StoragePreviewController;
import hanamuramiyu.karakuri.storage.StoragePreviewTarget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class StoragePreviewScreen extends Screen {
    private static final int PANEL_X = 10;
    private static final int PANEL_Y = 10;
    private static final int PANEL_MAX_WIDTH = 338;
    private static final int HEADER_HEIGHT = 48;
    private static final int ROW_HEIGHT = 48;
    private static final int PANEL_PADDING = 10;
    private static final int MAX_VISIBLE_ROWS = 8;

    public StoragePreviewScreen() {
        super(Component.literal("Storage Preview"));
    }

    @Override
    public void tick() {
        if (!StoragePreviewController.active()) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float delta
    ) {
        List<StoragePreviewTarget> targets =
            StoragePreviewController.targets();
        int panelWidth = panelWidth();
        int maximumRows = Math.max(
            1,
            Math.min(
                MAX_VISIBLE_ROWS,
                (
                    height
                        - PANEL_Y * 2
                        - HEADER_HEIGHT
                        - PANEL_PADDING
                ) / ROW_HEIGHT
            )
        );
        int visibleRows = Math.min(
            targets.size(),
            maximumRows
        );
        int panelHeight =
            HEADER_HEIGHT
                + visibleRows * ROW_HEIGHT
                + PANEL_PADDING;

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
            title,
            PANEL_X + PANEL_PADDING,
            PANEL_Y + 9,
            0xFFF6F2FA,
            false
        );
        graphics.drawString(
            font,
            Component.literal(
                "Esc to close  ·  "
                    + targets.size()
                    + (targets.size() == 1
                        ? " storage"
                        : " storages")
            ),
            PANEL_X + PANEL_PADDING,
            PANEL_Y + 27,
            0xFFB9AEBE,
            false
        );

        for (int index = 0; index < visibleRows; index++) {
            renderRow(
                graphics,
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
                PANEL_X + PANEL_PADDING,
                PANEL_Y + panelHeight - 12,
                0xFF8F8499,
                false
            );
        }
    }

    @Override
    public void onClose() {
        StoragePreviewController.stop();
        minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderRow(
        GuiGraphics graphics,
        StoragePreviewTarget target,
        int y,
        int panelWidth
    ) {
        StorageMarker marker = target.marker();
        StoragePreviewController.PreviewStatus status =
            StoragePreviewController.status(
                minecraft,
                marker
            );

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
            target.group().color().color()
        );

        graphics.drawString(
            font,
            Component.literal(
                truncate(
                    target.group().name()
                        + " · "
                        + marker.name(),
                    Math.max(60, panelWidth - 142)
                )
            ),
            PANEL_X + 14,
            y + 7,
            target.group().color().color(),
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
            y + 23,
            0xFFC8BECF,
            false
        );

        String distance = distanceLabel(marker);
        int rightX =
            PANEL_X
                + panelWidth
                - PANEL_PADDING
                - font.width(distance);
        graphics.drawString(
            font,
            Component.literal(distance),
            rightX,
            y + 7,
            0xFFECE6F1,
            false
        );

        int statusX =
            PANEL_X
                + panelWidth
                - PANEL_PADDING
                - font.width(status.label());
        graphics.drawString(
            font,
            Component.literal(status.label()),
            statusX,
            y + 23,
            status.color(),
            false
        );
    }

    private String distanceLabel(
        StorageMarker marker
    ) {
        double distance =
            StoragePreviewController.distance(
                minecraft,
                marker
            );

        if (distance >= 0.0) {
            return Math.round(distance) + " blocks";
        }

        StoragePreviewController.PreviewStatus status =
            StoragePreviewController.status(
                minecraft,
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

    private int panelWidth() {
        return Math.min(
            PANEL_MAX_WIDTH,
            width - PANEL_X * 2
        );
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