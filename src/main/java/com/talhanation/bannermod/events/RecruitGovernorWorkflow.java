package com.talhanation.bannermod.events;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.governance.BannerModGovernorAuthority;
import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.governance.BannerModGovernorPolicy;
import com.talhanation.bannermod.governance.BannerModGovernorService;
import com.talhanation.bannermod.governance.BannerModGovernorSnapshot;
import com.talhanation.bannermod.inventory.military.GovernorContainer;
import com.talhanation.bannermod.network.messages.military.MessageOpenGovernorScreen;
import com.talhanation.bannermod.network.messages.military.MessageToClientUpdateGovernorScreen;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementClientSnapshotContract.Envelope;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementClientSnapshotContract.Payload;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementClientSnapshotContract.RefreshTrigger;
import com.talhanation.bannermod.settlement.BannerModSettlementManager;
import com.talhanation.bannermod.settlement.BannerModSettlementService;
import com.talhanation.bannermod.settlement.BannerModSettlementSnapshot;
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
        long gameTime = recruit.getCommandSenderWorld().getGameTime();
        Envelope envelope;
        if (claim == null || !(recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) {
            envelope = Envelope.empty(0L, gameTime, RefreshTrigger.SCREEN_OPEN);
        } else {
            BannerModGovernorSnapshot governorSnapshot = governorService(serverLevel).getOrCreateGovernorSnapshot(claim);
            BannerModSettlementSnapshot settlementSnapshot = BannerModSettlementManager.get(serverLevel).getSnapshot(claim.getUUID());
            envelope = Envelope.ready(gameTime, gameTime, RefreshTrigger.SCREEN_OPEN,
                    new Payload(claim.getUUID(), settlementSnapshot, governorSnapshot));
        }

        BannerModMain.SIMPLE_CHANNEL.send(BannerModPacketDistributor.PLAYER.with(() -> player),
                new MessageToClientUpdateGovernorScreen(recruit.getUUID(), envelope));
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

}
