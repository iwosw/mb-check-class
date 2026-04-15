package com.talhanation.recruits.world;

import com.talhanation.recruits.testsupport.RecruitsFixtures;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitsManagerPersistenceMutationTest {

    private static final UUID UNIT_PLAYER_UUID = UUID.fromString("78787878-7878-7878-7878-787878787878");
    private static final UUID GROUP_PLAYER_UUID = UUID.fromString("89898989-8989-8989-8989-898989898989");

    @Test
    void claimMutationsPersistIntoSaveDataAndMarkDirty() throws Exception {
        RecruitsClaimManager manager = new RecruitsClaimManager();
        RecruitsClaimSaveData saveData = new RecruitsClaimSaveData();
        RecruitsClaim claim = RecruitsFixtures.sampleClaim();

        Field claimsField = RecruitsClaimManager.class.getDeclaredField("claims");
        claimsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<net.minecraft.world.level.ChunkPos, RecruitsClaim> claims = (Map<net.minecraft.world.level.ChunkPos, RecruitsClaim>) claimsField.get(manager);
        claim.getClaimedChunks().forEach(chunkPos -> claims.put(chunkPos, claim));

        Method persistClaims = RecruitsClaimManager.class.getDeclaredMethod("persistClaims", RecruitsClaimSaveData.class);
        persistClaims.setAccessible(true);
        persistClaims.invoke(manager, saveData);

        assertTrue(saveData.isDirty());
        assertEquals(1, saveData.getAllClaims().size());
        assertEquals(claim.getUUID(), saveData.getAllClaims().get(0).getUUID());

        claims.clear();
        persistClaims.invoke(manager, saveData);

        assertTrue(saveData.isDirty());
        assertTrue(saveData.getAllClaims().isEmpty());
    }

    @Test
    void playerUnitMutationsPersistImmediatelyWithoutWaitingForLaterWorldSave() throws Exception {
        RecruitsPlayerUnitManager manager = new RecruitsPlayerUnitManager();
        RecruitPlayerUnitSaveData saveData = new RecruitPlayerUnitSaveData();

        Method setRecruitCount = RecruitsPlayerUnitManager.class.getDeclaredMethod("setRecruitCount", UUID.class, int.class, RecruitPlayerUnitSaveData.class);
        Method addRecruits = RecruitsPlayerUnitManager.class.getDeclaredMethod("addRecruits", UUID.class, int.class, RecruitPlayerUnitSaveData.class);
        Method removeRecruits = RecruitsPlayerUnitManager.class.getDeclaredMethod("removeRecruits", UUID.class, int.class, RecruitPlayerUnitSaveData.class);
        setRecruitCount.setAccessible(true);
        addRecruits.setAccessible(true);
        removeRecruits.setAccessible(true);

        setRecruitCount.invoke(manager, UNIT_PLAYER_UUID, 3, saveData);
        addRecruits.invoke(manager, UNIT_PLAYER_UUID, 4, saveData);
        removeRecruits.invoke(manager, UNIT_PLAYER_UUID, 2, saveData);

        assertTrue(saveData.isDirty());
        assertEquals(5, saveData.getRecruitCountMap().get(UNIT_PLAYER_UUID));

        RecruitPlayerUnitSaveData restored = RecruitPlayerUnitSaveData.load(saveData.save(new net.minecraft.nbt.CompoundTag()));
        assertEquals(5, restored.getRecruitCountMap().get(UNIT_PLAYER_UUID));
    }

    @Test
    void baseGroupsAreCreatedOnceAndSurviveSaveLoadBoundaries() throws Exception {
        RecruitsGroupsManager manager = new RecruitsGroupsManager();
        RecruitsGroupsSaveData saveData = new RecruitsGroupsSaveData();

        Method getOrCreatePlayerGroups = RecruitsGroupsManager.class.getDeclaredMethod("getOrCreatePlayerGroups", UUID.class, String.class, RecruitsGroupsSaveData.class);
        getOrCreatePlayerGroups.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<RecruitsGroup> first = (List<RecruitsGroup>) getOrCreatePlayerGroups.invoke(manager, GROUP_PLAYER_UUID, "Stable Owner", saveData);
        @SuppressWarnings("unchecked")
        List<RecruitsGroup> second = (List<RecruitsGroup>) getOrCreatePlayerGroups.invoke(manager, GROUP_PLAYER_UUID, "Stable Owner", saveData);

        assertEquals(4, first.size());
        assertEquals(4, second.size());
        assertTrue(saveData.isDirty());
        assertEquals(first.stream().map(RecruitsGroup::getUUID).toList(), second.stream().map(RecruitsGroup::getUUID).toList());

        RecruitsGroupsSaveData restored = RecruitsGroupsSaveData.load(saveData.save(new net.minecraft.nbt.CompoundTag()));
        assertEquals(4, restored.getAllGroups().size());
        assertFalse(restored.getAllGroups().isEmpty());
        assertNotNull(restored.getAllGroups().get(0).getPlayerUUID());
    }
}
