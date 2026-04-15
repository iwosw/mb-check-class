package com.talhanation.recruits.world;

import com.talhanation.recruits.testsupport.PersistenceRoundTripAssertions;
import com.talhanation.recruits.testsupport.RecruitsFixtures;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import net.minecraft.nbt.CompoundTag;

class RecruitsWorldSaveDataSerializationTest {

    private static final UUID GROUP_OWNER_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID GROUP_MEMBER_ONE = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID GROUP_MEMBER_TWO = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID GROUP_UUID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID LEADER_UUID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID OLD_GROUP_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    private static final UUID RECRUIT_REDIRECT_UUID = UUID.fromString("12121212-1212-1212-1212-121212121212");
    private static final UUID PLAYER_ONE_UUID = UUID.fromString("34343434-3434-3434-3434-343434343434");
    private static final UUID PLAYER_TWO_UUID = UUID.fromString("56565656-5656-5656-5656-565656565656");

    @Test
    void preservesTeamsGroupsDiplomacyTreatiesAndPlayerUnitCountsAcrossRoundTrip() {
        RecruitsTeamSaveData teamSaveData = new RecruitsTeamSaveData();
        Map<String, RecruitsFaction> teams = new HashMap<>();
        RecruitsFaction faction = RecruitsFixtures.sampleFaction();
        teams.put(faction.getStringID(), faction);
        teamSaveData.setTeams(teams);

        RecruitsGroupsSaveData groupsSaveData = new RecruitsGroupsSaveData();
        RecruitsGroup group = sampleGroup();
        groupsSaveData.setAllGroups(new ArrayList<>(List.of(group)));

        RecruitsDiplomacySaveData.diplomacyMap.clear();
        RecruitsDiplomacySaveData diplomacySaveData = new RecruitsDiplomacySaveData();
        diplomacySaveData.setRelation("test-faction", "ally-faction", RecruitsDiplomacyManager.DiplomacyStatus.ALLY.getByteValue());

        RecruitsTreatySaveData treatySaveData = new RecruitsTreatySaveData();
        Map<String, Long> treaties = new HashMap<>();
        treaties.put("test-faction|ally-faction", 7200L);
        treatySaveData.setTreaties(treaties);

        RecruitPlayerUnitSaveData.recruitCountMap.clear();
        RecruitPlayerUnitSaveData playerUnitSaveData = new RecruitPlayerUnitSaveData();
        playerUnitSaveData.setRecruitCount(PLAYER_ONE_UUID, 4);
        playerUnitSaveData.setRecruitCount(PLAYER_TWO_UUID, 9);

        RecruitsTeamSaveData restoredTeams = PersistenceRoundTripAssertions.roundTrip(teamSaveData, RecruitsTeamSaveData::load, PersistenceRoundTripAssertions::save);
        RecruitsGroupsSaveData restoredGroups = PersistenceRoundTripAssertions.roundTrip(groupsSaveData, RecruitsGroupsSaveData::load, data -> data.save(new net.minecraft.nbt.CompoundTag()));
        RecruitsDiplomacySaveData restoredDiplomacy = PersistenceRoundTripAssertions.roundTrip(diplomacySaveData, RecruitsDiplomacySaveData::load, data -> data.save(new net.minecraft.nbt.CompoundTag()));
        RecruitsTreatySaveData restoredTreaties = PersistenceRoundTripAssertions.roundTrip(treatySaveData, RecruitsTreatySaveData::load, data -> data.save(new net.minecraft.nbt.CompoundTag()));
        RecruitPlayerUnitSaveData restoredPlayerUnits = PersistenceRoundTripAssertions.roundTrip(playerUnitSaveData, RecruitPlayerUnitSaveData::load, data -> data.save(new net.minecraft.nbt.CompoundTag()));

        PersistenceRoundTripAssertions.assertTeamsEqual(teams, restoredTeams.getTeams());
        PersistenceRoundTripAssertions.assertGroupsEqual(List.of(group), restoredGroups.getAllGroups());
        PersistenceRoundTripAssertions.assertDiplomacyEquals(Map.of("test-faction", Map.of("ally-faction", RecruitsDiplomacyManager.DiplomacyStatus.ALLY)), restoredDiplomacy.getDiplomacyMap());
        assertEquals(treaties, restoredTreaties.getTreaties());
        PersistenceRoundTripAssertions.assertCountMapContains(restoredPlayerUnits.getRecruitCountMap(), PLAYER_ONE_UUID, 4);
        PersistenceRoundTripAssertions.assertCountMapContains(restoredPlayerUnits.getRecruitCountMap(), PLAYER_TWO_UUID, 9);
    }

    @Test
    void preservesRedirectMapsJoinRequestsAndTreatyExpiryValuesExactly() {
        RecruitsGroupsSaveData groupsSaveData = new RecruitsGroupsSaveData();
        groupsSaveData.setAllGroups(new ArrayList<>(List.of(sampleGroup())));
        groupsSaveData.setRedirects(new HashMap<>(Map.of(OLD_GROUP_UUID, GROUP_UUID)));
        groupsSaveData.setRecruitRedirects(new HashMap<>(Map.of(RECRUIT_REDIRECT_UUID, GROUP_UUID)));

        RecruitsTeamSaveData teamSaveData = new RecruitsTeamSaveData();
        RecruitsFaction faction = RecruitsFixtures.sampleFaction();
        teamSaveData.setTeams(new HashMap<>(Map.of(faction.getStringID(), faction)));

        RecruitsTreatySaveData treatySaveData = new RecruitsTreatySaveData();
        treatySaveData.setTreaties(new HashMap<>(Map.of(
                "test-faction|ally-faction", Long.MAX_VALUE - 10,
                "test-faction|enemy-faction", 1L
        )));

        RecruitsGroupsSaveData restoredGroups = RecruitsGroupsSaveData.load(groupsSaveData.save(new net.minecraft.nbt.CompoundTag()));
        RecruitsTeamSaveData restoredTeams = RecruitsTeamSaveData.load(teamSaveData.save(new net.minecraft.nbt.CompoundTag()));
        RecruitsTreatySaveData restoredTreaties = RecruitsTreatySaveData.load(treatySaveData.save(new net.minecraft.nbt.CompoundTag()));

        PersistenceRoundTripAssertions.assertUuidMapEquals(groupsSaveData.getRedirects(), restoredGroups.getRedirects());
        PersistenceRoundTripAssertions.assertUuidMapEquals(groupsSaveData.getRecruitRedirects(), restoredGroups.getRecruitRedirects());
        assertEquals(faction.getJoinRequests(), restoredTeams.getTeams().get(faction.getStringID()).getJoinRequests());
        assertEquals(treatySaveData.getTreaties(), restoredTreaties.getTreaties());
    }

    @Test
    void emptyOrFreshPayloadsDoNotClearPreviouslyLoadedStateDuringRoundTripSetup() {
        RecruitsDiplomacySaveData.diplomacyMap.clear();
        CompoundTag diplomacyTag = new CompoundTag();
        diplomacyTag.put("teams", RecruitsDiplomacyManager.mapToNbt(Map.of("alpha", Map.of("beta", RecruitsDiplomacyManager.DiplomacyStatus.ENEMY))));
        RecruitsDiplomacySaveData loadedDiplomacy = RecruitsDiplomacySaveData.load(diplomacyTag);
        assertEquals(RecruitsDiplomacyManager.DiplomacyStatus.ENEMY, loadedDiplomacy.getDiplomacyMap().get("alpha").get("beta"));

        RecruitPlayerUnitSaveData.recruitCountMap.clear();
        CompoundTag countTag = new CompoundTag();
        CompoundTag recruitCountsTag = new CompoundTag();
        recruitCountsTag.putInt(PLAYER_ONE_UUID.toString(), 6);
        countTag.put("recruitCounts", recruitCountsTag);
        RecruitPlayerUnitSaveData loadedCounts = RecruitPlayerUnitSaveData.load(countTag);
        assertEquals(6, loadedCounts.getRecruitCountMap().get(PLAYER_ONE_UUID));

        RecruitsTeamSaveData loadedTeams = RecruitsTeamSaveData.load(new RecruitsTeamSaveData().save(new net.minecraft.nbt.CompoundTag()));
        assertFalse(loadedTeams.getTeams().containsKey("missing-team"));

        new RecruitsDiplomacySaveData();
        new RecruitPlayerUnitSaveData();

        assertTrue(loadedDiplomacy.getDiplomacyMap().containsKey("alpha"));
        assertEquals(6, loadedCounts.getRecruitCountMap().get(PLAYER_ONE_UUID));
    }

    private static RecruitsGroup sampleGroup() {
        RecruitsGroup group = new RecruitsGroup("North Squad", GROUP_OWNER_UUID, "Captain Cedar", 3);
        group.setUUID(GROUP_UUID);
        group.addMember(GROUP_MEMBER_ONE);
        group.addMember(GROUP_MEMBER_TWO);
        group.leaderUUID = LEADER_UUID;
        group.removed = true;
        group.upkeep = new BlockPos(1, 64, 1);
        return group;
    }
}
