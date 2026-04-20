package com.talhanation.bannermod.army.command;

/**
 * Standard priority levels mirroring HYW's goal-priority constants. Higher number =
 * more urgent. The dispatcher doesn't currently use priority beyond logging — the
 * queue and preemption machinery will consume it in the next slice.
 */
public final class CommandIntentPriority {
    public static final int LOW = 1;
    public static final int NORMAL = 3;
    public static final int HIGH = 5;
    public static final int IMMEDIATE = 10;

    private CommandIntentPriority() {
    }
}
