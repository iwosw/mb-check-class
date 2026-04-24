package com.talhanation.bannermod.network;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import de.maxhenkel.corelib.CommonRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// Military network messages (migrated from recruits.network.*)
import com.talhanation.bannermod.network.messages.military.*;

// Civilian network messages (migrated from workers.network.*)
import com.talhanation.bannermod.network.messages.civilian.*;

/**
 * Owns the single shared SimpleChannel for the merged bannermod runtime.
 *
 * Military packets are registered at indices [0..MILITARY_MESSAGES.length) and
 * civilian packets at [MILITARY_MESSAGES.length..MILITARY_MESSAGES.length+CIVILIAN_MESSAGES.length).
 *
 * Ordering within each family is preserved verbatim from the legacy messages[] arrays
 * in recruits.Main.setup() and workers.WorkersMain.setup() respectively, so packet IDs
 * remain stable across saves and client/server pairs.
 *
 * workerPacketOffset() == MILITARY_MESSAGES.length == 106.
 */
public class BannerModNetworkBootstrap {

    /**
     * Military message catalog (verbatim order from recruits.Main legacy setup).
     * Registered at channel indices [0..MILITARY_MESSAGES.length).
     * Count: 106.
     */
    @SuppressWarnings({"rawtypes"})
    public static final Class[] MILITARY_MESSAGES = {
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
        MessageUpdateGovernorPolicy.class,
        MessageDebugGui.class,
        MessageDebugScreen.class,
        MessageSelectRecruits.class,
        MessageCombatStance.class,
        MessageCombatStanceGui.class,
    };

    /**
     * Civilian message catalog (verbatim order from workers.WorkersMain legacy setup).
     * Registered at channel indices [MILITARY_MESSAGES.length..MILITARY_MESSAGES.length+CIVILIAN_MESSAGES.length).
     * Count: 20. workerPacketOffset == MILITARY_MESSAGES.length == 106.
     */
    @SuppressWarnings({"rawtypes"})
    public static final Class[] CIVILIAN_MESSAGES = {
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
        MessageRequestPlaceBuilding.class,
        MessageRequestValidateBuilding.class,
    };

    private BannerModNetworkBootstrap() {
    }

    /**
     * Returns the offset at which civilian (worker) packets begin in the shared channel.
     * Equal to MILITARY_MESSAGES.length (106).
     * Matches the merged runtime's current worker packet offset.
     */
    public static int workerPacketOffset() {
        return MILITARY_MESSAGES.length;
    }

    /**
     * Creates and returns the single shared SimpleChannel with all military and civilian
     * packets registered. Must be called once during FMLCommonSetupEvent.
     *
     * Military packets: indices [0..106)
     * Civilian packets: indices [106..126)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static SimpleChannel createSharedChannel() {
        SimpleChannel channel = CommonRegistry.registerChannel(BannerModMain.MOD_ID, "default");

        // --- Military messages [0..MILITARY_MESSAGES.length) ---
        for (int i = 0; i < MILITARY_MESSAGES.length; i++) {
            CommonRegistry.registerMessage(channel, i, MILITARY_MESSAGES[i]);
        }

        // --- Civilian messages [MILITARY_MESSAGES.length..MILITARY_MESSAGES.length+CIVILIAN_MESSAGES.length) ---
        for (int j = 0; j < CIVILIAN_MESSAGES.length; j++) {
            CommonRegistry.registerMessage(channel, MILITARY_MESSAGES.length + j, CIVILIAN_MESSAGES[j]);
        }

        // Bind to WorkersRuntime for compatibility
        com.talhanation.bannermod.bootstrap.WorkersRuntime.bindChannel(channel);

        return channel;
    }
}
