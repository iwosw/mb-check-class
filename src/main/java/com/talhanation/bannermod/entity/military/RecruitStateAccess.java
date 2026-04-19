package com.talhanation.bannermod.entity.military;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.Optional;
import java.util.UUID;

final class RecruitStateAccess {
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOLLOW_STATE = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHOULD_FOLLOW = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_BLOCK = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_MOUNT = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_PROTECT = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_HOLD_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_MOVE_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> HOLD_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> MOVE_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> UPKEEP_POS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> LISTEN = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FOLLOWING = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> MOUNT_ID = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> PROTECT_ID = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> GROUP = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> XP = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> KILLS = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLEEING = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> HUNGER = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MORAL = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> OWNED = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COST = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> UPKEEP_ID = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> BIOME = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> SHOULD_REST = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_RANGED = SynchedEntityData.defineId(AbstractRecruitEntity.class, EntityDataSerializers.BOOLEAN);

    private RecruitStateAccess() {
    }

    static void ensureAccessorsRegistered() {
    }

    static void defineSynchedData(AbstractRecruitEntity recruit) {
        recruit.defineStateData(DATA_REMAINING_ANGER_TIME, 0);
        recruit.defineStateData(GROUP, Optional.empty());
        recruit.defineStateData(SHOULD_FOLLOW, false);
        recruit.defineStateData(SHOULD_BLOCK, false);
        recruit.defineStateData(SHOULD_MOUNT, false);
        recruit.defineStateData(SHOULD_PROTECT, false);
        recruit.defineStateData(SHOULD_HOLD_POS, false);
        recruit.defineStateData(SHOULD_MOVE_POS, false);
        recruit.defineStateData(FLEEING, false);
        recruit.defineStateData(STATE, 0);
        recruit.defineStateData(VARIANT, 0);
        recruit.defineStateData(XP, 0);
        recruit.defineStateData(KILLS, 0);
        recruit.defineStateData(LEVEL, 1);
        recruit.defineStateData(FOLLOW_STATE, 0);
        recruit.defineStateData(HOLD_POS, Optional.empty());
        recruit.defineStateData(UPKEEP_POS, Optional.empty());
        recruit.defineStateData(MOVE_POS, Optional.empty());
        recruit.defineStateData(LISTEN, true);
        recruit.defineStateData(MOUNT_ID, Optional.empty());
        recruit.defineStateData(PROTECT_ID, Optional.empty());
        recruit.defineStateData(IS_FOLLOWING, false);
        recruit.defineStateData(HUNGER, 50F);
        recruit.defineStateData(MORAL, 50F);
        recruit.defineStateData(OWNER_ID, Optional.empty());
        recruit.defineStateData(UPKEEP_ID, Optional.empty());
        recruit.defineStateData(OWNED, false);
        recruit.defineStateData(COST, 1);
        recruit.defineStateData(COLOR, (byte) 0);
        recruit.defineStateData(BIOME, (byte) 0);
        recruit.defineStateData(SHOULD_REST, false);
        recruit.defineStateData(SHOULD_RANGED, true);
    }

    static int getVariant(AbstractRecruitEntity recruit) { return recruit.getStateData(VARIANT); }
    static void setVariant(AbstractRecruitEntity recruit, int variant) { recruit.setStateData(VARIANT, variant); }
    static int getCost(AbstractRecruitEntity recruit) { return recruit.getStateData(COST); }
    static void setCost(AbstractRecruitEntity recruit, int cost) { recruit.setStateData(COST, cost); }
    static int getColor(AbstractRecruitEntity recruit) { return recruit.getStateData(COLOR); }
    static void setColor(AbstractRecruitEntity recruit, byte color) { recruit.setStateData(COLOR, color); }
    static int getBiome(AbstractRecruitEntity recruit) { return recruit.getStateData(BIOME); }
    static void setBiome(AbstractRecruitEntity recruit, byte biome) { recruit.setStateData(BIOME, biome); }
    static UUID getUpkeepUUID(AbstractRecruitEntity recruit) { return recruit.getStateData(UPKEEP_ID).orElse(null); }
    static void setUpkeepUUID(AbstractRecruitEntity recruit, Optional<UUID> id) { recruit.setStateData(UPKEEP_ID, id); }
    static BlockPos getUpkeepPos(AbstractRecruitEntity recruit) { return recruit.getStateData(UPKEEP_POS).orElse(null); }
    static void setUpkeepPos(AbstractRecruitEntity recruit, BlockPos pos) { recruit.setStateData(UPKEEP_POS, Optional.of(pos)); }
    static void clearUpkeepPos(AbstractRecruitEntity recruit) { recruit.setStateData(UPKEEP_POS, Optional.empty()); }
    static void clearUpkeepEntity(AbstractRecruitEntity recruit) { recruit.setStateData(UPKEEP_ID, Optional.empty()); }
    static UUID getOwnerUUID(AbstractRecruitEntity recruit) { return recruit.getStateData(OWNER_ID).orElse(null); }
    static void setOwnerUUID(AbstractRecruitEntity recruit, Optional<UUID> id) { recruit.setStateData(OWNER_ID, id); }
    static UUID getProtectUUID(AbstractRecruitEntity recruit) { return recruit.getStateData(PROTECT_ID).orElse(null); }
    static void setProtectUUID(AbstractRecruitEntity recruit, Optional<UUID> id) { recruit.setStateData(PROTECT_ID, id); }
    static UUID getMountUUID(AbstractRecruitEntity recruit) { return recruit.getStateData(MOUNT_ID).orElse(null); }
    static void setMountUUID(AbstractRecruitEntity recruit, Optional<UUID> id) { recruit.setStateData(MOUNT_ID, id); }
    static boolean getIsOwned(AbstractRecruitEntity recruit) { return recruit.getStateData(OWNED); }
    static void setIsOwned(AbstractRecruitEntity recruit, boolean owned) { recruit.setStateData(OWNED, owned); }
    static float getMorale(AbstractRecruitEntity recruit) { return recruit.getStateData(MORAL); }
    static void setMorale(AbstractRecruitEntity recruit, float value) { recruit.setStateData(MORAL, value); }
    static float getHunger(AbstractRecruitEntity recruit) { return recruit.getStateData(HUNGER); }
    static void setHunger(AbstractRecruitEntity recruit, float value) { recruit.setStateData(HUNGER, value); }
    static boolean getFleeing(AbstractRecruitEntity recruit) { return recruit.getStateData(FLEEING); }
    static void setFleeing(AbstractRecruitEntity recruit, boolean fleeing) { recruit.setStateData(FLEEING, fleeing); }
    static int getKills(AbstractRecruitEntity recruit) { return recruit.getStateData(KILLS); }
    static void setKills(AbstractRecruitEntity recruit, int kills) { recruit.setStateData(KILLS, kills); }
    static int getXpLevel(AbstractRecruitEntity recruit) { return recruit.getStateData(LEVEL); }
    static void setXpLevel(AbstractRecruitEntity recruit, int xpLevel) { recruit.setStateData(LEVEL, xpLevel); }
    static int getXp(AbstractRecruitEntity recruit) { return recruit.getStateData(XP); }
    static void setXp(AbstractRecruitEntity recruit, int xp) { recruit.setStateData(XP, xp); }
    static boolean getShouldMovePos(AbstractRecruitEntity recruit) { return recruit.getStateData(SHOULD_MOVE_POS); }
    static void setShouldMovePos(AbstractRecruitEntity recruit, boolean shouldMovePos) { recruit.setStateData(SHOULD_MOVE_POS, shouldMovePos); }
    static boolean getShouldHoldPos(AbstractRecruitEntity recruit) { return recruit.getStateData(SHOULD_HOLD_POS); }
    static void setShouldHoldPos(AbstractRecruitEntity recruit, boolean shouldHoldPos) { recruit.setStateData(SHOULD_HOLD_POS, shouldHoldPos); }
    static boolean getShouldMount(AbstractRecruitEntity recruit) { return recruit.getStateData(SHOULD_MOUNT); }
    static void setShouldMount(AbstractRecruitEntity recruit, boolean shouldMount) { recruit.setStateData(SHOULD_MOUNT, shouldMount); }
    static boolean getShouldProtect(AbstractRecruitEntity recruit) { return recruit.getStateData(SHOULD_PROTECT); }
    static void setShouldProtect(AbstractRecruitEntity recruit, boolean shouldProtect) { recruit.setStateData(SHOULD_PROTECT, shouldProtect); }
    static boolean getShouldFollow(AbstractRecruitEntity recruit) { return recruit.getStateData(SHOULD_FOLLOW); }
    static void setShouldFollow(AbstractRecruitEntity recruit, boolean shouldFollow) { recruit.setStateData(SHOULD_FOLLOW, shouldFollow); }
    static boolean getShouldBlock(AbstractRecruitEntity recruit) { return recruit.getStateData(SHOULD_BLOCK); }
    static void setShouldBlock(AbstractRecruitEntity recruit, boolean shouldBlock) { recruit.setStateData(SHOULD_BLOCK, shouldBlock); }
    static boolean isFollowing(AbstractRecruitEntity recruit) { return recruit.getStateData(IS_FOLLOWING); }
    static void setIsFollowing(AbstractRecruitEntity recruit, boolean isFollowing) { recruit.setStateData(IS_FOLLOWING, isFollowing); }
    static boolean getShouldRest(AbstractRecruitEntity recruit) { return recruit.getStateData(SHOULD_REST); }
    static void setShouldRest(AbstractRecruitEntity recruit, boolean shouldRest) { recruit.setStateData(SHOULD_REST, shouldRest); }
    static boolean getShouldRanged(AbstractRecruitEntity recruit) { return recruit.getStateData(SHOULD_RANGED); }
    static void setShouldRanged(AbstractRecruitEntity recruit, boolean shouldRanged) { recruit.setStateData(SHOULD_RANGED, shouldRanged); }
    static int getState(AbstractRecruitEntity recruit) { return recruit.getStateData(STATE); }
    static Optional<UUID> getGroupUUID(AbstractRecruitEntity recruit) { return recruit.getStateData(GROUP); }
    static void setGroupUUID(AbstractRecruitEntity recruit, UUID uuid) { recruit.setStateData(GROUP, uuid == null ? Optional.empty() : Optional.of(uuid)); }
    static int getFollowState(AbstractRecruitEntity recruit) { return recruit.getStateData(FOLLOW_STATE); }
    static BlockPos getMovePos(AbstractRecruitEntity recruit) { return recruit.getStateData(MOVE_POS).orElse(null); }
    static boolean getListen(AbstractRecruitEntity recruit) { return recruit.getStateData(LISTEN); }
    static void setListen(AbstractRecruitEntity recruit, boolean listen) { recruit.setStateData(LISTEN, listen); }
    static void writeAggroState(AbstractRecruitEntity recruit, int state) { recruit.setStateData(STATE, state); }
    static void writeFollowState(AbstractRecruitEntity recruit, int state) { recruit.setStateData(FOLLOW_STATE, state); }
    static void writeHoldPos(AbstractRecruitEntity recruit, Optional<BlockPos> holdPos) { recruit.setStateData(HOLD_POS, holdPos); }
    static void writeMovePos(AbstractRecruitEntity recruit, Optional<BlockPos> movePos) { recruit.setStateData(MOVE_POS, movePos); }
}
