package com.talhanation.bannermod.logistics;

import com.talhanation.bannerlord.entity.civilian.WorkerStorageRequestState;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Deprecated(forRemoval = false)
public final class BannerModSupplyStatus {

    private BannerModSupplyStatus() {
    }

    public enum BuildState {
        NO_TEMPLATE,
        READY,
        NEEDS_MATERIALS,
        COMPLETE;

        private static BuildState fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.BuildState state) {
            return BuildState.valueOf(state.name());
        }
    }

    public enum RecruitSupplyState {
        NO_UPKEEP,
        READY,
        NEEDS_FOOD,
        NEEDS_PAYMENT,
        NEEDS_FOOD_AND_PAYMENT;

        private static RecruitSupplyState fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.RecruitSupplyState state) {
            return RecruitSupplyState.valueOf(state.name());
        }
    }

    public record BuildProjectStatus(BuildState state, int materialTypes, int materialCount) {
        private static BuildProjectStatus fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.BuildProjectStatus status) {
            return new BuildProjectStatus(BuildState.fromShared(status.state()), status.materialTypes(), status.materialCount());
        }
    }

    public record WorkerSupplyStatus(boolean blocked, String reasonToken, String message) {
        private static WorkerSupplyStatus fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.WorkerSupplyStatus status) {
            return new WorkerSupplyStatus(status.blocked(), status.reasonToken(), status.message());
        }
    }

    public record RecruitSupplyStatus(RecruitSupplyState state, boolean blocked, boolean needsFood, boolean needsPayment,
                                      String reasonToken) {
        private static RecruitSupplyStatus fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.RecruitSupplyStatus status) {
            return new RecruitSupplyStatus(RecruitSupplyState.fromShared(status.state()), status.blocked(), status.needsFood(), status.needsPayment(), status.reasonToken());
        }
    }

    public static BuildProjectStatus buildProjectStatus(boolean hasTemplate, boolean hasPendingWork, List<ItemStack> requiredMaterials) {
        return BuildProjectStatus.fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.buildProjectStatus(hasTemplate, hasPendingWork, requiredMaterials));
    }

    public static BuildProjectStatus buildProjectStatus(boolean hasTemplate, boolean hasPendingWork, int materialTypes, int materialCount) {
        return BuildProjectStatus.fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.buildProjectStatus(hasTemplate, hasPendingWork, materialTypes, materialCount));
    }

    public static WorkerSupplyStatus workerSupplyStatus(WorkerStorageRequestState.PendingComplaint pendingComplaint) {
        return WorkerSupplyStatus.fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.workerSupplyStatus(pendingComplaint));
    }

    public static RecruitSupplyStatus recruitSupplyStatus(boolean hasUpkeepSource, boolean needsFood, boolean paymentDue,
                                                          boolean upkeepHasFood, boolean upkeepHasPayment,
                                                          boolean inventoryHasPayment) {
        return RecruitSupplyStatus.fromShared(com.talhanation.bannerlord.shared.logistics.BannerModSupplyStatus.recruitSupplyStatus(
                hasUpkeepSource,
                needsFood,
                paymentDue,
                upkeepHasFood,
                upkeepHasPayment,
                inventoryHasPayment
        ));
    }
}
