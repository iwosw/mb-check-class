package com.talhanation.bannermod.settlement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public record BannerModSettlementTradeRouteHandoffSeed(
        int sellerDispatchCount,
        int readySellerDispatchCount,
        int routedStorageCount,
        int portEntrypointCount,
        List<BannerModSettlementDesiredGoodSeed> desiredGoods,
        List<BannerModSettlementSellerDispatchRecord> sellerDispatches
) {
    public BannerModSettlementTradeRouteHandoffSeed {
        sellerDispatchCount = Math.max(0, sellerDispatchCount);
        readySellerDispatchCount = Math.max(0, Math.min(readySellerDispatchCount, sellerDispatchCount));
        routedStorageCount = Math.max(0, routedStorageCount);
        portEntrypointCount = Math.max(0, portEntrypointCount);
        desiredGoods = List.copyOf(desiredGoods == null ? List.of() : desiredGoods);
        sellerDispatches = List.copyOf(sellerDispatches == null ? List.of() : sellerDispatches);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SellerDispatchCount", this.sellerDispatchCount);
        tag.putInt("ReadySellerDispatchCount", this.readySellerDispatchCount);
        tag.putInt("RoutedStorageCount", this.routedStorageCount);
        tag.putInt("PortEntrypointCount", this.portEntrypointCount);

        ListTag desiredGoodsList = new ListTag();
        for (BannerModSettlementDesiredGoodSeed desiredGood : this.desiredGoods) {
            desiredGoodsList.add(desiredGood.toTag());
        }
        tag.put("DesiredGoods", desiredGoodsList);

        ListTag sellerDispatchList = new ListTag();
        for (BannerModSettlementSellerDispatchRecord sellerDispatch : this.sellerDispatches) {
            sellerDispatchList.add(sellerDispatch.toTag());
        }
        tag.put("SellerDispatches", sellerDispatchList);
        return tag;
    }

    public static BannerModSettlementTradeRouteHandoffSeed fromTag(CompoundTag tag) {
        return new BannerModSettlementTradeRouteHandoffSeed(
                tag.getInt("SellerDispatchCount"),
                tag.getInt("ReadySellerDispatchCount"),
                tag.getInt("RoutedStorageCount"),
                tag.getInt("PortEntrypointCount"),
                readDesiredGoods(tag.getList("DesiredGoods", Tag.TAG_COMPOUND)),
                readSellerDispatches(tag.getList("SellerDispatches", Tag.TAG_COMPOUND))
        );
    }

    public static BannerModSettlementTradeRouteHandoffSeed empty() {
        return new BannerModSettlementTradeRouteHandoffSeed(0, 0, 0, 0, List.of(), List.of());
    }

    private static List<BannerModSettlementDesiredGoodSeed> readDesiredGoods(ListTag list) {
        List<BannerModSettlementDesiredGoodSeed> desiredGoods = new ArrayList<>();
        for (Tag entry : list) {
            desiredGoods.add(BannerModSettlementDesiredGoodSeed.fromTag((CompoundTag) entry));
        }
        return desiredGoods;
    }

    private static List<BannerModSettlementSellerDispatchRecord> readSellerDispatches(ListTag list) {
        List<BannerModSettlementSellerDispatchRecord> sellerDispatches = new ArrayList<>();
        for (Tag entry : list) {
            sellerDispatches.add(BannerModSettlementSellerDispatchRecord.fromTag((CompoundTag) entry));
        }
        return sellerDispatches;
    }
}
