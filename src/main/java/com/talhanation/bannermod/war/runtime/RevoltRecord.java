package com.talhanation.bannermod.war.runtime;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record RevoltRecord(
        UUID id,
        UUID occupationId,
        UUID rebelEntityId,
        UUID occupierEntityId,
        long scheduledAtGameTime,
        long resolvedAtGameTime,
        RevoltState state
) {
    public RevoltRecord {
        state = state == null ? RevoltState.PENDING : state;
    }

    public RevoltRecord withState(RevoltState newState, long newResolvedAtGameTime) {
        return new RevoltRecord(id, occupationId, rebelEntityId, occupierEntityId,
                scheduledAtGameTime, newResolvedAtGameTime, newState);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("Occupation", occupationId);
        tag.putUUID("Rebel", rebelEntityId);
        tag.putUUID("Occupier", occupierEntityId);
        tag.putLong("ScheduledAtGameTime", scheduledAtGameTime);
        tag.putLong("ResolvedAtGameTime", resolvedAtGameTime);
        tag.putString("State", state.name());
        return tag;
    }

    public static RevoltRecord fromTag(CompoundTag tag) {
        RevoltState state = RevoltState.PENDING;
        try {
            state = RevoltState.valueOf(tag.getString("State"));
        } catch (IllegalArgumentException ignored) {
            // unknown state falls back to PENDING
        }
        return new RevoltRecord(
                tag.getUUID("Id"),
                tag.getUUID("Occupation"),
                tag.getUUID("Rebel"),
                tag.getUUID("Occupier"),
                tag.getLong("ScheduledAtGameTime"),
                tag.getLong("ResolvedAtGameTime"),
                state
        );
    }
}
