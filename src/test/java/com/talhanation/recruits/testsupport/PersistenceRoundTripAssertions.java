package com.talhanation.recruits.testsupport;

import com.talhanation.recruits.world.RecruitsDiplomacyManager;
import com.talhanation.recruits.world.RecruitsFaction;
import com.talhanation.recruits.world.RecruitsGroup;
import com.talhanation.recruits.world.RecruitsTeamSaveData;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class PersistenceRoundTripAssertions {

    private PersistenceRoundTripAssertions() {
    }

    public static <T> T roundTrip(T original, Function<CompoundTag, T> loader, Function<T, CompoundTag> saver) {
        CompoundTag written = saver.apply(original);
        T restored = loader.apply(written);
        assertNotNull(restored);
        return restored;
    }

    public static void assertTeamsEqual(Map<String, RecruitsFaction> expected, Map<String, RecruitsFaction> actual) {
        assertEquals(expected.keySet(), actual.keySet());
        expected.forEach((teamId, expectedFaction) -> {
            RecruitsFaction actualFaction = actual.get(teamId);
            assertNotNull(actualFaction);
            NbtRoundTripAssertions.assertFactionRoundTrip(expectedFaction);
            assertEquals(expectedFaction.getStringID(), actualFaction.getStringID());
            assertEquals(expectedFaction.getTeamDisplayName(), actualFaction.getTeamDisplayName());
            assertEquals(expectedFaction.getTeamLeaderUUID(), actualFaction.getTeamLeaderUUID());
            assertEquals(expectedFaction.getTeamLeaderName(), actualFaction.getTeamLeaderName());
            assertEquals(expectedFaction.getBanner(), actualFaction.getBanner());
            assertEquals(expectedFaction.getJoinRequests(), actualFaction.getJoinRequests());
            assertEquals(expectedFaction.getMembers().size(), actualFaction.getMembers().size());
            assertEquals(expectedFaction.getPlayers(), actualFaction.getPlayers());
            assertEquals(expectedFaction.getNPCs(), actualFaction.getNPCs());
            assertEquals(expectedFaction.getMaxPlayers(), actualFaction.getMaxPlayers());
            assertEquals(expectedFaction.getMaxNPCs(), actualFaction.getMaxNPCs());
            assertEquals(expectedFaction.getUnitColor(), actualFaction.getUnitColor());
            assertEquals(expectedFaction.getTeamColor(), actualFaction.getTeamColor());
            assertEquals(expectedFaction.getMaxNPCsPerPlayer(), actualFaction.getMaxNPCsPerPlayer());

            for (int i = 0; i < expectedFaction.getMembers().size(); i++) {
                assertEquals(expectedFaction.getMembers().get(i).getUUID(), actualFaction.getMembers().get(i).getUUID());
                assertEquals(expectedFaction.getMembers().get(i).getName(), actualFaction.getMembers().get(i).getName());
            }
        });
    }

    public static void assertGroupsEqual(List<RecruitsGroup> expected, List<RecruitsGroup> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            RecruitsGroup expectedGroup = expected.get(i);
            RecruitsGroup actualGroup = actual.get(i);
            assertNotNull(actualGroup);
            assertEquals(expectedGroup.getUUID(), actualGroup.getUUID());
            assertEquals(expectedGroup.getName(), actualGroup.getName());
            assertEquals(expectedGroup.getPlayerUUID(), actualGroup.getPlayerUUID());
            assertEquals(expectedGroup.getPlayerName(), actualGroup.getPlayerName());
            assertEquals(expectedGroup.getSize(), actualGroup.getSize());
            assertEquals(expectedGroup.getImage(), actualGroup.getImage());
            assertEquals(expectedGroup.removed, actualGroup.removed);
            assertEquals(expectedGroup.leaderUUID, actualGroup.leaderUUID);
            assertEquals(expectedGroup.members, actualGroup.members);
        }
    }

    public static void assertUuidMapEquals(Map<UUID, UUID> expected, Map<UUID, UUID> actual) {
        assertEquals(expected, actual);
    }

    public static void assertDiplomacyEquals(Map<String, Map<String, RecruitsDiplomacyManager.DiplomacyStatus>> expected,
                                             Map<String, Map<String, RecruitsDiplomacyManager.DiplomacyStatus>> actual) {
        assertEquals(expected.keySet(), actual.keySet());
        expected.forEach((team, expectedRelations) -> assertEquals(expectedRelations, actual.get(team)));
    }

    public static CompoundTag save(RecruitsTeamSaveData data) {
        return data.save(new CompoundTag());
    }

    public static void assertCountMapContains(Map<UUID, Integer> actual, UUID playerId, int expectedCount) {
        assertTrue(actual.containsKey(playerId));
        assertEquals(expectedCount, actual.get(playerId));
    }
}
