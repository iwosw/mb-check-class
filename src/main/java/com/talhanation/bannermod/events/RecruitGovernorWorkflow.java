package com.talhanation.bannermod.events;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.governance.BannerModGovernorAuthority;
import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.governance.BannerModGovernorPolicy;
import com.talhanation.bannermod.governance.BannerModGovernorRecommendation;
import com.talhanation.bannermod.governance.BannerModGovernorService;
import com.talhanation.bannermod.governance.BannerModGovernorSnapshot;
import com.talhanation.bannermod.inventory.military.GovernorContainer;
import com.talhanation.bannermod.network.messages.military.MessageOpenGovernorScreen;
import com.talhanation.bannermod.network.messages.military.MessageToClientUpdateGovernorScreen;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.settlement.BannerModSettlementManager;
import com.talhanation.bannermod.settlement.BannerModSettlementService;
import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;
import com.talhanation.bannermod.settlement.BannerModSettlementStrategicSignals;
import com.talhanation.bannermod.settlement.BannerModSettlementOrchestrator;
import com.talhanation.bannermod.settlement.BannerModSettlementDesiredGoodSeed;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderExecutionReceipt;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderRuntime;
import com.talhanation.bannermod.settlement.workorder.SettlementWorkOrderStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import com.talhanation.bannermod.network.compat.BannerModNetworkHooks;
import com.talhanation.bannermod.network.compat.BannerModPacketDistributor;
import org.jetbrains.annotations.NotNull;

final class RecruitGovernorWorkflow {

    private RecruitGovernorWorkflow() {
    }

    static boolean tryPromoteRecruit(AbstractRecruitEntity recruit, String name, ServerPlayer player) {
        if (!(recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) {
            return true;
        }
        if (recruit.getXpLevel() < 7 || recruit.getOwnerUUID() == null) {
            player.sendSystemMessage(Component.literal("Governor designation denied: recruit is not eligible"));
            return true;
        }
        if (name != null && !name.isBlank()) {
            recruit.setCustomName(Component.literal(name));
        }

        BannerModGovernorService.OperationResult result = governorService(serverLevel)
                .assignGovernor(resolveClaim(recruit), player, recruit);
        if (result.allowed()) {
            RecruitsClaim claim = resolveClaim(recruit);
            if (claim != null) {
                BannerModSettlementService.refreshClaim(
                        serverLevel,
                        ClaimEvents.recruitsClaimManager,
                        BannerModSettlementManager.get(serverLevel),
                        BannerModGovernorManager.get(serverLevel),
                        claim
                );
            }
            player.sendSystemMessage(Component.literal(recruit.getName().getString() + " designated as governor"));
            openGovernorScreen(player, recruit);
        } else {
            player.sendSystemMessage(Component.literal("Governor designation denied: " + result.governorDecision().name().toLowerCase()));
        }
        return true;
    }

    static void openGovernorScreen(Player player, AbstractRecruitEntity recruit) {
        if (player instanceof ServerPlayer serverPlayer) {
            BannerModNetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.literal("Governor");
                }

                @Override
                public AbstractContainerMenu createMenu(int i, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                    return new GovernorContainer(i, playerEntity, recruit);
                }
            }, packetBuffer -> packetBuffer.writeUUID(recruit.getUUID()));
            syncGovernorScreen(serverPlayer, recruit);
        } else {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenGovernorScreen(recruit.getUUID(), true));
        }
    }

    static void syncGovernorScreen(ServerPlayer player, AbstractRecruitEntity recruit) {
        RecruitsClaim claim = resolveClaim(recruit);
        BannerModGovernorSnapshot snapshot = claim == null
                ? null
                : governorService((ServerLevel) recruit.getCommandSenderWorld()).getOrCreateGovernorSnapshot(claim);
        BannerModSettlementBinding.Binding binding = claim == null
                ? BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, recruit.blockPosition(), recruit.getTeam() == null ? null : recruit.getTeam().getName())
                : BannerModSettlementBinding.resolveSettlementStatus(claim, claim.getCenter() == null ? new ChunkPos(recruit.blockPosition()) : claim.getCenter(), claim.getOwnerPoliticalEntityId() == null ? null : claim.getOwnerPoliticalEntityId().toString());
        java.util.List<String> recommendations = snapshot == null ? java.util.List.of() : new java.util.ArrayList<>(snapshot.recommendationTokens());
        java.util.List<String> logisticsLines = java.util.List.of("No settlement logistics snapshot");
        if (claim != null && recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel) {
            BannerModSettlementSnapshot settlementSnapshot = BannerModSettlementManager.get(serverLevel).getSnapshot(claim.getUUID());
            if (settlementSnapshot != null) {
                recommendations.addAll(settlementSnapshot.tradeRouteHandoffSeed().seaTradeStatusLines());
                logisticsLines = buildLogisticsLines(serverLevel, settlementSnapshot);
            }
        }

        BannerModMain.SIMPLE_CHANNEL.send(BannerModPacketDistributor.PLAYER.with(() -> player), new MessageToClientUpdateGovernorScreen(
                recruit.getUUID(),
                binding.status().name().toLowerCase(),
                snapshot == null ? 0 : snapshot.citizenCount(),
                snapshot == null ? 0 : snapshot.taxesDue(),
                snapshot == null ? 0 : snapshot.taxesCollected(),
                snapshot == null ? 0L : snapshot.lastHeartbeatTick(),
                recommendationLabel(snapshot, true),
                recommendationLabel(snapshot, false),
                snapshot == null ? BannerModGovernorPolicy.DEFAULT_VALUE : snapshot.garrisonPriority(),
                snapshot == null ? BannerModGovernorPolicy.DEFAULT_VALUE : snapshot.fortificationPriority(),
                snapshot == null ? BannerModGovernorPolicy.DEFAULT_VALUE : snapshot.taxPressure(),
                snapshot == null ? 0 : snapshot.treasuryBalance(),
                snapshot == null ? 0 : snapshot.lastTreasuryNet(),
                snapshot == null ? 0 : snapshot.projectedTreasuryBalance(),
                snapshot == null ? java.util.List.of() : snapshot.incidentTokens(),
                recommendations,
                logisticsLines
        ));
    }

    private static java.util.List<String> buildLogisticsLines(ServerLevel serverLevel, BannerModSettlementSnapshot settlementSnapshot) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        SettlementWorkOrderRuntime runtime = BannerModSettlementOrchestrator.workOrderRuntime(serverLevel);
        int pending = runtime == null ? 0 : runtime.countForClaim(settlementSnapshot.claimUuid(), SettlementWorkOrderStatus.PENDING);
        int claimed = runtime == null ? 0 : runtime.countForClaim(settlementSnapshot.claimUuid(), SettlementWorkOrderStatus.CLAIMED);
        int completed = 0;
        if (runtime != null) {
            for (SettlementWorkOrderExecutionReceipt receipt : runtime.recentCompletions()) {
                if (settlementSnapshot.claimUuid().equals(receipt.claimUuid())) {
                    completed++;
                }
            }
        }
        lines.add("Work orders: pending " + pending + ", claimed " + claimed + ", completed " + completed);
        lines.add("Blocked: " + settlementSnapshot.supplySignalState().shortageSignalCount() + " shortages / " + settlementSnapshot.supplySignalState().shortageUnitCount() + " units");
        lines.add("Stockpile: " + settlementSnapshot.stockpileSummary().containerCount() + " containers, " + settlementSnapshot.stockpileSummary().slotCapacity() + " slots");
        lines.add("Storage routes: " + settlementSnapshot.stockpileSummary().routedStorageCount() + " routed, " + settlementSnapshot.stockpileSummary().portEntrypointCount() + " ports");
        BannerModSettlementStrategicSignals signals = BannerModSettlementStrategicSignals.fromSnapshot(settlementSnapshot);
        lines.add("Role: " + signals.roleId() + " - " + signals.roleDescription());
        lines.add("Route cost: " + signals.routeCostId() + " - " + signals.routeCostDescription());
        if (!"none".equals(signals.specializationId())) {
            lines.add("Specialty: " + signals.specializationId() + " - " + signals.specializationDescription());
        }
        if (!signals.logisticsObjectiveIds().isEmpty()) {
            lines.add("War objectives: " + String.join(", ", signals.logisticsObjectiveIds()));
        }
        if (!signals.loyaltyPressureIds().isEmpty()) {
            lines.add("Loyalty pressure: " + String.join(", ", signals.loyaltyPressureIds()));
        }
        java.util.List<BannerModSettlementDesiredGoodSeed> desiredGoods = settlementSnapshot.desiredGoodsSeed().desiredGoods();
        if (desiredGoods.isEmpty()) {
            lines.add("Missing goods: none signaled");
        } else {
            java.util.List<String> goods = new java.util.ArrayList<>();
            for (int i = 0; i < Math.min(3, desiredGoods.size()); i++) {
                BannerModSettlementDesiredGoodSeed desiredGood = desiredGoods.get(i);
                goods.add(desiredGood.desiredGoodId() + " x" + desiredGood.driverCount());
            }
            lines.add("Missing goods: " + String.join(", ", goods));
        }
        if (!settlementSnapshot.projectCandidateSeed().candidateId().equals("none")) {
            lines.add("Project: " + settlementSnapshot.projectCandidateSeed().candidateId() + " priority " + settlementSnapshot.projectCandidateSeed().priority());
        } else {
            lines.add("Project: none queued");
        }
        return lines;
    }

    static void updateGovernorPolicy(ServerPlayer player, AbstractRecruitEntity recruit, BannerModGovernorPolicy policy, int value) {
        if (!(recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) {
            return;
        }
        BannerModGovernorService.OperationResult result = governorService(serverLevel)
                .updatePolicy(resolveClaim(recruit), BannerModGovernorAuthority.actor(player), policy, value);
        if (!result.allowed()) {
            player.sendSystemMessage(Component.literal("Governor policy update denied: " + result.governorDecision().name().toLowerCase()));
            return;
        }
        syncGovernorScreen(player, recruit);
    }

    private static BannerModGovernorService governorService(ServerLevel level) {
        return new BannerModGovernorService(BannerModGovernorManager.get(level));
    }

    private static RecruitsClaim resolveClaim(AbstractRecruitEntity recruit) {
        return ClaimEvents.recruitsClaimManager == null
                ? null
                : ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(recruit.blockPosition()));
    }

    private static String recommendationLabel(BannerModGovernorSnapshot snapshot, boolean garrison) {
        if (snapshot == null) {
            return BannerModGovernorRecommendation.HOLD_COURSE.token();
        }
        for (String token : snapshot.recommendationTokens()) {
            if (garrison && BannerModGovernorRecommendation.INCREASE_GARRISON.token().equals(token)) {
                return token;
            }
            if (!garrison && BannerModGovernorRecommendation.STRENGTHEN_FORTIFICATIONS.token().equals(token)) {
                return token;
            }
        }
        return BannerModGovernorRecommendation.HOLD_COURSE.token();
    }
}
