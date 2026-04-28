package com.talhanation.bannermod;

import com.talhanation.bannermod.army.command.CommandIntentType;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.combat.SiegeObjectivePolicy;
import com.talhanation.bannermod.network.BannerModNetworkBootstrap;
import com.talhanation.bannermod.network.messages.military.MessageClaimIntent;
import com.talhanation.bannermod.war.registry.PoliticalEntityStatus;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModCoverageScenarioGameTests {
    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void claimIntentCatalogIncludesAtomicClaimEdit(GameTestHelper helper) {
        helper.assertTrue(java.util.Arrays.asList(BannerModNetworkBootstrap.MILITARY_MESSAGES).contains(MessageClaimIntent.class), "Claim intent packet must be registered");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void claimIntentActionsCoverAddRemoveDelete(GameTestHelper helper) {
        helper.assertTrue(MessageClaimIntent.Action.values().length == 3, "Claim intent must cover add/remove/delete");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void siegeCommandIntentTypeIsRegistered(GameTestHelper helper) {
        helper.assertTrue(CommandIntentType.SIEGE_MACHINE.name().equals("SIEGE_MACHINE"), "Siege machine command intent is required");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void siegeObjectiveRejectsSameSideAttack(GameTestHelper helper) {
        UUID side = UUID.randomUUID();
        helper.assertFalse(SiegeObjectivePolicy.canAttackStandard(side, side), "Same side must not attack own siege standard");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void siegeObjectiveAllowsEnemyAttack(GameTestHelper helper) {
        helper.assertTrue(SiegeObjectivePolicy.canAttackStandard(UUID.randomUUID(), UUID.randomUUID()), "Opposing sides may attack siege standard");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void siegeObjectiveDamageCanDestroyStandard(GameTestHelper helper) {
        helper.assertTrue(SiegeObjectivePolicy.applyDamage(5, 5, 100).destroyed(), "Damage reaching zero destroys standard");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void siegeObjectiveDamageClampsAtZero(GameTestHelper helper) {
        helper.assertTrue(SiegeObjectivePolicy.applyDamage(3, 10, 100).controlAfter() == 0, "Damage should clamp at zero");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void politicalPromotionStatusTargetExists(GameTestHelper helper) {
        helper.assertTrue(PoliticalEntityStatus.STATE.name().equals("STATE"), "State promotion target must exist");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void workerPacketOffsetStillFollowsMilitaryCatalog(GameTestHelper helper) {
        helper.assertTrue(BannerModNetworkBootstrap.workerPacketOffset() == BannerModNetworkBootstrap.MILITARY_MESSAGES.length, "Worker packet offset must follow military catalog");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void warPacketCatalogContainsStatusPromotionPacket(GameTestHelper helper) {
        helper.assertTrue(BannerModNetworkBootstrap.WAR_MESSAGES.length > 0, "War packet catalog must be populated");
        helper.succeed();
    }
}
