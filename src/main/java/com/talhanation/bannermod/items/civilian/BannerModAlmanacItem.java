package com.talhanation.bannermod.items.civilian;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.bannermod.banner_almanac.tooltip.use"));
        tooltip.add(Component.translatable("item.bannermod.banner_almanac.tooltip.scope"));
    }

    private static ItemStack createBookStack() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.ITEM_NAME, Component.translatable("item.bannermod.banner_almanac"));

        List<Filterable<Component>> pages = new ArrayList<>();
        for (int page = 1; page <= PAGE_COUNT; page++) {
            pages.add(Filterable.passThrough(Component.translatable("item.bannermod.banner_almanac.page_" + page)));
        }
        book.set(DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(Filterable.passThrough(BOOK_TITLE), BOOK_AUTHOR, 0, pages, true));
        return book;
    }
}
