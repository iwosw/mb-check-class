package com.talhanation.workers;

import com.talhanation.workers.entities.workarea.MiningArea;
import com.talhanation.workers.entities.workarea.MiningPatternSettings;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MiningAreaPatternContractTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.bootStrap();
    }

    @SuppressWarnings("unchecked")
    private static EntityType<MiningArea> testType() {
        return (EntityType<MiningArea>) (EntityType<?>) EntityType.ARMOR_STAND;
    }

    @Test
    void tunnelAndBranchModesUseFixedInternalSegmentBudgetInsteadOfDepthSize() {
        MiningArea tunnelArea = new MiningArea(testType(), null);
        tunnelArea.setDepthSize(99);
        tunnelArea.setMiningMode(MiningArea.MiningMode.TUNNEL);

        MiningArea branchArea = new MiningArea(testType(), null);
        branchArea.setDepthSize(2);
        branchArea.setMiningMode(MiningArea.MiningMode.BRANCH);

        assertEquals(16, tunnelArea.getTotalSegments());
        assertEquals(16, branchArea.getTotalSegments());
    }

    @Test
    void applyPatternSettingsKeepsLegacyDepthFieldUntouchedWhileUpdatingAuthoredPatternSettings() {
        MiningArea area = new MiningArea(testType(), null);
        area.setDepthSize(12);

        area.applyPatternSettings(new MiningPatternSettings(
                MiningPatternSettings.Mode.BRANCH,
                5,
                4,
                -3,
                false,
                7,
                11,
                2
        ));

        assertEquals(5, area.getWidthSize());
        assertEquals(4, area.getHeightSize());
        assertEquals(12, area.getDepthSize());
        assertEquals(-3, area.getHeightOffset());
        assertEquals(false, area.getCloseFloor());
        assertEquals(7, area.getBranchSpacing());
        assertEquals(11, area.getBranchLength());
        assertEquals(2, area.getDescentStep());
        assertEquals(16, area.getTotalSegments());
    }
}
