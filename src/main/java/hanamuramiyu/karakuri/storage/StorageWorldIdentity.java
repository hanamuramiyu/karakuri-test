package hanamuramiyu.karakuri.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;

import java.util.Locale;

public final class StorageWorldIdentity {
    private StorageWorldIdentity() {
    }

    public static String worldId(
        Minecraft client
    ) {
        if (client == null) {
            return "unavailable";
        }

        IntegratedServer server =
            client.getSingleplayerServer();

        if (server != null) {
            return "singleplayer:"
                + normalize(
                    server.getWorldData().getLevelName()
                );
        }

        ServerData serverData =
            client.getCurrentServer();

        if (
            serverData != null
                && serverData.ip != null
                && !serverData.ip.isBlank()
        ) {
            return "server:"
                + normalize(serverData.ip);
        }

        return "unavailable";
    }

    public static String dimensionId(
        Minecraft client
    ) {
        if (client == null || client.level == null) {
            return "unavailable";
        }

        return client.level
            .dimension()
            .identifier()
            .toString();
    }

    private static String normalize(
        String value
    ) {
        return value
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}