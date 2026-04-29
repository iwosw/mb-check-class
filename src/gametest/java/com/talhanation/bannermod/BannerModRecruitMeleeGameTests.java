package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import com.talhanation.bannermod.util.AttackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModRecruitMeleeGameTests {
    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000008001");
    private static final String TEAM_ID = "vanilla008_melee_team";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitMeleeDamagesValidEnemy(GameTestHelper helper) {
        AbstractRecruitEntity recruit = spawnRecruit(helper, new BlockPos(2, 2, 2), "melee-valid-attacker");
        Zombie target = spawnZombie(helper, new BlockPos(3, 2, 2));
        float before = target.getHealth();

        boolean damaged = recruit.doHurtTarget(target);

        helper.assertTrue(damaged, "Expected recruit melee to report a landed hit on a valid enemy");
        helper.assertTrue(target.getHealth() < before, "Expected valid enemy health to decrease after melee hit");
        helper.assertTrue(recruit.getLastHurtMob() == target, "Expected recruit melee callback to record last hurt mob");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitMeleeDeniesSameTeamTarget(GameTestHelper helper) {
        AbstractRecruitEntity attacker = spawnRecruit(helper, new BlockPos(2, 2, 2), "melee-friendly-attacker");
        AbstractRecruitEntity target = spawnRecruit(helper, new BlockPos(3, 2, 2), "melee-friendly-target");
        BannerModDedicatedServerGameTestSupport.joinTeam(helper.getLevel(), TEAM_ID, attacker, target);
        attacker.setAggroState(2);
        float before = target.getHealth();

        boolean damaged = attacker.doHurtTarget(target);

        helper.assertFalse(damaged, "Expected recruit melee to deny same-team targets");
        helper.assertTrue(target.getHealth() == before, "Expected same-team target health to remain unchanged");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitMeleeRespectsReachAndCooldown(GameTestHelper helper) {
        AbstractRecruitEntity recruit = spawnRecruit(helper, new BlockPos(2, 2, 2), "melee-reach-cooldown-attacker");
        Zombie farTarget = spawnZombie(helper, new BlockPos(12, 2, 2));
        float farHealth = farTarget.getHealth();

        boolean farAttack = AttackUtil.performAttack(recruit, farTarget);
        helper.assertFalse(farAttack, "Expected recruit melee to deny targets outside reach");
        helper.assertTrue(farTarget.getHealth() == farHealth, "Expected out-of-reach target health to remain unchanged");
        helper.assertTrue(recruit.attackCooldown == 0, "Expected denied out-of-reach attack not to start cooldown");

        Zombie closeTarget = spawnZombie(helper, new BlockPos(3, 2, 2));
        float closeHealth = closeTarget.getHealth();
        recruit.attackCooldown = 10;
        boolean cooldownAttack = AttackUtil.performAttack(recruit, closeTarget);

        helper.assertFalse(cooldownAttack, "Expected recruit melee to deny attacks while cooldown is active");
        helper.assertTrue(closeTarget.getHealth() == closeHealth, "Expected cooldown-blocked target health to remain unchanged");
        helper.succeed();
    }

    private static AbstractRecruitEntity spawnRecruit(GameTestHelper helper, BlockPos relativePos, String name) {
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                relativePos,
                name,
                OWNER_UUID
        );
        recruit.setAggroState(0);
        recruit.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        return recruit;
    }

    private static Zombie spawnZombie(GameTestHelper helper, BlockPos relativePos) {
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        if (zombie == null) {
            throw new IllegalArgumentException("Failed to create zombie test target");
        }
        BlockPos absolutePos = helper.absolutePos(relativePos);
        zombie.moveTo(absolutePos.getX() + 0.5D, absolutePos.getY(), absolutePos.getZ() + 0.5D, 0.0F, 0.0F);
        zombie.setPersistenceRequired();
        zombie.setHealth(zombie.getMaxHealth());
        if (!helper.getLevel().addFreshEntity(zombie)) {
            throw new IllegalArgumentException("Failed to insert zombie test target");
        }
        return zombie;
    }
}
