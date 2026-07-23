package hanamuramiyu.karakuri.task.input;

import hanamuramiyu.karakuri.task.TaskExecutionContext;
import net.minecraft.client.Minecraft;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class InputOwnershipManager {
    private static final Map<InputControl, Set<Long>> OWNERS =
        new EnumMap<>(InputControl.class);

    static {
        for (InputControl control : InputControl.values()) {
            OWNERS.put(control, new HashSet<>());
        }
    }

    private InputOwnershipManager() {
    }

    public static void setDown(
        InputControl control,
        Minecraft client,
        boolean down
    ) {
        if (control == null || client == null) {
            return;
        }

        long sessionId =
            TaskExecutionContext.currentSessionId();

        if (
            sessionId
                == TaskExecutionContext.NO_SESSION
        ) {
            control.key(client).setDown(down);
            return;
        }

        Set<Long> owners = OWNERS.get(control);

        if (down) {
            owners.add(sessionId);
        } else {
            owners.remove(sessionId);
        }

        control.key(client).setDown(
            !owners.isEmpty()
        );
    }

    public static void releaseAll(
        long sessionId,
        Minecraft client
    ) {
        if (client == null) {
            return;
        }

        for (
            Map.Entry<InputControl, Set<Long>> entry :
            OWNERS.entrySet()
        ) {
            if (entry.getValue().remove(sessionId)) {
                entry.getKey()
                    .key(client)
                    .setDown(
                        !entry.getValue().isEmpty()
                    );
            }
        }
    }

    public static void releaseEverything(
        Minecraft client
    ) {
        if (client == null) {
            return;
        }

        for (
            Map.Entry<InputControl, Set<Long>> entry :
            OWNERS.entrySet()
        ) {
            entry.getValue().clear();
            entry.getKey()
                .key(client)
                .setDown(false);
        }
    }
}