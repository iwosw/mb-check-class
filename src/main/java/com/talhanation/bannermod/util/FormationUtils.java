package com.talhanation.bannermod.util;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        lineFormation(forward, recruits, targetPos, 3, 2.0D * spacingMultiplier);
    }

    public static void lineUpFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        lineUpFormation(player, recruits, targetPos, 1.0);
    }

    public static void lineUpFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        int maxInRow = recruits.size() <= 20 ? recruits.size() : (recruits.size() + 1) / 2;
        lineFormation(forward, recruits, targetPos, maxInRow, 1.75D * spacingMultiplier);
    }

    public static void lineFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, int maxInRow, double spacing) {
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);
        List<Vec3> possiblePositions = new ArrayList<>();

        spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, spacing);

        double rowDistance = spacing * 1.75;

        for (int i = 0; i < recruits.size(); i++) {
            int row = i / maxInRow;
            int recruitsInCurrentRow = Math.min(maxInRow, recruits.size() - row * maxInRow);
            int positionInRow = i % maxInRow;

            double centerOffset = (recruitsInCurrentRow - 1) / 2.0;

            Vec3 basePos = targetPos.add(forward.scale(-rowDistance * row));
            Vec3 offset = left.scale((positionInRow - centerOffset) * spacing);

            possiblePositions.add(basePos.add(offset));
        }

        FormationLayoutPlanner.assignAndApplySlots(recruits, possiblePositions, null, false);
    }

    public static void squareFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        squareFormation(player, recruits, targetPos, 1.0);
    }

    public static void squareFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        squareFormation(forward, recruits, targetPos, 2.5 * spacingMultiplier);
    }

    public static void squareFormation(Vec3 forward, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacing) {
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);

        spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, spacing);

        int numRecruits = recruits.size();
        int sideLength = (int) Math.ceil(Math.sqrt(numRecruits));

        List<Vec3> possiblePositions = new ArrayList<>();

        for (int i = 0; i < numRecruits; i++) {
            int row = i / sideLength;
            int col = i % sideLength;

            Vec3 rowOffset = forward.scale(-row * spacing);
            Vec3 colOffset = left.scale((col - sideLength / 2F) * spacing);

            possiblePositions.add(targetPos.add(rowOffset).add(colOffset));
        }

        FormationLayoutPlanner.assignAndApplySlots(recruits, possiblePositions, null, false);
    }



    public static void triangleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        triangleFormation(player, recruits, targetPos, 1.0);
    }

    public static void triangleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);

        double spacing = 2.5 * spacingMultiplier;
        int numRecruits = recruits.size();

        spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, spacing);

        double rowDistance = spacing * 1.5;

        List<Vec3> possiblePositions = new ArrayList<>();

        int index = 0;
        int rowCount = 1;
        while (index < numRecruits) {
            for (int positionInRow = 0; positionInRow < rowCount && index < numRecruits; positionInRow++, index++) {
                Vec3 basePos = targetPos.add(forward.scale(-rowDistance * (rowCount - 1)));
                Vec3 offset = left.scale((positionInRow - (rowCount - 1) / 2F) * spacing);

                possiblePositions.add(basePos.add(offset));
            }
            rowCount++;
        }

        FormationLayoutPlanner.assignAndApplySlots(recruits, possiblePositions, player.getYRot(), true);
    }

    public static void hollowCircleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        hollowCircleFormation(player, recruits, targetPos, 1.0);
    }

    public static void hollowCircleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        double spacing = 2.5 * spacingMultiplier;
        int numRecruits = recruits.size();

        spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, spacing);

        double radius = spacing * numRecruits / (2 * Math.PI); // Calculate radius based on the number of recruits
        List<Vec3> possiblePositions = new ArrayList<>();

        for (int i = 0; i < numRecruits; i++) {
            double angle = (2 * Math.PI / numRecruits) * i; // Angle for each recruit

            // Calculate position for each recruit in the circle
            double x = targetPos.x + radius * Math.cos(angle);
            double z = targetPos.z + radius * Math.sin(angle);

            possiblePositions.add(new Vec3(x, targetPos.y, z));
        }

        FormationLayoutPlanner.assignAndApplySlots(recruits, possiblePositions, player.getYRot(), true);
    }

    public static void circleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        circleFormation(player, recruits, targetPos, 1.0);
    }

    public static void circleFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        double spacing = 2.5 * spacingMultiplier; // Abstand zwischen den Rekruten in jedem Ring
        int numRecruits = recruits.size();

        spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, spacing);

        // Aufteilen der Rekruten auf drei Ringe
        int innerRingCount = Math.min(5, numRecruits); // Innerer Ring hat max 5
        int middleRingCount = Math.min(10, numRecruits - innerRingCount); // Mittlerer Ring hat max 10
        int outerRingCount = numRecruits - innerRingCount - middleRingCount; // Äußerer Ring bekommt den Rest

        double innerRadius = spacing * innerRingCount / (2 * Math.PI); // Radius des inneren Rings
        double middleRadius = spacing * middleRingCount / (2 * Math.PI); // Radius des mittleren Rings
        double outerRadius = spacing * outerRingCount / (2 * Math.PI); // Radius des äußeren Rings

        List<Vec3> possiblePositions = new ArrayList<>();

        // Positionen für den inneren Ring
        for (int i = 0; i < innerRingCount; i++) {
            double angle = (2 * Math.PI / innerRingCount) * i;
            double x = targetPos.x + innerRadius * Math.cos(angle);
            double z = targetPos.z + innerRadius * Math.sin(angle);
            possiblePositions.add(new Vec3(x, targetPos.y, z));
        }

        // Positionen für den mittleren Ring
        for (int i = 0; i < middleRingCount; i++) {
            double angle = (2 * Math.PI / middleRingCount) * i;
            double x = targetPos.x + middleRadius * Math.cos(angle);
            double z = targetPos.z + middleRadius * Math.sin(angle);
            possiblePositions.add(new Vec3(x, targetPos.y, z));
        }

        // Positionen für den äußeren Ring
        for (int i = 0; i < outerRingCount; i++) {
            double angle = (2 * Math.PI / outerRingCount) * i;
            double x = targetPos.x + outerRadius * Math.cos(angle);
            double z = targetPos.z + outerRadius * Math.sin(angle);
            possiblePositions.add(new Vec3(x, targetPos.y, z));
        }

        FormationLayoutPlanner.assignAndApplySlots(recruits, possiblePositions, player.getYRot(), true);
    }

    public static void hollowSquareFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        hollowSquareFormation(player, recruits, targetPos, 1.0);
    }

    public static void hollowSquareFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);

        int recruitsPerSide = Math.max(2, recruits.size() / 4); // Ensure at least 2 recruits per side
        double spacing = 2.5 * spacingMultiplier;

        spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, spacing);

        int totalRecruitsNeeded = recruitsPerSide * 4;
        if (totalRecruitsNeeded > recruits.size()) {
            recruitsPerSide = recruits.size() / 4;
            totalRecruitsNeeded = recruitsPerSide * 4;
        }

        List<Vec3> possiblePositions = new ArrayList<>();

        for (int row = 0; row < 2; row++) { // Two rows per side
            double offset = (spacing * recruitsPerSide) / 2.0;
            for (int i = 0; i < recruitsPerSide; i++) {
                double positionOffset = i * spacing - offset;

                possiblePositions.add(targetPos.add(forward.scale(-offset - row * spacing)).add(left.scale(positionOffset)));

                possiblePositions.add(targetPos.add(forward.scale(offset + row * spacing)).add(left.scale(positionOffset)));

                possiblePositions.add(targetPos.add(left.scale(-offset - row * spacing)).add(forward.scale(positionOffset)));

                possiblePositions.add(targetPos.add(left.scale(offset + row * spacing)).add(forward.scale(positionOffset)));
            }
        }

        FormationLayoutPlanner.assignAndApplySlots(recruits, possiblePositions, player.getYRot(), true);
    }


    public static void vFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos) {
        vFormation(player, recruits, targetPos, 1.0);
    }

    public static void vFormation(Player player, List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        Vec3 left = new Vec3(-forward.z, forward.y, forward.x);

        double spacing = 2.5 * spacingMultiplier;
        int recruitsPerWing = recruits.size() / 2;

        spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, spacing);

        List<Vec3> possiblePositions = new ArrayList<>();

        for (int i = 0; i < recruitsPerWing; i++) {
            double offset = i * spacing;


            Vec3 rightWingPos = targetPos.add(forward.scale(offset)).add(left.scale(offset));
            possiblePositions.add(rightWingPos);


            Vec3 leftWingPos = targetPos.add(forward.scale(offset)).subtract(left.scale(offset));
            possiblePositions.add(leftWingPos);
        }


        if (recruits.size() % 2 != 0) {
            possiblePositions.add(targetPos);
        }

        FormationLayoutPlanner.applySequentialSlots(recruits, possiblePositions, player.getYRot(), true);
    }

    public record FormationFallbackSlot(int slotIndex, Vec3 position, boolean occupied) {
    }

    public record FormationFallbackDecision(int fromSlotIndex, int toSlotIndex) {
    }

    public static Optional<FormationFallbackDecision> chooseNearestFreeFormationSlot(
            Vec3 recruitPosition,
            int currentSlotIndex,
            List<FormationFallbackSlot> slots
    ) {
        return slots.stream()
                .filter(slot -> slot.slotIndex() != currentSlotIndex)
                .filter(slot -> !slot.occupied())
                .min(Comparator.comparingDouble(slot -> slot.position().distanceToSqr(recruitPosition)))
                .map(slot -> new FormationFallbackDecision(currentSlotIndex, slot.slotIndex()));
    }

    public static boolean tryFallbackToNearestFreeSlot(AbstractRecruitEntity blockedRecruit) {
        if (!blockedRecruit.isInFormation || blockedRecruit.getFollowState() != 3 || blockedRecruit.getHoldPos() == null || blockedRecruit.formationPos < 0) {
            return false;
        }

        List<AbstractRecruitEntity> cohort = getFormationCohort(blockedRecruit);
        if (cohort.size() < 2) {
            return false;
        }

        List<FormationFallbackSlot> slots = new ArrayList<>();
        for (AbstractRecruitEntity recruit : cohort) {
            if (recruit.getHoldPos() == null || recruit.formationPos < 0) {
                continue;
            }
            slots.add(new FormationFallbackSlot(
                    recruit.formationPos,
                    recruit.getHoldPos(),
                    isFormationSlotOccupied(recruit.getHoldPos(), cohort, blockedRecruit)
            ));
        }

        Optional<FormationFallbackDecision> decision = chooseNearestFreeFormationSlot(
                blockedRecruit.position(),
                blockedRecruit.formationPos,
                slots
        );
        if (decision.isEmpty()) {
            return false;
        }

        AbstractRecruitEntity targetOwner = findRecruitBySlot(cohort, decision.get().toSlotIndex());
        if (targetOwner == null || targetOwner == blockedRecruit || targetOwner.getHoldPos() == null) {
            return false;
        }

        Vec3 originalHoldPos = blockedRecruit.getHoldPos();
        int originalSlot = blockedRecruit.formationPos;

        blockedRecruit.setHoldPos(targetOwner.getHoldPos());
        blockedRecruit.formationPos = targetOwner.formationPos;
        blockedRecruit.setFollowState(3);
        blockedRecruit.isInFormation = true;

        targetOwner.setHoldPos(originalHoldPos);
        targetOwner.formationPos = originalSlot;
        targetOwner.setFollowState(3);
        targetOwner.isInFormation = true;
        return true;
    }

    private static List<AbstractRecruitEntity> getFormationCohort(AbstractRecruitEntity blockedRecruit) {
        UUID ownerId = blockedRecruit.getOwnerUUID();
        UUID groupId = blockedRecruit.getGroup();
        return blockedRecruit.level().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                blockedRecruit.getBoundingBox().inflate(64.0D),
                candidate -> candidate.isAlive()
                        && !candidate.isRemoved()
                        && candidate.isInFormation
                        && candidate.getHoldPos() != null
                        && Objects.equals(candidate.getOwnerUUID(), ownerId)
                        && sameFormationGroup(groupId, candidate.getGroup())
        );
    }

    private static boolean sameFormationGroup(UUID groupId, UUID candidateGroupId) {
        if (groupId == null && candidateGroupId == null) {
            return true;
        }
        return Objects.equals(groupId, candidateGroupId);
    }

    private static boolean isFormationSlotOccupied(Vec3 slotPos, List<AbstractRecruitEntity> cohort, AbstractRecruitEntity blockedRecruit) {
        for (AbstractRecruitEntity recruit : cohort) {
            if (recruit == blockedRecruit) {
                continue;
            }
            if (recruit.distanceToSqr(slotPos) <= 0.75D) {
                return true;
            }
        }
        return false;
    }

    private static AbstractRecruitEntity findRecruitBySlot(List<AbstractRecruitEntity> cohort, int slotIndex) {
        for (AbstractRecruitEntity recruit : cohort) {
            if (recruit.formationPos == slotIndex) {
                return recruit;
            }
        }
        return null;
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
