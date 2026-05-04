package com.talhanation.bannermod.society;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import javax.annotation.Nullable;

public final class NpcHamletSnapshotContract {
    public static final String NBT_HAS_CLAIM = "HasClaim";
    public static final String NBT_CLAIM_UUID = "ClaimUuid";
    public static final String NBT_CAN_MANAGE = "CanManage";
    public static final String NBT_DENIAL_KEY = "DenialKey";
    public static final String NBT_HAMLETS = "Hamlets";

    private NpcHamletSnapshotContract() {
    }

    public static CompoundTag encode(@Nullable java.util.UUID claimUuid,
                                     boolean canManage,
                                     @Nullable String denialKey,
                                     Iterable<NpcHamletRecord> hamlets) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(NBT_HAS_CLAIM, claimUuid != null);
        if (claimUuid != null) {
            tag.putUUID(NBT_CLAIM_UUID, claimUuid);
        }
        tag.putBoolean(NBT_CAN_MANAGE, canManage);
        if (denialKey != null && !denialKey.isBlank()) {
            tag.putString(NBT_DENIAL_KEY, denialKey);
        }
        ListTag hamletTags = new ListTag();
        if (hamlets != null) {
            for (NpcHamletRecord hamlet : hamlets) {
                if (hamlet != null) {
                    hamletTags.add(hamlet.toTag());
                }
            }
        }
        tag.put(NBT_HAMLETS, hamletTags);
        return tag;
    }
}
