package hanamuramiyu.karakuri.task.action;

import hanamuramiyu.karakuri.scenario.model.DepositItemsStep;
import hanamuramiyu.karakuri.storage.StorageGroup;
import hanamuramiyu.karakuri.storage.StorageMarker;
import hanamuramiyu.karakuri.storage.StorageRegistry;
import hanamuramiyu.karakuri.storage.StorageTargeting;
import hanamuramiyu.karakuri.storage.StorageWorldIdentity;
import hanamuramiyu.karakuri.task.ClientTask;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashSet;
import java.util.Set;

public final class DepositItemsTask implements ClientTask {
    private static final int OPEN_TIMEOUT_TICKS = 40;
    private static final int CLICK_SETTLE_TICKS = 2;

    private final DepositItemsStep step;
    private final Set<Integer> blockedInventorySlots =
        new HashSet<>();

    private StorageGroup group;
    private StorageMarker marker;
    private AbstractContainerMenu openedMenu;
    private Phase phase = Phase.OPENING;
    private int phaseTicks;
    private int pendingInventorySlot = -1;
    private int pendingCount;
    private String pendingItemId;
    private int movedStacks;
    private boolean foundMatchingItem;
    private boolean started;
    private boolean finished;

    public DepositItemsTask(
        DepositItemsStep step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Deposit items step must not be null"
            );
        }

        this.step = step;
    }

    @Override
    public void start(
        Minecraft client
    ) {
        if (started || finished) {
            return;
        }

        started = true;

        if (
            client == null
                || client.player == null
                || client.level == null
                || client.gameMode == null
        ) {
            finishWithoutMessage(client);
            return;
        }

        if (
            client.player.containerMenu
                != client.player.inventoryMenu
        ) {
            finish(
                client,
                "Deposit Items skipped: close the open container"
            );
            return;
        }

        if (!step.hasAssignedGroup()) {
            finish(
                client,
                "Deposit Items skipped: select a storage group"
            );
            return;
        }

        group = StorageRegistry.findGroup(
            step.storageGroupId()
        );

        if (group == null) {
            finish(
                client,
                "Deposit Items skipped: storage group is missing"
            );
            return;
        }

        if (!group.enabled()) {
            finish(
                client,
                "Deposit Items skipped: storage group is disabled"
            );
            return;
        }

        if (group.itemFilter().emptyFilter()) {
            finish(
                client,
                "Deposit Items skipped: storage group filter is empty"
            );
            return;
        }

        StorageTargeting.TargetedStorage target =
            StorageTargeting.targetedStorage(client);

        if (
            target == null
                || !(client.hitResult
                    instanceof BlockHitResult hitResult)
        ) {
            finish(
                client,
                "Deposit Items skipped: look at a registered storage"
            );
            return;
        }

        marker = findTargetedMarker(
            client,
            target
        );

        if (marker == null) {
            finish(
                client,
                "Deposit Items skipped: targeted storage is not registered"
            );
            return;
        }

        if (!marker.belongsTo(group.id())) {
            finish(
                client,
                "Deposit Items skipped: targeted storage is not in \""
                    + group.name()
                    + "\""
            );
            return;
        }

        if (!marker.enabled()) {
            finish(
                client,
                "Deposit Items skipped: targeted storage is disabled"
            );
            return;
        }

        if (!marker.blockId().equals(target.blockId())) {
            finish(
                client,
                "Deposit Items skipped: targeted storage block has changed"
            );
            return;
        }

        client.gameMode.useItemOn(
            client.player,
            InteractionHand.MAIN_HAND,
            hitResult
        );
    }

    @Override
    public void tick(
        Minecraft client
    ) {
        if (!started || finished) {
            return;
        }

        if (
            client == null
                || client.player == null
                || client.level == null
                || client.gameMode == null
        ) {
            finishWithoutMessage(client);
            return;
        }

        switch (phase) {
            case OPENING -> tickOpening(client);
            case DEPOSITING -> tickDepositing(client);
            case WAITING_FOR_CLICK -> tickWaitingForClick(client);
        }
    }

    @Override
    public void pause(
        Minecraft client
    ) {
    }

    @Override
    public void resume(
        Minecraft client
    ) {
    }

    @Override
    public void stop(
        Minecraft client
    ) {
        closeOpenedContainer(client);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private void tickOpening(
        Minecraft client
    ) {
        if (
            client.player.containerMenu
                != client.player.inventoryMenu
        ) {
            openedMenu = client.player.containerMenu;

            if (!isSupportedContainerMenu(client)) {
                finish(
                    client,
                    "Deposit Items stopped: opened screen is not a storage container"
                );
                return;
            }

            phase = Phase.DEPOSITING;
            phaseTicks = 0;
            return;
        }

        phaseTicks++;

        if (phaseTicks >= OPEN_TIMEOUT_TICKS) {
            finish(
                client,
                "Deposit Items skipped: storage did not open"
            );
        }
    }

    private void tickDepositing(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                "Deposit Items stopped: storage was closed"
            );
            return;
        }

        int menuSlot = findNextMenuSlot(client);

        if (menuSlot < 0) {
            finishDepositing(client);
            return;
        }

        Slot slot = openedMenu.slots.get(menuSlot);
        ItemStack stack = slot.getItem();

        pendingInventorySlot = slot.getContainerSlot();
        pendingCount = stack.getCount();
        pendingItemId = itemId(stack);
        foundMatchingItem = true;

        client.gameMode.handleInventoryMouseClick(
            openedMenu.containerId,
            menuSlot,
            0,
            ClickType.QUICK_MOVE,
            client.player
        );

        phase = Phase.WAITING_FOR_CLICK;
        phaseTicks = CLICK_SETTLE_TICKS;
    }

    private void tickWaitingForClick(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                "Deposit Items stopped: storage was closed"
            );
            return;
        }

        phaseTicks--;

        if (phaseTicks > 0) {
            return;
        }

        ItemStack current = client.player
            .getInventory()
            .getItem(pendingInventorySlot);

        boolean moved = current.isEmpty()
            || !itemId(current).equals(pendingItemId)
            || current.getCount() < pendingCount;

        if (moved) {
            movedStacks++;
            blockedInventorySlots.remove(
                pendingInventorySlot
            );
        } else {
            blockedInventorySlots.add(
                pendingInventorySlot
            );
        }

        pendingInventorySlot = -1;
        pendingCount = 0;
        pendingItemId = null;
        phase = Phase.DEPOSITING;
    }

    private int findNextMenuSlot(
        Minecraft client
    ) {
        for (
            int menuSlot = 0;
            menuSlot < openedMenu.slots.size();
            menuSlot++
        ) {
            Slot slot = openedMenu.slots.get(menuSlot);

            if (
                slot.container
                    != client.player.getInventory()
            ) {
                continue;
            }

            int inventorySlot = slot.getContainerSlot();

            if (
                inventorySlot < 0
                    || inventorySlot > 35
                    || blockedInventorySlots.contains(
                        inventorySlot
                    )
                    || !step.includeHotbar()
                        && inventorySlot <= 8
            ) {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (
                !stack.isEmpty()
                    && group.itemFilter().accepts(
                        itemId(stack)
                    )
            ) {
                return menuSlot;
            }
        }

        return -1;
    }

    private boolean isSupportedContainerMenu(
        Minecraft client
    ) {
        if (openedMenu == null) {
            return false;
        }

        boolean hasPlayerInventory = false;
        boolean hasStorageSlots = false;

        for (Slot slot : openedMenu.slots) {
            if (
                slot.container
                    == client.player.getInventory()
            ) {
                int inventorySlot = slot.getContainerSlot();

                if (
                    inventorySlot >= 0
                        && inventorySlot <= 35
                ) {
                    hasPlayerInventory = true;
                }
            } else {
                hasStorageSlots = true;
            }
        }

        return hasPlayerInventory && hasStorageSlots;
    }

    private boolean isOpenedMenuActive(
        Minecraft client
    ) {
        return openedMenu != null
            && client.player != null
            && client.player.containerMenu == openedMenu;
    }

    private StorageMarker findTargetedMarker(
        Minecraft client,
        StorageTargeting.TargetedStorage target
    ) {
        String worldId = StorageWorldIdentity.worldId(client);
        String dimensionId = StorageWorldIdentity.dimensionId(client);

        for (BlockPos position : target.positions()) {
            StorageMarker targeted =
                StorageRegistry.findMarkerAt(
                    worldId,
                    dimensionId,
                    position
                );

            if (targeted != null) {
                return targeted;
            }
        }

        return null;
    }

    private void finishDepositing(
        Minecraft client
    ) {
        if (movedStacks > 0) {
            finish(
                client,
                "Deposited "
                    + movedStacks
                    + (movedStacks == 1
                        ? " matching stack"
                        : " matching stacks")
                    + " into \""
                    + marker.name()
                    + "\""
            );
            return;
        }

        if (foundMatchingItem) {
            finish(
                client,
                "Deposit Items stopped: storage has no space for matching items"
            );
            return;
        }

        finish(
            client,
            "Deposit Items: no matching items in "
                + (step.includeHotbar()
                    ? "inventory or hotbar"
                    : "main inventory")
        );
    }

    private void finish(
        Minecraft client,
        String message
    ) {
        closeOpenedContainer(client);
        notify(client, message);
        finished = true;
    }

    private void finishWithoutMessage(
        Minecraft client
    ) {
        closeOpenedContainer(client);
        finished = true;
    }

    private void closeOpenedContainer(
        Minecraft client
    ) {
        if (
            client != null
                && client.player != null
                && openedMenu != null
                && client.player.containerMenu
                    == openedMenu
        ) {
            client.player.closeContainer();
        }

        openedMenu = null;
    }

    private static String itemId(
        ItemStack stack
    ) {
        return BuiltInRegistries.ITEM
            .getKey(stack.getItem())
            .toString();
    }

    private static void notify(
        Minecraft client,
        String message
    ) {
        if (
            client != null
                && client.player != null
        ) {
            client.player.displayClientMessage(
                Component.literal(message),
                true
            );
        }
    }

    private enum Phase {
        OPENING,
        DEPOSITING,
        WAITING_FOR_CLICK
    }
}