package hanamuramiyu.karakuri.task;

import net.minecraft.client.Minecraft;

public interface ClientTask {
    void start(Minecraft client);

    void tick(Minecraft client);

    void pause(Minecraft client);

    void resume(Minecraft client);

    void stop(Minecraft client);

    boolean isFinished();

    default void beginRender(
        Minecraft client,
        float tickProgress
    ) {
    }

    default void endRender(
        Minecraft client
    ) {
    }
}