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

    private static void addRingPositions(List<Vec3> positions, Vec3 targetPos, int count, double radius) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double x = targetPos.x + radius * Math.cos(angle);
            double z = targetPos.z + radius * Math.sin(angle);
            positions.add(new Vec3(x, targetPos.y, z));
        }
    }
}
