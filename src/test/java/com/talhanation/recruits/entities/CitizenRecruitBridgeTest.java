package com.talhanation.recruits.entities;

import com.talhanation.bannermod.citizen.CitizenPersistenceBridge;
import com.talhanation.bannermod.citizen.CitizenStateSnapshot;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitizenRecruitBridgeTest {

    private static final Path RECRUIT_SOURCE = Path.of("recruits/src/main/java/com/talhanation/recruits/entities/AbstractRecruitEntity.java");

    @Test
    void recruitOwnershipAndFollowStateCanFlowThroughTheCitizenBridgeWithoutChangingLegacyKeys() {
        UUID ownerId = UUID.randomUUID();
        CompoundTag legacy = new CompoundTag();
        legacy.putUUID("OwnerUUID", ownerId);
        legacy.putInt("FollowState", 2);
        legacy.putBoolean("isOwned", true);

        CitizenStateSnapshot snapshot = CitizenPersistenceBridge.fromRecruitLegacy(legacy);

        assertEquals(ownerId, snapshot.ownerUuid());
        assertEquals(2, snapshot.followState());
        assertEquals(true, snapshot.owned());
    }

    @Test
    void recruitWrapperSourceRoutesPersistenceThroughCitizenHelpers() throws Exception {
        String source = Files.readString(RECRUIT_SOURCE);

        assertTrue(source.contains("CitizenPersistenceBridge"));
        assertTrue(source.contains("hydrateCitizenStateFromLegacy"));
        assertTrue(source.contains("persistCitizenStateToLegacy"));
        assertTrue(source.contains("getCitizenCore()"));
    }

    @Test
    void recruitPersistenceHelperWritesLegacyKeysWithoutChangingWrapperFacingContract() {
        UUID ownerId = UUID.randomUUID();
        CitizenStateSnapshot snapshot = CitizenStateSnapshot.builder()
                .ownerUuid(ownerId)
                .followState(1)
                .owned(true)
                .build();

        CompoundTag legacy = CitizenPersistenceBridge.writeRecruitLegacy(snapshot, new CompoundTag());

        assertEquals(ownerId, legacy.getUUID("OwnerUUID"));
        assertEquals(1, legacy.getInt("FollowState"));
        assertEquals(true, legacy.getBoolean("isOwned"));
    }
}
