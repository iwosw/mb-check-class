package com.talhanation.bannermod;

import com.talhanation.bannermod.ai.military.CombatStance;
import com.talhanation.bannermod.ai.military.UseShield;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.RecruitShieldmanEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * STALEREF-001: assert {@link UseShield#cachedNearestHostile} is invalidated
 * once the cached target dies, so a recruit holding a shield wall does not
 * keep tracking / blocking toward a corpse for up to a full scan interval.
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModUseShieldStaleRefGameTests {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000aa1001");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void useShieldDropsCachedHostileOnDeath(GameTestHelper helper) throws Exception {
        // Spawn a shieldman recruit at the harness origin and a hostile zombie nearby.
        // We seed UseShield.cachedNearestHostile directly via reflection rather than
        // driving the full canUse() pipeline, because canUse() filters through the
        // recruit's targetingConditions / faction / aggro state — out of scope for
        // STALEREF-001, which is about the invalidation behaviour once the cache is
        // populated. Reflection lets us isolate that behaviour deterministically.
        RecruitShieldmanEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT_SHIELDMAN.get(),
                new BlockPos(2, 2, 2),
                "stale-ref-shieldman",
                OWNER_UUID
        );
        recruit.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        recruit.setCombatStance(CombatStance.SHIELD_WALL);

        Zombie zombie = spawnZombie(helper, new BlockPos(4, 2, 2));

        UseShield goal = new UseShield(recruit);
        cachedNearestHostileField().set(goal, zombie);

        LivingEntity cachedBefore = (LivingEntity) cachedNearestHostileField().get(goal);
        helper.assertTrue(cachedBefore == zombie,
                "Expected the seeded zombie to be the cached hostile before kill");

        // Kill the cached target. tick() should observe the death and drop the reference.
        zombie.kill();
        helper.assertFalse(zombie.isAlive(),
                "Expected zombie to be dead after kill()");

        goal.tick();

        LivingEntity cachedAfter = (LivingEntity) cachedNearestHostileField().get(goal);
        helper.assertTrue(cachedAfter == null,
                "Expected UseShield.cachedNearestHostile to be null after the cached target died and tick() ran; got " + cachedAfter);

        // And the recruit must not be persistently shouldBlock'ing toward the corpse.
        helper.assertFalse(recruit.getShouldBlock(),
                "Expected recruit not to be flagged shouldBlock toward a dead target");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void useShieldInvalidateStaleHostileIsIdempotent(GameTestHelper helper) throws Exception {
        // Sanity check: invalidate is safe to call repeatedly when there is no cache.
        RecruitShieldmanEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT_SHIELDMAN.get(),
                new BlockPos(2, 2, 2),
                "idempotent-shieldman",
                OWNER_UUID
        );
        UseShield goal = new UseShield(recruit);
        goal.invalidateStaleHostile();
        goal.invalidateStaleHostile();
        helper.assertTrue(cachedNearestHostileField().get(goal) == null,
                "Expected cachedNearestHostile to remain null after redundant invalidate calls");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void isLiveTargetRejectsDeadEntity(GameTestHelper helper) {
        // Direct end-to-end check of the predicate against a real LivingEntity.
        Zombie zombie = spawnZombie(helper, new BlockPos(2, 2, 2));
        helper.assertTrue(UseShield.isLiveTarget(zombie),
                "Expected isLiveTarget(zombie) to be true while alive");
        zombie.kill();
        helper.assertFalse(UseShield.isLiveTarget(zombie),
                "Expected isLiveTarget(zombie) to be false after kill()");
        helper.assertFalse(UseShield.isLiveTarget(null),
                "Expected isLiveTarget(null) to be false");
        helper.succeed();
    }

    private static Field cachedNearestHostileField() throws NoSuchFieldException {
        Field f = UseShield.class.getDeclaredField("cachedNearestHostile");
        f.setAccessible(true);
        return f;
    }

    private static Zombie spawnZombie(GameTestHelper helper, BlockPos relativePos) {
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        if (zombie == null) {
            throw new IllegalArgumentException("Failed to create zombie test target");
        }
        BlockPos abs = helper.absolutePos(relativePos);
        zombie.moveTo(abs.getX() + 0.5D, abs.getY(), abs.getZ() + 0.5D, 0.0F, 0.0F);
        zombie.setPersistenceRequired();
        zombie.setHealth(zombie.getMaxHealth());
        if (!helper.getLevel().addFreshEntity(zombie)) {
            throw new IllegalArgumentException("Failed to insert zombie test target");
        }
        return zombie;
    }
}
