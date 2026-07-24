package hanamuramiyu.karakuri.task.action;

import hanamuramiyu.karakuri.scenario.model.StorageTransferAmountMode;
import hanamuramiyu.karakuri.scenario.model.StorageTransferDirection;
import hanamuramiyu.karakuri.scenario.model.StorageTransferItemMode;
import hanamuramiyu.karakuri.scenario.model.StorageTransferOptions;
import hanamuramiyu.karakuri.scenario.model.StorageTransferSpeed;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class StorageTransferTask implements ClientTask {
    private static final int OPEN_TIMEOUT_TICKS = 40;
    private static final int FAST_SETTLE_TICKS = 1;
    private static final int CONTROLLED_SETTLE_TICKS = 4;

    private final StorageTransferDirection direction;
    private final StorageTransferOptions options;
    private final Set<Integer> blockedSourceSlots =
        new HashSet<>();
    private final Set<String> blockedTargetSlots =
        new HashSet<>();
    private final Map<String, Integer> movedAmounts =
        new HashMap<>();

    private StorageGroup group;
    private StorageMarker marker;
    private Set<String> effectiveItemIds = Set.of();
    private AbstractContainerMenu openedMenu;
    private Phase phase = Phase.OPENING;
    private int phaseTicks;
    private int sourceMenuSlot = -1;
    private int targetMenuSlot = -1;
    private ItemStack pendingSourceStack = ItemStack.EMPTY;
    private int pendingCarriedCount;
    private boolean foundSource;
    private boolean targetBlocked;
    private boolean started;
    private boolean finished;

    public StorageTransferTask(
        StorageTransferDirection direction,
        StorageTransferOptions options
    ) {
        this.direction = java.util.Objects.requireNonNull(
            direction,
            "Storage transfer direction must not be null"
        );
        this.options = java.util.Objects.requireNonNull(
            options,
            "Storage transfer options must not be null"
        );

        if (!options.amountMode().validFor(direction)) {
            throw new IllegalArgumentException(
                "Storage transfer amount mode does not match its direction"
            );
        }
    }

    @Override
    public void start(
        Minecraft client
    ) {
        if (started || finished) {
            return;
        }

        started = true;

        if (!clientAvailable(client)) {
            finishWithoutMessage(client);
            return;
        }

        if (
            client.player.containerMenu
                != client.player.inventoryMenu
        ) {
            finish(
                client,
                actionLabel() + " skipped: close the open container"
            );
            return;
        }

        if (!options.hasAssignedGroup()) {
            finish(
                client,
                actionLabel() + " skipped: select a storage group"
            );
            return;
        }

        group = StorageRegistry.findGroup(
            options.storageGroupId()
        );

        if (group == null) {
            finish(
                client,
                actionLabel() + " skipped: storage group is missing"
            );
            return;
        }

        if (!group.enabled()) {
            finish(
                client,
                actionLabel() + " skipped: storage group is disabled"
            );
            return;
        }

        if (!resolveEffectiveItems(client)) {
            return;
        }

        if (
            direction == StorageTransferDirection.DEPOSIT
                && !hasTransferableInventorySource(client)
        ) {
            finish(
                client,
                "Deposit Items: no matching amount to deposit"
            );
            return;
        }

        if (
            direction == StorageTransferDirection.WITHDRAW
                && options.amountMode()
                    == StorageTransferAmountMode.TARGET
                && goalsComplete(client)
        ) {
            finish(
                client,
                "Restock Items: target amounts are already satisfied"
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
                actionLabel()
                    + " skipped: look at a registered storage"
            );
            return;
        }

        marker = findTargetedMarker(client, target);

        if (marker == null) {
            finish(
                client,
                actionLabel()
                    + " skipped: targeted storage is not registered"
            );
            return;
        }

        if (!marker.belongsTo(group.id())) {
            finish(
                client,
                actionLabel()
                    + " skipped: targeted storage is not in \""
                    + group.name()
                    + "\""
            );
            return;
        }

        if (!marker.enabled()) {
            finish(
                client,
                actionLabel()
                    + " skipped: targeted storage is disabled"
            );
            return;
        }

        if (!marker.blockId().equals(target.blockId())) {
            finish(
                client,
                actionLabel()
                    + " skipped: targeted storage block has changed"
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

        if (!clientAvailable(client)) {
            finishWithoutMessage(client);
            return;
        }

        switch (phase) {
            case OPENING -> tickOpening(client);
            case FINDING_SOURCE -> tickFindingSource(client);
            case WAITING_FOR_QUICK_MOVE ->
                tickWaitingForQuickMove(client);
            case WAITING_FOR_PICKUP ->
                tickWaitingForPickup(client);
            case PLACING -> tickPlacing(client);
            case WAITING_FOR_PLACE ->
                tickWaitingForPlace(client);
            case WAITING_FOR_RETURN ->
                tickWaitingForReturn(client);
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

    private boolean resolveEffectiveItems(
        Minecraft client
    ) {
        if (group.itemFilter().emptyFilter()) {
            finish(
                client,
                actionLabel()
                    + " skipped: storage group filter is empty"
            );
            return false;
        }

        LinkedHashSet<String> resolved =
            new LinkedHashSet<>();

        if (
            options.itemMode()
                == StorageTransferItemMode.GROUP_FILTER
        ) {
            resolved.addAll(group.itemFilter().itemIds());
        } else {
            if (!options.hasSelectedItems()) {
                finish(
                    client,
                    actionLabel() + " skipped: select at least one item"
                );
                return false;
            }

            for (String itemId : options.itemIds()) {
                if (!group.itemFilter().accepts(itemId)) {
                    finish(
                        client,
                        actionLabel()
                            + " skipped: selected item is not in the group filter"
                    );
                    return false;
                }

                resolved.add(itemId);
            }
        }

        effectiveItemIds = Set.copyOf(resolved);
        return true;
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
                    actionLabel()
                        + " stopped: opened screen is not a storage container"
                );
                return;
            }

            phase = Phase.FINDING_SOURCE;
            phaseTicks = 0;
            return;
        }

        phaseTicks++;

        if (phaseTicks >= OPEN_TIMEOUT_TICKS) {
            finish(
                client,
                actionLabel() + " skipped: storage did not open"
            );
        }
    }

    private void tickFindingSource(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                actionLabel() + " stopped: storage was closed"
            );
            return;
        }

        if (goalsComplete(client)) {
            finishTransfer(client);
            return;
        }

        sourceMenuSlot = findNextSourceMenuSlot(client);

        if (sourceMenuSlot < 0) {
            finishTransfer(client);
            return;
        }

        Slot sourceSlot = openedMenu.slots.get(sourceMenuSlot);
        ItemStack sourceStack = sourceSlot.getItem();
        int allowance = transferAllowance(
            itemId(sourceStack),
            client
        );

        if (allowance <= 0) {
            blockedSourceSlots.add(sourceMenuSlot);
            sourceMenuSlot = -1;
            return;
        }

        foundSource = true;
        pendingSourceStack = sourceStack.copy();

        if (
            options.speed() == StorageTransferSpeed.FAST
                && allowance >= sourceStack.getCount()
                && (
                    direction == StorageTransferDirection.DEPOSIT
                        || options.includeHotbar()
                )
        ) {
            client.gameMode.handleInventoryMouseClick(
                openedMenu.containerId,
                sourceMenuSlot,
                0,
                ClickType.QUICK_MOVE,
                client.player
            );

            phase = Phase.WAITING_FOR_QUICK_MOVE;
            phaseTicks = settleTicks();
            return;
        }

        client.gameMode.handleInventoryMouseClick(
            openedMenu.containerId,
            sourceMenuSlot,
            0,
            ClickType.PICKUP,
            client.player
        );

        phase = Phase.WAITING_FOR_PICKUP;
        phaseTicks = settleTicks();
    }

    private void tickWaitingForQuickMove(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                actionLabel() + " stopped: storage was closed"
            );
            return;
        }

        if (!waitComplete()) {
            return;
        }

        ItemStack current = openedMenu
            .slots
            .get(sourceMenuSlot)
            .getItem();
        int transferred = transferredFromSource(
            pendingSourceStack,
            current
        );

        if (transferred > 0) {
            recordMoved(
                itemId(pendingSourceStack),
                transferred
            );
            blockedSourceSlots.remove(sourceMenuSlot);
        } else {
            blockedSourceSlots.add(sourceMenuSlot);
            targetBlocked = true;
        }

        clearSourceState();
        phase = Phase.FINDING_SOURCE;
    }

    private void tickWaitingForPickup(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                actionLabel() + " stopped: storage was closed"
            );
            return;
        }

        if (!waitComplete()) {
            return;
        }

        ItemStack carried = openedMenu.getCarried();

        if (carried.isEmpty()) {
            blockedSourceSlots.add(sourceMenuSlot);
            clearSourceState();
            phase = Phase.FINDING_SOURCE;
            return;
        }

        if (!effectiveItemIds.contains(itemId(carried))) {
            blockedSourceSlots.add(sourceMenuSlot);
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
                actionLabel() + " stopped: storage was closed"
            );
            return;
        }

        ItemStack carried = openedMenu.getCarried();

        if (carried.isEmpty()) {
            clearSourceState();
            phase = Phase.FINDING_SOURCE;
            return;
        }

        int allowance = transferAllowance(
            itemId(carried),
            client
        );

        if (allowance <= 0) {
            beginReturn(client);
            return;
        }

        targetMenuSlot = findTargetMenuSlot(
            client,
            carried
        );

        if (targetMenuSlot < 0) {
            targetBlocked = true;
            beginReturn(client);
            return;
        }

        Slot targetSlot = openedMenu.slots.get(targetMenuSlot);
        int capacity = targetCapacity(targetSlot, carried);
        int transferable = Math.min(
            allowance,
            Math.min(carried.getCount(), capacity)
        );

        if (transferable <= 0) {
            blockedTargetSlots.add(
                targetKey(targetMenuSlot, itemId(carried))
            );
            targetMenuSlot = -1;
            return;
        }

        pendingCarriedCount = carried.getCount();
        int button = transferable == carried.getCount()
            && capacity >= carried.getCount()
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
        phaseTicks = settleTicks();
    }

    private void tickWaitingForPlace(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                actionLabel() + " stopped: storage was closed"
            );
            return;
        }

        if (!waitComplete()) {
            return;
        }

        ItemStack carried = openedMenu.getCarried();
        int transferred = Math.max(
            0,
            pendingCarriedCount - carried.getCount()
        );

        if (transferred > 0) {
            recordMoved(
                itemId(pendingSourceStack),
                transferred
            );
            blockedTargetSlots.remove(
                targetKey(
                    targetMenuSlot,
                    itemId(pendingSourceStack)
                )
            );
        } else {
            blockedTargetSlots.add(
                targetKey(
                    targetMenuSlot,
                    itemId(pendingSourceStack)
                )
            );
        }

        targetMenuSlot = -1;
        pendingCarriedCount = 0;
        phase = Phase.PLACING;
    }

    private void beginReturn(
        Minecraft client
    ) {
        ItemStack carried = openedMenu.getCarried();

        if (carried.isEmpty()) {
            clearSourceState();
            phase = Phase.FINDING_SOURCE;
            return;
        }

        if (
            sourceMenuSlot < 0
                || sourceMenuSlot >= openedMenu.slots.size()
        ) {
            finish(
                client,
                actionLabel()
                    + " stopped: could not return the remaining stack"
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
        phaseTicks = settleTicks();
    }

    private void tickWaitingForReturn(
        Minecraft client
    ) {
        if (!isOpenedMenuActive(client)) {
            finish(
                client,
                actionLabel() + " stopped: storage was closed"
            );
            return;
        }

        if (!waitComplete()) {
            return;
        }

        if (!openedMenu.getCarried().isEmpty()) {
            finish(
                client,
                actionLabel()
                    + " stopped: could not return the remaining stack"
            );
            return;
        }

        clearSourceState();

        if (targetBlocked) {
            finishTransfer(client);
        } else {
            phase = Phase.FINDING_SOURCE;
        }
    }

    private int findNextSourceMenuSlot(
        Minecraft client
    ) {
        for (
            int menuSlot = 0;
            menuSlot < openedMenu.slots.size();
            menuSlot++
        ) {
            if (blockedSourceSlots.contains(menuSlot)) {
                continue;
            }

            Slot slot = openedMenu.slots.get(menuSlot);

            if (!isSourceSlot(client, slot)) {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (
                stack.isEmpty()
                    || !effectiveItemIds.contains(itemId(stack))
                    || transferAllowance(
                        itemId(stack),
                        client
                    ) <= 0
            ) {
                continue;
            }

            return menuSlot;
        }

        return -1;
    }

    private int findTargetMenuSlot(
        Minecraft client,
        ItemStack carried
    ) {
        int emptySlot = -1;
        String carriedId = itemId(carried);

        for (
            int menuSlot = 0;
            menuSlot < openedMenu.slots.size();
            menuSlot++
        ) {
            if (
                blockedTargetSlots.contains(
                    targetKey(menuSlot, carriedId)
                )
            ) {
                continue;
            }

            Slot slot = openedMenu.slots.get(menuSlot);

            if (!isTargetSlot(client, slot)) {
                continue;
            }

            ItemStack target = slot.getItem();

            if (target.isEmpty()) {
                if (
                    emptySlot < 0
                        && slot.mayPlace(carried)
                ) {
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

    private boolean isSourceSlot(
        Minecraft client,
        Slot slot
    ) {
        boolean playerSlot = slot.container
            == client.player.getInventory();

        if (direction == StorageTransferDirection.DEPOSIT) {
            if (!playerSlot) {
                return false;
            }

            return isIncludedInventorySlot(
                slot.getContainerSlot()
            );
        }

        return !playerSlot;
    }

    private boolean isTargetSlot(
        Minecraft client,
        Slot slot
    ) {
        boolean playerSlot = slot.container
            == client.player.getInventory();

        if (direction == StorageTransferDirection.DEPOSIT) {
            return !playerSlot;
        }

        return playerSlot
            && isIncludedInventorySlot(
                slot.getContainerSlot()
            );
    }

    private int targetCapacity(
        Slot slot,
        ItemStack carried
    ) {
        if (!slot.mayPlace(carried)) {
            return 0;
        }

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

    private int transferAllowance(
        String itemId,
        Minecraft client
    ) {
        int moved = movedAmounts.getOrDefault(itemId, 0);

        return switch (direction) {
            case DEPOSIT -> switch (options.amountMode()) {
                case ALL -> Integer.MAX_VALUE;
                case UP_TO -> Math.max(
                    0,
                    options.amount() - moved
                );
                case KEEP -> Math.max(
                    0,
                    inventoryAmountIncludingCarried(
                        client,
                        itemId
                    )
                        - options.amount()
                );
                case TARGET -> 0;
            };
            case WITHDRAW -> switch (options.amountMode()) {
                case ALL -> Integer.MAX_VALUE;
                case UP_TO -> Math.max(
                    0,
                    options.amount() - moved
                );
                case TARGET -> Math.max(
                    0,
                    options.amount()
                        - inventoryAmount(client, itemId)
                );
                case KEEP -> 0;
            };
        };
    }

    private boolean goalsComplete(
        Minecraft client
    ) {
        if (options.amountMode() == StorageTransferAmountMode.ALL) {
            return false;
        }

        for (String itemId : effectiveItemIds) {
            if (transferAllowance(itemId, client) > 0) {
                return false;
            }
        }

        return true;
    }

    private boolean hasTransferableInventorySource(
        Minecraft client
    ) {
        for (int inventorySlot = 0; inventorySlot <= 35; inventorySlot++) {
            if (!isIncludedInventorySlot(inventorySlot)) {
                continue;
            }

            ItemStack stack = client.player
                .getInventory()
                .getItem(inventorySlot);

            if (
                !stack.isEmpty()
                    && effectiveItemIds.contains(itemId(stack))
                    && transferAllowance(
                        itemId(stack),
                        client
                    ) > 0
            ) {
                return true;
            }
        }

        return false;
    }

    private int inventoryAmountIncludingCarried(
        Minecraft client,
        String itemId
    ) {
        int amount = inventoryAmount(client, itemId);

        if (openedMenu == null) {
            return amount;
        }

        ItemStack carried = openedMenu.getCarried();

        if (
            !carried.isEmpty()
                && itemId(carried).equals(itemId)
        ) {
            amount += carried.getCount();
        }

        return amount;
    }

    private int inventoryAmount(
        Minecraft client,
        String itemId
    ) {
        int amount = 0;

        for (int inventorySlot = 0; inventorySlot <= 35; inventorySlot++) {
            if (!isIncludedInventorySlot(inventorySlot)) {
                continue;
            }

            ItemStack stack = client.player
                .getInventory()
                .getItem(inventorySlot);

            if (
                !stack.isEmpty()
                    && itemId(stack).equals(itemId)
            ) {
                amount += stack.getCount();
            }
        }

        return amount;
    }

    private boolean isIncludedInventorySlot(
        int inventorySlot
    ) {
        return inventorySlot >= 0
            && inventorySlot <= 35
            && (options.includeHotbar() || inventorySlot >= 9);
    }

    private int transferredFromSource(
        ItemStack before,
        ItemStack after
    ) {
        if (before.isEmpty()) {
            return 0;
        }

        if (after.isEmpty()) {
            return before.getCount();
        }

        if (
            !ItemStack.isSameItemSameComponents(before, after)
        ) {
            return before.getCount();
        }

        return Math.max(
            0,
            before.getCount() - after.getCount()
        );
    }

    private void recordMoved(
        String itemId,
        int amount
    ) {
        if (amount <= 0) {
            return;
        }

        movedAmounts.merge(itemId, amount, Integer::sum);
    }

    private int totalMoved() {
        return movedAmounts.values()
            .stream()
            .mapToInt(Integer::intValue)
            .sum();
    }

    private int movedTypes() {
        return (int) movedAmounts.values()
            .stream()
            .filter(amount -> amount > 0)
            .count();
    }

    private int totalMissing(
        Minecraft client
    ) {
        if (
            direction != StorageTransferDirection.WITHDRAW
                || options.amountMode()
                    != StorageTransferAmountMode.TARGET
        ) {
            return 0;
        }

        int missing = 0;

        for (String itemId : effectiveItemIds) {
            missing += Math.max(
                0,
                options.amount()
                    - inventoryAmount(client, itemId)
            );
        }

        return missing;
    }

    private void finishTransfer(
        Minecraft client
    ) {
        int moved = totalMoved();

        if (targetBlocked) {
            finish(
                client,
                direction == StorageTransferDirection.DEPOSIT
                    ? "Deposit Items stopped: storage has no space"
                    : "Restock Items stopped: inventory has no space"
            );
            return;
        }

        if (
            direction == StorageTransferDirection.WITHDRAW
                && options.amountMode()
                    == StorageTransferAmountMode.TARGET
                && !goalsComplete(client)
        ) {
            int missing = totalMissing(client);
            finish(
                client,
                "Restock Items stopped: storage is short by "
                    + missing
                    + (moved > 0
                        ? " after moving " + moved
                        : "")
            );
            return;
        }

        if (moved > 0) {
            String verb = direction
                == StorageTransferDirection.DEPOSIT
                    ? "Deposited "
                    : "Withdrew ";
            String relation = direction
                == StorageTransferDirection.DEPOSIT
                    ? " into \""
                    : " from \"";

            finish(
                client,
                verb
                    + moved
                    + (moved == 1 ? " item" : " items")
                    + " across "
                    + movedTypes()
                    + (movedTypes() == 1 ? " type" : " types")
                    + relation
                    + marker.name()
                    + "\""
            );
            return;
        }

        if (foundSource) {
            finish(
                client,
                direction == StorageTransferDirection.DEPOSIT
                    ? "Deposit Items: no transferable matching amount"
                    : "Restock Items: no transferable matching amount"
            );
            return;
        }

        finish(
            client,
            direction == StorageTransferDirection.DEPOSIT
                ? "Deposit Items: no matching items in the selected inventory area"
                : "Restock Items: storage has no matching items"
        );
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

    private void returnCarriedStack(
        Minecraft client
    ) {
        if (
            !clientAvailable(client)
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

    private void clearSourceState() {
        sourceMenuSlot = -1;
        targetMenuSlot = -1;
        pendingSourceStack = ItemStack.EMPTY;
        pendingCarriedCount = 0;
    }

    private boolean waitComplete() {
        phaseTicks--;
        return phaseTicks <= 0;
    }

    private int settleTicks() {
        return options.speed() == StorageTransferSpeed.FAST
            ? FAST_SETTLE_TICKS
            : CONTROLLED_SETTLE_TICKS;
    }

    private String actionLabel() {
        return direction.label();
    }

    private static String targetKey(
        int menuSlot,
        String itemId
    ) {
        return menuSlot + "\n" + itemId;
    }

    private static boolean clientAvailable(
        Minecraft client
    ) {
        return client != null
            && client.player != null
            && client.level != null
            && client.gameMode != null;
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
        FINDING_SOURCE,
        WAITING_FOR_QUICK_MOVE,
        WAITING_FOR_PICKUP,
        PLACING,
        WAITING_FOR_PLACE,
        WAITING_FOR_RETURN
    }
}