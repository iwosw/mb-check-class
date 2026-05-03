package com.talhanation.bannermod.society;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

public record NpcMemoryRecord(
        UUID residentUuid,
        NpcSocialMemoryType type,
        NpcSocialMemoryScope scope,
        @Nullable UUID actorUuid,
        int intensity,
        long createdGameTime,
        long expiresGameTime,
        long version,
        long lastUpdatedGameTime
) {
    public NpcMemoryRecord {
        if (residentUuid == null) {
            throw new IllegalArgumentException("residentUuid must not be null");
        }
        type = type == null ? NpcSocialMemoryType.ASSAULTED_BY_PLAYER : type;
        scope = scope == null ? NpcSocialMemoryScope.PERSONAL : scope;
        intensity = clampIntensity(intensity);
        if (expiresGameTime <= createdGameTime) {
            expiresGameTime = createdGameTime + 1L;
        }
        version = Math.max(1L, version);
    }

    public static NpcMemoryRecord create(UUID residentUuid,
                                         NpcSocialMemoryType type,
                                         NpcSocialMemoryScope scope,
                                         @Nullable UUID actorUuid,
                                         int intensity,
                                         long createdGameTime,
                                         long expiresGameTime) {
        return new NpcMemoryRecord(
                residentUuid,
                type,
                scope,
                actorUuid,
                intensity,
                createdGameTime,
                expiresGameTime,
                1L,
                createdGameTime
        );
    }

    public boolean sameKey(NpcMemoryRecord other) {
        return other != null
                && this.type == other.type
                && this.scope == other.scope
                && sameNullableUuid(this.actorUuid, other.actorUuid);
    }

    public boolean isExpired(long gameTime) {
        return gameTime >= this.expiresGameTime;
    }

    public long durationTicks() {
        return Math.max(1L, this.expiresGameTime - this.createdGameTime);
    }

    public NpcMemoryRecord mergeRefresh(NpcMemoryRecord candidate, long gameTime) {
        if (candidate == null || !sameKey(candidate)) {
            return this;
        }
        int mergedIntensity = Math.max(this.intensity, candidate.intensity);
        long mergedCreated = Math.min(this.createdGameTime, candidate.createdGameTime);
        long mergedExpires = Math.max(this.expiresGameTime, candidate.expiresGameTime);
        if (mergedIntensity == this.intensity
                && mergedCreated == this.createdGameTime
                && mergedExpires == this.expiresGameTime
                && this.lastUpdatedGameTime == gameTime) {
            return this;
        }
        return new NpcMemoryRecord(
                this.residentUuid,
                this.type,
                this.scope,
                this.actorUuid,
                mergedIntensity,
                mergedCreated,
                mergedExpires,
                this.version + 1L,
                gameTime
        );
    }

    public NpcMemoryRecord moveResident(UUID toResidentUuid, long gameTime) {
        if (toResidentUuid == null || toResidentUuid.equals(this.residentUuid)) {
            return this;
        }
        return new NpcMemoryRecord(
                toResidentUuid,
                this.type,
                this.scope,
                this.actorUuid,
                this.intensity,
                this.createdGameTime,
                this.expiresGameTime,
                this.version + 1L,
                gameTime
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ResidentUuid", this.residentUuid);
        tag.putString("Type", this.type.name());
        tag.putString("Scope", this.scope.name());
        if (this.actorUuid != null) {
            tag.putUUID("ActorUuid", this.actorUuid);
        }
        tag.putInt("Intensity", this.intensity);
        tag.putLong("CreatedGameTime", this.createdGameTime);
        tag.putLong("ExpiresGameTime", this.expiresGameTime);
        tag.putLong("Version", this.version);
        tag.putLong("LastUpdatedGameTime", this.lastUpdatedGameTime);
        return tag;
    }

    public static NpcMemoryRecord fromTag(CompoundTag tag) {
        UUID residentUuid = tag.getUUID("ResidentUuid");
        long createdGameTime = tag.getLong("CreatedGameTime");
        long expiresGameTime = tag.getLong("ExpiresGameTime");
        if (expiresGameTime <= createdGameTime) {
            expiresGameTime = createdGameTime + 1L;
        }
        return new NpcMemoryRecord(
                residentUuid,
                NpcSocialMemoryType.fromName(tag.getString("Type")),
                NpcSocialMemoryScope.fromName(tag.getString("Scope")),
                tag.contains("ActorUuid") ? tag.getUUID("ActorUuid") : null,
                clampIntensity(tag.getInt("Intensity")),
                createdGameTime,
                expiresGameTime,
                Math.max(1L, tag.getLong("Version")),
                tag.getLong("LastUpdatedGameTime")
        );
    }

    private static boolean sameNullableUuid(@Nullable UUID left, @Nullable UUID right) {
        return left == null ? right == null : left.equals(right);
    }

    private static int clampIntensity(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
