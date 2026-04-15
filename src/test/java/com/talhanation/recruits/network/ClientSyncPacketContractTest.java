package com.talhanation.recruits.network;

import com.talhanation.recruits.client.ClientManager;
import com.talhanation.recruits.testsupport.RecruitsFixtures;
import com.talhanation.recruits.world.RecruitsClaim;
import com.talhanation.recruits.world.RecruitsDiplomacyManager;
import com.talhanation.recruits.world.RecruitsFaction;
import com.talhanation.recruits.world.RecruitsGroup;
import com.talhanation.recruits.world.RecruitsRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientSyncPacketContractTest {

    @Test
    void resetClearsSynchronizedCachesWithoutTouchingLocalRoutes() throws Exception {
        ClientManager.recruitsClaims = new java.util.ArrayList<>(List.of(RecruitsFixtures.sampleClaim()));
        ClientManager.activeSiegeClaims = new HashMap<>(Map.of(ClientManager.recruitsClaims.get(0).getUUID(), ClientManager.recruitsClaims.get(0)));
        ClientManager.factions = new java.util.ArrayList<>(List.of(RecruitsFixtures.sampleFaction()));
        ClientManager.groups = new java.util.ArrayList<>(List.of(new RecruitsGroup("Alpha", UUID.randomUUID(), "Owner", 0)));
        ClientManager.diplomacyMap = new HashMap<>(Map.of("alpha", Map.of("beta", RecruitsDiplomacyManager.DiplomacyStatus.ALLY)));
        ClientManager.treaties = new HashMap<>(Map.of("alpha|beta", 99L));
        ClientManager.currentClaim = ClientManager.recruitsClaims.get(0);
        ClientManager.availableRecruitsToHire = 7;
        ClientManager.routesMap = new HashMap<>();
        RecruitsRoute localRoute = new RecruitsRoute("Local Only");
        ClientManager.routesMap.put(localRoute.getId().toString(), localRoute);

        Method reset = ClientManager.class.getDeclaredMethod("resetSynchronizedState");
        reset.setAccessible(true);
        reset.invoke(null);

        assertTrue(ClientManager.recruitsClaims.isEmpty());
        assertTrue(ClientManager.activeSiegeClaims.isEmpty());
        assertTrue(ClientManager.factions.isEmpty());
        assertTrue(ClientManager.groups.isEmpty());
        assertTrue(ClientManager.diplomacyMap.isEmpty());
        assertTrue(ClientManager.treaties.isEmpty());
        assertNull(ClientManager.currentClaim);
        assertEquals(0, ClientManager.availableRecruitsToHire);
        assertEquals(1, ClientManager.routesMap.size());
        assertTrue(ClientManager.routesMap.containsKey(localRoute.getId().toString()));
    }

    @Test
    void invalidOrMissingRoutePayloadsDoNotDecodeIntoSavableRoutes() throws Exception {
        Method decode = MessageToClientReceiveRoute.class.getDeclaredMethod("decodeRouteForClient", CompoundTag.class);
        decode.setAccessible(true);

        assertNull(decode.invoke(null, new CompoundTag()));

        CompoundTag missingName = new CompoundTag();
        missingName.putUUID("ID", UUID.randomUUID());
        assertNull(decode.invoke(null, missingName));

        CompoundTag missingId = new CompoundTag();
        missingId.putString("Name", "broken");
        assertNull(decode.invoke(null, missingId));
    }

    @Test
    void validUpdatePacketsRepopulateCriticalCachesAfterReset() throws Exception {
        Method reset = ClientManager.class.getDeclaredMethod("resetSynchronizedState");
        reset.setAccessible(true);
        reset.invoke(null);

        RecruitsClaim claim = RecruitsFixtures.sampleClaim();
        RecruitsFaction faction = RecruitsFixtures.sampleFaction();
        RecruitsGroup group = new RecruitsGroup("Bravo", UUID.randomUUID(), "Owner", 2);
        group.addMember(UUID.randomUUID());

        new MessageToClientUpdateClaims(List.of(claim), 4, 2, true, true, false, null).executeClientSide(null);
        new MessageToClientUpdateFactions(List.of(faction), faction.getStringID(), true, false, 12, 5, null).executeClientSide(null);
        new MessageToClientUpdateGroups(RecruitsGroup.listToNbt(List.of(group))).executeClientSide(null);
        new MessageToClientUpdateDiplomacyList(Map.of("alpha", Map.of("beta", RecruitsDiplomacyManager.DiplomacyStatus.ENEMY))).executeClientSide(null);
        new MessageToClientUpdateTreaties(Map.of("alpha|beta", 77L)).executeClientSide(null);
        new MessageToClientUpdateUnitInfo(true, 6).executeClientSide(null);

        assertEquals(1, ClientManager.recruitsClaims.size());
        assertEquals(claim.getUUID(), ClientManager.recruitsClaims.get(0).getUUID());
        assertEquals(1, ClientManager.factions.size());
        assertEquals(faction.getStringID(), ClientManager.factions.get(0).getStringID());
        assertEquals(1, ClientManager.groups.size());
        assertEquals(group.getUUID(), ClientManager.groups.get(0).getUUID());
        assertNotNull(ClientManager.diplomacyMap.get("alpha"));
        assertEquals(RecruitsDiplomacyManager.DiplomacyStatus.ENEMY, ClientManager.diplomacyMap.get("alpha").get("beta"));
        assertEquals(77L, ClientManager.treaties.get("alpha|beta"));
        assertTrue(ClientManager.configValueNobleNeedsVillagers);
        assertEquals(6, ClientManager.availableRecruitsToHire);
        assertFalse(ClientManager.activeSiegeClaims.isEmpty());
    }
}
