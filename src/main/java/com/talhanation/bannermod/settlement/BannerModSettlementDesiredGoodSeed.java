package com.talhanation.bannermod.settlement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record BannerModSettlementDesiredGoodSeed(
        String desiredGoodId,
        int driverCount
) {
    public BannerModSettlementDesiredGoodSeed {
        desiredGoodId = desiredGoodId == null ? "" : desiredGoodId;
        driverCount = Math.max(0, driverCount);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        if (!this.desiredGoodId.isBlank()) {
            tag.putString("DesiredGoodId", this.desiredGoodId);
        }
        tag.putInt("DriverCount", this.driverCount);
        return tag;
    }

    public static BannerModSettlementDesiredGoodSeed fromTag(CompoundTag tag) {
        return new BannerModSettlementDesiredGoodSeed(
                tag.contains("DesiredGoodId", Tag.TAG_STRING) ? tag.getString("DesiredGoodId") : "",
                tag.getInt("DriverCount")
        );
    }
}
