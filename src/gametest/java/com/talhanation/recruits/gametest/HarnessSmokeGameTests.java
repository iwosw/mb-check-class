package com.talhanation.recruits.gametest;

import com.talhanation.recruits.Main;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Main.MOD_ID)
public class HarnessSmokeGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void harnessSmokePasses(GameTestHelper helper) {
        helper.succeed();
    }
}
