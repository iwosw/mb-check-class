package com.talhanation.bannermod.war.registry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.UUID;

/** Centralized political authority checks for command, packet, and UI paths. */
public final class PoliticalEntityAuthority {
    public static final String DENIAL_NOT_AUTHORIZED = "Only the political entity leader, an authorized republic co-leader, or an op can do that.";
    public static final String DENIAL_LEADER_ONLY = "Only the political entity leader (or an op) can do that.";
    public static final String DENIAL_NOT_AUTHORIZED_KEY = "gui.bannermod.war.denial.not_authorized";
    public static final String DENIAL_LEADER_ONLY_KEY = "gui.bannermod.war.denial.leader_only";
    public static final String DENIAL_NO_STATE_KEY = "gui.bannermod.war.denial.no_state";
    public static final String DENIAL_CO_LEADER_MONARCHY_KEY = "gui.bannermod.war.denial.co_leader_monarchy";
    public static final String DENIAL_OUTSIDER_REPUBLIC_KEY = "gui.bannermod.war.denial.outsider_republic";

    private PoliticalEntityAuthority() {
    }

    public static boolean isLeaderOrOp(@Nullable ServerPlayer player, @Nullable PoliticalEntityRecord record) {
        if (player == null || record == null) {
            return false;
        }
        if (player.hasPermissions(2)) {
            return true;
        }
        UUID leader = record.leaderUuid();
        return leader != null && leader.equals(player.getUUID());
    }

    /** Auth check that takes the actor uuid and op-flag directly so it is unit-testable. */
    public static boolean isLeaderOrOp(@Nullable UUID actorUuid, boolean opPrivilege, @Nullable PoliticalEntityRecord record) {
        if (record == null) {
            return false;
        }
        if (opPrivilege) {
            return true;
        }
        UUID leader = record.leaderUuid();
        return leader != null && leader.equals(actorUuid);
    }

    /**
     * Government-form aware variant. {@link GovernmentForm#REPUBLIC} extends authority to
     * registered co-leaders; {@link GovernmentForm#MONARCHY} keeps it leader-only. Op
     * permission still overrides everything so admins can recover from misconfiguration.
     */
    public static boolean canAct(@Nullable ServerPlayer player, @Nullable PoliticalEntityRecord record) {
        if (player == null || record == null) {
            return false;
        }
        if (player.hasPermissions(2)) {
            return true;
        }
        return canAct(player.getUUID(), false, record);
    }

    /** Pure variant of {@link #canAct(ServerPlayer, PoliticalEntityRecord)} for tests. */
    public static boolean canAct(@Nullable UUID actorUuid, boolean opPrivilege, @Nullable PoliticalEntityRecord record) {
        if (record == null) {
            return false;
        }
        if (opPrivilege) {
            return true;
        }
        if (actorUuid == null) {
            return false;
        }
        UUID leader = record.leaderUuid();
        if (leader != null && leader.equals(actorUuid)) {
            return true;
        }
        if (record.governmentForm() != null && record.governmentForm().coLeadersShareAuthority()) {
            for (UUID coLeader : record.coLeaderUuids()) {
                if (coLeader != null && coLeader.equals(actorUuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Component denialReason(@Nullable UUID actorUuid, boolean opPrivilege, @Nullable PoliticalEntityRecord record) {
        return Component.translatable(denialReasonKey(actorUuid, opPrivilege, record));
    }

    public static String denialReasonKey(@Nullable UUID actorUuid, boolean opPrivilege, @Nullable PoliticalEntityRecord record) {
        if (record == null) {
            return DENIAL_NO_STATE_KEY;
        }
        if (canAct(actorUuid, opPrivilege, record)) {
            return "gui.bannermod.war.denial.allowed";
        }
        if (actorUuid != null && record.coLeaderUuids().contains(actorUuid)
                && (record.governmentForm() == null || !record.governmentForm().coLeadersShareAuthority())) {
            return DENIAL_CO_LEADER_MONARCHY_KEY;
        }
        if (record.governmentForm() != null && record.governmentForm().coLeadersShareAuthority()) {
            return DENIAL_OUTSIDER_REPUBLIC_KEY;
        }
        return DENIAL_LEADER_ONLY_KEY;
    }
}
