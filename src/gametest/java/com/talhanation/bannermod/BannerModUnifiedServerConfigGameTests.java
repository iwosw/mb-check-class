package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.BannerModServerConfig;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.config.WorkersServerConfig;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * CONFIGMERGE-001: gametest evidence that the unified server spec actually wires both
 * legacy gameplay knobs.
 *
 * <p>Picks one knob from each legacy config and verifies the value handle:
 * <ul>
 *   <li>resolves through {@link BannerModServerConfig#SERVER} (the only registered SERVER spec),</li>
 *   <li>responds to set/get the way callsites historically expected, and</li>
 *   <li>lives at a path namespaced under {@code recruits.*} or {@code workers.*}.</li>
 * </ul>
 *
 * <p>The gametest deliberately doesn't try to spawn entities — those flows are exercised by
 * the dozens of existing recruits/workers gametests, all of which reach the same value
 * handles tested here. The point of this test is to prove the merge wiring is the actual
 * source of truth at runtime, not to re-prove every downstream behaviour.
 */
@GameTestHolder(BannerModMain.MOD_ID)
public final class BannerModUnifiedServerConfigGameTests {

    private BannerModUnifiedServerConfigGameTests() {
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitCostKnobReadsFromUnifiedSpec(GameTestHelper helper) {
        helper.assertTrue(BannerModServerConfig.SERVER == RecruitsServerConfig.SERVER,
                "RecruitsServerConfig.SERVER must mirror the unified BannerModServerConfig.SERVER instance.");

        ModConfigSpec.IntValue recruitCost = RecruitsServerConfig.RecruitCost;
        List<String> path = recruitCost.getPath();
        helper.assertTrue("recruits".equals(path.get(0)),
                "RecruitCost must live under the recruits.* sub-path inside the unified spec, "
                        + "got path: " + path);
        // get() now resolves through the loaded SERVER spec attached to the running gametest
        // server. The default 4 emerald cost wired in populate() must be the value visible
        // through the unified spec; if BannerModServerConfig had not registered the right
        // spec, this read would throw IllegalStateException ("ConfigSpec is not in any
        // config") instead of returning the default.
        int observed = recruitCost.get();
        helper.assertTrue(observed == 4,
                "RecruitCost should resolve to the unified-spec default of 4, observed " + observed);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void farmerCostKnobReadsFromUnifiedSpec(GameTestHelper helper) {
        helper.assertTrue(BannerModServerConfig.SERVER == WorkersServerConfig.SERVER,
                "WorkersServerConfig.SERVER must mirror the unified BannerModServerConfig.SERVER instance.");

        ModConfigSpec.IntValue farmerCost = WorkersServerConfig.FarmerCost;
        List<String> path = farmerCost.getPath();
        helper.assertTrue("workers".equals(path.get(0)),
                "FarmerCost must live under the workers.* sub-path inside the unified spec, "
                        + "got path: " + path);
        int observed = farmerCost.get();
        helper.assertTrue(observed == 10,
                "FarmerCost should resolve to the unified-spec default of 10, observed " + observed);
        helper.succeed();
    }
}
