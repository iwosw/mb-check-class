package com.talhanation.bannermod.network;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import de.maxhenkel.corelib.CommonRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// Recruits network messages
import com.talhanation.bannermod.network.messages.military.*;

// Workers network messages
import com.talhanation.bannermod.network.messages.civilian.*;

/**
 * Owns the single shared SimpleChannel for the merged bannermod runtime.
 *
 * Recruit packets are registered at indices [0..N) and worker packets at [N..N+M),
 * preserving the exact order from the two legacy messages[] arrays.
 */
public class BannerModNetworkBootstrap {

    /**
     * The number of recruit messages in the catalog. Worker packets start at this offset.
     * Must match WorkersRuntime.ROOT_NETWORK_ID_OFFSET = 104.
     */
    public static final int WORKER_PACKET_OFFSET = 104;

    private BannerModNetworkBootstrap() {
    }

    /**
     * Returns the offset at which worker packets begin in the shared channel.
     */
    public static int workerPacketOffset() {
        return WORKER_PACKET_OFFSET;
    }

    /**
     * Creates and returns the single shared SimpleChannel with all recruit and worker
     * packets registered. Must be called once during FMLCommonSetupEvent.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static SimpleChannel createSharedChannel() {
        SimpleChannel channel = CommonRegistry.registerChannel(BannerModMain.MOD_ID, "default");

        // --- Recruit messages [0..103] (verbatim order from recruits.Main legacy setup) ---
        Class[] recruitMessages = {
            MessageMovement.class,
            MessageCommandScreen.class,
            MessageRecruitGui.class,
            MessageHire.class,
            MessageHireGui.class,
            MessageDisband.class,
            MessageRest.class,
            MessageAttack.class,
            MessageAggro.class,
            MessageAggroGui.class,
            MessageListen.class,
            MessageProtectEntity.class,
            MessageMountEntity.class,
            MessageMountEntityGui.class,
            MessageDismount.class,
            MessageDismountGui.class,
            MessageBackToMountEntity.class,
            MessageRangedFire.class,
            MessageStrategicFire.class,
            MessageShields.class,
            MessageGroup.class,
            MessageSplitGroup.class,
            MessageMergeGroup.class,
            MessageDisbandGroup.class,
            MessageSetLeaderGroup.class,
            MessageRecruitCount.class,
            MessageAssassinCount.class,
            MessageAssassinGui.class,
            MessageAssassinate.class,
            MessageFaceCommand.class,
            MessageTeleportPlayer.class,
            MessageScoutTask.class,
            MessageFollowGui.class,
            MessageClearTarget.class,
            MessageClearTargetGui.class,
            MessageOpenSpecialScreen.class,
            MessageOpenPromoteScreen.class,
            MessageOpenDisbandScreen.class,
            MessageToClientOpenNobleTradeScreen.class,
            MessageHireFromNobleVillager.class,
            MessageWriteSpawnEgg.class,
            MessageDoPayment.class,
            MessageUpkeepEntity.class,
            MessageUpkeepPos.class,
            MessageClearUpkeep.class,
            MessageClearUpkeepGui.class,
            MessageCreateTeam.class,
            MessageSaveTeamSettings.class,
            MessageLeaveTeam.class,
            MessageAddPlayerToTeam.class,
            MessageRemoveFromTeam.class,
            MessageAddRecruitToTeam.class,
            MessageTeamMainScreen.class,
            MessageOpenTeamEditScreen.class,
            MessageOpenTeamAddPlayerScreen.class,
            MessageOpenTeamListScreen.class,
            MessageOpenTeamInspectionScreen.class,
            MessageSendJoinRequestTeam.class,
            MessageToClientUpdateFactions.class,
            MessageToClientUpdateOwnFaction.class,
            MessageToClientUpdateOnlinePlayers.class,
            MessageToClientUpdateDiplomacyList.class,
            MessageToClientUpdateTreaties.class,
            MessageToClientSetDiplomaticToast.class,
            MessageChangeDiplomacyStatus.class,
            MessageSendTreaty.class,
            MessageAnswerTreaty.class,
            MessageToClientOpenTreatyAnswerScreen.class,
            MessageSendMessenger.class,
            MessageAnswerMessenger.class,
            MessageToClientOpenMessengerAnswerScreen.class,
            MessageToClientUpdateMessengerScreen.class,
            MessageToClientUpdateHireState.class,
            MessageToClientUpdateClaim.class,
            MessageToClientUpdateClaims.class,
            MessageUpdateClaim.class,
            MessageDeleteClaim.class,
            MessageToClientReceiveRoute.class,
            MessageTransferRoute.class,
            MessageToClientUpdateGroups.class,
            MessageUpdateGroup.class,
            MessageAssignGroupToPlayer.class,
            MessageAssignGroupToCompanion.class,
            MessageRemoveAssignedGroupFromCompanion.class,
            MessageAssignNearbyRecruitsInGroup.class,
            MessageApplyNoGroup.class,
            MessageToClientUpdateUnitInfo.class,
            MessageToClientUpdateLeaderScreen.class,
            MessageSaveFormationFollowMovement.class,
            MessageFormationFollowMovement.class,
            MessagePatrolLeaderSetRoute.class,
            MessagePatrolLeaderAddWayPoint.class,
            MessagePatrolLeaderRemoveWayPoint.class,
            MessagePatrolLeaderSetCycle.class,
            MessagePatrolLeaderSetPatrolState.class,
            MessagePatrolLeaderSetPatrollingSpeed.class,
            MessagePatrolLeaderSetWaitTime.class,
            MessagePatrolLeaderSetEnemyAction.class,
            MessagePatrolLeaderSetInfoMode.class,
            MessageToClientOpenTakeOverScreen.class,
            MessageToClientSetToast.class,
            MessagePromoteRecruit.class,
            MessageOpenGovernorScreen.class,
            MessageToClientUpdateGovernorScreen.class,
            MessageDebugGui.class,
            MessageDebugScreen.class,
        };
        for (int i = 0; i < recruitMessages.length; i++) {
            CommonRegistry.registerMessage(channel, i, recruitMessages[i]);
        }

        // --- Worker messages [WORKER_PACKET_OFFSET..WORKER_PACKET_OFFSET+M) ---
        Class[] workerMessages = {
            MessageAddWorkArea.class,
            MessageToClientOpenWorkAreaScreen.class,
            MessageUpdateWorkArea.class,
            MessageUpdateCropArea.class,
            MessageUpdateLumberArea.class,
            MessageUpdateBuildArea.class,
            MessageUpdateMiningArea.class,
            MessageUpdateMerchantTrade.class,
            MessageUpdateMerchant.class,
            MessageDoTradeWithMerchant.class,
            MessageOpenMerchantEditTradeScreen.class,
            MessageOpenMerchantTradeScreen.class,
            MessageToClientUpdateConfig.class,
            MessageUpdateStorageArea.class,
            MessageUpdateAnimalPenArea.class,
            MessageRotateWorkArea.class,
            MessageMoveMerchantTrade.class,
            MessageUpdateMarketArea.class,
            MessageUpdateOwner.class,
            MessageRecoverWorkerControl.class,
        };
        for (int i = 0; i < workerMessages.length; i++) {
            CommonRegistry.registerMessage(channel, WORKER_PACKET_OFFSET + i, workerMessages[i]);
        }

        // Bind to WorkersRuntime for compatibility
        com.talhanation.workers.WorkersRuntime.bindChannel(channel);

        return channel;
    }
}
