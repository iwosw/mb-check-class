package com.talhanation.bannermod.settlement.building;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public record ZoneSelection(
        ZoneRole role,
        BlockPos min,
        BlockPos max,
        @Nullable BlockPos marker
) {
    public ZoneSelection {
        role = role == null ? ZoneRole.INTERIOR : role;
        min = min == null ? BlockPos.ZERO : min;
        max = max == null ? min : max;
    }

    public boolean contains(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        int minX = Math.min(this.min.getX(), this.max.getX());
        int minY = Math.min(this.min.getY(), this.max.getY());
        int minZ = Math.min(this.min.getZ(), this.max.getZ());
        int maxX = Math.max(this.min.getX(), this.max.getX());
        int maxY = Math.max(this.min.getY(), this.max.getY());
        int maxZ = Math.max(this.min.getZ(), this.max.getZ());
        return pos.getX() >= minX && pos.getX() <= maxX
                && pos.getY() >= minY && pos.getY() <= maxY
                && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    public AABB toAabb() {
        int minX = Math.min(this.min.getX(), this.max.getX());
        int minY = Math.min(this.min.getY(), this.max.getY());
        int minZ = Math.min(this.min.getZ(), this.max.getZ());
        int maxX = Math.max(this.min.getX(), this.max.getX());
        int maxY = Math.max(this.min.getY(), this.max.getY());
        int maxZ = Math.max(this.min.getZ(), this.max.getZ());
        return new AABB(minX, minY, minZ, maxX + 1.0D, maxY + 1.0D, maxZ + 1.0D);
    }

    public int volume() {
        int dx = Math.abs(this.max.getX() - this.min.getX()) + 1;
        int dy = Math.abs(this.max.getY() - this.min.getY()) + 1;
        int dz = Math.abs(this.max.getZ() - this.min.getZ()) + 1;
        return dx * dy * dz;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Role", this.role.name());
        tag.putLong("Min", this.min.asLong());
        tag.putLong("Max", this.max.asLong());
        if (this.marker != null) {
            tag.putLong("Marker", this.marker.asLong());
        }
        return tag;
    }

    public static ZoneSelection fromTag(CompoundTag tag) {
        ZoneRole role = tag.contains("Role", Tag.TAG_STRING)
                ? safeRole(tag.getString("Role"))
                : ZoneRole.INTERIOR;
        BlockPos min = tag.contains("Min", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("Min")) : BlockPos.ZERO;
        BlockPos max = tag.contains("Max", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("Max")) : min;
        BlockPos marker = tag.contains("Marker", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("Marker")) : null;
        return new ZoneSelection(role, min, max, marker);
    }

    private static ZoneRole safeRole(String raw) {
        try {
            return ZoneRole.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return ZoneRole.INTERIOR;
        }
    }
}
