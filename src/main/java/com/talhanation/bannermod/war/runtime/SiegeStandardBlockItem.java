package com.talhanation.bannermod.war.runtime;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * BlockItem subclass whose only purpose is to surface a player-facing tooltip explaining that
 * this block is part of the regulated-warfare system and should be placed via the War Room.
 *
 * <p>Tooltip strings are kept as literal text (not translation keys) on purpose — this slice
 * does not ship a language file and {@code I18n} is client-only, so calling it from a
 * cross-side {@code BlockItem} would crash the dedicated server.</p>
 */
public class SiegeStandardBlockItem extends BlockItem {

    private static final Component HINT_LINE_1 = Component.literal(
            "Place during a declared war to anchor a siege zone.").withStyle(ChatFormatting.GRAY);
    private static final Component HINT_LINE_2 = Component.literal(
            "War Room \"Place siege here\" wires this for side leaders.").withStyle(ChatFormatting.DARK_GRAY);

    public SiegeStandardBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(HINT_LINE_1);
        tooltip.add(HINT_LINE_2);
    }
}
