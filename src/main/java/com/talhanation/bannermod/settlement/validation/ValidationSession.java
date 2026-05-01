package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ValidationSession(
        UUID playerId,
        SurveyorMode mode,
        BlockPos anchorPos,
        List<ZoneSelection> selections
) {
    public ValidationSession {
        playerId = playerId == null ? new UUID(0L, 0L) : playerId;
        mode = mode == null ? SurveyorMode.BOOTSTRAP_FORT : mode;
        anchorPos = anchorPos == null ? BlockPos.ZERO : anchorPos;
        selections = List.copyOf(selections == null ? List.of() : selections);
    }

    public ValidationSession withAnchor(BlockPos newAnchor) {
        return new ValidationSession(this.playerId, this.mode, newAnchor, this.selections);
    }

    public ValidationSession withMode(SurveyorMode newMode) {
        return new ValidationSession(this.playerId, newMode, this.anchorPos, this.selections);
    }

    public ValidationSession withSelections(List<ZoneSelection> newSelections) {
        return new ValidationSession(this.playerId, this.mode, this.anchorPos, newSelections);
    }

    public ValidationSession addSelection(ZoneSelection selection) {
        if (selection == null) {
            return this;
        }
        List<ZoneSelection> next = new ArrayList<>(this.selections);
        next.add(selection);
        return withSelections(next);
    }

    public ValidationSession upsertSelection(ZoneRole role, BlockPos min, BlockPos max, BlockPos marker) {
        if (role == null || min == null || max == null) {
            return this;
        }
        List<ZoneSelection> next = new ArrayList<>(this.selections);
        next.removeIf(selection -> selection.role() == role);
        next.add(new ZoneSelection(role, min, max, marker));
        return withSelections(next);
    }

    public ValidationSession withoutSelection(ZoneRole role) {
        if (role == null) {
            return this;
        }
        List<ZoneSelection> next = new ArrayList<>(this.selections);
        next.removeIf(selection -> selection.role() == role);
        return withSelections(next);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PlayerId", this.playerId);
        tag.putString("Mode", this.mode.name());
        tag.putLong("AnchorPos", this.anchorPos.asLong());
        ListTag selectionsTag = new ListTag();
        for (ZoneSelection selection : this.selections) {
            selectionsTag.add(selection.toTag());
        }
        tag.put("Selections", selectionsTag);
        return tag;
    }

    public static ValidationSession fromTag(CompoundTag tag) {
        UUID playerId = tag.hasUUID("PlayerId") ? tag.getUUID("PlayerId") : new UUID(0L, 0L);
        SurveyorMode mode = parseMode(tag.getString("Mode"));
        BlockPos anchorPos = tag.contains("AnchorPos", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("AnchorPos")) : BlockPos.ZERO;
        List<ZoneSelection> selections = new ArrayList<>();
        ListTag selectionsTag = tag.getList("Selections", Tag.TAG_COMPOUND);
        for (Tag raw : selectionsTag) {
            if (raw instanceof CompoundTag selectionTag) {
                selections.add(ZoneSelection.fromTag(selectionTag));
            }
        }
        return new ValidationSession(playerId, mode, anchorPos, selections);
    }

    private static SurveyorMode parseMode(String rawMode) {
        try {
            return SurveyorMode.valueOf(rawMode);
        } catch (IllegalArgumentException ex) {
            return SurveyorMode.BOOTSTRAP_FORT;
        }
    }
}
