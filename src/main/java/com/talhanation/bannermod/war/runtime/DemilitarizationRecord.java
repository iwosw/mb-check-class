package com.talhanation.bannermod.war.runtime;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record DemilitarizationRecord(
        UUID id,
        UUID politicalEntityId,
        UUID imposedByWarId,
        long endsAtGameTime
) {
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("Entity", politicalEntityId);
        if (imposedByWarId != null) {
            tag.putUUID("WarId", imposedByWarId);
        }
        tag.putLong("EndsAtGameTime", endsAtGameTime);
        return tag;
    }

    public static DemilitarizationRecord fromTag(CompoundTag tag) {
        UUID warId = tag.hasUUID("WarId") ? tag.getUUID("WarId") : null;
        return new DemilitarizationRecord(
                tag.getUUID("Id"),
                tag.getUUID("Entity"),
                warId,
                tag.getLong("EndsAtGameTime")
        );
    }
}
