package com.talhanation.recruits.testsupport;

import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsFaction;
import com.talhanation.recruits.world.RecruitsPlayerInfo;
import net.minecraft.nbt.CompoundTag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class NbtRoundTripAssertions {

    private NbtRoundTripAssertions() {
    }

    public static RecruitsFaction assertFactionRoundTrip(RecruitsFaction original) {
        CompoundTag nbt = original.toNBT();
        RecruitsFaction restored = RecruitsFaction.fromNBT(nbt);

        assertNotNull(restored);
        assertEquals(original.getStringID(), restored.getStringID());
        assertEquals(original.getTeamDisplayName(), restored.getTeamDisplayName());
        assertEquals(original.getTeamLeaderUUID(), restored.getTeamLeaderUUID());
        assertEquals(original.getTeamLeaderName(), restored.getTeamLeaderName());
        assertEquals(original.getBanner(), restored.getBanner());
        assertEquals(original.getJoinRequests(), restored.getJoinRequests());
        assertEquals(original.getPlayers(), restored.getPlayers());
        assertEquals(original.getNPCs(), restored.getNPCs());
        assertEquals(original.getMaxPlayers(), restored.getMaxPlayers());
        assertEquals(original.getMaxNPCs(), restored.getMaxNPCs());
        assertEquals(original.getUnitColor(), restored.getUnitColor());
        assertEquals(original.getTeamColor(), restored.getTeamColor());
        assertEquals(original.getMaxNPCsPerPlayer(), restored.getMaxNPCsPerPlayer());
        assertEquals(original.getMembers().size(), restored.getMembers().size());

        for (int i = 0; i < original.getMembers().size(); i++) {
            RecruitsPlayerInfo originalMember = original.getMembers().get(i);
            RecruitsPlayerInfo restoredMember = restored.getMembers().get(i);
            assertEquals(originalMember.getUUID(), restoredMember.getUUID());
            assertEquals(originalMember.getName(), restoredMember.getName());
            assertEquals(originalMember.isOnline(), restoredMember.isOnline());
        }

        return restored;
    }

    public static RecruitsClaim assertClaimRoundTrip(RecruitsClaim original) {
        CompoundTag nbt = original.toNBT();
        RecruitsClaim restored = RecruitsClaim.fromNBT(nbt);

        assertNotNull(restored);
        assertEquals(original.getUUID(), restored.getUUID());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getOwnerFactionStringID(), restored.getOwnerFactionStringID());
        assertEquals(original.getOwnerFaction().getTeamDisplayName(), restored.getOwnerFaction().getTeamDisplayName());
        assertEquals(original.getClaimedChunks(), restored.getClaimedChunks());
        assertEquals(original.getCenter(), restored.getCenter());
        assertEquals(original.getHealth(), restored.getHealth());
        assertEquals(original.getSiegeSpeedPercent(), restored.getSiegeSpeedPercent());
        assertEquals(original.isBlockInteractionAllowed(), restored.isBlockInteractionAllowed());
        assertEquals(original.isBlockPlacementAllowed(), restored.isBlockPlacementAllowed());
        assertEquals(original.isBlockBreakingAllowed(), restored.isBlockBreakingAllowed());
        assertEquals(original.isAdmin, restored.isAdmin);
        assertEquals(original.isUnderSiege, restored.isUnderSiege);
        assertEquals(original.isRemoved, restored.isRemoved);
        assertNotNull(restored.getPlayerInfo());
        assertEquals(original.getPlayerInfo().getUUID(), restored.getPlayerInfo().getUUID());
        assertEquals(original.getPlayerInfo().getName(), restored.getPlayerInfo().getName());
        assertEquals(original.defendingParties.size(), restored.defendingParties.size());
        assertEquals(original.attackingParties.size(), restored.attackingParties.size());
        assertTrue(restored.defendingParties.stream().anyMatch(faction -> faction.equalsFaction(original.getOwnerFaction())));
        assertTrue(restored.attackingParties.stream().anyMatch(faction -> faction.equalsFaction(original.attackingParties.get(0))));

        return restored;
    }
}
