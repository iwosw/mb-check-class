package com.talhanation.bannermod.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BannerModSettlementBuildingRecordTest {

    @Test
    void buildingRecordRoundTripsCategoryAndProfileSeed() {
        BannerModSettlementBuildingRecord original = new BannerModSettlementBuildingRecord(
                UUID.randomUUID(),
                "bannermod:market_area",
                new BlockPos(12, 64, 12),
                UUID.randomUUID(),
                "blueguild",
                0,
                1,
                1,
                List.of(UUID.randomUUID()),
                true,
                3,
                81,
                true,
                true,
                List.of("food", "materials"),
                BannerModSettlementBuildingCategory.MARKET,
                BannerModSettlementBuildingProfileSeed.MARKET
        );

        BannerModSettlementBuildingRecord restored = BannerModSettlementBuildingRecord.fromTag(original.toTag());

        assertEquals(original, restored);
    }

    @Test
    void buildingRecordDefaultsLegacyProfileSeedFromBuildingType() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("BuildingUuid", UUID.randomUUID());
        tag.putString("BuildingTypeId", "bannermod:storage_area");
        tag.putLong("OriginPos", new BlockPos(4, 70, 4).asLong());
        tag.putInt("ResidentCapacity", 0);
        tag.putInt("WorkplaceSlots", 1);
        tag.putInt("AssignedWorkerCount", 0);

        BannerModSettlementBuildingRecord restored = BannerModSettlementBuildingRecord.fromTag(tag);

        assertEquals(BannerModSettlementBuildingCategory.STORAGE, restored.buildingCategory());
        assertEquals(BannerModSettlementBuildingProfileSeed.STORAGE, restored.buildingProfileSeed());
    }
}
