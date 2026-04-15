package com.talhanation.bannerlord.shared.logistics;

import com.talhanation.bannerlord.entity.civilian.WorkerStorageRequestState;
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

    public record BuildProjectStatus(BuildState state, int materialTypes, int materialCount) {
    }

    public record WorkerSupplyStatus(boolean blocked, String reasonToken, String message) {
    }

    public record RecruitSupplyStatus(RecruitSupplyState state, boolean blocked, boolean needsFood, boolean needsPayment,
                                      String reasonToken) {
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
                                                          boolean inventoryHasPayment) {
        if (!hasUpkeepSource) {
            return new RecruitSupplyStatus(RecruitSupplyState.NO_UPKEEP, false, false, false, null);
        }

        boolean missingFood = needsFood && !upkeepHasFood;
        boolean missingPayment = paymentDue && !upkeepHasPayment && !inventoryHasPayment;

        if (missingFood && missingPayment) {
            return new RecruitSupplyStatus(RecruitSupplyState.NEEDS_FOOD_AND_PAYMENT, true, true, true,
                    "recruit_upkeep_missing_food_and_payment");
        }

        if (missingFood) {
            return new RecruitSupplyStatus(RecruitSupplyState.NEEDS_FOOD, true, true, false,
                    "recruit_upkeep_missing_food");
        }

        if (missingPayment) {
            return new RecruitSupplyStatus(RecruitSupplyState.NEEDS_PAYMENT, true, false, true,
                    "recruit_upkeep_missing_payment");
        }

        return new RecruitSupplyStatus(RecruitSupplyState.READY, false, false, false, null);
    }
}
