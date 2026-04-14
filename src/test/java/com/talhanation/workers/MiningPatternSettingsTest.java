package com.talhanation.workers;

import com.talhanation.bannerlord.entity.civilian.workarea.MiningPatternSettings;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningPatternSettingsTest {

    @Test
    void tunnelSettingsKeepCrossSectionDescentAndFloorChoice() {
        MiningPatternSettings settings = MiningPatternSettings.tunnel(3, 4, -6, true, 2);

        assertEquals(MiningPatternSettings.Mode.TUNNEL, settings.mode());
        assertEquals(3, settings.width());
        assertEquals(4, settings.height());
        assertEquals(-6, settings.heightOffset());
        assertTrue(settings.closeFloor());
        assertEquals(2, settings.descentStep());
    }

    @Test
    void branchSettingsClampSpacingAndLengthToSaneBounds() {
        MiningPatternSettings settings = MiningPatternSettings.branch(3, 3, 12, false, 0, -9);

        assertEquals(MiningPatternSettings.Mode.BRANCH, settings.mode());
        assertEquals(1, settings.branchSpacing());
        assertEquals(1, settings.branchLength());
        assertFalse(settings.closeFloor());
    }

    @Test
    void settingsRoundTripThroughNbtFriendlyPrimitives() {
        MiningPatternSettings original = new MiningPatternSettings(
                MiningPatternSettings.Mode.BRANCH,
                5,
                4,
                -8,
                false,
                7,
                11,
                3
        );

        CompoundTag tag = original.toTag();
        MiningPatternSettings restored = MiningPatternSettings.fromTag(tag);

        assertEquals(original, restored);
    }
}
