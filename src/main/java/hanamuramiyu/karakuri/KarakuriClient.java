package hanamuramiyu.karakuri;

import com.mojang.blaze3d.platform.InputConstants;
import hanamuramiyu.karakuri.quicklaunch.QuickLaunchController;
import hanamuramiyu.karakuri.quicklaunch.QuickLaunchRegistry;
import hanamuramiyu.karakuri.scenario.ScenarioLibrary;
import hanamuramiyu.karakuri.storage.StoragePreviewController;
import hanamuramiyu.karakuri.storage.StoragePreviewHud;
import hanamuramiyu.karakuri.storage.StorageRegistry;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.ui.KarakuriScreen;
import hanamuramiyu.karakuri.ui.StorageManagerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KarakuriClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(
                Karakuri.MOD_ID,
                "general"
            )
        );

    private static final KeyMapping OPEN_MENU_KEY =
        register(
            "key.karakuri.open_menu",
            GLFW.GLFW_KEY_K
        );

    private static final KeyMapping[] QUICK_SLOT_KEYS =
        new KeyMapping[
            QuickLaunchRegistry.SLOT_COUNT
        ];

    private static final KeyMapping PAUSE_RESUME_LAST_KEY =
        registerUnbound(
            "key.karakuri.pause_resume_last"
        );

    private static final KeyMapping STOP_LAST_KEY =
        registerUnbound(
            "key.karakuri.stop_last"
        );

    private static final KeyMapping OPEN_RUNNING_SESSIONS_KEY =
        registerUnbound(
            "key.karakuri.open_running_sessions"
        );

    private static final KeyMapping EMERGENCY_STOP_ALL_KEY =
        registerUnbound(
            "key.karakuri.emergency_stop_all"
        );

    static {
        for (
            int index = 0;
            index < QUICK_SLOT_KEYS.length;
            index++
        ) {
            QUICK_SLOT_KEYS[index] =
                registerUnbound(
                    "key.karakuri.quick_slot_"
                        + (index + 1)
                );
        }
    }

    @Override
    public void onInitializeClient() {
        ScenarioLibrary.initialize();
        QuickLaunchRegistry.initialize();
        StorageRegistry.initialize();

        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(
                Karakuri.MOD_ID,
                "storage_preview"
            ),
            StoragePreviewHud::render
        );

        ClientTickEvents.END_CLIENT_TICK.register(
            client -> {
                TaskManager.tick(client);
                StoragePreviewController.tick(client);

                while (OPEN_MENU_KEY.consumeClick()) {
                    if (client.player == null) {
                        continue;
                    }

                    if (StoragePreviewController.active()) {
                        StoragePreviewController.stop();
                        client.setScreen(
                            new StorageManagerScreen(
                                new KarakuriScreen(null)
                            )
                        );
                        continue;
                    }

                    if (!(client.screen instanceof KarakuriScreen)) {
                        client.setScreen(
                            new KarakuriScreen(
                                client.screen
                            )
                        );
                    }
                }

                boolean gameplayInput =
                    client.player != null
                        && client.level != null
                        && client.screen == null;

                for (
                    int index = 0;
                    index < QUICK_SLOT_KEYS.length;
                    index++
                ) {
                    while (
                        QUICK_SLOT_KEYS[index]
                            .consumeClick()
                    ) {
                        if (gameplayInput) {
                            QuickLaunchController
                                .launchSlot(
                                    index + 1,
                                    client
                                );
                        }
                    }
                }

                while (
                    PAUSE_RESUME_LAST_KEY
                        .consumeClick()
                ) {
                    if (gameplayInput) {
                        QuickLaunchController
                            .pauseResumeLast(client);
                    }
                }

                while (STOP_LAST_KEY.consumeClick()) {
                    if (gameplayInput) {
                        QuickLaunchController
                            .stopLast(client);
                    }
                }

                while (
                    OPEN_RUNNING_SESSIONS_KEY
                        .consumeClick()
                ) {
                    if (gameplayInput) {
                        QuickLaunchController
                            .openRunningSessions(
                                null,
                                client
                            );
                    }
                }

                while (
                    EMERGENCY_STOP_ALL_KEY
                        .consumeClick()
                ) {
                    if (gameplayInput) {
                        QuickLaunchController
                            .emergencyStopAll(client);
                    }
                }
            }
        );
    }

    public static KeyMapping openMenuKey() {
        return OPEN_MENU_KEY;
    }

    public static KeyMapping quickSlotKey(
        int slotNumber
    ) {
        if (
            slotNumber < 1
                || slotNumber
                    > QUICK_SLOT_KEYS.length
        ) {
            throw new IllegalArgumentException(
                "Quick slot number is out of bounds: "
                    + slotNumber
            );
        }

        return QUICK_SLOT_KEYS[slotNumber - 1];
    }

    private static KeyMapping registerUnbound(
        String translationKey
    ) {
        return register(
            translationKey,
            GLFW.GLFW_KEY_UNKNOWN
        );
    }

    private static KeyMapping register(
        String translationKey,
        int keyCode
    ) {
        return KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                translationKey,
                InputConstants.Type.KEYSYM,
                keyCode,
                CATEGORY
            )
        );
    }
}