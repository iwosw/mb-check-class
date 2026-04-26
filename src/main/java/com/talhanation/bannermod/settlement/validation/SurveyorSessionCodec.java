package com.talhanation.bannermod.settlement.validation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class SurveyorSessionCodec {
    private static final String SESSION_TAG = "SettlementValidationSession";

    private SurveyorSessionCodec() {
    }

    public static ValidationSession read(ItemStack stack) {
        if (stack == null || !stack.hasTag()) {
            return null;
        }
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(SESSION_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return ValidationSession.fromTag(root.getCompound(SESSION_TAG));
    }

    public static void write(ItemStack stack, ValidationSession session) {
        if (stack == null || session == null) {
            return;
        }
        CompoundTag root = stack.getOrCreateTag();
        root.put(SESSION_TAG, session.toTag());
    }

    public static void clear(ItemStack stack) {
        if (stack == null || !stack.hasTag()) {
            return;
        }
        CompoundTag root = stack.getTag();
        if (root != null) {
            root.remove(SESSION_TAG);
        }
    }
}
