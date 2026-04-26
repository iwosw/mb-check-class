package com.talhanation.bannermod.war.registry;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Centralised leader-or-op authority check for political-entity mutations.
 *
 * <p>Mirrors the slash-command authority gate in {@code WarCommandSupport.isLeaderOrOp} so
 * client→server packets and command paths share one source of truth.</p>
 */
public final class PoliticalEntityAuthority {

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
}
