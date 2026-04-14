package com.talhanation.workers;

import com.talhanation.bannerlord.entity.civilian.workarea.MiningPatternContract;
import com.talhanation.bannerlord.entity.civilian.workarea.MiningPatternSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MiningAreaPatternContractTest {

    @Test
    void tunnelAndBranchModesUseFixedInternalSegmentBudgetInsteadOfDepthSize() {
        assertEquals(16, MiningPatternContract.resolveTotalSegments(MiningPatternContract.PatternMode.TUNNEL, 99));
        assertEquals(16, MiningPatternContract.resolveTotalSegments(MiningPatternContract.PatternMode.BRANCH, 2));
        assertEquals(7, MiningPatternContract.resolveTotalSegments(MiningPatternContract.PatternMode.CUSTOM, 7));
    }

    @Test
    void applyPatternSettingsKeepsLegacyDepthFieldUntouchedWhileUpdatingAuthoredPatternSettings() {
        MiningPatternContract.PatternApplication applied = MiningPatternContract.projectPatternSettings(new MiningPatternSettings(
                MiningPatternSettings.Mode.BRANCH,
                5,
                4,
                -3,
                false,
                7,
                11,
                2
        ), 12);

        assertEquals(5, applied.widthSize());
        assertEquals(4, applied.heightSize());
        assertEquals(12, applied.depthSize());
        assertEquals(-3, applied.heightOffset());
        assertFalse(applied.closeFloor());
        assertEquals(7, applied.branchSpacing());
        assertEquals(11, applied.branchLength());
        assertEquals(2, applied.descentStep());
        assertEquals(MiningPatternContract.PatternMode.BRANCH, applied.miningMode());
        assertEquals(16, MiningPatternContract.resolveTotalSegments(applied.miningMode(), applied.depthSize()));
    }
}
