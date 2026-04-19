package com.talhanation.bannermod.settlement;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BannerModSettlementDesiredGoodsSeedTest {

    @Test
    void desiredGoodsSeedRoundTripsPersistedDrivers() {
        BannerModSettlementDesiredGoodsSeed original = new BannerModSettlementDesiredGoodsSeed(List.of(
                new BannerModSettlementDesiredGoodSeed("food", 2),
                new BannerModSettlementDesiredGoodSeed("storage_type:merchants", 1),
                new BannerModSettlementDesiredGoodSeed("market_goods", 3)
        ));

        CompoundTag tag = original.toTag();
        BannerModSettlementDesiredGoodsSeed restored = BannerModSettlementDesiredGoodsSeed.fromTag(tag);

        assertEquals(original, restored);
    }
}
