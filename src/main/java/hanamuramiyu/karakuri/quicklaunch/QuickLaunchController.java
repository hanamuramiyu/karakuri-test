package hanamuramiyu.karakuri.quicklaunch;

import hanamuramiyu.karakuri.scenario.model.Scenario;
import hanamuramiyu.karakuri.task.ScenarioConflictAnalyzer;
import hanamuramiyu.karakuri.task.ScenarioGroupStartResult;
import hanamuramiyu.karakuri.task.TaskChannel;
import hanamuramiyu.karakuri.task.TaskGroupControlResult;
import hanamuramiyu.karakuri.task.TaskManager;
import hanamuramiyu.karakuri.ui.QuickLaunchConflictScreen;
import hanamuramiyu.karakuri.ui.RunningScenariosScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class QuickLaunchController {
    private QuickLaunchController() {
    }

    public static void launchSlot(
        int slotNumber,
        Minecraft client
    ) {
        QuickLaunchSlot slot =
            QuickLaunchRegistry.slot(
                slotNumber
            );

        if (slot.empty()) {
            notify(
                client,
                "Quick Slot "
                    + slotNumber
                    + " is empty"
            );
            return;
        }

        int missingCount =
            QuickLaunchRegistry
                .missingScenarioIds(slot)
                .size();

        if (missingCount > 0) {
            notify(
                client,
                "Quick Slot "
                    + slotNumber
                    + " has "
                    + missingCount
                    + " missing scenario"
                    + (missingCount == 1
                        ? ""
                        : "s")
            );
            return;
        }

        List<Scenario> scenarios =
            QuickLaunchRegistry
                .resolveScenarios(slot);

        QuickLaunchValidation validation =
            QuickLaunchValidation.analyze(
                scenarios
            );

        if (!validation.valid()) {
            notify(
                client,
                "Quick Slot "
                    + slotNumber
                    + " has conflicting scenarios"
            );
            return;
        }

        ScenarioGroupStartResult result =
            TaskManager.startScenarioGroup(
                slot.label(),
                scenarios,
                1,
                client
            );

        handleStartResult(
            null,
            slotNumber,
            scenarios,
            result,
            client
        );
    }

    public static void handleStartResult(
        Screen parent,
        int slotNumber,
        List<Scenario> scenarios,
        ScenarioGroupStartResult result,
        Minecraft client
    ) {
        switch (result.status()) {
            case STARTED -> notify(
                client,
                startedMessage(
                    slotNumber,
                    scenarios.size()
                )
            );

            case CONFLICT -> client.setScreen(
                new QuickLaunchConflictScreen(
                    parent,
                    slotNumber,
                    scenarios,
                    result
                )
            );

            case INTERNAL_CONFLICT -> notify(
                client,
                "Quick Slot "
                    + slotNumber
                    + " has conflicting scenarios"
            );

            case UNAVAILABLE -> notify(
                client,
                "Quick Launch is unavailable right now"
            );
        }
    }

    public static List<Scenario> compatibleScenarios(
        List<Scenario> scenarios
    ) {
        EnumSet<TaskChannel> occupied =
            EnumSet.noneOf(TaskChannel.class);

        occupied.addAll(
            TaskManager.activeChannels()
        );

        List<Scenario> compatible =
            new ArrayList<>();

        for (Scenario scenario : scenarios) {
            if (
                TaskManager.isScenarioActive(
                    scenario.id()
                )
            ) {
                continue;
            }

            Set<TaskChannel> channels =
                ScenarioConflictAnalyzer.channels(
                    scenario
                );

            if (
                !ScenarioConflictAnalyzer
                    .conflicts(
                        occupied,
                        channels
                    )
                    .isEmpty()
            ) {
                continue;
            }

            compatible.add(scenario);
            occupied.addAll(channels);
        }

        return List.copyOf(compatible);
    }

    public static void pauseResumeLast(
        Minecraft client
    ) {
        TaskGroupControlResult result =
            TaskManager.togglePauseLastGroup(
                client
            );

        notify(
            client,
            switch (result) {
                case PAUSED ->
                    "Paused the last scenario group";
                case RESUMED ->
                    "Resumed the last scenario group";
                case NO_ACTIVE ->
                    "No active scenario group";
                case STOPPED ->
                    "Stopped the last scenario group";
            }
        );
    }

    public static void stopLast(
        Minecraft client
    ) {
        TaskGroupControlResult result =
            TaskManager.stopLastGroup(client);

        notify(
            client,
            result == TaskGroupControlResult.STOPPED
                ? "Stopped the last scenario group"
                : "No active scenario group"
        );
    }

    public static void openRunningSessions(
        Screen parent,
        Minecraft client
    ) {
        client.setScreen(
            new RunningScenariosScreen(
                parent,
                RunningScenariosScreen
                    .OpenMode.MANAGE
            )
        );
    }

    public static void emergencyStopAll(
        Minecraft client
    ) {
        int count = TaskManager.activeCount();
        TaskManager.stop(client);

        notify(
            client,
            count == 0
                ? "No active scenarios"
                : count == 1
                    ? "Emergency stop: 1 scenario"
                    : "Emergency stop: "
                        + count
                        + " scenarios"
        );
    }

    public static void notify(
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

    public static String startedMessage(
        int slotNumber,
        int scenarioCount
    ) {
        return scenarioCount == 1
            ? "Started Quick Slot "
                + slotNumber
            : "Started Quick Slot "
                + slotNumber
                + " · "
                + scenarioCount
                + " scenarios";
    }
}