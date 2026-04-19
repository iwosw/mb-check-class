package com.talhanation.bannermod.settlement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public record BannerModSettlementDesiredGoodsSeed(
        List<BannerModSettlementDesiredGoodSeed> desiredGoods
) {
    public BannerModSettlementDesiredGoodsSeed {
        desiredGoods = List.copyOf(desiredGoods == null ? List.of() : desiredGoods);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag desiredGoodsList = new ListTag();
        for (BannerModSettlementDesiredGoodSeed desiredGood : this.desiredGoods) {
            desiredGoodsList.add(desiredGood.toTag());
        }
        tag.put("DesiredGoods", desiredGoodsList);
        return tag;
    }

    public static BannerModSettlementDesiredGoodsSeed fromTag(CompoundTag tag) {
        List<BannerModSettlementDesiredGoodSeed> desiredGoods = new ArrayList<>();
        for (Tag entry : tag.getList("DesiredGoods", Tag.TAG_COMPOUND)) {
            desiredGoods.add(BannerModSettlementDesiredGoodSeed.fromTag((CompoundTag) entry));
        }
        return new BannerModSettlementDesiredGoodsSeed(desiredGoods);
    }

    public static BannerModSettlementDesiredGoodsSeed empty() {
        return new BannerModSettlementDesiredGoodsSeed(List.of());
    }
}
