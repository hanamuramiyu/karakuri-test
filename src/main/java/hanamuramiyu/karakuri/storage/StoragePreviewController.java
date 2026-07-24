package hanamuramiyu.karakuri.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class StoragePreviewController {
    private static List<StoragePreviewTarget> targets = List.of();

    private StoragePreviewController() {
    }

    public static void start(
        List<StoragePreviewTarget> updatedTargets
    ) {
        if (updatedTargets == null || updatedTargets.isEmpty()) {
            throw new IllegalArgumentException(
                "Storage preview targets must not be empty"
            );
        }

        List<StoragePreviewTarget> normalized =
            new ArrayList<>();

        for (StoragePreviewTarget target : updatedTargets) {
            if (!target.marker().enabled()) {
                continue;
            }

            List<StorageGroup> enabledGroups =
                target.groups()
                    .stream()
                    .filter(StorageGroup::enabled)
                    .toList();

            if (!enabledGroups.isEmpty()) {
                normalized.add(
                    new StoragePreviewTarget(
                        enabledGroups,
                        target.marker()
                    )
                );
            }
        }

        targets = normalized.stream()
            .distinct()
            .sorted(
                Comparator.comparingInt(
                    (StoragePreviewTarget target) ->
                        target.marker().priority()
                ).thenComparing(
                    target -> target.marker().name(),
                    String.CASE_INSENSITIVE_ORDER
                )
            )
            .toList();

        if (targets.isEmpty()) {
            throw new IllegalArgumentException(
                "Storage preview targets must contain enabled storage"
            );
        }
    }

    public static void stop() {
        targets = List.of();
    }

    public static boolean active() {
        return !targets.isEmpty();
    }

    public static List<StoragePreviewTarget> targets() {
        return targets;
    }

    public static void tick(
        Minecraft client
    ) {
        if (!active()) {
            return;
        }

        if (
            client == null
                || client.level == null
                || client.player == null
        ) {
            stop();
            return;
        }

        String worldId =
            StorageWorldIdentity.worldId(client);
        String dimensionId =
            StorageWorldIdentity.dimensionId(client);

        try (
            Gizmos.TemporaryCollection ignored =
                client.collectPerTickGizmos()
        ) {
            for (StoragePreviewTarget target : targets) {
                StorageMarker marker = target.marker();

                if (
                    !marker.worldId().equals(worldId)
                        || !marker.dimensionId()
                            .equals(dimensionId)
                        || !StorageTargeting.isLoaded(
                            client,
                            marker.position()
                        )
                ) {
                    continue;
                }

                StorageColor color =
                    target.primaryGroup().color();
                GizmoStyle style =
                    GizmoStyle.strokeAndFill(
                        color.color(),
                        2.0f,
                        color.fillColor()
                    );

                for (
                    BlockPos position :
                    StorageTargeting.storagePositions(
                        client,
                        marker
                    )
                ) {
                    Gizmos.cuboid(
                        position,
                        0.01f,
                        style
                    ).setAlwaysOnTop();
                }

                Gizmos.billboardTextOverBlock(
                    marker.name()
                        + " · "
                        + target.groupLabel(),
                    marker.position(),
                    18,
                    color.color(),
                    0.8f
                ).setAlwaysOnTop();
            }
        }
    }

    public static PreviewStatus status(
        Minecraft client,
        StorageMarker marker
    ) {
        if (
            client == null
                || client.level == null
                || client.player == null
        ) {
            return PreviewStatus.UNAVAILABLE;
        }

        if (
            !marker.worldId().equals(
                StorageWorldIdentity.worldId(client)
            )
        ) {
            return PreviewStatus.OTHER_WORLD;
        }

        if (
            !marker.dimensionId().equals(
                StorageWorldIdentity.dimensionId(client)
            )
        ) {
            return PreviewStatus.OTHER_DIMENSION;
        }

        if (!StorageTargeting.isLoaded(client, marker.position())) {
            return PreviewStatus.NOT_LOADED;
        }

        String currentBlockId =
            StorageTargeting.blockId(
                client.level
                    .getBlockState(marker.position())
                    .getBlock()
            );

        if (currentBlockId.equals(marker.blockId())) {
            return PreviewStatus.LOADED;
        }

        return client.level
            .getBlockState(marker.position())
            .isAir()
                ? PreviewStatus.MISSING
                : PreviewStatus.CHANGED;
    }

    public static double distance(
        Minecraft client,
        StorageMarker marker
    ) {
        if (
            client == null
                || client.player == null
        ) {
            return -1.0;
        }

        PreviewStatus status = status(client, marker);

        if (
            status == PreviewStatus.OTHER_WORLD
                || status == PreviewStatus.OTHER_DIMENSION
        ) {
            return -1.0;
        }

        BlockPos position = marker.position();
        return Math.sqrt(
            client.player.distanceToSqr(
                position.getX() + 0.5,
                position.getY() + 0.5,
                position.getZ() + 0.5
            )
        );
    }

    public enum PreviewStatus {
        LOADED("Loaded", 0xFF61D394),
        NOT_LOADED("Not loaded", 0xFFF1C36E),
        OTHER_DIMENSION("Other dimension", 0xFF67C7E8),
        OTHER_WORLD("Other world", 0xFFE66777),
        MISSING("Missing", 0xFFE66777),
        CHANGED("Changed", 0xFFE66777),
        UNAVAILABLE("Unavailable", 0xFF8F8499);

        private final String label;
        private final int color;

        PreviewStatus(
            String label,
            int color
        ) {
            this.label = label;
            this.color = color;
        }

        public String label() {
            return label;
        }

        public int color() {
            return color;
        }
    }
}