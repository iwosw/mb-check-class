package com.talhanation.bannermod.settlement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.UUID;

public record BannerModSettlementSellerDispatchRecord(
        UUID residentUuid,
        UUID marketUuid,
        @Nullable String marketName,
        BannerModSettlementSellerDispatchState dispatchState
) {
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ResidentUuid", this.residentUuid);
        tag.putUUID("MarketUuid", this.marketUuid);
        if (this.marketName != null && !this.marketName.isBlank()) {
            tag.putString("MarketName", this.marketName);
        }
        tag.putString("DispatchState", this.dispatchState.name());
        return tag;
    }

    public static BannerModSettlementSellerDispatchRecord fromTag(CompoundTag tag) {
        return new BannerModSettlementSellerDispatchRecord(
                tag.getUUID("ResidentUuid"),
                tag.getUUID("MarketUuid"),
                tag.contains("MarketName", Tag.TAG_STRING) ? tag.getString("MarketName") : null,
                tag.contains("DispatchState", Tag.TAG_STRING)
                        ? BannerModSettlementSellerDispatchState.valueOf(tag.getString("DispatchState"))
                        : BannerModSettlementSellerDispatchState.READY
        );
    }
}
