package com.talhanation.recruits.testsupport;

import com.talhanation.recruits.network.MessageMovement;
import com.talhanation.recruits.network.MessageAttack;
import com.talhanation.recruits.network.MessageShields;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsFaction;
import com.talhanation.recruits.world.RecruitsPlayerInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.lang.reflect.Constructor;
import java.util.UUID;

public final class RecruitsFixtures {
    public static final UUID LEADER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID MEMBER_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID CLAIM_PLAYER_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID MOVEMENT_PLAYER_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID MOVEMENT_GROUP_UUID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID ATTACK_PLAYER_UUID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    public static final UUID ATTACK_GROUP_UUID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private RecruitsFixtures() {
    }

    public static RecruitsFaction sampleFaction() {
        CompoundTag banner = new CompoundTag();
        banner.putString("pattern", "bannermod:test_banner");

        RecruitsFaction faction = new RecruitsFaction("test-faction", "Captain Rowan", banner);
        faction.setTeamDisplayName("Test Faction");
        faction.setTeamLeaderID(LEADER_UUID);
        faction.setPlayers(2);
        faction.setNPCs(7);
        faction.setMaxPlayers(8);
        faction.setMaxNPCs(20);
        faction.setUnitColor((byte) 12);
        faction.setTeamColor(0x44AA88);
        faction.setMaxNPCsPerPlayer(5);
        faction.addPlayerAsJoinRequest("ScoutAda");
        faction.addPlayerAsJoinRequest("QuartermasterBo");
        faction.addMember(LEADER_UUID, "Captain Rowan");
        faction.addMember(MEMBER_UUID, "Scout Ada");
        return faction;
    }

    public static RecruitsClaim sampleClaim() {
        RecruitsFaction faction = sampleFaction();
        RecruitsClaim claim = createClaim("Northwatch", faction);
        claim.setPlayer(new RecruitsPlayerInfo(CLAIM_PLAYER_UUID, "ClaimKeeper", faction));
        claim.setBlockInteractionAllowed(true);
        claim.setBlockPlacementAllowed(true);
        claim.setBlockBreakingAllowed(false);
        claim.setAdminClaim(true);
        claim.isUnderSiege = true;
        claim.isRemoved = false;
        claim.addChunk(new ChunkPos(10, 20));
        claim.addChunk(new ChunkPos(11, 20));
        claim.setCenter(new ChunkPos(10, 20));
        claim.setHealth(37);
        claim.setSiegeSpeedPercent(1.25F);
        claim.defendingParties.add(faction);
        claim.attackingParties.add(sampleAttackingFaction());
        return claim;
    }

    public static MessageMovement sampleMovementMessage() {
        return new MessageMovement(MOVEMENT_PLAYER_UUID, 3, MOVEMENT_GROUP_UUID, 2, true);
    }

    public static MessageAttack sampleAttackMessage() {
        return new MessageAttack(ATTACK_PLAYER_UUID, ATTACK_GROUP_UUID);
    }

    public static MessageShields sampleShieldsMessage() {
        return new MessageShields(ATTACK_PLAYER_UUID, ATTACK_GROUP_UUID, true);
    }

    private static RecruitsFaction sampleAttackingFaction() {
        CompoundTag banner = new CompoundTag();
        banner.putString("pattern", "bannermod:attacker_banner");

        RecruitsFaction faction = new RecruitsFaction("attacker-faction", "Marshal Reed", banner);
        faction.setTeamDisplayName("Attacker Faction");
        faction.setTeamLeaderID(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        faction.addMember(faction.getTeamLeaderUUID(), "Marshal Reed");
        return faction;
    }

    private static RecruitsClaim createClaim(String name, RecruitsFaction faction) {
        try {
            Constructor<RecruitsClaim> constructor = RecruitsClaim.class.getDeclaredConstructor(UUID.class, String.class, RecruitsFaction.class);
            constructor.setAccessible(true);
            return constructor.newInstance(UUID.fromString("77777777-7777-7777-7777-777777777777"), name, faction);
        }
        catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to create test claim fixture", e);
        }
    }
}
