package com.talhanation.bannermod;

import com.mojang.authlib.GameProfile;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import com.talhanation.workers.entities.workarea.AbstractWorkAreaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Optional;
import java.util.UUID;

public final class BannerModDedicatedServerGameTestSupport {

    private BannerModDedicatedServerGameTestSupport() {
    }

    public static Player createFakeServerPlayer(ServerLevel level, UUID playerId, String name) {
        FakePlayer player = new FakePlayer(level, new GameProfile(playerId, name));
        if (level.getPlayerByUUID(playerId) == null) {
            level.addFreshEntity(player);
        }
        return player;
    }

    public static void assignDetachedOwnership(AbstractRecruitEntity recruit, UUID ownerId) {
        recruit.setOwnerUUID(Optional.of(ownerId));
        recruit.setIsOwned(true);
    }

    public static void assignDetachedOwnership(AbstractWorkerEntity worker, UUID ownerId) {
        worker.setOwnerUUID(Optional.of(ownerId));
        worker.setIsOwned(true);
    }

    public static void assignDetachedOwnership(AbstractWorkAreaEntity workArea, UUID ownerId, String ownerName) {
        workArea.setPlayerUUID(ownerId);
        workArea.setPlayerName(ownerName);
    }

    public static CompoundTag saveEntity(Entity entity) {
        return entity.saveWithoutId(new CompoundTag());
    }

    public static <T extends Entity> T loadEntity(GameTestHelper helper, EntityType<T> entityType, BlockPos relativePos, CompoundTag savedData) {
        T entity = BannerModGameTestSupport.spawnEntity(helper, entityType, relativePos);
        entity.load(savedData);
        return entity;
    }
}
