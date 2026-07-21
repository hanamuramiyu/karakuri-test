package hanamuramiyu.karakuri;

import com.mojang.blaze3d.platform.InputConstants;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.ui.KarakuriScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KarakuriClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(Karakuri.MOD_ID, "general")
    );

    private static final KeyMapping OPEN_MENU_KEY = KeyBindingHelper.registerKeyBinding(
        new KeyMapping(
            "key.karakuri.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY
        )
    );

    @Override
    public void onInitializeClient() {
        ScenarioLibrary.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TaskManager.tick(client);

            while (OPEN_MENU_KEY.consumeClick()) {
                if (client.player != null && !(client.screen instanceof KarakuriScreen)) {
                    client.setScreen(new KarakuriScreen(client.screen));
                }
            }
        });
    }
}