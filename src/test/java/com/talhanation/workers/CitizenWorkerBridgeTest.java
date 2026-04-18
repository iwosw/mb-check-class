package com.talhanation.workers;

import com.talhanation.bannermod.citizen.CitizenPersistenceBridge;
import com.talhanation.bannermod.citizen.CitizenStateSnapshot;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitizenWorkerBridgeTest {

    private static final Path WORKER_SOURCE = Path.of("src/main/java/com/talhanation/bannermod/entity/civilian/AbstractWorkerEntity.java");
    private static final Path BINDING_SOURCE = Path.of("src/main/java/com/talhanation/bannermod/entity/civilian/WorkerBindingResume.java");

    @Test
    void workerRecoveryAndBindingDataCanFlowThroughTheCitizenBridge() {
        UUID ownerId = UUID.randomUUID();
        UUID boundWorkAreaId = UUID.randomUUID();
        CompoundTag legacy = new CompoundTag();
        legacy.putUUID("OwnerUUID", ownerId);
        legacy.putInt("FollowState", 6);
        legacy.putBoolean("isOwned", true);
        legacy.putUUID("boundWorkArea", boundWorkAreaId);

        CitizenStateSnapshot snapshot = CitizenPersistenceBridge.fromWorkerLegacy(legacy);

        assertEquals(ownerId, snapshot.ownerUuid());
        assertEquals(6, snapshot.followState());
        assertEquals(true, snapshot.owned());
        assertEquals(boundWorkAreaId, snapshot.boundWorkAreaUuid());
    }

    @Test
    void workerWrapperSourceRoutesBindingAndPersistenceThroughCitizenHelpers() throws Exception {
        String workerSource = Files.readString(WORKER_SOURCE);
        String bindingSource = Files.readString(BINDING_SOURCE);

        assertTrue(workerSource.contains("CitizenPersistenceBridge"));
        assertTrue(workerSource.contains("hydrateCitizenStateFromLegacy"));
        assertTrue(workerSource.contains("persistCitizenStateToLegacy"));
        assertTrue(workerSource.contains("getCitizenCore()"));
        assertTrue(bindingSource.contains("CitizenCore") || bindingSource.contains("boundWorkArea"));
    }

    @Test
    void workerPersistenceBridgeWritesLegacyKeysBackOut() {
        UUID ownerId = UUID.randomUUID();
        UUID boundWorkAreaId = UUID.randomUUID();
        CitizenStateSnapshot snapshot = CitizenStateSnapshot.builder()
                .ownerUuid(ownerId)
                .followState(6)
                .owned(true)
                .working(true)
                .boundWorkAreaUuid(boundWorkAreaId)
                .build();

        CompoundTag legacy = CitizenPersistenceBridge.writeWorkerLegacy(snapshot, new CompoundTag());

        assertEquals(ownerId, legacy.getUUID("OwnerUUID"));
        assertEquals(6, legacy.getInt("FollowState"));
        assertEquals(true, legacy.getBoolean("isOwned"));
        assertEquals(boundWorkAreaId, legacy.getUUID("boundWorkArea"));
    }
}
