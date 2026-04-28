package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.shared.logistics.BannerModSupplyStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

interface RecruitUpkeepAccess {
    private AbstractRecruitEntity recruit() {
        return (AbstractRecruitEntity) this;
    }

    default int getUpkeepTimer() {
        return recruit().upkeepTimer;
    }

    default void setUpkeepTimer(int upkeepTimer) {
        recruit().upkeepTimer = upkeepTimer;
    }

    default float getMorale() {
        return RecruitStateAccess.getMorale(recruit());
    }

    default float getHunger() {
        return RecruitStateAccess.getHunger(recruit());
    }

    default void setMoral(float value) {
        RecruitStateAccess.setMorale(recruit(), value);
        recruit().applyMoralEffects();
    }

    default void setHunger(float value) {
        float currentHunger = getHunger();
        if (value < 0 && currentHunger - value <= 0) {
            RecruitStateAccess.setHunger(recruit(), 0F);
        } else {
            RecruitStateAccess.setHunger(recruit(), value);
        }
    }

    default void updateMorale() {
        RecruitUpkeepService.updateMorale(recruit());
    }

    default void applyMoralEffects() {
        RecruitUpkeepService.applyMoralEffects(recruit());
    }

    default void updateHunger() {
        RecruitUpkeepService.updateHunger(recruit());
    }

    default boolean needsToGetFood() {
        return RecruitUpkeepService.needsToGetFood(recruit());
    }

    default boolean hasFoodInInv() {
        return RecruitUpkeepService.hasFoodInInv(recruit());
    }

    default boolean needsToEat() {
        return RecruitUpkeepService.needsToEat(recruit());
    }

    default boolean isStarving() {
        return RecruitUpkeepService.isStarving(recruit());
    }

    default boolean isSaturated() {
        return RecruitUpkeepService.isSaturated(recruit());
    }

    default BannerModSupplyStatus.RecruitSupplyStatus getSupplyStatus(@Nullable Container upkeepContainer) {
        return RecruitUpkeepService.getSupplyStatus(recruit(), upkeepContainer);
    }

    default void upkeepReequip(@NotNull Container container) {
        RecruitUpkeepService.upkeepReequip(recruit(), container);
    }

    default int getUpkeepCooldown() {
        return 3000;
    }

    default boolean canEatItemStack(ItemStack stack) {
        ResourceLocation location = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (RecruitsServerConfig.FoodBlackList.get().contains(location.toString())) {
            return false;
        }
        return stack.isEdible();
    }

    default void checkPayment(Container container) {
        RecruitUpkeepService.checkPayment(recruit(), container, recruit().getOwner() != null ? recruit().textNoPayment(recruit().getName().getString()) : null);
    }

    default void doNoPaymentAction() {
        RecruitUpkeepService.doNoPaymentAction(recruit());
    }

    default void resetPaymentTimer() {
        RecruitUpkeepService.resetPaymentTimer(recruit());
    }
}
