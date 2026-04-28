package com.talhanation.bannermod.items.civilian;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BannerModAlmanacItem extends Item {
    private static final String BOOK_TITLE = "BannerMod Almanac";
    private static final String BOOK_AUTHOR = "BannerMod";
    private static final int PAGE_COUNT = 12;

    public BannerModAlmanacItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.openItemGui(createBookStack(), hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.bannermod.banner_almanac.tooltip.use"));
        tooltip.add(Component.translatable("item.bannermod.banner_almanac.tooltip.scope"));
    }

    private static ItemStack createBookStack() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.setHoverName(Component.translatable("item.bannermod.banner_almanac"));

        CompoundTag tag = book.getOrCreateTag();
        ListTag pages = new ListTag();
        for (int page = 1; page <= PAGE_COUNT; page++) {
            pages.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.translatable("item.bannermod.banner_almanac.page_" + page)
            )));
        }
        tag.put("pages", pages);
        tag.putString("title", BOOK_TITLE);
        tag.putString("author", BOOK_AUTHOR);
        tag.putInt("generation", 0);
        tag.putBoolean("resolved", true);
        return book;
    }
}
