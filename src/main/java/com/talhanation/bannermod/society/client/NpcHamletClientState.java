package com.talhanation.bannermod.society.client;

import com.talhanation.bannermod.society.NpcHamletRecord;
import com.talhanation.bannermod.society.NpcHamletSnapshotContract;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NpcHamletClientState {
    private static List<NpcHamletRecord> hamlets = List.of();
    private static Map<UUID, NpcHamletRecord> hamletsById = Map.of();
    private static boolean hasClaim;
    private static boolean canManage;
    private static boolean hasSnapshot;
    private static boolean syncPending;
    private static String denialKey = "";
    @Nullable
    private static UUID claimUuid;
    private static int version;

    private NpcHamletClientState() {
    }

    public static List<NpcHamletRecord> hamlets() {
        return hamlets;
    }

    public static @Nullable NpcHamletRecord hamletById(@Nullable UUID hamletId) {
        return hamletId == null ? null : hamletsById.get(hamletId);
    }

    public static boolean hasClaim() {
        return hasClaim;
    }

    public static boolean canManage() {
        return canManage;
    }

    public static boolean hasSnapshot() {
        return hasSnapshot;
    }

    public static boolean syncPending() {
        return syncPending;
    }

    public static String denialKey() {
        return denialKey;
    }

    public static @Nullable UUID claimUuid() {
        return claimUuid;
    }

    public static int version() {
        return version;
    }

    public static void beginSync() {
        syncPending = true;
    }

    public static void clear() {
        hamlets = List.of();
        hamletsById = Map.of();
        hasClaim = false;
        canManage = false;
        hasSnapshot = false;
        syncPending = false;
        denialKey = "";
        claimUuid = null;
        version++;
    }

    public static void applyFromNbt(CompoundTag tag) {
        if (tag == null) {
            clear();
            return;
        }
        List<NpcHamletRecord> decoded = new ArrayList<>();
        for (Tag entry : tag.getList(NpcHamletSnapshotContract.NBT_HAMLETS, Tag.TAG_COMPOUND)) {
            decoded.add(NpcHamletRecord.fromTag((CompoundTag) entry));
        }
        hamlets = List.copyOf(decoded);
        Map<UUID, NpcHamletRecord> byId = new HashMap<>();
        for (NpcHamletRecord hamlet : decoded) {
            byId.put(hamlet.hamletId(), hamlet);
        }
        hamletsById = Map.copyOf(byId);
        hasClaim = tag.getBoolean(NpcHamletSnapshotContract.NBT_HAS_CLAIM);
        claimUuid = hasClaim && tag.contains(NpcHamletSnapshotContract.NBT_CLAIM_UUID)
                ? tag.getUUID(NpcHamletSnapshotContract.NBT_CLAIM_UUID)
                : null;
        canManage = tag.getBoolean(NpcHamletSnapshotContract.NBT_CAN_MANAGE);
        denialKey = tag.contains(NpcHamletSnapshotContract.NBT_DENIAL_KEY)
                ? tag.getString(NpcHamletSnapshotContract.NBT_DENIAL_KEY)
                : "";
        hasSnapshot = true;
        syncPending = false;
        version++;
    }
}
