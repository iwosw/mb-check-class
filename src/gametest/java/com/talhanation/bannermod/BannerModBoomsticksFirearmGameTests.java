package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.compat.MedievalBoomsticksCompat;
import com.talhanation.bannermod.entity.military.CrossBowmanEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModBoomsticksFirearmGameTests {
    private static final ResourceLocation PISTOL_ID = ResourceLocation.fromNamespaceAndPath("musketmod", "pistol");
    private static final ResourceLocation CARTRIDGE_ID = ResourceLocation.fromNamespaceAndPath("musketmod", "cartridge");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", templateNamespace = BannerModMain.MOD_ID, timeoutTicks = 220)
    public static void recruitPistolUsesBoomsticksProjectileReloadAndAmmoDenial(GameTestHelper helper) {
        if (!BannerModMain.isMusketModLoaded) {
            BannerModMain.LOGGER.warn("BOOM-003A GameTest skipped: Medieval Boomsticks (musketmod) is not loaded in this runtime.");
            helper.succeed();
            return;
        }

        Item pistol = BuiltInRegistries.ITEM.getOptional(PISTOL_ID)
                .orElseThrow(() -> new IllegalStateException("Expected Medieval Boomsticks pistol item to be registered."));
        Item cartridge = BuiltInRegistries.ITEM.getOptional(CARTRIDGE_ID)
                .orElseThrow(() -> new IllegalStateException("Expected Medieval Boomsticks cartridge item to be registered."));

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        CrossBowmanEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.CROSSBOWMAN.get(),
                new BlockPos(2, 2, 2),
                "BOOM-003A pistol recruit",
                owner.getUUID()
        );
        Zombie target = spawnZombie(helper, new BlockPos(14, 2, 2));
        ItemStack pistolStack = new ItemStack(pistol);
        recruit.setItemSlot(EquipmentSlot.MAINHAND, pistolStack);
        recruit.setAggroState(1);
        recruit.setTarget(target);

        helper.assertTrue(MedievalBoomsticksCompat.isSupportedRecruitFirearm(pistolStack),
                "Expected the Medieval Boomsticks pistol to be routed through recruit firearm compat.");
        AABB testBounds = testBounds(helper);
        int initialProjectiles = countBoomsticksProjectiles(helper.getLevel(), testBounds);
        int reloadDuration = MedievalBoomsticksCompat.reloadDurationOrDefault(pistolStack, 40);

        helper.runAfterDelay(Math.min(40, Math.max(1, reloadDuration - 1)), () -> {
            helper.assertTrue(countBoomsticksProjectiles(helper.getLevel(), testBounds) == initialProjectiles,
                    "Expected an unloaded recruit pistol with no cartridges to deny fire and spawn no Boomsticks projectile.");
            recruit.getInventory().addItem(new ItemStack(cartridge));
        });

        helper.runAfterDelay(Math.max(1, reloadDuration - 1), () -> helper.assertTrue(
                countBoomsticksProjectiles(helper.getLevel(), testBounds) == initialProjectiles,
                "Expected the recruit pistol not to fire before the Boomsticks reload cadence elapses."));

        helper.succeedWhen(() -> {
            helper.assertTrue(countBoomsticksProjectiles(helper.getLevel(), testBounds) > initialProjectiles,
                    "Expected the recruit pistol to spawn a Medieval Boomsticks projectile after reload and aim cadence.");
            helper.assertTrue(countCartridges(recruit) == 0,
                    "Expected the recruit pistol to consume the one available cartridge before firing.");
        });
    }

    private static Zombie spawnZombie(GameTestHelper helper, BlockPos relativePos) {
        ServerLevel level = helper.getLevel();
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) {
            throw new IllegalStateException("Failed to create zombie target.");
        }
        BlockPos absolutePos = helper.absolutePos(relativePos);
        zombie.moveTo(absolutePos.getX() + 0.5D, absolutePos.getY(), absolutePos.getZ() + 0.5D, 0.0F, 0.0F);
        zombie.setPersistenceRequired();
        if (!level.addFreshEntity(zombie)) {
            throw new IllegalStateException("Failed to add zombie target to GameTest level.");
        }
        return zombie;
    }

    private static AABB testBounds(GameTestHelper helper) {
        return new AABB(
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(0, 0, 0))),
                Vec3.atLowerCornerOf(helper.absolutePos(new BlockPos(32, 16, 32)))
        );
    }

    private static int countBoomsticksProjectiles(ServerLevel level, AABB bounds) {
        return level.getEntitiesOfClass(Entity.class, bounds, entity -> {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            return PISTOL_ID.getNamespace().equals(id.getNamespace()) && id.getPath().contains("bullet");
        }).size();
    }

    private static int countCartridges(CrossBowmanEntity recruit) {
        int count = 0;
        for (int i = 0; i < recruit.getInventory().getContainerSize(); i++) {
            ItemStack stack = recruit.getInventory().getItem(i);
            if (MedievalBoomsticksCompat.isAmmo(stack, CARTRIDGE_ID)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
