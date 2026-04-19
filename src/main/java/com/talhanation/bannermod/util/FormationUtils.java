package com.talhanation.bannermod.util;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

public class FormationUtils {
    public static final double spacing = 1.75D;
    public static Vec3 calculateLineBlockPosition(Vec3 targetPos, Vec3 linePos, int size, int index, Level level) {
        Vec3 toTarget = linePos.vectorTo(targetPos).normalize();
        Vec3 rotation = toTarget.yRot(3.14F/2).normalize();
        Vec3 pos;
        if(index == 0 || size/index > size/2)
            pos = linePos.lerp(linePos.add(rotation), index * 1.50);
        else
            pos = linePos.lerp(linePos.add(rotation.reverse()), index * 1.50);

        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                level,
                new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)
        );
        
        return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());

    }

    public static void movementFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        movementFormation(player, recruits, targetPos, 1.0);
    }

    public static void movementFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        FormationLayoutPlanner.assignAndApplySlots(
                recruits,
                FormationPatternBuilder.buildMovementPositions(recruits, targetPos, player.getYRot(), spacingMultiplier),
                null,
                false
        );
    }

    public static void lineUpFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        lineUpFormation(player, recruits, targetPos, 1.0);
    }

    public static void lineUpFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        FormationLayoutPlanner.assignAndApplySlots(
                recruits,
                FormationPatternBuilder.buildLineUpPositions(recruits, targetPos, player.getYRot(), spacingMultiplier),
                null,
                false
        );
    }

    public static void lineFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, int maxInRow, double spacing) {
        FormationLayoutPlanner.assignAndApplySlots(
                recruits,
                FormationPatternBuilder.buildLinePositions(recruits, forward, targetPos, maxInRow, spacing),
                null,
                false
        );
    }

    public static void squareFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        squareFormation(player, recruits, targetPos, 1.0);
    }

    public static void squareFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        squareFormation(FormationPatternBuilder.forwardFromYaw(player.getYRot()), recruits, targetPos, 2.5 * spacingMultiplier);
    }

    public static void squareFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing) {
        FormationLayoutPlanner.assignAndApplySlots(
                recruits,
                FormationPatternBuilder.buildSquarePositions(recruits, forward, targetPos, spacing),
                null,
                false
        );
    }



    public static void triangleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        triangleFormation(player, recruits, targetPos, 1.0);
    }

    public static void triangleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        FormationLayoutPlanner.assignAndApplySlots(
                recruits,
                FormationPatternBuilder.buildTrianglePositions(recruits, targetPos, player.getYRot(), spacingMultiplier),
                player.getYRot(),
                true
        );
    }

    public static void hollowCircleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        hollowCircleFormation(player, recruits, targetPos, 1.0);
    }

    public static void hollowCircleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        FormationLayoutPlanner.assignAndApplySlots(
                recruits,
                FormationPatternBuilder.buildHollowCirclePositions(recruits, targetPos, spacingMultiplier),
                player.getYRot(),
                true
        );
    }

    public static void circleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        circleFormation(player, recruits, targetPos, 1.0);
    }

    public static void circleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        FormationLayoutPlanner.assignAndApplySlots(
                recruits,
                FormationPatternBuilder.buildCirclePositions(recruits, targetPos, spacingMultiplier),
                player.getYRot(),
                true
        );
    }

    public static void hollowSquareFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        hollowSquareFormation(player, recruits, targetPos, 1.0);
    }

    public static void hollowSquareFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        FormationLayoutPlanner.assignAndApplySlots(
                recruits,
                FormationPatternBuilder.buildHollowSquarePositions(recruits, targetPos, player.getYRot(), spacingMultiplier),
                player.getYRot(),
                true
        );
    }


    public static void vFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        vFormation(player, recruits, targetPos, 1.0);
    }

    public static void vFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        FormationLayoutPlanner.applySequentialSlots(
                recruits,
                FormationPatternBuilder.buildVPositions(recruits, targetPos, player.getYRot(), spacingMultiplier),
                player.getYRot(),
                true
        );
    }

    public static Vec3 getCenterOfPositions(List<LivingEntity> recruits, ServerLevel level) {
        if (recruits.isEmpty()) return Vec3.ZERO;

        double sumX = 0;
        double sumY = 0;
        double sumZ = 0;

        for (LivingEntity recruit : recruits) {
            Vec3 pos = recruit.position();
            sumX += pos.x;
            sumY += pos.y;
            sumZ += pos.z;
        }

        double centerX = sumX / recruits.size();
        double centerY = sumY / recruits.size();
        double centerZ = sumZ / recruits.size();

        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                level,
                new BlockPos((int) centerX, (int) centerY, (int) centerZ)
        );

        return new Vec3(centerX, blockPos.getY(), centerZ);
    }

    public static Vec3 getFarthestRecruitsCenter(List<AbstractRecruitEntity> recruits, ServerLevel level) {
        if (recruits.size() < 2) {
            return recruits.isEmpty() ? Vec3.ZERO : recruits.get(0).position();
        }

        AbstractRecruitEntity farthestRecruit1 = null;
        AbstractRecruitEntity farthestRecruit2 = null;
        double maxDistance = Double.MIN_VALUE;

        for (int i = 0; i < recruits.size() - 1; i++) {
            for (int j = i + 1; j < recruits.size(); j++) {
                double distance = recruits.get(i).distanceToSqr(recruits.get(j));
                if (distance > maxDistance) {
                    maxDistance = distance;
                    farthestRecruit1 = recruits.get(i);
                    farthestRecruit2 = recruits.get(j);
                }
            }
        }

        Vec3 pos1 = Objects.requireNonNull(farthestRecruit1).position();
        Vec3 pos2 = Objects.requireNonNull(farthestRecruit2).position();

        double centerX = (pos1.x + pos2.x) / 2.0;
        double centerY = (pos1.y + pos2.y) / 2.0;
        double centerZ = (pos1.z + pos2.z) / 2.0;

        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                level,
                new BlockPos((int) centerX, (int) centerY, (int) centerZ)
        );

        return new Vec3(centerX, blockPos.getY(), centerZ);
    }

    public static Vec3 getGeometricMedian(List<AbstractRecruitEntity> recruits, ServerLevel level) {
        if (recruits.isEmpty()) {
            return Vec3.ZERO;
        }

        // Initial guess: average position
        double sumX = 0, sumY = 0, sumZ = 0;
        for (AbstractRecruitEntity recruit : recruits) {
            Vec3 pos = recruit.position();
            sumX += pos.x;
            sumY += pos.y;
            sumZ += pos.z;
        }
        Vec3 currentGuess = new Vec3(sumX / recruits.size(), sumY / recruits.size(), sumZ / recruits.size());

        // Weiszfeld algorithm
        double tolerance = 1e-4;
        int maxIterations = 100;
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            double numeratorX = 0, numeratorY = 0, numeratorZ = 0;
            double denominator = 0;

            for (AbstractRecruitEntity recruit : recruits) {
                Vec3 pos = recruit.position();
                double distance = currentGuess.distanceTo(pos);

                if (distance < tolerance) {
                    continue;
                }

                double weight = 1 / distance;
                numeratorX += pos.x * weight;
                numeratorY += pos.y * weight;
                numeratorZ += pos.z * weight;
                denominator += weight;
            }

            Vec3 newGuess = new Vec3(numeratorX / denominator, numeratorY / denominator, numeratorZ / denominator);

            if (currentGuess.distanceTo(newGuess) < tolerance) {
                break;
            }

            currentGuess = newGuess;
        }

        BlockPos blockPos = FormationUtils.getPositionOrSurface(
                level,
                new BlockPos((int) currentGuess.x, (int) currentGuess.y, (int) currentGuess.z)
        );

        return new Vec3(currentGuess.x, blockPos.getY(), currentGuess.z);
    }

    public static BlockPos getPositionOrSurface(Level level, BlockPos pos) {
        boolean positionFree = true;
        for(int i = 0; i < 3; i++) {
            if(!level.getBlockState(pos.above(i)).isAir( )) {
                positionFree = false;
                break;
            }
        }

        return positionFree ? pos : new BlockPos(
                pos.getX(),
                level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY(),
                pos.getZ()
        );
    }
}
