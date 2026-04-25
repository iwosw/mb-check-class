package com.talhanation.bannermod.util;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

final class FormationPatternBuilder {
    private FormationPatternBuilder() {
    }

    static Vec3 forwardFromYaw(float yaw) {
        return new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
    }

    static List<Vec3> buildMovementPositions(List<AbstractRecruitEntity> recruits, Vec3 targetPos, float yaw, double spacingMultiplier) {
        return buildLinePositions(recruits, forwardFromYaw(yaw), targetPos, 3, 2.0D * spacingMultiplier);
    }

    static List<Vec3> buildLineUpPositions(List<AbstractRecruitEntity> recruits, Vec3 targetPos, float yaw, double spacingMultiplier) {
        int maxInRow = recruits.size() <= 20 ? recruits.size() : (recruits.size() + 1) / 2;
        return buildLinePositions(recruits, forwardFromYaw(yaw), targetPos, maxInRow, 1.75D * spacingMultiplier);
    }

    static List<Vec3> buildLinePositions(List<AbstractRecruitEntity> recruits, Vec3 forward, Vec3 targetPos, int maxInRow, double spacing) {
        Vec3 left = leftOf(forward);
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

        return possiblePositions;
    }

    static List<Vec3> buildSquarePositions(List<AbstractRecruitEntity> recruits, Vec3 forward, Vec3 targetPos, double spacing) {
        Vec3 left = leftOf(forward);
        spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, spacing);

        int sideLength = (int) Math.ceil(Math.sqrt(recruits.size()));
        List<Vec3> possiblePositions = new ArrayList<>();

        for (int i = 0; i < recruits.size(); i++) {
            int row = i / sideLength;
            int col = i % sideLength;

            Vec3 rowOffset = forward.scale(-row * spacing);
            Vec3 colOffset = left.scale((col - sideLength / 2F) * spacing);
            possiblePositions.add(targetPos.add(rowOffset).add(colOffset));
        }

        return possiblePositions;
    }

    static List<Vec3> buildTestudoPositions(List<AbstractRecruitEntity> recruits, Vec3 targetPos, float yaw, double spacingMultiplier) {
        Vec3 forward = forwardFromYaw(yaw);
        Vec3 left = leftOf(forward);
        double spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, 1.35D * spacingMultiplier);
        int size = recruits.size();
        List<Vec3> possiblePositions = new ArrayList<>();
        if (size <= 0) {
            return possiblePositions;
        }

        List<double[]> localOffsets = size <= 9
                ? smallTestudoOffsets(size)
                : layeredSquareOffsets(size);

        for (double[] offset : localOffsets) {
            Vec3 lateral = left.scale(offset[0] * spacing);
            Vec3 depth = forward.scale(offset[1] * spacing);
            possiblePositions.add(targetPos.add(lateral).add(depth));
        }
        return possiblePositions;
    }

    static List<Vec3> buildTrianglePositions(List<AbstractRecruitEntity> recruits, Vec3 targetPos, float yaw, double spacingMultiplier) {
        Vec3 forward = forwardFromYaw(yaw);
        Vec3 left = leftOf(forward);
        double spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, 2.5 * spacingMultiplier);
        double rowDistance = spacing * 1.5;
        List<Vec3> possiblePositions = new ArrayList<>();

        int index = 0;
        int rowCount = 1;
        while (index < recruits.size()) {
            for (int positionInRow = 0; positionInRow < rowCount && index < recruits.size(); positionInRow++, index++) {
                Vec3 basePos = targetPos.add(forward.scale(-rowDistance * (rowCount - 1)));
                Vec3 offset = left.scale((positionInRow - (rowCount - 1) / 2F) * spacing);
                possiblePositions.add(basePos.add(offset));
            }
            rowCount++;
        }

        return possiblePositions;
    }

    static List<Vec3> buildHollowCirclePositions(List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        double spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, 2.5 * spacingMultiplier);
        int numRecruits = recruits.size();
        double radius = spacing * numRecruits / (2 * Math.PI);
        List<Vec3> possiblePositions = new ArrayList<>();

        for (int i = 0; i < numRecruits; i++) {
            double angle = (2 * Math.PI / numRecruits) * i;
            double x = targetPos.x + radius * Math.cos(angle);
            double z = targetPos.z + radius * Math.sin(angle);
            possiblePositions.add(new Vec3(x, targetPos.y, z));
        }

        return possiblePositions;
    }

    static List<Vec3> buildCirclePositions(List<AbstractRecruitEntity> recruits, Vec3 targetPos, double spacingMultiplier) {
        double spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, 2.5 * spacingMultiplier);
        int numRecruits = recruits.size();

        int innerRingCount = Math.min(5, numRecruits);
        int middleRingCount = Math.min(10, numRecruits - innerRingCount);
        int outerRingCount = numRecruits - innerRingCount - middleRingCount;

        double innerRadius = spacing * innerRingCount / (2 * Math.PI);
        double middleRadius = spacing * middleRingCount / (2 * Math.PI);
        double outerRadius = spacing * outerRingCount / (2 * Math.PI);

        List<Vec3> possiblePositions = new ArrayList<>();
        addRingPositions(possiblePositions, targetPos, innerRingCount, innerRadius);
        addRingPositions(possiblePositions, targetPos, middleRingCount, middleRadius);
        addRingPositions(possiblePositions, targetPos, outerRingCount, outerRadius);
        return possiblePositions;
    }

    static List<Vec3> buildHollowSquarePositions(List<AbstractRecruitEntity> recruits, Vec3 targetPos, float yaw, double spacingMultiplier) {
        Vec3 forward = forwardFromYaw(yaw);
        Vec3 left = leftOf(forward);
        int recruitsPerSide = Math.max(2, recruits.size() / 4);
        double spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, 2.5 * spacingMultiplier);

        int totalRecruitsNeeded = recruitsPerSide * 4;
        if (totalRecruitsNeeded > recruits.size()) {
            recruitsPerSide = recruits.size() / 4;
        }

        List<Vec3> possiblePositions = new ArrayList<>();

        for (int row = 0; row < 2; row++) {
            double offset = (spacing * recruitsPerSide) / 2.0;
            for (int i = 0; i < recruitsPerSide; i++) {
                double positionOffset = i * spacing - offset;

                possiblePositions.add(targetPos.add(forward.scale(-offset - row * spacing)).add(left.scale(positionOffset)));
                possiblePositions.add(targetPos.add(forward.scale(offset + row * spacing)).add(left.scale(positionOffset)));
                possiblePositions.add(targetPos.add(left.scale(-offset - row * spacing)).add(forward.scale(positionOffset)));
                possiblePositions.add(targetPos.add(left.scale(offset + row * spacing)).add(forward.scale(positionOffset)));
            }
        }

        return possiblePositions;
    }

    static List<Vec3> buildVPositions(List<AbstractRecruitEntity> recruits, Vec3 targetPos, float yaw, double spacingMultiplier) {
        Vec3 forward = forwardFromYaw(yaw);
        Vec3 left = leftOf(forward);
        double spacing = FormationLayoutPlanner.scaleSpacingForShipCaptains(recruits, 2.5 * spacingMultiplier);
        int recruitsPerWing = recruits.size() / 2;
        List<Vec3> possiblePositions = new ArrayList<>();

        for (int i = 0; i < recruitsPerWing; i++) {
            double offset = i * spacing;
            possiblePositions.add(targetPos.add(forward.scale(offset)).add(left.scale(offset)));
            possiblePositions.add(targetPos.add(forward.scale(offset)).subtract(left.scale(offset)));
        }

        if (recruits.size() % 2 != 0) {
            possiblePositions.add(targetPos);
        }

        return possiblePositions;
    }

    private static Vec3 leftOf(Vec3 forward) {
        return new Vec3(-forward.z, forward.y, forward.x);
    }

    private static List<double[]> smallTestudoOffsets(int size) {
        List<double[]> out = new ArrayList<>();
        switch (size) {
            case 1 -> out.add(new double[]{0.0D, 0.0D});
            case 2 -> {
                out.add(new double[]{-0.5D, 0.0D});
                out.add(new double[]{0.5D, 0.0D});
            }
            case 3 -> {
                out.add(new double[]{-1.0D, 0.0D});
                out.add(new double[]{0.0D, 0.0D});
                out.add(new double[]{1.0D, 0.0D});
            }
            case 4 -> {
                out.add(new double[]{-0.5D, 0.5D});
                out.add(new double[]{0.5D, 0.5D});
                out.add(new double[]{-0.5D, -0.5D});
                out.add(new double[]{0.5D, -0.5D});
            }
            case 5 -> {
                out.add(new double[]{-0.5D, 0.5D});
                out.add(new double[]{0.5D, 0.5D});
                out.add(new double[]{-0.5D, -0.5D});
                out.add(new double[]{0.5D, -0.5D});
                out.add(new double[]{0.0D, 0.0D});
            }
            case 6 -> {
                out.add(new double[]{-1.0D, 0.5D});
                out.add(new double[]{0.0D, 0.5D});
                out.add(new double[]{1.0D, 0.5D});
                out.add(new double[]{-1.0D, -0.5D});
                out.add(new double[]{0.0D, -0.5D});
                out.add(new double[]{1.0D, -0.5D});
            }
            case 7 -> {
                out.add(new double[]{-1.0D, 1.0D});
                out.add(new double[]{0.0D, 1.0D});
                out.add(new double[]{1.0D, 1.0D});
                out.add(new double[]{-1.0D, 0.0D});
                out.add(new double[]{1.0D, 0.0D});
                out.add(new double[]{-0.5D, -1.0D});
                out.add(new double[]{0.5D, -1.0D});
            }
            case 8 -> {
                out.add(new double[]{-1.0D, 1.0D});
                out.add(new double[]{0.0D, 1.0D});
                out.add(new double[]{1.0D, 1.0D});
                out.add(new double[]{-1.0D, 0.0D});
                out.add(new double[]{1.0D, 0.0D});
                out.add(new double[]{-1.0D, -1.0D});
                out.add(new double[]{0.0D, -1.0D});
                out.add(new double[]{1.0D, -1.0D});
            }
            default -> {
                out.add(new double[]{-1.0D, 1.0D});
                out.add(new double[]{0.0D, 1.0D});
                out.add(new double[]{1.0D, 1.0D});
                out.add(new double[]{-1.0D, 0.0D});
                out.add(new double[]{0.0D, 0.0D});
                out.add(new double[]{1.0D, 0.0D});
                out.add(new double[]{-1.0D, -1.0D});
                out.add(new double[]{0.0D, -1.0D});
                out.add(new double[]{1.0D, -1.0D});
            }
        }
        return out;
    }

    private static List<double[]> layeredSquareOffsets(int size) {
        int side = (int) Math.ceil(Math.sqrt(size));
        double center = (side - 1) / 2.0D;
        List<double[]> out = new ArrayList<>();
        for (int ring = 0; ring <= side / 2 && out.size() < size; ring++) {
            int min = ring;
            int max = side - 1 - ring;
            for (int z = max; z >= min && out.size() < size; z--) {
                for (int x = min; x <= max && out.size() < size; x++) {
                    if (x != min && x != max && z != min && z != max) {
                        continue;
                    }
                    out.add(new double[]{x - center, z - center});
                }
            }
        }
        return out;
    }

    private static void addRingPositions(List<Vec3> positions, Vec3 targetPos, int count, double radius) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double x = targetPos.x + radius * Math.cos(angle);
            double z = targetPos.z + radius * Math.sin(angle);
            positions.add(new Vec3(x, targetPos.y, z));
        }
    }
}
