package com.talhanation.bannermod.entity.military.perks;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerkProgressNbtRoundTripTest {

    @Test
    void emptyProgressRoundTripsCleanly() {
        PerkProgress original = new PerkProgress();
        PerkProgress restored = new PerkProgress();

        restored.fromNbt(original.toNbt());

        assertEquals(0, restored.getAvailablePoints());
        assertTrue(restored.getOwnedPerks().isEmpty());
    }

    @Test
    void grantedPointsAndUnlockedPerkSurviveSaveLoad() {
        PerkProgress original = new PerkProgress();
        original.grantPoints(3);
        PerkNode node = PerkRegistry.get("universal/toughness_i").orElseThrow();
        assertEquals(PerkProgress.UnlockResult.OK, original.unlock(node));
        assertEquals(2, original.getAvailablePoints());
        assertTrue(original.isOwned(node.id()));

        CompoundTag tag = original.toNbt();
        PerkProgress restored = new PerkProgress();
        restored.fromNbt(tag);

        assertEquals(2, restored.getAvailablePoints());
        assertTrue(restored.isOwned(node.id()));
    }

    @Test
    void unknownPerkIdsAreDroppedSilentlyOnLoad() {
        CompoundTag forged = new CompoundTag();
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        list.add(net.minecraft.nbt.StringTag.valueOf("does_not_exist"));
        list.add(net.minecraft.nbt.StringTag.valueOf("universal/toughness_i"));
        forged.put("OwnedPerks", list);
        forged.putInt("AvailablePoints", 5);

        PerkProgress restored = new PerkProgress();
        restored.fromNbt(forged);

        assertEquals(5, restored.getAvailablePoints());
        assertTrue(restored.isOwned("universal/toughness_i"));
        assertFalse(restored.isOwned("does_not_exist"));
    }

    @Test
    void respecRefundsPointsAndClearsOwnedSet() {
        PerkProgress progress = new PerkProgress();
        progress.grantPoints(2);
        PerkNode node = PerkRegistry.get("swordsman/iron_grip_i").orElseThrow();
        assertEquals(PerkProgress.UnlockResult.OK, progress.unlock(node));
        assertEquals(1, progress.getAvailablePoints());

        int refunded = progress.respec();

        assertEquals(node.pointCost(), refunded);
        assertEquals(2, progress.getAvailablePoints());
        assertTrue(progress.getOwnedPerks().isEmpty());
    }

    @Test
    void unlockBlocksWhenPointsAreInsufficient() {
        PerkProgress progress = new PerkProgress();
        PerkNode node = PerkRegistry.get("universal/toughness_i").orElseThrow();

        assertEquals(PerkProgress.UnlockResult.NOT_ENOUGH_POINTS, progress.unlock(node));
        assertFalse(progress.isOwned(node.id()));
    }
}
