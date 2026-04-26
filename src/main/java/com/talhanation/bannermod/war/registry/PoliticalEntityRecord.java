package com.talhanation.bannermod.war.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Immutable political entity registration record for regulated warfare-RP. */
public record PoliticalEntityRecord(
        UUID id,
        String name,
        PoliticalEntityStatus status,
        UUID leaderUuid,
        List<UUID> coLeaderUuids,
        BlockPos capitalPos,
        String color,
        String charter,
        String ideology,
        String homeRegion,
        long createdAtGameTime,
        GovernmentForm governmentForm
) {
    public PoliticalEntityRecord {
        coLeaderUuids = coLeaderUuids == null ? List.of() : List.copyOf(coLeaderUuids);
        status = status == null ? PoliticalEntityStatus.SETTLEMENT : status;
        name = name == null ? "" : name.trim();
        color = color == null ? "" : color.trim();
        charter = charter == null ? "" : charter.trim();
        ideology = ideology == null ? "" : ideology.trim();
        homeRegion = homeRegion == null ? "" : homeRegion.trim();
        governmentForm = governmentForm == null ? GovernmentForm.MONARCHY : governmentForm;
    }

    /** Backwards-compatible constructor used by older call sites that pre-date POL-001. */
    public PoliticalEntityRecord(UUID id,
                                 String name,
                                 PoliticalEntityStatus status,
                                 UUID leaderUuid,
                                 List<UUID> coLeaderUuids,
                                 BlockPos capitalPos,
                                 String color,
                                 String charter,
                                 String ideology,
                                 String homeRegion,
                                 long createdAtGameTime) {
        this(id, name, status, leaderUuid, coLeaderUuids, capitalPos, color, charter, ideology,
                homeRegion, createdAtGameTime, GovernmentForm.MONARCHY);
    }

    public PoliticalEntityRecord withStatus(PoliticalEntityStatus newStatus) {
        return new PoliticalEntityRecord(id, name, newStatus, leaderUuid, coLeaderUuids, capitalPos,
                color, charter, ideology, homeRegion, createdAtGameTime, governmentForm);
    }

    public PoliticalEntityRecord withCapital(BlockPos newCapital) {
        return new PoliticalEntityRecord(id, name, status, leaderUuid, coLeaderUuids,
                newCapital == null ? capitalPos : newCapital.immutable(),
                color, charter, ideology, homeRegion, createdAtGameTime, governmentForm);
    }

    public PoliticalEntityRecord withName(String newName) {
        return new PoliticalEntityRecord(id, newName == null ? name : newName, status, leaderUuid,
                coLeaderUuids, capitalPos, color, charter, ideology, homeRegion, createdAtGameTime, governmentForm);
    }

    public PoliticalEntityRecord withGovernmentForm(GovernmentForm newForm) {
        return new PoliticalEntityRecord(id, name, status, leaderUuid, coLeaderUuids, capitalPos,
                color, charter, ideology, homeRegion, createdAtGameTime,
                newForm == null ? governmentForm : newForm);
    }

    public PoliticalEntityRecord withColor(String newColor) {
        return new PoliticalEntityRecord(id, name, status, leaderUuid, coLeaderUuids, capitalPos,
                newColor == null ? color : newColor,
                charter, ideology, homeRegion, createdAtGameTime, governmentForm);
    }

    public PoliticalEntityRecord withCharter(String newCharter) {
        return new PoliticalEntityRecord(id, name, status, leaderUuid, coLeaderUuids, capitalPos,
                color,
                newCharter == null ? charter : newCharter,
                ideology, homeRegion, createdAtGameTime, governmentForm);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Name", name);
        tag.putString("Status", status.name());
        tag.putUUID("Leader", leaderUuid);
        tag.putInt("CapitalX", capitalPos.getX());
        tag.putInt("CapitalY", capitalPos.getY());
        tag.putInt("CapitalZ", capitalPos.getZ());
        tag.putString("Color", color);
        tag.putString("Charter", charter);
        tag.putString("Ideology", ideology);
        tag.putString("HomeRegion", homeRegion);
        tag.putLong("CreatedAtGameTime", createdAtGameTime);
        tag.putString("GovernmentForm", governmentForm.name());

        net.minecraft.nbt.ListTag coLeaders = new net.minecraft.nbt.ListTag();
        for (UUID coLeaderUuid : coLeaderUuids) {
            CompoundTag coLeaderTag = new CompoundTag();
            coLeaderTag.putUUID("Uuid", coLeaderUuid);
            coLeaders.add(coLeaderTag);
        }
        tag.put("CoLeaders", coLeaders);
        return tag;
    }

    public static PoliticalEntityRecord fromTag(CompoundTag tag) {
        List<UUID> coLeaders = new ArrayList<>();
        net.minecraft.nbt.ListTag coLeaderTags = tag.getList("CoLeaders", Tag.TAG_COMPOUND);
        for (int i = 0; i < coLeaderTags.size(); i++) {
            CompoundTag coLeaderTag = coLeaderTags.getCompound(i);
            if (coLeaderTag.hasUUID("Uuid")) {
                coLeaders.add(coLeaderTag.getUUID("Uuid"));
            }
        }

        PoliticalEntityStatus status = PoliticalEntityStatus.SETTLEMENT;
        try {
            status = PoliticalEntityStatus.valueOf(tag.getString("Status"));
        } catch (IllegalArgumentException ignored) {
            // Unknown saved status falls back to the least privileged registered status.
        }

        GovernmentForm governmentForm = tag.contains("GovernmentForm", Tag.TAG_STRING)
                ? GovernmentForm.fromTagName(tag.getString("GovernmentForm"))
                : GovernmentForm.MONARCHY;

        return new PoliticalEntityRecord(
                tag.getUUID("Id"),
                tag.getString("Name"),
                status,
                tag.getUUID("Leader"),
                coLeaders,
                new BlockPos(tag.getInt("CapitalX"), tag.getInt("CapitalY"), tag.getInt("CapitalZ")),
                tag.getString("Color"),
                tag.getString("Charter"),
                tag.getString("Ideology"),
                tag.getString("HomeRegion"),
                tag.getLong("CreatedAtGameTime"),
                governmentForm
        );
    }
}
