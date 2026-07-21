package hanamuramiyu.karakuri.task;

import hanamuramiyu.karakuri.scenario.model.HotbarStep;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public final class HotbarTask implements ClientTask {
    private final HotbarStep step;

    private boolean finished;

    public HotbarTask(
        HotbarStep step
    ) {
        if (step == null) {
            throw new IllegalArgumentException(
                "Hotbar step must not be null"
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
        ) {
            finished = true;
            return;
        }

        client.player
            .getInventory()
            .setSelectedSlot(step.slot());

        if (client.getConnection() != null) {
            client.getConnection().send(
                new ServerboundSetCarriedItemPacket(
                    step.slot()
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
}
