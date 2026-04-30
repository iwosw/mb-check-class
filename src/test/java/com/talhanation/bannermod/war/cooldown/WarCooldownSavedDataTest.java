package com.talhanation.bannermod.war.cooldown;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarCooldownSavedDataTest {

    @Test
    void grantingCooldownMarksSavedDataDirty() {
        WarCooldownSavedData data = new WarCooldownSavedData();

        assertFalse(data.isDirty());

        data.runtime().grant(UUID.randomUUID(), WarCooldownKind.LOST_TERRITORY_IMMUNITY, 120L);

        assertTrue(data.isDirty());
    }

    @Test
    void saveAndLoadRoundTripPreservesCooldownRecords() {
        WarCooldownSavedData data = new WarCooldownSavedData();
        UUID entity = UUID.randomUUID();
        data.runtime().grant(entity, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 240L);

        CompoundTag saved = data.save(new CompoundTag(), null);
        WarCooldownSavedData reloaded = WarCooldownSavedData.load(saved, null);

        assertTrue(saved.contains("WarCooldowns", Tag.TAG_LIST));
        assertTrue(reloaded.runtime().isActive(entity, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 100L));
        assertEquals(240L, reloaded.runtime().endsAtFor(entity, WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 100L));
    }

    @Test
    void runtimeAccessorExposesLoadedCooldownRuntime() {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        UUID entity = UUID.randomUUID();
        runtime.grant(entity, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 90L);

        CompoundTag tag = new CompoundTag();
        tag.put("WarCooldowns", runtime.toTag().getList("WarCooldowns", Tag.TAG_COMPOUND));
        WarCooldownSavedData loaded = WarCooldownSavedData.load(tag, null);

        assertEquals(1, loaded.runtime().all().size());
        assertTrue(loaded.runtime().byId(loaded.runtime().all().iterator().next().id()).isPresent());
    }
}
