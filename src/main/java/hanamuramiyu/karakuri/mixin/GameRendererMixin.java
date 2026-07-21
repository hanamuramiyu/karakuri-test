package hanamuramiyu.karakuri.mixin;

import hanamuramiyu.karakuri.task.TaskManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(
        method = "updateCamera",
        at = @At("HEAD")
    )
    private void karakuri$beginCameraFrame(
        DeltaTracker deltaTracker,
        CallbackInfo callbackInfo
    ) {
        TaskManager.beginRender(
            Minecraft.getInstance(),
            deltaTracker
                .getGameTimeDeltaPartialTick(false)
        );
    }

    @Inject(
        method = "updateCamera",
        at = @At("RETURN")
    )
    private void karakuri$endCameraFrame(
        DeltaTracker deltaTracker,
        CallbackInfo callbackInfo
    ) {
        TaskManager.endRender(
            Minecraft.getInstance()
        );
    }
}