package com.talhanation.bannermod.entity.military;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

interface RecruitOwnershipAccess {
    private AbstractRecruitEntity recruit() {
        return (AbstractRecruitEntity) this;
    }

    default int getCost() {
        return RecruitStateAccess.getCost(recruit());
    }

    default void setCost(int cost) {
        RecruitStateAccess.setCost(recruit(), cost);
    }

    default UUID getUpkeepUUID() {
        return RecruitStateAccess.getUpkeepUUID(recruit());
    }

    default void setUpkeepUUID(Optional<UUID> id) {
        RecruitStateAccess.setUpkeepUUID(recruit(), id);
    }

    default BlockPos getUpkeepPos() {
        return RecruitStateAccess.getUpkeepPos(recruit());
    }

    default void setUpkeepPos(BlockPos pos) {
        RecruitStateAccess.setUpkeepPos(recruit(), pos);
    }

    default void clearUpkeepPos() {
        RecruitStateAccess.clearUpkeepPos(recruit());
    }

    default void clearUpkeepEntity() {
        RecruitStateAccess.clearUpkeepEntity(recruit());
    }

    default boolean hasUpkeep() {
        return this.getUpkeepPos() != null || this.getUpkeepUUID() != null;
    }

    @Nullable
    default Player getOwner() {
        UUID ownerId = this.getOwnerUUID();
        return ownerId == null ? null : recruit().getCommandSenderWorld().getPlayerByUUID(ownerId);
    }

    default UUID getOwnerUUID() {
        return RecruitStateAccess.getOwnerUUID(recruit());
    }

    default void setOwnerUUID(Optional<UUID> id) {
        RecruitStateAccess.setOwnerUUID(recruit(), id);
    }

    default boolean getIsOwned() {
        return RecruitStateAccess.getIsOwned(recruit());
    }

    default void setIsOwned(boolean value) {
        RecruitStateAccess.setIsOwned(recruit(), value);
    }

    default boolean isOwned() {
        return this.getIsOwned();
    }

    default boolean isOwnedBy(Player player) {
        return player.getUUID() == this.getOwnerUUID() || player == this.getOwner();
    }

    default UUID getGroup() {
        Optional<UUID> groupUuid = this.getGroupUUID();
        return groupUuid.isPresent() ? groupUuid.get() : null;
    }

    default Optional<UUID> getGroupUUID() {
        return RecruitStateAccess.getGroupUUID(recruit());
    }

    default void setGroupUUID(UUID uuid) {
        RecruitStateAccess.setGroupUUID(recruit(), uuid);
    }

    default int getKills() {
        return RecruitStateAccess.getKills(recruit());
    }

    default void setKills(int kills) {
        RecruitStateAccess.setKills(recruit(), kills);
    }

    default int getXpLevel() {
        return RecruitStateAccess.getXpLevel(recruit());
    }

    default void setXpLevel(int xpLevel) {
        RecruitStateAccess.setXpLevel(recruit(), xpLevel);
    }

    default int getXp() {
        return RecruitStateAccess.getXp(recruit());
    }

    default void setXp(int xp) {
        RecruitStateAccess.setXp(recruit(), xp);
    }

    default void addXp(int xp) {
        RecruitStateAccess.setXp(recruit(), this.getXp() + xp);
    }
}
