package com.talhanation.recruits.gametest.recruit;

import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.RecruitEntity;
import com.talhanation.recruits.gametest.support.RecruitsEntityAsserts;
import com.talhanation.recruits.gametest.support.RecruitsGameTestSupport;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Main.MOD_ID)
public class RecruitSpawnGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "recruit_spawn_pad")
    public static void recruitSpawnsAliveWithDefaultName(GameTestHelper helper) {
        RecruitEntity recruit = RecruitsGameTestSupport.spawnRecruit(helper, RecruitsGameTestSupport.PRIMARY_RECRUIT_POS);

        helper.runAfterDelay(5, () -> {
            RecruitsEntityAsserts.assertRecruitPresentAt(helper, recruit, RecruitsGameTestSupport.PRIMARY_RECRUIT_POS);
            RecruitsEntityAsserts.assertRecruitAlive(recruit);
            RecruitsEntityAsserts.assertRecruitCustomName(recruit, "Recruit");
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "recruit_spawn_pad")
    public static void supportSpawnsTwoRecruitsOnDistinctPads(GameTestHelper helper) {
        RecruitEntity primaryRecruit = RecruitsGameTestSupport.spawnRecruit(helper, RecruitsGameTestSupport.PRIMARY_RECRUIT_POS);
        RecruitEntity secondaryRecruit = RecruitsGameTestSupport.spawnRecruit(helper, RecruitsGameTestSupport.SECONDARY_RECRUIT_POS);

        helper.runAfterDelay(5, () -> {
            RecruitsEntityAsserts.assertRecruitPresentAt(helper, primaryRecruit, RecruitsGameTestSupport.PRIMARY_RECRUIT_POS);
            RecruitsEntityAsserts.assertRecruitPresentAt(helper, secondaryRecruit, RecruitsGameTestSupport.SECONDARY_RECRUIT_POS);
            RecruitsEntityAsserts.assertRecruitAlive(primaryRecruit);
            RecruitsEntityAsserts.assertRecruitAlive(secondaryRecruit);
            helper.succeed();
        });
    }
}
