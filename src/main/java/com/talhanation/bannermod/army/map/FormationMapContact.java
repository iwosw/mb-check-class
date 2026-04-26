package com.talhanation.bannermod.army.map;

import com.talhanation.bannermod.army.command.CommandRole;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record FormationMapContact(
        UUID contactId,
        @Nullable UUID groupId,
        @Nullable UUID leaderId,
        @Nullable String teamId,
        FormationMapRelation relation,
        CommandRole commandRole,
        double x,
        double y,
        double z,
        int unitCount,
        int visibleUnitCount,
        int rangedUnitCount
) {
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ContactId", contactId);
        if (groupId != null) tag.putUUID("GroupId", groupId);
        if (leaderId != null) tag.putUUID("LeaderId", leaderId);
        if (teamId != null) tag.putString("TeamId", teamId);
        tag.putString("Relation", relation.name());
        tag.putString("CommandRole", commandRole.name());
        tag.putDouble("X", x);
        tag.putDouble("Y", y);
        tag.putDouble("Z", z);
        tag.putInt("UnitCount", unitCount);
        tag.putInt("VisibleUnitCount", visibleUnitCount);
        tag.putInt("RangedUnitCount", rangedUnitCount);
        return tag;
    }

    public static FormationMapContact fromNbt(CompoundTag tag) {
        return new FormationMapContact(
                tag.getUUID("ContactId"),
                tag.hasUUID("GroupId") ? tag.getUUID("GroupId") : null,
                tag.hasUUID("LeaderId") ? tag.getUUID("LeaderId") : null,
                tag.contains("TeamId") ? tag.getString("TeamId") : null,
                parseRelation(tag.getString("Relation")),
                parseRole(tag.getString("CommandRole")),
                tag.getDouble("X"),
                tag.getDouble("Y"),
                tag.getDouble("Z"),
                tag.getInt("UnitCount"),
                tag.getInt("VisibleUnitCount"),
                tag.getInt("RangedUnitCount")
        );
    }

    public static CompoundTag listToNbt(List<FormationMapContact> contacts) {
        CompoundTag tag = new CompoundTag();
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (FormationMapContact contact : contacts) {
            list.add(contact.toNbt());
        }
        tag.put("Contacts", list);
        return tag;
    }

    public static List<FormationMapContact> listFromNbt(CompoundTag tag) {
        List<FormationMapContact> contacts = new ArrayList<>();
        if (tag == null || !tag.contains("Contacts")) return contacts;
        net.minecraft.nbt.ListTag list = tag.getList("Contacts", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            contacts.add(fromNbt(list.getCompound(i)));
        }
        return contacts;
    }

    private static FormationMapRelation parseRelation(String value) {
        try {
            return FormationMapRelation.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return FormationMapRelation.NEUTRAL;
        }
    }

    private static CommandRole parseRole(String value) {
        try {
            return CommandRole.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return CommandRole.NONE;
        }
    }
}
