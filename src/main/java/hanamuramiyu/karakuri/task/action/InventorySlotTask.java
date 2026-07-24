package hanamuramiyu.karakuri.task.action;

import hanamuramiyu.karakuri.scenario.model.InventorySlotStep;
import hanamuramiyu.karakuri.task.ClientTask;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;

public final class InventorySlotTask implements ClientTask {
    private final InventorySlotStep step;

    private boolean finished;

    public InventorySlotTask(
        InventorySlotStep step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Inventory slot step must not be null"
            );
        }

        this.step = step;
    }

    @Override
    public void start(
        Minecraft client
    ) {
        if (finished) {
            return;
        }

        if (
            client.player == null
                || client.level == null
                || client.gameMode == null
        ) {
            finished = true;
            return;
        }

        if (
            client.player.containerMenu
                != client.player.inventoryMenu
        ) {
            notify(
                client,
                "Inventory Slot skipped: close the open container"
            );
            finished = true;
            return;
        }

        if (
            client.player
                .getInventory()
                .getItem(step.inventorySlot())
                .isEmpty()
        ) {
            notify(
                client,
                "Inventory Slot skipped: selected slot is empty"
            );
            finished = true;
            return;
        }

        if (
            step.inventorySlot()
                != step.hotbarSlot()
        ) {
            client.gameMode.handleInventoryMouseClick(
                client.player.inventoryMenu.containerId,
                menuSlot(step.inventorySlot()),
                step.hotbarSlot(),
                ClickType.SWAP,
                client.player
            );
        }

        client.player
            .getInventory()
            .setSelectedSlot(step.hotbarSlot());

        if (client.getConnection() != null) {
            client.getConnection().send(
                new ServerboundSetCarriedItemPacket(
                    step.hotbarSlot()
                )
            );
        }

        finished = true;
    }

    @Override
    public void tick(
        Minecraft client
    ) {
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
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private static int menuSlot(
        int inventorySlot
    ) {
        return inventorySlot <= InventorySlotStep.MAX_HOTBAR_SLOT
            ? InventoryMenu.USE_ROW_SLOT_START + inventorySlot
            : InventoryMenu.INV_SLOT_START
                + inventorySlot
                - InventorySlotStep.DEFAULT_INVENTORY_SLOT;
    }

    private static void notify(
        Minecraft client,
        String message
    ) {
        if (client.player != null) {
            client.player.displayClientMessage(
                Component.literal(message),
                true
            );
        }
    }
}