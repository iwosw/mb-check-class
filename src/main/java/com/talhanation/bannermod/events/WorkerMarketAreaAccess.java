package com.talhanation.bannermod.events;

import com.talhanation.bannermod.entity.civilian.workarea.MarketArea;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

final class WorkerMarketAreaAccess {

    private WorkerMarketAreaAccess() {
    }

    static boolean shouldBlockInteraction(Player player, Level level, BlockPos pos) {
        List<MarketArea> markets = level.getEntitiesOfClass(MarketArea.class, new AABB(pos).inflate(8));
        if (markets.isEmpty()) {
            return false;
        }

        markets.removeIf(marketArea -> !isInsideMarketArea(pos, marketArea));
        if (markets.isEmpty() || !(level.getBlockEntity(pos) instanceof Container)) {
            return false;
        }

        MarketArea market = markets.get(0);
        UUID ownerUUID = market.getPlayerUUID();
        boolean isOwner = ownerUUID != null && player.getUUID().equals(ownerUUID);
        boolean isAdmin = player.isCreative() && player.hasPermissions(2);
        return !isOwner && !isAdmin;
    }

    private static boolean isInsideMarketArea(BlockPos pos, MarketArea marketArea) {
        AABB area = marketArea.getArea();
        return pos.getX() >= area.minX && pos.getX() <= area.maxX
                && pos.getY() >= area.minY && pos.getY() <= area.maxY
                && pos.getZ() >= area.minZ && pos.getZ() <= area.maxZ;
    }
}
