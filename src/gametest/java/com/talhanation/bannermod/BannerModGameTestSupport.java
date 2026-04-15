package com.talhanation.bannermod;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.RecruitEntity;
import com.talhanation.workers.entities.FarmerEntity;
import com.talhanation.workers.entities.workarea.BuildArea;
import com.talhanation.workers.entities.workarea.CropArea;
import com.talhanation.workers.entities.workarea.StorageArea;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class BannerModGameTestSupport {

    private BannerModGameTestSupport() {
    }

    public static FarmerEntity spawnOwnedFarmer(GameTestHelper helper, Player player, BlockPos relativePos) {
        FarmerEntity worker = spawnEntity(helper, com.talhanation.workers.init.ModEntityTypes.FARMER.get(), relativePos);
        worker.setCustomName(Component.literal("Integrated Farmer"));
        worker.setCustomNameVisible(true);
        worker.setPersistenceRequired();
        worker.setOwnerUUID(Optional.of(player.getUUID()));
        worker.setIsOwned(true);
        worker.setFollowState(2);
        worker.setHoldPos(Vec3.atCenterOf(worker.blockPosition()));
        return worker;
    }

    public static RecruitEntity spawnOwnedRecruit(GameTestHelper helper, Player player, BlockPos relativePos) {
        RecruitEntity recruit = spawnEntity(helper, com.talhanation.recruits.init.ModEntityTypes.RECRUIT.get(), relativePos);
        recruit.setCustomName(Component.literal("Governor Recruit"));
        recruit.setCustomNameVisible(true);
        recruit.setPersistenceRequired();
        recruit.setOwnerUUID(Optional.of(player.getUUID()));
        recruit.setIsOwned(true);
        recruit.setFollowState(1);
        return recruit;
    }

    public static CropArea spawnOwnedCropArea(GameTestHelper helper, Player player, BlockPos relativePos) {
        CropArea cropArea = spawnEntity(helper, com.talhanation.workers.init.ModEntityTypes.CROPAREA.get(), relativePos);
        cropArea.setPlayerUUID(player.getUUID());
        cropArea.setPlayerName(player.getName().getString());
        cropArea.setWidthSize(3);
        cropArea.setDepthSize(3);
        cropArea.setHeightSize(2);
        return cropArea;
    }

    public static StorageArea spawnOwnedStorageArea(GameTestHelper helper, Player player, BlockPos relativePos) {
        StorageArea storageArea = spawnEntity(helper, com.talhanation.workers.init.ModEntityTypes.STORAGEAREA.get(), relativePos);
        storageArea.setPlayerUUID(player.getUUID());
        storageArea.setPlayerName(player.getName().getString());
        storageArea.setWidthSize(3);
        storageArea.setDepthSize(3);
        storageArea.setHeightSize(2);
        return storageArea;
    }

    public static BuildArea spawnOwnedBuildArea(GameTestHelper helper, Player player, BlockPos relativePos) {
        BuildArea buildArea = spawnEntity(helper, com.talhanation.workers.init.ModEntityTypes.BUILDAREA.get(), relativePos);
        buildArea.setPlayerUUID(player.getUUID());
        buildArea.setPlayerName(player.getName().getString());
        buildArea.setWidthSize(3);
        buildArea.setDepthSize(3);
        buildArea.setHeightSize(4);
        return buildArea;
    }

    public static Villager spawnVillagerWithMemories(GameTestHelper helper, BlockPos relativePos, String name) {
        Villager villager = spawnEntity(helper, EntityType.VILLAGER, relativePos);
        villager.setCustomName(Component.literal(name));
        villager.setCustomNameVisible(true);
        villager.setPersistenceRequired();
        villager.setAge(0);

        BlockPos absolutePos = helper.absolutePos(relativePos);
        GlobalPos home = GlobalPos.of(helper.getLevel().dimension(), absolutePos.north());
        GlobalPos meeting = GlobalPos.of(helper.getLevel().dimension(), absolutePos.south());
        GlobalPos jobSite = GlobalPos.of(helper.getLevel().dimension(), absolutePos.east());
        villager.getBrain().setMemory(MemoryModuleType.HOME, home);
        villager.getBrain().setMemory(MemoryModuleType.MEETING_POINT, meeting);
        villager.getBrain().setMemory(MemoryModuleType.JOB_SITE, jobSite);
        return villager;
    }

    public static CompoundTag createMinimalBuildTemplate() {
        CompoundTag structure = new CompoundTag();
        structure.putInt("width", 1);
        structure.putString("facing", "north");
        ListTag blocks = new ListTag();
        blocks.add(createTemplateBlock(0, 0, 0));
        blocks.add(createTemplateBlock(0, 1, 0));
        blocks.add(createTemplateBlock(0, 2, 0));
        blocks.add(createTemplateBlock(0, 3, 0));
        structure.put("blocks", blocks);
        return structure;
    }

    public static <T extends Entity> T spawnEntity(GameTestHelper helper, EntityType<T> entityType, BlockPos relativePos) {
        ServerLevel level = helper.getLevel();
        T entity = entityType.create(level);

        if (entity == null) {
            throw new IllegalArgumentException("Failed to create integrated runtime test entity");
        }

        Vec3 spawnCenter = spawnCenter(helper, relativePos);
        entity.moveTo(spawnCenter.x(), spawnCenter.y(), spawnCenter.z(), 0.0F, 0.0F);
        if (entity instanceof AbstractRecruitEntity recruit) {
            recruit.initSpawn();
        }

        if (!level.addFreshEntity(entity)) {
            throw new IllegalArgumentException("Failed to insert integrated runtime test entity into GameTest level");
        }

        return entity;
    }

    private static Vec3 spawnCenter(GameTestHelper helper, BlockPos relativePos) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        return new Vec3(absolutePos.getX() + 0.5D, absolutePos.getY() + 1.0D, absolutePos.getZ() + 0.5D);
    }

    private static CompoundTag createTemplateBlock(int x, int y, int z) {
        CompoundTag block = new CompoundTag();
        block.putInt("x", x);
        block.putInt("y", y);
        block.putInt("z", z);
        block.put("state", NbtUtils.writeBlockState(Blocks.OAK_PLANKS.defaultBlockState()));
        return block;
    }
}
