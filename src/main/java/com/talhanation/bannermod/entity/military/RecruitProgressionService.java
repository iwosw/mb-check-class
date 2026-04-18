package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.events.RecruitEvent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.common.MinecraftForge;

final class RecruitProgressionService {
    private RecruitProgressionService() {
    }

    static void addXpLevel(AbstractRecruitEntity recruit, int level) {
        int currentLevel = recruit.getXpLevel();
        int newLevel = currentLevel + level;
        if (newLevel > RecruitsServerConfig.RecruitsMaxXpLevel.get()) {
            newLevel = RecruitsServerConfig.RecruitsMaxXpLevel.get();
        } else {
            recruit.makeLevelUpSound();
            applyLevelBuffs(recruit);
        }
        recruit.setXpLevel(newLevel);
    }

    static void applyLevelBuffs(AbstractRecruitEntity recruit) {
        int level = recruit.getXpLevel();
        if (level <= 10) {
            recruit.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("heath_bonus_level", 2D, AttributeModifier.Operation.ADDITION));
            recruit.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("attack_bonus_level", 0.03D, AttributeModifier.Operation.ADDITION));
            recruit.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier("knockback_bonus_level", 0.0012D, AttributeModifier.Operation.ADDITION));
            recruit.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("speed_bonus_level", 0.0025D, AttributeModifier.Operation.ADDITION));
        }
        if (level > 10) {
            recruit.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("heath_bonus_level", 2D, AttributeModifier.Operation.ADDITION));
        }
    }

    static void applyLevelBuffsForLevel(AbstractRecruitEntity recruit, int level) {
        for (int i = 0; i < level; i++) {
            if (level <= 10) {
                recruit.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("heath_bonus_level", 2D, AttributeModifier.Operation.ADDITION));
                recruit.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("attack_bonus_level", 0.03D, AttributeModifier.Operation.ADDITION));
                recruit.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(new AttributeModifier("knockback_bonus_level", 0.0012D, AttributeModifier.Operation.ADDITION));
                recruit.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("speed_bonus_level", 0.0025D, AttributeModifier.Operation.ADDITION));
            }
            if (level > 10) {
                recruit.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("heath_bonus_level", 2D, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    static void checkLevel(AbstractRecruitEntity recruit) {
        int currentXp = recruit.getXp();
        if (currentXp >= RecruitsServerConfig.RecruitsMaxXpForLevelUp.get()) {
            addXpLevel(recruit, 1);
            recruit.setXp(0);
            recruit.heal(10F);
            recalculateCost(recruit);
            if (recruit.getMorale() < 100) recruit.setMoral(recruit.getMorale() + 5F);
            if (!recruit.getCommandSenderWorld().isClientSide()) {
                MinecraftForge.EVENT_BUS.post(new RecruitEvent.LevelUp(recruit, recruit.getXpLevel()));
            }
        }
    }

    static void recalculateCost(AbstractRecruitEntity recruit) {
        int currCost = recruit.getCost();
        int armorBonus = recruit.getArmorValue() * 2;
        int weaponBonus = 4;
        int speedBonus = (int) (recruit.getSpeed() * 2);
        int shieldBonus = recruit.getOffhandItem().getItem() instanceof ShieldItem ? 10 : 0;
        int newCost = Math.abs(shieldBonus + speedBonus + weaponBonus + armorBonus + currCost + recruit.getXpLevel() * 2);
        recruit.setCost(newCost);
    }
}
