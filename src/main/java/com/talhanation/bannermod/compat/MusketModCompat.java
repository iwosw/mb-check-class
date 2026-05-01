package com.talhanation.bannermod.compat;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class MusketModCompat {
    private static final String MOD_ID = "musketmod";
    private static final ResourceLocation CARTRIDGE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "cartridge");
    private static final Map<String, FirearmContract> SUPPORTED_FIREARMS = Map.of(
            "musket", new FirearmContract(CARTRIDGE, MusketWeapon::new),
            "musket_with_bayonet", new FirearmContract(CARTRIDGE, MusketBayonetWeapon::new),
            "musket_with_scope", new FirearmContract(CARTRIDGE, MusketScopeWeapon::new),
            "blunderbuss", new FirearmContract(CARTRIDGE, BlunderbussWeapon::new),
            "pistol", new FirearmContract(CARTRIDGE, PistolWeapon::new)
    );

    private MusketModCompat() {
    }

    public static boolean isSupportedRecruitFirearm(ItemStack stack) {
        return contract(stack).isPresent();
    }

    public static boolean isSupportedRecruitItem(ItemStack stack) {
        return isSupportedRecruitFirearm(stack) || isAmmo(stack, CARTRIDGE);
    }

    public static boolean isMusketModItem(ItemStack stack) {
        ResourceLocation id = itemId(stack);
        return id != null && MOD_ID.equals(id.getNamespace());
    }

    public static Optional<IWeapon> createRecruitWeapon(ItemStack stack) {
        return contract(stack).map(FirearmContract::createWeapon);
    }

    public static Optional<ResourceLocation> ammoContract(ItemStack stack) {
        return contract(stack).map(FirearmContract::ammoId);
    }

    public static boolean isAmmo(ItemStack stack, ResourceLocation ammoId) {
        ResourceLocation id = itemId(stack);
        return ammoId.equals(id);
    }

    public static ItemStack createAmmoStack(ResourceLocation ammoId, int count) {
        Item item = BuiltInRegistries.ITEM.getOptional(ammoId).orElse(null);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = item.getDefaultInstance();
        stack.setCount(count);
        return stack;
    }

    public static int reloadDurationOrDefault(ItemStack stack, int defaultDuration) {
        try {
            Class<?> gunItemClass = Class.forName("ewewukek.musketmod.GunItem");
            Method reloadDuration = gunItemClass.getMethod("reloadDuration", ItemStack.class);
            Object duration = reloadDuration.invoke(null, stack);
            if (duration instanceof Integer ticks) {
                return ticks;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            BannerModMain.LOGGER.info("Ewewukek's Musket Mod reload duration API was not found");
        }
        return defaultDuration;
    }

    public static boolean isLoaded(ItemStack stack) {
        try {
            Class<?> gunItemClass = Class.forName("ewewukek.musketmod.GunItem");
            Method isLoaded = gunItemClass.getMethod("isLoaded", ItemStack.class);
            Object loaded = isLoaded.invoke(null, stack);
            return loaded instanceof Boolean state && state;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            BannerModMain.LOGGER.info("Ewewukek's Musket Mod loaded-state API was not found");
            return false;
        }
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        try {
            Class<?> gunItemClass = Class.forName("ewewukek.musketmod.GunItem");
            Method setLoaded = gunItemClass.getMethod("setLoaded", ItemStack.class, boolean.class);
            setLoaded.invoke(null, stack, loaded);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            BannerModMain.LOGGER.info("Ewewukek's Musket Mod loaded-state API was not found");
        }
    }

    public static boolean fireWithMusketMod(AbstractRecruitEntity shooter, double x, double y, double z) {
        ItemStack stack = shooter.getMainHandItem();
        if (!isSupportedRecruitFirearm(stack)) {
            return false;
        }

        try {
            Class<?> gunItemClass = Class.forName("ewewukek.musketmod.GunItem");
            if (!gunItemClass.isInstance(stack.getItem())) {
                return false;
            }

            Vec3 direction = new Vec3(x - shooter.getX(), y + 0.5D - shooter.getEyeY(), z - shooter.getZ()).normalize();
            Method mobUse = gunItemClass.getMethod("mobUse", net.minecraft.world.entity.LivingEntity.class, InteractionHand.class, Vec3.class);
            mobUse.invoke(stack.getItem(), shooter, InteractionHand.MAIN_HAND, direction);
            shooter.damageMainHandItem();
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            BannerModMain.LOGGER.info("Ewewukek's Musket Mod mob firing API was not found");
            return false;
        }
    }

    private static Optional<FirearmContract> contract(ItemStack stack) {
        ResourceLocation id = itemId(stack);
        if (id == null || !MOD_ID.equals(id.getNamespace())) {
            return Optional.empty();
        }

        return Optional.ofNullable(SUPPORTED_FIREARMS.get(id.getPath()));
    }

    private static ResourceLocation itemId(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private record FirearmContract(ResourceLocation ammoId, Supplier<IWeapon> weaponFactory) {
        IWeapon createWeapon() {
            return weaponFactory.get();
        }
    }
}
