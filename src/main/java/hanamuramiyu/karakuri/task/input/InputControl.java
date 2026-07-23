package hanamuramiyu.karakuri.task.input;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public enum InputControl {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    SPRINT,
    SNEAK,
    JUMP,
    ATTACK,
    USE;

    KeyMapping key(
        Minecraft client
    ) {
        return switch (this) {
            case FORWARD -> client.options.keyUp;
            case BACKWARD -> client.options.keyDown;
            case LEFT -> client.options.keyLeft;
            case RIGHT -> client.options.keyRight;
            case SPRINT -> client.options.keySprint;
            case SNEAK -> client.options.keyShift;
            case JUMP -> client.options.keyJump;
            case ATTACK -> client.options.keyAttack;
            case USE -> client.options.keyUse;
        };
    }
}