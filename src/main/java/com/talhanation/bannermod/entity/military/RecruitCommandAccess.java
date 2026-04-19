package com.talhanation.bannermod.entity.military;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

interface RecruitCommandAccess {
    private AbstractRecruitEntity recruit() {
        return (AbstractRecruitEntity) this;
    }

    default UUID getProtectUUID() {
        return RecruitStateAccess.getProtectUUID(recruit());
    }

    default UUID getMountUUID() {
        return RecruitStateAccess.getMountUUID(recruit());
    }

    default boolean getShouldMovePos() {
        return RecruitStateAccess.getShouldMovePos(recruit());
    }

    default boolean getShouldHoldPos() {
        return RecruitStateAccess.getShouldHoldPos(recruit());
    }

    default boolean getShouldMount() {
        return RecruitStateAccess.getShouldMount(recruit());
    }

    default boolean getShouldProtect() {
        return RecruitStateAccess.getShouldProtect(recruit());
    }

    default boolean getShouldFollow() {
        return RecruitStateAccess.getShouldFollow(recruit());
    }

    default boolean getShouldBlock() {
        return RecruitStateAccess.getShouldBlock(recruit());
    }

    default boolean isFollowing() {
        return RecruitStateAccess.isFollowing(recruit());
    }

    default boolean getShouldRest() {
        return RecruitStateAccess.getShouldRest(recruit());
    }

    default boolean getShouldRanged() {
        return RecruitStateAccess.getShouldRanged(recruit());
    }

    default int getState() {
        return RecruitStateAccess.getState(recruit());
    }

    default int getFollowState() {
        return RecruitStateAccess.getFollowState(recruit());
    }

    @Nullable
    default Vec3 getHoldPos() {
        return recruit().holdPosVec;
    }

    @Nullable
    default BlockPos getMovePos() {
        return RecruitStateAccess.getMovePos(recruit());
    }

    default boolean getListen() {
        return RecruitStateAccess.getListen(recruit());
    }

    default void setProtectUUID(Optional<UUID> id) {
        RecruitStateAccess.setProtectUUID(recruit(), id);
    }

    default void setMountUUID(Optional<UUID> id) {
        RecruitStateAccess.setMountUUID(recruit(), id);
    }

    default void setShouldHoldPos(boolean value) {
        RecruitStateAccess.setShouldHoldPos(recruit(), value);
    }

    default void setShouldMovePos(boolean value) {
        RecruitStateAccess.setShouldMovePos(recruit(), value);
    }

    default void setShouldProtect(boolean value) {
        RecruitStateAccess.setShouldProtect(recruit(), value);
    }

    default void setShouldMount(boolean value) {
        RecruitStateAccess.setShouldMount(recruit(), value);
    }

    default void setShouldFollow(boolean value) {
        RecruitStateAccess.setShouldFollow(recruit(), value);
    }

    default void setShouldBlock(boolean value) {
        RecruitStateAccess.setShouldBlock(recruit(), value);
    }

    default void setIsFollowing(boolean value) {
        RecruitStateAccess.setIsFollowing(recruit(), value);
    }

    default void setShouldRest(boolean value) {
        if (value) {
            setFollowState(0);
        }
        RecruitStateAccess.setShouldRest(recruit(), value);
    }

    default void setShouldRanged(boolean value) {
        RecruitStateAccess.setShouldRanged(recruit(), value);
    }

    default void setAggroState(int state) {
        RecruitCommandStateService.setAggroState(recruit(), state);
    }

    default void setFollowState(int state) {
        RecruitCommandStateService.setFollowState(recruit(), state);
    }

    default void setHoldPos(Vec3 holdPos) {
        RecruitCommandStateService.setHoldPos(recruit(), holdPos);
    }

    default void setMovePos(BlockPos movePos) {
        RecruitCommandStateService.setMovePos(recruit(), movePos);
    }

    default void clearHoldPos() {
        RecruitCommandStateService.clearHoldPos(recruit());
    }

    default void clearMovePos() {
        RecruitCommandStateService.clearMovePos(recruit());
    }

    default void setListen(boolean value) {
        RecruitStateAccess.setListen(recruit(), value);
    }

    default void shouldMount(boolean should, UUID mountUuid) {
        RecruitCommandStateService.shouldMount(recruit(), should, mountUuid);
    }

    default void shouldProtect(boolean should, UUID protectUuid) {
        RecruitCommandStateService.shouldProtect(recruit(), should, protectUuid);
    }
}
