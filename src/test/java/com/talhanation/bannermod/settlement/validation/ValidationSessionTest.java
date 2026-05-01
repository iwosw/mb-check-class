package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationSessionTest {
    @Test
    void guidePreviewFlagRoundTripsThroughTag() {
        ValidationSession session = new ValidationSession(
                UUID.randomUUID(),
                SurveyorMode.HOUSE,
                new BlockPos(12, 70, -4),
                List.of(new ZoneSelection(ZoneRole.INTERIOR, new BlockPos(10, 70, -6), new BlockPos(14, 73, -2), new BlockPos(14, 73, -2))),
                false);

        CompoundTag tag = session.toTag();
        ValidationSession restored = ValidationSession.fromTag(tag);

        assertFalse(restored.showGuidePreview());
    }

    @Test
    void guidePreviewDefaultsToVisibleForLegacySessions() {
        ValidationSession session = new ValidationSession(
                UUID.randomUUID(),
                SurveyorMode.FARM,
                new BlockPos(0, 64, 0),
                List.of(),
                false);

        CompoundTag tag = session.toTag();
        tag.remove("ShowGuidePreview");

        ValidationSession restored = ValidationSession.fromTag(tag);

        assertTrue(restored.showGuidePreview());
    }
}
