package com.talhanation.bannermod.ai.civilian;

public final class BuilderBuildProgress {

    private BuilderBuildProgress() {
    }

    public enum State {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETE
    }

    public static State classify(boolean buildStarted, boolean done, boolean hasStructureTemplate) {
        if (done) {
            return State.COMPLETE;
        }

        if (buildStarted && hasStructureTemplate) {
            return State.IN_PROGRESS;
        }

        return State.NOT_STARTED;
    }

    public static boolean hasPendingWorldWork(int blocksToBreak, int blocksToPlace, int multiBlocksToPlace) {
        return blocksToBreak > 0 || blocksToPlace > 0 || multiBlocksToPlace > 0;
    }
}
