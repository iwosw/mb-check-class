package com.talhanation.bannerlord.ai.civilian;

import java.util.Objects;

public class AnimalFarmerLoopProgress {

    public enum Action {
        PREPARE_BREED,
        PREPARE_SPECIAL_TASK,
        PREPARE_SLAUGHTER,
        WAIT_FOR_ITEM,
        WAIT_FOR_DEPOSIT,
        FINISHED
    }

    public record Decision(Action action, Action resumeAction, boolean keepCurrentPen) {

        public boolean isWaiting() {
            return action == Action.WAIT_FOR_ITEM || action == Action.WAIT_FOR_DEPOSIT;
        }

        public boolean isFinished() {
            return action == Action.FINISHED;
        }
    }

    public static Decision selectNextAction(boolean breedEnabled, boolean breedTimeReady, int breedCandidates, boolean specialEnabled,
                                            int specialCandidates, boolean specialAlwaysAvailable, boolean slaughterEnabled,
                                            int slaughterCandidates, int maxAnimals) {
        int breedPairs = breedCandidates - (breedCandidates % 2);
        if (breedEnabled && breedTimeReady && breedPairs >= 2) {
            return new Decision(Action.PREPARE_BREED, Action.PREPARE_BREED, true);
        }

        if (specialEnabled && (specialAlwaysAvailable || specialCandidates > 0)) {
            return new Decision(Action.PREPARE_SPECIAL_TASK, Action.PREPARE_SPECIAL_TASK, true);
        }

        if (slaughterEnabled && slaughterCandidates > maxAnimals) {
            return new Decision(Action.PREPARE_SLAUGHTER, Action.PREPARE_SLAUGHTER, true);
        }

        return new Decision(Action.FINISHED, Action.FINISHED, false);
    }

    public static Decision waitForRequiredItem(Action resumeAction) {
        return new Decision(Action.WAIT_FOR_ITEM, requireResumeAction(resumeAction), true);
    }

    public static Decision waitForDeposit(Action resumeAction) {
        return new Decision(Action.WAIT_FOR_DEPOSIT, requireResumeAction(resumeAction), true);
    }

    private static Action requireResumeAction(Action resumeAction) {
        if (resumeAction != Action.PREPARE_BREED && resumeAction != Action.PREPARE_SPECIAL_TASK && resumeAction != Action.PREPARE_SLAUGHTER) {
            throw new IllegalArgumentException("resumeAction must keep the animal farmer inside the current pen");
        }

        return Objects.requireNonNull(resumeAction);
    }

    private AnimalFarmerLoopProgress() {
    }
}
