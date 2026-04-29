package com.talhanation.bannermod.client.military.render;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RecruitRenderLod {
    private static final double CROWD_SCAN_RADIUS = 48.0D;
    private static final double CROWD_IMPOSTOR_DISTANCE = 48.0D;
    private static final int CROWD_SCAN_INTERVAL_TICKS = 10;
    private static final int CROWDED_RECRUIT_COUNT = 48;

    private static long lastCrowdScanTick = Long.MIN_VALUE;
    private static ResourceKey<Level> lastCrowdScanDimension;
    private static boolean crowded;

    private RecruitRenderLod() {
    }

    public static boolean shouldRenderCosmeticModelLayer(AbstractRecruitEntity recruit) {
        double distanceSqr = cameraDistanceSqr(recruit);
        if (distanceSqr <= square(24.0D)) return true;
        if (isCrowdedNearCamera()) return distanceSqr <= square(32.0D);
        return distanceSqr <= square(64.0D);
    }

    public static boolean shouldRenderArmor(AbstractRecruitEntity recruit) {
        double distanceSqr = cameraDistanceSqr(recruit);
        if (distanceSqr <= square(32.0D)) return true;
        if (isCrowdedNearCamera()) return false;
        return distanceSqr <= square(48.0D);
    }

    public static boolean shouldRenderHeldItems(AbstractRecruitEntity recruit) {
        double distanceSqr = cameraDistanceSqr(recruit);
        if (distanceSqr <= square(32.0D)) return true;
        if (isCrowdedNearCamera()) return false;
        return distanceSqr <= square(48.0D) && (recruit.isAggressive() || recruit.getUseItemRemainingTicks() > 0);
    }

    public static boolean shouldRenderCustomHead(AbstractRecruitEntity recruit) {
        double distanceSqr = cameraDistanceSqr(recruit);
        if (distanceSqr <= square(24.0D)) return true;
        if (isCrowdedNearCamera()) return false;
        return distanceSqr <= square(32.0D);
    }

    public static boolean shouldRenderName(AbstractRecruitEntity recruit) {
        double distanceSqr = cameraDistanceSqr(recruit);
        if (isCrowdedNearCamera()) return distanceSqr <= square(16.0D);
        return distanceSqr <= square(32.0D);
    }

    public static boolean shouldUseCrowdImpostor(AbstractRecruitEntity recruit) {
        if (recruit == null || !recruit.isAlive() || recruit.isInvisible() || recruit.isPassenger()) {
            return false;
        }
        if (!isCrowdedNearCamera()) {
            return false;
        }
        Vec3 cameraPos = cameraPosition();
        if (cameraPos == null) {
            return false;
        }
        return recruit.distanceToSqr(cameraPos) >= square(CROWD_IMPOSTOR_DISTANCE);
    }

    private static double cameraDistanceSqr(AbstractRecruitEntity recruit) {
        Vec3 cameraPos = cameraPosition();
        return cameraPos == null ? 0.0D : recruit.distanceToSqr(cameraPos);
    }

    public static boolean isCrowdedNearCamera() {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        Vec3 cameraPos = cameraPosition();
        if (cameraPos == null || level == null) return false;

        long gameTime = level.getGameTime();
        ResourceKey<Level> dimension = level.dimension();
        if (dimension.equals(lastCrowdScanDimension) && gameTime >= lastCrowdScanTick && gameTime - lastCrowdScanTick < CROWD_SCAN_INTERVAL_TICKS) {
            return crowded;
        }

        lastCrowdScanTick = gameTime;
        lastCrowdScanDimension = dimension;
        crowded = level.getEntitiesOfClass(
                AbstractRecruitEntity.class,
                AABB.ofSize(cameraPos, CROWD_SCAN_RADIUS * 2.0D, CROWD_SCAN_RADIUS * 2.0D, CROWD_SCAN_RADIUS * 2.0D),
                recruit -> recruit.isAlive() && !recruit.isInvisible()
        ).size() >= CROWDED_RECRUIT_COUNT;
        return crowded;
    }

    private static Vec3 cameraPosition() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null) return null;
        return minecraft.gameRenderer.getMainCamera().getPosition();
    }

    private static double square(double value) {
        return value * value;
    }
}
