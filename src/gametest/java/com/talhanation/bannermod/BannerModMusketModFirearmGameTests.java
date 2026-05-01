package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.compat.MusketModCompat;
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
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModMusketModFirearmGameTests {
    private static final int FIREARM_TEST_TIMEOUT_TICKS = 320;
    private static final ResourceLocation MUSKET_ID = ResourceLocation.fromNamespaceAndPath("musketmod", "musket");
    private static final ResourceLocation MUSKET_WITH_BAYONET_ID = ResourceLocation.fromNamespaceAndPath("musketmod", "musket_with_bayonet");
    private static final ResourceLocation MUSKET_WITH_SCOPE_ID = ResourceLocation.fromNamespaceAndPath("musketmod", "musket_with_scope");
    private static final ResourceLocation BLUNDERBUSS_ID = ResourceLocation.fromNamespaceAndPath("musketmod", "blunderbuss");
    private static final ResourceLocation PISTOL_ID = ResourceLocation.fromNamespaceAndPath("musketmod", "pistol");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", templateNamespace = BannerModMain.MOD_ID, timeoutTicks = FIREARM_TEST_TIMEOUT_TICKS)
    public static void recruitMusketUsesMusketModProjectileReloadAndAmmoDenial(GameTestHelper helper) {
        verifySupportedFirearmUsesMusketModProjectileReloadAndAmmoDenial(helper, MUSKET_ID, "musket");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", templateNamespace = BannerModMain.MOD_ID, timeoutTicks = FIREARM_TEST_TIMEOUT_TICKS)
    public static void recruitBayonetMusketUsesMusketModProjectileReloadAndAmmoDenial(GameTestHelper helper) {
        verifySupportedFirearmUsesMusketModProjectileReloadAndAmmoDenial(helper, MUSKET_WITH_BAYONET_ID, "musket-with-bayonet");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", templateNamespace = BannerModMain.MOD_ID, timeoutTicks = FIREARM_TEST_TIMEOUT_TICKS)
    public static void recruitScopedMusketUsesMusketModProjectileReloadAndAmmoDenial(GameTestHelper helper) {
        verifySupportedFirearmUsesMusketModProjectileReloadAndAmmoDenial(helper, MUSKET_WITH_SCOPE_ID, "scoped-musket");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", templateNamespace = BannerModMain.MOD_ID, timeoutTicks = FIREARM_TEST_TIMEOUT_TICKS)
    public static void recruitBlunderbussUsesMusketModProjectileReloadAndAmmoDenial(GameTestHelper helper) {
        verifySupportedFirearmUsesMusketModProjectileReloadAndAmmoDenial(helper, BLUNDERBUSS_ID, "blunderbuss");
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty", templateNamespace = BannerModMain.MOD_ID, timeoutTicks = FIREARM_TEST_TIMEOUT_TICKS)
    public static void recruitPistolUsesMusketModProjectileReloadAndAmmoDenial(GameTestHelper helper) {
        verifySupportedFirearmUsesMusketModProjectileReloadAndAmmoDenial(helper, PISTOL_ID, "pistol");
    }

    private static void verifySupportedFirearmUsesMusketModProjectileReloadAndAmmoDenial(GameTestHelper helper, ResourceLocation firearmId, String recruitLabel) {
        if (!BannerModMain.isMusketModLoaded) {
            BannerModMain.LOGGER.warn("BOOM-003A GameTest skipped: Ewewukek's Musket Mod (musketmod) is not loaded in this runtime.");
            helper.succeed();
            return;
        }

        Item firearm = resolveRegisteredItem(firearmId, "Expected supported Musket Mod firearm to be registered for GameTest coverage.");
        ItemStack firearmStack = new ItemStack(firearm);
        ResourceLocation ammoId = MusketModCompat.ammoContract(firearmStack)
                .orElseThrow(() -> new IllegalStateException("Expected supported recruit firearm to resolve a Musket Mod ammo contract: " + firearmId));
        Item ammoItem = resolveRegisteredItem(ammoId, "Expected Musket Mod ammo item to be registered for GameTest coverage.");

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        CrossBowmanEntity loadedRecruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.CROSSBOWMAN.get(),
                new BlockPos(2, 2, 2),
                "BOOM-003A " + recruitLabel + " loaded recruit",
                owner.getUUID()
        );
        CrossBowmanEntity noAmmoRecruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.CROSSBOWMAN.get(),
                new BlockPos(2, 2, 10),
                "BOOM-003A " + recruitLabel + " empty recruit",
                owner.getUUID()
        );
        Zombie loadedTarget = spawnZombie(helper, new BlockPos(14, 2, 2));
        Zombie noAmmoTarget = spawnZombie(helper, new BlockPos(26, 2, 10));
        configureRecruitForFirearm(loadedRecruit, loadedTarget, firearmStack.copy(), MusketModCompat.createAmmoStack(ammoId, 1));
        configureRecruitForFirearm(noAmmoRecruit, noAmmoTarget, firearmStack.copy(), ItemStack.EMPTY);

        helper.assertTrue(MusketModCompat.isSupportedRecruitFirearm(firearmStack),
                "Expected the musketmod firearm to be routed through recruit firearm compat: " + firearmId);
        AABB loadedLaneBounds = laneBounds(helper, new BlockPos(0, 0, 0), new BlockPos(18, 8, 6));
        AABB noAmmoLaneBounds = laneBounds(helper, new BlockPos(0, 0, 8), new BlockPos(32, 8, 14));
        ServerLevel level = helper.getLevel();
        int initialLoadedProjectiles = countMusketModProjectiles(level, loadedLaneBounds);
        int initialNoAmmoProjectiles = countMusketModProjectiles(level, noAmmoLaneBounds);
        int reloadDuration = MusketModCompat.reloadDurationOrDefault(firearmStack, 40);
        int noAmmoWindowDelay = Math.max(reloadDuration + 80, 120);
        AtomicBoolean loadedProjectileOrImpactSeen = new AtomicBoolean(false);
        AtomicBoolean noAmmoProjectileOrImpactSeen = new AtomicBoolean(false);
        AtomicBoolean loadedReadyObserved = new AtomicBoolean(false);
        AtomicBoolean noAmmoWindowCleared = new AtomicBoolean(false);

        helper.assertFalse(MusketModCompat.isLoaded(loadedRecruit.getMainHandItem()),
                "Expected recruit firearm " + firearmId + " to start unloaded so the reload path is exercised.");
        helper.assertFalse(MusketModCompat.isLoaded(noAmmoRecruit.getMainHandItem()),
                "Expected no-ammo recruit firearm " + firearmId + " to start unloaded so denial is not bypassed by a preloaded gun.");

        helper.runAfterDelay(noAmmoWindowDelay, () -> {
            observeProjectile(level, noAmmoLaneBounds, initialNoAmmoProjectiles, noAmmoProjectileOrImpactSeen);
            helper.assertFalse(noAmmoProjectileOrImpactSeen.get(),
                    "Expected recruit firearm " + firearmId + " to deny fire consistently when no Musket Mod ammo is available.");
            noAmmoWindowCleared.set(true);
        });

        helper.succeedWhen(() -> {
            observeLoadedReadyState(loadedRecruit, loadedProjectileOrImpactSeen, loadedReadyObserved);
            observeProjectile(level, loadedLaneBounds, initialLoadedProjectiles, loadedProjectileOrImpactSeen);
            observeProjectile(level, noAmmoLaneBounds, initialNoAmmoProjectiles, noAmmoProjectileOrImpactSeen);
            helper.assertTrue(noAmmoWindowCleared.get(),
                    "Expected recruit firearm " + firearmId + " to stay denied long enough to verify the no-ammo path.");
            helper.assertTrue(loadedReadyObserved.get(),
                    "Expected recruit firearm " + firearmId + " to reach the Musket Mod loaded state before it fires.");
            helper.assertTrue(loadedProjectileOrImpactSeen.get(),
                    "Expected recruit firearm " + firearmId + " to spawn a Musket Mod projectile or land an impact after reloading.");
            helper.assertFalse(noAmmoProjectileOrImpactSeen.get(),
                    "Expected no-ammo recruit firearm " + firearmId + " never to spawn a Musket Mod projectile or damage its target.");
            helper.assertTrue(countAmmo(loadedRecruit, ammoId) == 0,
                    "Expected recruit firearm " + firearmId + " to consume the one available Musket Mod cartridge before or on first fire.");
            helper.assertFalse(MusketModCompat.isLoaded(loadedRecruit.getMainHandItem()),
                    "Expected recruit firearm " + firearmId + " to return to the unloaded state after firing.");
        });
    }

    private static void configureRecruitForFirearm(CrossBowmanEntity recruit, Zombie target, ItemStack firearmStack, ItemStack ammoStack) {
        MusketModCompat.setLoaded(firearmStack, false);
        recruit.setItemSlot(EquipmentSlot.MAINHAND, firearmStack);
        if (!ammoStack.isEmpty()) {
            recruit.getInventory().addItem(ammoStack);
        }
        recruit.setAggroState(1);
        recruit.setTarget(target);
    }

    private static Zombie spawnZombie(GameTestHelper helper, BlockPos relativePos) {
        ServerLevel level = helper.getLevel();
        Husk zombie = EntityType.HUSK.create(level);
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

    private static Item resolveRegisteredItem(ResourceLocation itemId, String failureMessage) {
        return BuiltInRegistries.ITEM.getOptional(itemId)
                .orElseThrow(() -> new IllegalStateException(failureMessage + " Missing item: " + itemId));
    }

    private static AABB laneBounds(GameTestHelper helper, BlockPos from, BlockPos to) {
        return new AABB(
                Vec3.atLowerCornerOf(helper.absolutePos(from)),
                Vec3.atLowerCornerOf(helper.absolutePos(to))
        );
    }

    private static void observeProjectile(ServerLevel level, AABB bounds, int initialProjectiles, AtomicBoolean observed) {
        if (!observed.get() && countMusketModProjectiles(level, bounds) > initialProjectiles) {
            observed.set(true);
        }
    }

    private static void observeLoadedReadyState(CrossBowmanEntity recruit, AtomicBoolean projectileSeen, AtomicBoolean loadedReadyObserved) {
        if (!projectileSeen.get() && MusketModCompat.isLoaded(recruit.getMainHandItem())) {
            loadedReadyObserved.set(true);
        }
    }

    private static int countMusketModProjectiles(ServerLevel level, AABB bounds) {
        return level.getEntitiesOfClass(Entity.class, bounds, entity -> {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            return PISTOL_ID.getNamespace().equals(id.getNamespace()) && id.getPath().contains("bullet");
        }).size();
    }

    private static int countAmmo(CrossBowmanEntity recruit, ResourceLocation ammoId) {
        int count = 0;
        for (int i = 0; i < recruit.getInventory().getContainerSize(); i++) {
            ItemStack stack = recruit.getInventory().getItem(i);
            if (MusketModCompat.isAmmo(stack, ammoId)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
