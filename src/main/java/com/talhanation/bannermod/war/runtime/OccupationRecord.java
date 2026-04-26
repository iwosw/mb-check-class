package com.talhanation.bannermod.war.runtime;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record OccupationRecord(
        UUID id,
        UUID warId,
        UUID occupierEntityId,
        UUID occupiedEntityId,
        List<ChunkPos> chunks,
        long startedAtGameTime
) {
    public OccupationRecord {
        chunks = chunks == null ? List.of() : List.copyOf(chunks);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("WarId", warId);
        tag.putUUID("Occupier", occupierEntityId);
        tag.putUUID("Occupied", occupiedEntityId);
        long[] packed = new long[chunks.size()];
        for (int i = 0; i < chunks.size(); i++) {
            packed[i] = chunks.get(i).toLong();
        }
        tag.put("Chunks", new LongArrayTag(packed));
        tag.putLong("StartedAtGameTime", startedAtGameTime);
        return tag;
    }

    public static OccupationRecord fromTag(CompoundTag tag) {
        long[] packed = tag.getLongArray("Chunks");
        List<ChunkPos> chunks = new ArrayList<>(packed.length);
        for (long pack : packed) {
            chunks.add(new ChunkPos(pack));
        }
        return new OccupationRecord(
                tag.getUUID("Id"),
                tag.getUUID("WarId"),
                tag.getUUID("Occupier"),
                tag.getUUID("Occupied"),
                chunks,
                tag.getLong("StartedAtGameTime")
        );
    }
}
