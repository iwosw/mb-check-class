package com.talhanation.bannermod.entity.military;

import net.minecraft.sounds.SoundEvents;

interface RecruitProgressionAccess {
    private AbstractRecruitEntity recruit() {
        return (AbstractRecruitEntity) this;
    }

    default void addXpLevel(int level) {
        RecruitProgressionService.addXpLevel(recruit(), level);
    }

    default void addLevelBuffs() {
        RecruitProgressionService.applyLevelBuffs(recruit());
    }

    default void addLevelBuffsForLevel(int level) {
        RecruitProgressionService.applyLevelBuffsForLevel(recruit(), level);
    }

    default void checkLevel() {
        RecruitProgressionService.checkLevel(recruit());
    }

    default void recalculateCost() {
        RecruitProgressionService.recalculateCost(recruit());
    }

    default void makeLevelUpSound() {
        recruit().getCommandSenderWorld().playSound(null, recruit().getX(), recruit().getY() + 1, recruit().getZ(), SoundEvents.PLAYER_LEVELUP, recruit().getSoundSource(), 1.0F, 0.8F + 0.4F * recruit().getRandom().nextFloat());
        if (AbstractRecruitEntity.recruitsLookLikeVillagers()) {
            recruit().getCommandSenderWorld().playSound(null, recruit().getX(), recruit().getY() + 1, recruit().getZ(), SoundEvents.VILLAGER_CELEBRATE, recruit().getSoundSource(), 1.0F, 0.8F + 0.4F * recruit().getRandom().nextFloat());
        }
    }
}
