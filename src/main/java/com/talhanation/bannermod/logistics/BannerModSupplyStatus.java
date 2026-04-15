package com.talhanation.bannermod.logistics;

import com.talhanation.bannermod.entity.civilian.WorkerStorageRequestState;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @deprecated Use {@link com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus} instead.
 * Forwarder retained for staged migration per Phase 21 D-05 -- legacy shared-package overlap is
 * documented in MERGE_NOTES.md and is intentionally NOT deduplicated during Phase 21.
 *
 * <p>The nested {@link BuildState}, {@link RecruitSupplyState} enums and status records are
 * literal mirrors of the canonical types and delegate each calculator through shared -> legacy
 * mapping helpers. Do not add new members here -- add them to the canonical class.
 */
@Deprecated
public final class BannerModSupplyStatus {

    private BannerModSupplyStatus() {
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.BuildState}. */
    @Deprecated
    public enum BuildState {
        NO_TEMPLATE,
        READY,
        NEEDS_MATERIALS,
        COMPLETE
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.RecruitSupplyState}. */
    @Deprecated
    public enum RecruitSupplyState {
        NO_UPKEEP,
        READY,
        NEEDS_FOOD,
        NEEDS_PAYMENT,
        NEEDS_FOOD_AND_PAYMENT
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.BuildProjectStatus}. */
    @Deprecated
    public record BuildProjectStatus(BuildState state, int materialTypes, int materialCount) {
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.WorkerSupplyStatus}. */
    @Deprecated
    public record WorkerSupplyStatus(boolean blocked, String reasonToken, String message) {
    }

    /** @deprecated Use {@link com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.RecruitSupplyStatus}. */
    @Deprecated
    public record RecruitSupplyStatus(RecruitSupplyState state, boolean blocked, boolean needsFood, boolean needsPayment,
                                      String reasonToken) {
    }

    public static BuildProjectStatus buildProjectStatus(boolean hasTemplate, boolean hasPendingWork, List<ItemStack> requiredMaterials) {
        return fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.buildProjectStatus(hasTemplate, hasPendingWork, requiredMaterials));
    }

    public static BuildProjectStatus buildProjectStatus(boolean hasTemplate, boolean hasPendingWork, int materialTypes, int materialCount) {
        return fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.buildProjectStatus(hasTemplate, hasPendingWork, materialTypes, materialCount));
    }

    public static WorkerSupplyStatus workerSupplyStatus(WorkerStorageRequestState.PendingComplaint pendingComplaint) {
        return fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.workerSupplyStatus(pendingComplaint));
    }

    public static RecruitSupplyStatus recruitSupplyStatus(boolean hasUpkeepSource, boolean needsFood, boolean paymentDue,
                                                          boolean upkeepHasFood, boolean upkeepHasPayment,
                                                          boolean inventoryHasPayment) {
        return fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.recruitSupplyStatus(hasUpkeepSource, needsFood, paymentDue, upkeepHasFood, upkeepHasPayment, inventoryHasPayment));
    }

    private static BuildState fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.BuildState state) {
        return state == null ? null : BuildState.valueOf(state.name());
    }

    private static RecruitSupplyState fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.RecruitSupplyState state) {
        return state == null ? null : RecruitSupplyState.valueOf(state.name());
    }

    private static BuildProjectStatus fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.BuildProjectStatus status) {
        return status == null ? null : new BuildProjectStatus(fromShared(status.state()), status.materialTypes(), status.materialCount());
    }

    private static WorkerSupplyStatus fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.WorkerSupplyStatus status) {
        return status == null ? null : new WorkerSupplyStatus(status.blocked(), status.reasonToken(), status.message());
    }

    private static RecruitSupplyStatus fromShared(com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus.RecruitSupplyStatus status) {
        return status == null ? null : new RecruitSupplyStatus(fromShared(status.state()), status.blocked(), status.needsFood(), status.needsPayment(), status.reasonToken());
    }
}
