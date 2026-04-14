package com.talhanation.bannermod;

import com.talhanation.recruits.Main;
import com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity;
import com.talhanation.recruits.gametest.support.RecruitsBattleGameTestSupport;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Main.MOD_ID)
public class BannerModCitizenRecruitGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitOwnershipAndFollowStateSurviveCitizenBackedPersistence(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                com.talhanation.recruits.init.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FRONTLINE_POS,
                "Citizen Recruit",
                player.getUUID()
        );
        recruit.setFollowState(2);

        CompoundTag saved = BannerModDedicatedServerGameTestSupport.saveEntity(recruit);
        AbstractRecruitEntity reloaded = BannerModDedicatedServerGameTestSupport.loadEntity(
                helper,
                com.talhanation.recruits.init.ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.WEST_FLANK_POS,
                saved
        );

        helper.assertTrue(player.getUUID().equals(reloaded.getOwnerUUID()),
                "Expected recruit owner UUID to survive citizen-backed persistence round-trip.");
        helper.assertTrue(reloaded.getFollowState() == 2,
                "Expected recruit follow-state to survive citizen-backed persistence round-trip.");
        helper.assertTrue(player.getUUID().equals(reloaded.getCitizenCore().getOwnerUUID()),
                "Expected recruit citizen core to expose the same persisted owner UUID as the live wrapper.");
        helper.assertTrue(reloaded.getCitizenCore().getFollowState() == reloaded.getFollowState(),
                "Expected recruit citizen core follow-state to stay aligned with the wrapper after reload.");
        helper.succeed();
    }
}
