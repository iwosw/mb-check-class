package com.talhanation.bannermod.war.cooldown;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * One cooldown attached to a political entity. Entries with the same
 * {@code (politicalEntityId, kind)} are unique — the runtime overwrites them on grant.
 */
public record WarCooldownRecord(UUID id,
                                UUID politicalEntityId,
                                WarCooldownKind kind,
                                long endsAtGameTime) {

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("PoliticalEntityId", politicalEntityId);
        tag.putString("Kind", kind.name());
        tag.putLong("EndsAtGameTime", endsAtGameTime);
        return tag;
    }

    public static WarCooldownRecord fromTag(CompoundTag tag) {
        return new WarCooldownRecord(
                tag.getUUID("Id"),
                tag.getUUID("PoliticalEntityId"),
                WarCooldownKind.fromTagName(tag.getString("Kind")),
                tag.getLong("EndsAtGameTime")
        );
    }
}
