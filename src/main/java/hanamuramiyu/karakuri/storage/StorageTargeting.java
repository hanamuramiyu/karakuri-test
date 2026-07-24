package hanamuramiyu.karakuri.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public final class StorageTargeting {
    private static final List<Direction> HORIZONTAL_DIRECTIONS =
        List.of(
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
        );

    private StorageTargeting() {
    }

    public static TargetedStorage targetedStorage(
        Minecraft client
    ) {
        if (
            client == null
                || client.level == null
                || !(client.hitResult
                    instanceof BlockHitResult hitResult)
        ) {
            return null;
        }

        BlockPos position = hitResult.getBlockPos();
        BlockState state =
            client.level.getBlockState(position);

        if (!isSupported(state.getBlock())) {
            return null;
        }

        List<BlockPos> positions =
            storagePositions(client, position, state);

        return new TargetedStorage(
            position,
            positions,
            defaultName(state, positions.size()),
            blockId(state.getBlock())
        );
    }

    public static boolean isSupported(
        Block block
    ) {
        return block instanceof ChestBlock
            || block instanceof BarrelBlock
            || block instanceof ShulkerBoxBlock;
    }

    public static List<BlockPos> storagePositions(
        Minecraft client,
        StorageMarker marker
    ) {
        if (
            client == null
                || client.level == null
                || !StorageWorldIdentity
                    .worldId(client)
                    .equals(marker.worldId())
                || !StorageWorldIdentity
                    .dimensionId(client)
                    .equals(marker.dimensionId())
        ) {
            return List.of(marker.position());
        }

        BlockPos position = marker.position();

        if (!isLoaded(client, position)) {
            return List.of(position);
        }

        return storagePositions(
            client,
            position,
            client.level.getBlockState(position)
        );
    }

    public static boolean isLoaded(
        Minecraft client,
        BlockPos position
    ) {
        return client != null
            && client.level != null
            && client.level.hasChunk(
                position.getX() >> 4,
                position.getZ() >> 4
            );
    }

    public static String blockId(
        Block block
    ) {
        return BuiltInRegistries.BLOCK
            .getKey(block)
            .toString();
    }

    private static List<BlockPos> storagePositions(
        Minecraft client,
        BlockPos position,
        BlockState state
    ) {
        if (
            !(state.getBlock() instanceof ChestBlock)
                || !state.hasProperty(ChestBlock.TYPE)
                || state.getValue(ChestBlock.TYPE)
                    == ChestType.SINGLE
        ) {
            return List.of(position);
        }

        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            BlockPos adjacentPosition =
                position.relative(direction);

            if (!isLoaded(client, adjacentPosition)) {
                continue;
            }

            BlockState adjacentState =
                client.level.getBlockState(
                    adjacentPosition
                );

            if (
                adjacentState.getBlock()
                    == state.getBlock()
                    && adjacentState.hasProperty(
                        ChestBlock.TYPE
                    )
                    && adjacentState.getValue(
                        ChestBlock.TYPE
                    ) != ChestType.SINGLE
                    && adjacentState.hasProperty(
                        ChestBlock.FACING
                    )
                    && state.hasProperty(
                        ChestBlock.FACING
                    )
                    && adjacentState.getValue(
                        ChestBlock.FACING
                    ) == state.getValue(
                        ChestBlock.FACING
                    )
            ) {
                List<BlockPos> positions =
                    new ArrayList<>(2);
                positions.add(position);
                positions.add(adjacentPosition);
                return List.copyOf(positions);
            }
        }

        return List.of(position);
    }

    private static String defaultName(
        BlockState state,
        int positionCount
    ) {
        if (state.getBlock() instanceof ChestBlock) {
            return positionCount > 1
                ? "Double Chest"
                : "Chest";
        }

        if (state.getBlock() instanceof BarrelBlock) {
            return "Barrel";
        }

        return "Shulker Box";
    }

    public record TargetedStorage(
        BlockPos position,
        List<BlockPos> positions,
        String defaultName,
        String blockId
    ) {
        public TargetedStorage {
            positions = List.copyOf(positions);
        }
    }
}