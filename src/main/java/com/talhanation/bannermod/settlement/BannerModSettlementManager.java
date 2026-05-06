package com.talhanation.bannermod.settlement;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BannerModSettlementManager extends SavedData {
    private static final String FILE_ID = "bannermodSettlements";
    private static final SavedData.Factory<BannerModSettlementManager> FACTORY = new SavedData.Factory<>(BannerModSettlementManager::new, BannerModSettlementManager::load);

    private final Map<UUID, BannerModSettlementSnapshot> snapshots = new LinkedHashMap<>();

    public static BannerModSettlementManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static BannerModSettlementManager load(CompoundTag tag, HolderLookup.Provider registries) {
        BannerModSettlementManager manager = new BannerModSettlementManager();
        if (tag.contains("Snapshots", Tag.TAG_LIST)) {
            ListTag snapshots = tag.getList("Snapshots", Tag.TAG_COMPOUND);
            for (Tag entry : snapshots) {
                BannerModSettlementSnapshot snapshot = BannerModSettlementSnapshot.fromTag((CompoundTag) entry);
                manager.snapshots.put(snapshot.claimUuid(), snapshot);
            }
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (BannerModSettlementSnapshot snapshot : this.snapshots.values()) {
            list.add(snapshot.toTag());
        }
        tag.put("Snapshots", list);
        return tag;
    }

    @Nullable
    public BannerModSettlementSnapshot getSnapshot(UUID claimUuid) {
        return this.snapshots.get(claimUuid);
    }

    public void putSnapshot(BannerModSettlementSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        BannerModSettlementSnapshot previous = this.snapshots.put(snapshot.claimUuid(), snapshot);
        if (!snapshot.equals(previous)) {
            this.setDirty();
        }
    }

    @Nullable
    public BannerModSettlementSnapshot removeSnapshot(UUID claimUuid) {
        if (claimUuid == null) {
            return null;
        }
        BannerModSettlementSnapshot removed = this.snapshots.remove(claimUuid);
        if (removed != null) {
            this.setDirty();
        }
        return removed;
    }

    public void pruneMissingClaims(Set<UUID> activeClaimUuids) {
        boolean removed = false;
        for (UUID claimUuid : new ArrayList<>(this.snapshots.keySet())) {
            if (!activeClaimUuids.contains(claimUuid)) {
                this.snapshots.remove(claimUuid);
                removed = true;
            }
        }
        if (removed) {
            this.setDirty();
        }
    }

    public Collection<BannerModSettlementSnapshot> getAllSnapshots() {
        return this.snapshots.values();
    }
}
