package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.util.ItemStackComponentData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class SurveyorSessionCodec {
    private static final String SESSION_TAG = "SettlementValidationSession";

    private SurveyorSessionCodec() {
    }

    public static ValidationSession read(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        CompoundTag root = ItemStackComponentData.read(stack);
        if (root == null || !root.contains(SESSION_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return ValidationSession.fromTag(root.getCompound(SESSION_TAG));
    }

    public static void write(ItemStack stack, ValidationSession session) {
        if (stack == null || session == null) {
            return;
        }
        ItemStackComponentData.update(stack, root -> root.put(SESSION_TAG, session.toTag()));
    }

    public static void clear(ItemStack stack) {
        if (stack == null) {
            return;
        }
        ItemStackComponentData.update(stack, root -> root.remove(SESSION_TAG));
    }
}
