package com.talhanation.bannermod.compat.musketmod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

//all reflection links for classes

public class MusketModReflection {

    public static boolean isInitialized = false;
    public static boolean hasMusketMod = false;

    // Items
    public static Class<?> itemsClass;
    public static Object itemsInstance;
    public static Field blunderbussField;
    public static Field musketWithScopeField;
    public static Field musketField;
    public static Field pistolField;
    public static Field musketWithBayonetField;

    // Sounds
    public static Class<?> soundsClass;
    public static Object soundsInstance;
    public static Field blunderbussFireField;
    public static Field musketReadyField;
    public static Field musketFireField;
    public static Field pistolFireField;

    // PistolItem
    public static Class<?> pistolItemClass;
    public static Method pistolItemIsLoadedMethod;
    public static Method pistolItemSetLoadedMethod;

    // MusketItem
    public static Class<?> musketItemClass;
    public static Method musketItemIsLoadedMethod;
    public static Method musketItemSetLoadedMethod;

    // BulletEntity
    public static Class<?> bulletClass;
    public static Constructor<?> bulletConstructor;
    public static Field bulletDamageField;
    public static Method bulletSetInitialSpeedMethod;

    // MusketMod
    public static Class<?> musketModClass;
    public static Method sendSmokeEffectMethod;

    public static void init() {
        if (isInitialized) return;
        isInitialized = true;

        boolean essentialOk = true;

        // --- ewewukek.musketmod.Items ---
        try {
            itemsClass = Class.forName("ewewukek.musketmod.Items");
            itemsInstance = null;

            blunderbussField = itemsClass.getField("BLUNDERBUSS");
            musketWithScopeField = itemsClass.getField("MUSKET_WITH_SCOPE");
            musketField = itemsClass.getField("MUSKET");
            pistolField = itemsClass.getField("PISTOL");
            musketWithBayonetField = itemsClass.getField("MUSKET_WITH_BAYONET");
        } catch (ClassNotFoundException e) {
            BannerModMain.LOGGER.info("MusketMod Items class was not found.");
            essentialOk = false;
        } catch (NoSuchFieldException e) {
            BannerModMain.LOGGER.error("A field in MusketMod Items was not found", e);
            essentialOk = false;
        }

        // --- ewewukek.musketmod.Sounds ---
        try {
            soundsClass = Class.forName("ewewukek.musketmod.Sounds");
            soundsInstance = null;

            blunderbussFireField = soundsClass.getField("BLUNDERBUSS_FIRE");
            musketReadyField = soundsClass.getField("MUSKET_READY");
            musketFireField = soundsClass.getField("MUSKET_FIRE");
            pistolFireField = soundsClass.getField("PISTOL_FIRE");
        } catch (ClassNotFoundException e) {
            BannerModMain.LOGGER.info("MusketMod Sounds class was not found.");
        } catch (NoSuchFieldException e) {
            BannerModMain.LOGGER.error("A field in MusketMod Sounds was not found", e);
        }

        // --- PistolItem ---
        try {
            pistolItemClass = Class.forName("ewewukek.musketmod.PistolItem");
            pistolItemIsLoadedMethod = pistolItemClass.getMethod("isLoaded", ItemStack.class);
            pistolItemSetLoadedMethod = pistolItemClass.getMethod("setLoaded", ItemStack.class, boolean.class);
        } catch (ClassNotFoundException e) {
            BannerModMain.LOGGER.info("PistolItem class was not found.");
            essentialOk = false;
        } catch (NoSuchMethodException e) {
            BannerModMain.LOGGER.error("A method in PistolItem was not found", e);
            essentialOk = false;
        }

        // --- MusketItem ---
        try {
            musketItemClass = Class.forName("ewewukek.musketmod.MusketItem");
            musketItemIsLoadedMethod = musketItemClass.getMethod("isLoaded", ItemStack.class);
            musketItemSetLoadedMethod = musketItemClass.getMethod("setLoaded", ItemStack.class, boolean.class);
        } catch (ClassNotFoundException e) {
            BannerModMain.LOGGER.info("MusketItem class was not found.");
            essentialOk = false;
        } catch (NoSuchMethodException e) {
            BannerModMain.LOGGER.error("A method in MusketItem was not found", e);
            essentialOk = false;
        }

        // --- BulletEntity ---
        try {
            bulletClass = Class.forName("ewewukek.musketmod.BulletEntity");
            bulletConstructor = bulletClass.getConstructor(Level.class);

            bulletDamageField = bulletClass.getField("damage");
            bulletDamageField.setAccessible(true);

            bulletSetInitialSpeedMethod = bulletClass.getMethod("setInitialSpeed", float.class);
        } catch (ClassNotFoundException e) {
            BannerModMain.LOGGER.info("BulletEntity class was not found.");
            essentialOk = false;
        } catch (NoSuchFieldException e) {
            BannerModMain.LOGGER.error("BulletEntity field not found", e);
            essentialOk = false;
        } catch (NoSuchMethodException e) {
            BannerModMain.LOGGER.error("BulletEntity method not found", e);
            essentialOk = false;
        }

        // --- MusketMod ---
        try {
            musketModClass = Class.forName("ewewukek.musketmod.MusketMod");
            sendSmokeEffectMethod = musketModClass.getMethod("sendSmokeEffect", ServerLevel.class, Vec3.class, Vec3.class);
        } catch (ClassNotFoundException e) {
            BannerModMain.LOGGER.info("MusketMod class was not found.");
        } catch (NoSuchMethodException e) {
            BannerModMain.LOGGER.error("sendSmokeEffect method was not found", e);
        }

        hasMusketMod = essentialOk && itemsClass != null && bulletClass != null && (pistolItemClass != null || musketItemClass != null);
    }
}