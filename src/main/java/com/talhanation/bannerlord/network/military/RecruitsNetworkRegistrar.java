package com.talhanation.bannerlord.network.military;

import com.talhanation.recruits.migration.NetworkBootstrapSeams;
import de.maxhenkel.corelib.CommonRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public class RecruitsNetworkRegistrar implements NetworkBootstrapSeams.OrderedMessageCatalog, NetworkBootstrapSeams.ChannelRegistrar {

    private static final List<NetworkBootstrapSeams.MessageRegistration> ORDERED_MESSAGE_TYPES = List.of(
            registration(0, com.talhanation.recruits.network.MessageAggro.class),
            registration(1, com.talhanation.recruits.network.MessageAggroGui.class),
            registration(2, com.talhanation.recruits.network.MessageAssassinate.class),
            registration(3, com.talhanation.recruits.network.MessageAssassinCount.class),
            registration(4, com.talhanation.recruits.network.MessageAssassinGui.class),
            registration(5, com.talhanation.recruits.network.MessageMountEntity.class),
            registration(6, com.talhanation.recruits.network.MessageMountEntityGui.class),
            registration(7, com.talhanation.recruits.network.MessageClearTargetGui.class),
            registration(8, com.talhanation.recruits.network.MessageCommandScreen.class),
            registration(9, com.talhanation.recruits.network.MessageDisband.class),
            registration(10, com.talhanation.recruits.network.MessageMovement.class),
            registration(11, com.talhanation.recruits.network.MessageFollowGui.class),
            registration(12, com.talhanation.recruits.network.MessageGroup.class),
            registration(13, com.talhanation.recruits.network.MessageListen.class),
            registration(14, com.talhanation.recruits.network.MessageRecruitGui.class),
            registration(15, com.talhanation.recruits.network.MessageHireGui.class),
            registration(16, com.talhanation.recruits.network.MessageHire.class),
            registration(17, com.talhanation.recruits.network.MessageProtectEntity.class),
            registration(18, com.talhanation.recruits.network.MessageDismount.class),
            registration(19, com.talhanation.recruits.network.MessageDismountGui.class),
            registration(20, com.talhanation.recruits.network.MessageUpkeepPos.class),
            registration(21, com.talhanation.recruits.network.MessageStrategicFire.class),
            registration(22, com.talhanation.recruits.network.MessageShields.class),
            registration(23, com.talhanation.recruits.network.MessageDebugGui.class),
            registration(24, com.talhanation.recruits.network.MessageUpkeepEntity.class),
            registration(25, com.talhanation.recruits.network.MessageClearTarget.class),
            registration(26, com.talhanation.recruits.network.MessageCreateTeam.class),
            registration(27, com.talhanation.recruits.network.MessageOpenTeamEditScreen.class),
            registration(28, com.talhanation.recruits.network.MessageLeaveTeam.class),
            registration(29, com.talhanation.recruits.network.MessageTeamMainScreen.class),
            registration(30, com.talhanation.recruits.network.MessageOpenTeamInspectionScreen.class),
            registration(31, com.talhanation.recruits.network.MessageOpenTeamListScreen.class),
            registration(32, com.talhanation.recruits.network.MessageAddPlayerToTeam.class),
            registration(33, com.talhanation.recruits.network.MessageOpenTeamAddPlayerScreen.class),
            registration(34, com.talhanation.recruits.network.MessageAddRecruitToTeam.class),
            registration(35, com.talhanation.recruits.network.MessageSendJoinRequestTeam.class),
            registration(36, com.talhanation.recruits.network.MessageRemoveFromTeam.class),
            registration(37, com.talhanation.recruits.network.MessageOpenDisbandScreen.class),
            registration(38, com.talhanation.recruits.network.MessageAssignRecruitToPlayer.class),
            registration(39, com.talhanation.recruits.network.MessageWriteSpawnEgg.class),
            registration(40, com.talhanation.recruits.network.MessageBackToMountEntity.class),
            registration(41, com.talhanation.recruits.network.MessageDisbandGroup.class),
            registration(42, com.talhanation.recruits.network.MessageAssignGroupToPlayer.class),
            registration(43, com.talhanation.recruits.network.MessagePromoteRecruit.class),
            registration(44, com.talhanation.recruits.network.MessageOpenPromoteScreen.class),
            registration(45, com.talhanation.recruits.network.MessageOpenSpecialScreen.class),
            registration(46, com.talhanation.recruits.network.MessageSendMessenger.class),
            registration(47, com.talhanation.recruits.network.MessagePatrolLeaderSetWaitTime.class),
            registration(48, com.talhanation.recruits.network.MessageToClientUpdateLeaderScreen.class),
            registration(49, com.talhanation.recruits.network.MessagePatrolLeaderAddWayPoint.class),
            registration(50, com.talhanation.recruits.network.MessagePatrolLeaderRemoveWayPoint.class),
            registration(51, com.talhanation.recruits.network.MessagePatrolLeaderSetPatrolState.class),
            registration(52, com.talhanation.recruits.network.MessagePatrolLeaderSetCycle.class),
            registration(53, com.talhanation.recruits.network.MessagePatrolLeaderSetInfoMode.class),
            registration(54, com.talhanation.recruits.network.MessageAssignGroupToCompanion.class),
            registration(55, com.talhanation.recruits.network.MessagePatrolLeaderSetPatrollingSpeed.class),
            registration(56, com.talhanation.recruits.network.MessageToClientUpdateHireState.class),
            registration(57, com.talhanation.recruits.network.MessageRemoveAssignedGroupFromCompanion.class),
            registration(58, com.talhanation.recruits.network.MessageAnswerMessenger.class),
            registration(59, com.talhanation.recruits.network.MessageToClientOpenMessengerAnswerScreen.class),
            registration(60, com.talhanation.recruits.network.MessageClearUpkeepGui.class),
            registration(61, com.talhanation.recruits.network.MessageApplyNoGroup.class),
            registration(62, com.talhanation.recruits.network.MessageToClientUpdateGroups.class),
            registration(63, com.talhanation.recruits.network.MessagePatrolLeaderSetRoute.class),
            registration(64, com.talhanation.recruits.network.MessagePatrolLeaderSetEnemyAction.class),
            registration(65, com.talhanation.recruits.network.MessageSetLeaderGroup.class),
            registration(66, com.talhanation.recruits.network.MessageTransferRoute.class),
            registration(67, com.talhanation.recruits.network.MessageToClientReceiveRoute.class),
            registration(68, com.talhanation.recruits.network.MessageFormationFollowMovement.class),
            registration(69, com.talhanation.recruits.network.MessageRest.class),
            registration(70, com.talhanation.recruits.network.MessageRangedFire.class),
            registration(71, com.talhanation.recruits.network.MessageSaveFormationFollowMovement.class),
            registration(72, com.talhanation.recruits.network.MessageClearUpkeep.class),
            registration(73, com.talhanation.recruits.network.MessageToClientUpdateFactions.class),
            registration(74, com.talhanation.recruits.network.MessageToClientUpdateOnlinePlayers.class),
            registration(75, com.talhanation.recruits.network.MessageChangeDiplomacyStatus.class),
            registration(76, com.talhanation.recruits.network.MessageToClientSetToast.class),
            registration(77, com.talhanation.recruits.network.MessageToClientUpdateDiplomacyList.class),
            registration(78, com.talhanation.recruits.network.MessageSaveTeamSettings.class),
            registration(79, com.talhanation.recruits.network.MessageToClientSetDiplomaticToast.class),
            registration(80, com.talhanation.recruits.network.MessageScoutTask.class),
            registration(81, com.talhanation.recruits.network.MessageToClientOpenTakeOverScreen.class),
            registration(82, com.talhanation.recruits.network.MessageToClientUpdateClaims.class),
            registration(83, com.talhanation.recruits.network.MessageUpdateClaim.class),
            registration(84, com.talhanation.recruits.network.MessageDoPayment.class),
            registration(85, com.talhanation.recruits.network.MessageToClientUpdateClaim.class),
            registration(86, com.talhanation.recruits.network.MessageToClientUpdateOwnFaction.class),
            registration(87, com.talhanation.recruits.network.MessageDeleteClaim.class),
            registration(88, com.talhanation.recruits.network.MessageToClientOpenNobleTradeScreen.class),
            registration(89, com.talhanation.recruits.network.MessageHireFromNobleVillager.class),
            registration(90, com.talhanation.recruits.network.MessageAttack.class),
            registration(91, com.talhanation.recruits.network.MessageToClientUpdateUnitInfo.class),
            registration(92, com.talhanation.recruits.network.MessageUpdateGroup.class),
            registration(93, com.talhanation.recruits.network.MessageMergeGroup.class),
            registration(94, com.talhanation.recruits.network.MessageSplitGroup.class),
            registration(95, com.talhanation.recruits.network.MessageAssignNearbyRecruitsInGroup.class),
            registration(96, com.talhanation.recruits.network.MessageTeleportPlayer.class),
            registration(97, com.talhanation.recruits.network.MessageSendTreaty.class),
            registration(98, com.talhanation.recruits.network.MessageAnswerTreaty.class),
            registration(99, com.talhanation.recruits.network.MessageToClientOpenTreatyAnswerScreen.class),
            registration(100, com.talhanation.recruits.network.MessageToClientUpdateTreaties.class),
            registration(101, com.talhanation.recruits.network.MessageFaceCommand.class),
            registration(102, com.talhanation.recruits.network.MessageOpenGovernorScreen.class),
            registration(103, com.talhanation.recruits.network.MessageToClientUpdateGovernorScreen.class)
    );

    @Override
    public List<NetworkBootstrapSeams.MessageRegistration> orderedMessageTypes() {
        return ORDERED_MESSAGE_TYPES;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerAll(SimpleChannel channel) {
        for (NetworkBootstrapSeams.MessageRegistration registration : ORDERED_MESSAGE_TYPES) {
            CommonRegistry.registerMessage(channel, registration.id(), (Class) registration.messageClass());
        }
    }

    public int messageCount() {
        return ORDERED_MESSAGE_TYPES.size();
    }

    private static NetworkBootstrapSeams.MessageRegistration registration(int id, Class<?> messageClass) {
        return new NetworkBootstrapSeams.MessageRegistration(id, messageClass);
    }
}
