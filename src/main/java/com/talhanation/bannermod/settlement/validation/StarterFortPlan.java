package com.talhanation.bannermod.settlement.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class StarterFortPlan {
    public static final int OUTER_SIZE = 21;
    public static final int WALL_HEIGHT = 5;
    public static final int TOWER_HEIGHT = 6;
    public static final int WING_HEIGHT = 4;

    public static final RelativeBox NORTH_WALL = new RelativeBox(-10, 1, -10, 10, WALL_HEIGHT, -10);
    public static final RelativeBox SOUTH_WALL_LEFT = new RelativeBox(-10, 1, 10, -3, WALL_HEIGHT, 10);
    public static final RelativeBox SOUTH_WALL_RIGHT = new RelativeBox(3, 1, 10, 10, WALL_HEIGHT, 10);
    public static final RelativeBox WEST_WALL = new RelativeBox(-10, 1, -10, -10, WALL_HEIGHT, 10);
    public static final RelativeBox EAST_WALL = new RelativeBox(10, 1, -10, 10, WALL_HEIGHT, 10);

    public static final RelativeBox NORTH_WEST_TOWER = new RelativeBox(-10, 1, -10, -8, TOWER_HEIGHT, -8);
    public static final RelativeBox NORTH_EAST_TOWER = new RelativeBox(8, 1, -10, 10, TOWER_HEIGHT, -8);
    public static final RelativeBox SOUTH_WEST_TOWER = new RelativeBox(-10, 1, 8, -8, TOWER_HEIGHT, 10);
    public static final RelativeBox SOUTH_EAST_TOWER = new RelativeBox(8, 1, 8, 10, TOWER_HEIGHT, 10);

    public static final RelativeBox NORTH_WING = new RelativeBox(-8, 1, -10, 8, WING_HEIGHT, -6);
    public static final RelativeBox WEST_WING = new RelativeBox(-10, 1, -5, -6, WING_HEIGHT, 6);
    public static final RelativeBox EAST_WING = new RelativeBox(6, 1, -5, 10, WING_HEIGHT, 6);

    public static final RelativeBox GATE_POST_LEFT = new RelativeBox(-2, 1, 10, -2, WALL_HEIGHT, 10);
    public static final RelativeBox GATE_POST_RIGHT = new RelativeBox(2, 1, 10, 2, WALL_HEIGHT, 10);
    public static final RelativeBox GATE_LINTEL = new RelativeBox(-2, WALL_HEIGHT, 10, 2, WALL_HEIGHT, 10);
    public static final RelativeBox GATE_OPENING = new RelativeBox(-1, 1, 10, 1, WALL_HEIGHT - 1, 10);

    public static final RelativeBox COURTYARD = new RelativeBox(-5, 0, -4, 5, 0, 6);
    public static final RelativeBox AUTHORITY_GUIDE = new RelativeBox(-2, 0, -2, 2, 1, 2);
    public static final RelativeBox INTERIOR_GUIDE = new RelativeBox(-5, 1, -4, 5, 1, 6);

    public static final List<RelativeBox> PALISADE_SEGMENTS = List.of(
            NORTH_WALL,
            SOUTH_WALL_LEFT,
            SOUTH_WALL_RIGHT,
            WEST_WALL,
            EAST_WALL
    );

    public static final List<RelativeBox> TOWERS = List.of(
            NORTH_WEST_TOWER,
            NORTH_EAST_TOWER,
            SOUTH_WEST_TOWER,
            SOUTH_EAST_TOWER
    );

    public static final List<RelativeBox> WINGS = List.of(
            NORTH_WING,
            WEST_WING,
            EAST_WING
    );

    public static final List<RelativeBox> GATE_ARCH = List.of(
            GATE_POST_LEFT,
            GATE_POST_RIGHT,
            GATE_LINTEL
    );

    private StarterFortPlan() {
    }

    public record RelativeBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public RelativeBox {
            if (maxX < minX || maxY < minY || maxZ < minZ) {
                throw new IllegalArgumentException("Starter fort box max must be >= min.");
            }
        }

        public AABB toAabb(BlockPos anchor) {
            return new AABB(
                    anchor.getX() + this.minX,
                    anchor.getY() + this.minY,
                    anchor.getZ() + this.minZ,
                    anchor.getX() + this.maxX + 1.0D,
                    anchor.getY() + this.maxY + 1.0D,
                    anchor.getZ() + this.maxZ + 1.0D
            );
        }

        public BlockPos minPos(BlockPos anchor) {
            return anchor.offset(this.minX, this.minY, this.minZ);
        }

        public BlockPos maxPos(BlockPos anchor) {
            return anchor.offset(this.maxX, this.maxY, this.maxZ);
        }
    }
}
