package hanamuramiyu.karakuri.task.action;

import hanamuramiyu.karakuri.scenario.model.RestockItemsStep;
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

public final class RestockItemsTask implements ClientTask {
    private static final int OPEN_TIMEOUT_TICKS = 40;
    private static final int CLICK_SETTLE_TICKS = 2;

    private final RestockItemsStep step;
    private final Set<Integer> blockedStorageSlots =
        new HashSet<>();

    private StorageGroup group;
    private StorageMarker marker;
    private AbstractContainerMenu openedMenu;
    private Phase phase = Phase.OPENING;
    private int phaseTicks;
    private int sourceMenuSlot = -1;
    private boolean inventoryBlocked;
    private boolean started;
    private boolean finished;

    public RestockItemsTask(
        RestockItemsStep step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Restock items step must not be null"
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
                "Restock Items skipped: close the open container"
            );
            return;
        }

        if (!step.hasAssignedGroup()) {
            finish(
                client,
                "Restock Items skipped: select a storage group"
            );
            return;
        }

        if (!step.hasAssignedItem()) {
            finish(
                client,
                "Restock Items skipped: select an item"
            );
            return;
        }

        group = StorageRegistry.findGroup(
            step.storageGroupId()
        );

        if (group == null) {
            finish(
                client,
                "Restock Items skipped: storage group is missing"
            );
            return;
        }

        if (!group.enabled()) {
            finish(
                client,
                "Restock Items skipped: storage group is disabled"
            );
            return;
        }

        if (!group.itemFilter().accepts(step.itemId())) {
            finish(
                client,
                "Restock Items skipped: selected item is not in the group filter"
            );
            return;
        }

        int currentAmount = inventoryAmount(client);

        if (currentAmount >= step.targetAmount()) {
            finish(
                client,
                "Restock Items: already has "
                    + currentAmount
                    + " of "
                    + itemLabel()
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
                "Restock Items skipped: look at a registered storage"
            );
            return;
        }

        marker = findTargetedMarker(client, target);

        if (marker == null) {
            finish(
                client,
                "Restock Items skipped: targeted storage is not registered"
            );
            return;
        }

        if (!marker.belongsTo(group.id())) {
            finish(
                client,
                "Restock Items skipped: targeted storage is not in \""
                    + group.name()
                    + "\""
            );
            return;
        }

        if (!marker.enabled()) {
            finish(
                client,
                "Restock Items skipped: targeted storage is disabled"
            );
            return;
        }

        if (!marker.blockId().equals(target.blockId())) {
            finish(
                client,
                "Restock Items skipped: targeted storage block has changed"
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
            case SELECTING_SOURCE -> tickSelectingSource(client);
            case WAITING_FOR_SOURCE -> tickWaitingForSource(client);
            case PLACING -> tickPlacing(client);
            case WAITING_FOR_PLACE -> tickWaitingForPlace(client);
            case WAITING_FOR_RETURN -> tickWaitingForReturn(client);
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
        returnCarriedStack(client);
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
                    "Restock Items stopped: opened screen is not a storage container"
                );
                return;
            }

            phase = Phase.SELECTING_SOURCE;
            phaseTicks = 0;
            return;
        }

        phaseTicks++;

        if (phaseTicks >= OPEN_TIMEOUT_TICKS) {
            finish(
                client,
                "Restock Items skipped: storage did not open"
            );
        }
    }

    private void tickSelectingSource(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                "Restock Items stopped: storage was closed"
            );
            return;
        }

        if (inventoryAmount(client) >= step.targetAmount()) {
            finishRestocking(client);
            return;
        }

        sourceMenuSlot = findNextStorageMenuSlot(client);

        if (sourceMenuSlot < 0) {
            finishRestocking(client);
            return;
        }

        client.gameMode.handleInventoryMouseClick(
            openedMenu.containerId,
            sourceMenuSlot,
            0,
            ClickType.PICKUP,
            client.player
        );

        phase = Phase.WAITING_FOR_SOURCE;
        phaseTicks = CLICK_SETTLE_TICKS;
    }

    private void tickWaitingForSource(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                "Restock Items stopped: storage was closed"
            );
            return;
        }

        phaseTicks--;

        if (phaseTicks > 0) {
            return;
        }

        ItemStack carried = openedMenu.getCarried();

        if (carried.isEmpty()) {
            blockedStorageSlots.add(sourceMenuSlot);
            sourceMenuSlot = -1;
            phase = Phase.SELECTING_SOURCE;
            return;
        }

        if (!itemId(carried).equals(step.itemId())) {
            blockedStorageSlots.add(sourceMenuSlot);
            beginReturn(client);
            return;
        }

        phase = Phase.PLACING;
    }

    private void tickPlacing(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                "Restock Items stopped: storage was closed"
            );
            return;
        }

        ItemStack carried = openedMenu.getCarried();
        int currentAmount = inventoryAmount(client);

        if (
            carried.isEmpty()
                || currentAmount >= step.targetAmount()
        ) {
            beginReturn(client);
            return;
        }

        int targetMenuSlot = findTargetPlayerMenuSlot(
            client,
            carried
        );

        if (targetMenuSlot < 0) {
            inventoryBlocked = true;
            beginReturn(client);
            return;
        }

        Slot targetSlot = openedMenu.slots.get(targetMenuSlot);
        int missing = step.targetAmount() - currentAmount;
        int capacity = targetCapacity(targetSlot, carried);
        int transferable = Math.min(
            missing,
            Math.min(carried.getCount(), capacity)
        );

        if (transferable <= 0) {
            inventoryBlocked = true;
            beginReturn(client);
            return;
        }

        int button = transferable == carried.getCount()
            ? 0
            : 1;

        client.gameMode.handleInventoryMouseClick(
            openedMenu.containerId,
            targetMenuSlot,
            button,
            ClickType.PICKUP,
            client.player
        );

        phase = Phase.WAITING_FOR_PLACE;
        phaseTicks = CLICK_SETTLE_TICKS;
    }

    private void tickWaitingForPlace(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                "Restock Items stopped: storage was closed"
            );
            return;
        }

        phaseTicks--;

        if (phaseTicks > 0) {
            return;
        }

        phase = Phase.PLACING;
    }

    private void beginReturn(
        Minecraft client
    ) {
        ItemStack carried = openedMenu.getCarried();

        if (carried.isEmpty()) {
            sourceMenuSlot = -1;
            phase = Phase.SELECTING_SOURCE;
            return;
        }

        if (
            sourceMenuSlot < 0
                || sourceMenuSlot >= openedMenu.slots.size()
        ) {
            finish(
                client,
                "Restock Items stopped: could not return the remaining stack"
            );
            return;
        }

        client.gameMode.handleInventoryMouseClick(
            openedMenu.containerId,
            sourceMenuSlot,
            0,
            ClickType.PICKUP,
            client.player
        );

        phase = Phase.WAITING_FOR_RETURN;
        phaseTicks = CLICK_SETTLE_TICKS;
    }

    private void tickWaitingForReturn(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                "Restock Items stopped: storage was closed"
            );
            return;
        }

        phaseTicks--;

        if (phaseTicks > 0) {
            return;
        }

        if (!openedMenu.getCarried().isEmpty()) {
            finish(
                client,
                "Restock Items stopped: could not return the remaining stack"
            );
            return;
        }

        sourceMenuSlot = -1;

        if (inventoryBlocked) {
            finishRestocking(client);
        } else {
            phase = Phase.SELECTING_SOURCE;
        }
    }

    private int findNextStorageMenuSlot(
        Minecraft client
    ) {
        for (
            int menuSlot = 0;
            menuSlot < openedMenu.slots.size();
            menuSlot++
        ) {
            if (blockedStorageSlots.contains(menuSlot)) {
                continue;
            }

            Slot slot = openedMenu.slots.get(menuSlot);

            if (
                slot.container
                    == client.player.getInventory()
            ) {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (
                !stack.isEmpty()
                    && itemId(stack).equals(step.itemId())
            ) {
                return menuSlot;
            }
        }

        return -1;
    }

    private int findTargetPlayerMenuSlot(
        Minecraft client,
        ItemStack carried
    ) {
        int emptySlot = -1;

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

            if (!isCountedInventorySlot(inventorySlot)) {
                continue;
            }

            ItemStack target = slot.getItem();

            if (target.isEmpty()) {
                if (emptySlot < 0 && slot.mayPlace(carried)) {
                    emptySlot = menuSlot;
                }
                continue;
            }

            if (
                ItemStack.isSameItemSameComponents(
                    target,
                    carried
                )
                    && target.getCount()
                        < target.getMaxStackSize()
                    && slot.mayPlace(carried)
            ) {
                return menuSlot;
            }
        }

        return emptySlot;
    }

    private int targetCapacity(
        Slot slot,
        ItemStack carried
    ) {
        ItemStack target = slot.getItem();

        if (target.isEmpty()) {
            return carried.getMaxStackSize();
        }

        if (
            !ItemStack.isSameItemSameComponents(
                target,
                carried
            )
        ) {
            return 0;
        }

        return Math.max(
            0,
            target.getMaxStackSize() - target.getCount()
        );
    }

    private int inventoryAmount(
        Minecraft client
    ) {
        int amount = 0;

        for (int inventorySlot = 0; inventorySlot <= 35; inventorySlot++) {
            if (!isCountedInventorySlot(inventorySlot)) {
                continue;
            }

            ItemStack stack = client.player
                .getInventory()
                .getItem(inventorySlot);

            if (
                !stack.isEmpty()
                    && itemId(stack).equals(step.itemId())
            ) {
                amount += stack.getCount();
            }
        }

        return amount;
    }

    private boolean isCountedInventorySlot(
        int inventorySlot
    ) {
        return inventorySlot >= 0
            && inventorySlot <= 35
            && (step.countHotbar() || inventorySlot >= 9);
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

    private void finishRestocking(
        Minecraft client
    ) {
        int currentAmount = inventoryAmount(client);

        if (currentAmount >= step.targetAmount()) {
            finish(
                client,
                "Restocked "
                    + itemLabel()
                    + " to "
                    + currentAmount
                    + " from \""
                    + marker.name()
                    + "\""
            );
            return;
        }

        int missing = step.targetAmount() - currentAmount;

        if (inventoryBlocked) {
            finish(
                client,
                "Restock Items stopped at "
                    + currentAmount
                    + " of "
                    + step.targetAmount()
                    + ": inventory has no space"
            );
            return;
        }

        finish(
            client,
            "Restock Items stopped at "
                + currentAmount
                + " of "
                + step.targetAmount()
                + ": storage is short by "
                + missing
        );
    }

    private void finish(
        Minecraft client,
        String message
    ) {
        returnCarriedStack(client);
        closeOpenedContainer(client);
        notify(client, message);
        finished = true;
    }

    private void finishWithoutMessage(
        Minecraft client
    ) {
        returnCarriedStack(client);
        closeOpenedContainer(client);
        finished = true;
    }

    private void returnCarriedStack(
        Minecraft client
    ) {
        if (
            client == null
                || client.player == null
                || client.gameMode == null
                || openedMenu == null
                || client.player.containerMenu != openedMenu
                || openedMenu.getCarried().isEmpty()
                || sourceMenuSlot < 0
                || sourceMenuSlot >= openedMenu.slots.size()
        ) {
            return;
        }

        client.gameMode.handleInventoryMouseClick(
            openedMenu.containerId,
            sourceMenuSlot,
            0,
            ClickType.PICKUP,
            client.player
        );
    }

    private void closeOpenedContainer(
        Minecraft client
    ) {
        if (
            client != null
                && client.player != null
                && openedMenu != null
                && client.player.containerMenu == openedMenu
        ) {
            client.player.closeContainer();
        }

        openedMenu = null;
    }

    private String itemLabel() {
        return step.itemId();
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
        SELECTING_SOURCE,
        WAITING_FOR_SOURCE,
        PLACING,
        WAITING_FOR_PLACE,
        WAITING_FOR_RETURN
    }
}