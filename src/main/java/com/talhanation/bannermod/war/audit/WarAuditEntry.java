package com.talhanation.bannermod.war.audit;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record WarAuditEntry(
        UUID id,
        UUID warId,
        String type,
        String detail,
        long gameTime
) {
    public WarAuditEntry {
        type = type == null ? "" : type;
        detail = detail == null ? "" : detail;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        if (warId != null) {
            tag.putUUID("WarId", warId);
        }
        tag.putString("Type", type);
        tag.putString("Detail", detail);
        tag.putLong("GameTime", gameTime);
        return tag;
    }

    public static WarAuditEntry fromTag(CompoundTag tag) {
        UUID warId = tag.hasUUID("WarId") ? tag.getUUID("WarId") : null;
        return new WarAuditEntry(
                tag.getUUID("Id"),
                warId,
                tag.getString("Type"),
                tag.getString("Detail"),
                tag.getLong("GameTime")
        );
    }
}
