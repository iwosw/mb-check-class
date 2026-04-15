package com.talhanation.bannermod.ai.civilian;

import java.util.Objects;

public class FarmerLoopProgress {

    public enum Action {
        PREPARE_BREAK_BLOCKS,
        PREPARE_PLOWING,
        PREPARE_PLANT_SEEDS,
        WAIT_FOR_ITEM,
        FINISHED
    }

    public record Decision(Action action, Action resumeAction, boolean keepCurrentArea) {

        public boolean isWaitingForItem() {
            return action == Action.WAIT_FOR_ITEM;
        }

        public boolean isFinished() {
            return action == Action.FINISHED;
        }
    }

    public static Decision selectNextAction(boolean hasBlocksToBreak, boolean hasBlocksToPlow, boolean hasBlocksToPlant) {
        if (hasBlocksToBreak) {
            return new Decision(Action.PREPARE_BREAK_BLOCKS, Action.PREPARE_BREAK_BLOCKS, true);
        }

        if (hasBlocksToPlow) {
            return new Decision(Action.PREPARE_PLOWING, Action.PREPARE_PLOWING, true);
        }

        if (hasBlocksToPlant) {
            return new Decision(Action.PREPARE_PLANT_SEEDS, Action.PREPARE_PLANT_SEEDS, true);
        }

        return new Decision(Action.FINISHED, Action.FINISHED, false);
    }

    public static Decision waitForRequiredItem(Action resumeAction) {
        if (resumeAction != Action.PREPARE_PLOWING && resumeAction != Action.PREPARE_PLANT_SEEDS) {
            throw new IllegalArgumentException("resumeAction must keep the farmer inside the current field");
        }

        return new Decision(Action.WAIT_FOR_ITEM, Objects.requireNonNull(resumeAction), true);
    }

    private FarmerLoopProgress() {
    }
}
