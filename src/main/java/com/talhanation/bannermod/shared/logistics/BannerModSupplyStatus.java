package com.talhanation.bannermod.shared.logistics;

import com.talhanation.bannermod.entity.civilian.WorkerStorageRequestState;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class BannerModSupplyStatus {

    private BannerModSupplyStatus() {
    }

    public enum BuildState {
        NO_TEMPLATE,
        READY,
        NEEDS_MATERIALS,
        COMPLETE
    }

    public enum RecruitSupplyState {
        NO_UPKEEP,
        READY,
        NEEDS_FOOD,
        NEEDS_PAYMENT,
        NEEDS_FOOD_AND_PAYMENT
    }

    public enum ArmyUpkeepState {
        STABLE,
        UNPAID,
        STARVING,
        UNPAID_AND_STARVING
    }

    public record BuildProjectStatus(BuildState state, int materialTypes, int materialCount) {
    }

    public record WorkerSupplyStatus(boolean blocked, String reasonToken, String message) {
    }

    public record ArmyUpkeepStatus(ArmyUpkeepState state,
                                   boolean unpaid,
                                   boolean starving,
                                   int unpaidLevel,
                                   int starvingLevel,
                                   String reasonToken) {
    }

    public record RecruitSupplyStatus(RecruitSupplyState state, boolean blocked, boolean needsFood, boolean needsPayment,
                                      String reasonToken, ArmyUpkeepStatus accounting) {
    }

    public static BuildProjectStatus buildProjectStatus(boolean hasTemplate, boolean hasPendingWork, List<ItemStack> requiredMaterials) {
        int materialTypes = 0;
        int materialCount = 0;
        if (requiredMaterials != null) {
            for (ItemStack stack : requiredMaterials) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                materialTypes++;
                materialCount += stack.getCount();
            }
        }

        return buildProjectStatus(hasTemplate, hasPendingWork, materialTypes, materialCount);
    }

    public static BuildProjectStatus buildProjectStatus(boolean hasTemplate, boolean hasPendingWork, int materialTypes, int materialCount) {
        if (!hasTemplate) {
            return new BuildProjectStatus(BuildState.NO_TEMPLATE, 0, 0);
        }

        if (!hasPendingWork) {
            return new BuildProjectStatus(BuildState.COMPLETE, materialTypes, materialCount);
        }

        return new BuildProjectStatus(materialCount > 0 ? BuildState.NEEDS_MATERIALS : BuildState.READY, materialTypes, materialCount);
    }

    public static WorkerSupplyStatus workerSupplyStatus(WorkerStorageRequestState.PendingComplaint pendingComplaint) {
        if (pendingComplaint == null) {
            return new WorkerSupplyStatus(false, null, null);
        }
        return new WorkerSupplyStatus(true, pendingComplaint.reasonToken(), pendingComplaint.message());
    }

    public static RecruitSupplyStatus recruitSupplyStatus(boolean hasUpkeepSource, boolean needsFood, boolean paymentDue,
                                                          boolean upkeepHasFood, boolean upkeepHasPayment,
                                                          boolean inventoryHasPayment,
                                                          float hungerLevel) {
        if (!hasUpkeepSource) {
            return new RecruitSupplyStatus(RecruitSupplyState.NO_UPKEEP, false, false, false, null, armyUpkeepStatus(false, false, 0.0F));
        }

        boolean missingFood = needsFood && !upkeepHasFood;
        boolean missingPayment = paymentDue && !upkeepHasPayment && !inventoryHasPayment;
        ArmyUpkeepStatus accounting = armyUpkeepStatus(missingPayment, missingFood, hungerLevel);

        if (missingFood && missingPayment) {
            return new RecruitSupplyStatus(RecruitSupplyState.NEEDS_FOOD_AND_PAYMENT, true, true, true,
                    "recruit_upkeep_missing_food_and_payment", accounting);
        }

        if (missingFood) {
            return new RecruitSupplyStatus(RecruitSupplyState.NEEDS_FOOD, true, true, false,
                    "recruit_upkeep_missing_food", accounting);
        }

        if (missingPayment) {
            return new RecruitSupplyStatus(RecruitSupplyState.NEEDS_PAYMENT, true, false, true,
                    "recruit_upkeep_missing_payment", accounting);
        }

        return new RecruitSupplyStatus(RecruitSupplyState.READY, false, false, false, null, accounting);
    }

    public static ArmyUpkeepStatus armyUpkeepStatus(boolean unpaid, boolean starving, float hungerLevel) {
        int unpaidLevel = unpaid ? 1 : 0;
        int starvingLevel = starvingLevel(starving, hungerLevel);

        if (unpaid && starving) {
            return new ArmyUpkeepStatus(ArmyUpkeepState.UNPAID_AND_STARVING, true, true, unpaidLevel, starvingLevel,
                    "army_upkeep_unpaid_and_starving");
        }

        if (starving) {
            return new ArmyUpkeepStatus(ArmyUpkeepState.STARVING, false, true, unpaidLevel, starvingLevel,
                    "army_upkeep_starving");
        }

        if (unpaid) {
            return new ArmyUpkeepStatus(ArmyUpkeepState.UNPAID, true, false, unpaidLevel, starvingLevel,
                    "army_upkeep_unpaid");
        }

        return new ArmyUpkeepStatus(ArmyUpkeepState.STABLE, false, false, unpaidLevel, starvingLevel, null);
    }

    private static int starvingLevel(boolean starving, float hungerLevel) {
        if (!starving) {
            return 0;
        }
        if (hungerLevel <= 0.0F) {
            return 3;
        }
        if (hungerLevel <= 10.0F) {
            return 2;
        }
        return 1;
    }
}
