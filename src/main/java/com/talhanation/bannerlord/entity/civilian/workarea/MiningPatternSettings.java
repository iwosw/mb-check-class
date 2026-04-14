package com.talhanation.bannerlord.entity.civilian.workarea;

import net.minecraft.nbt.CompoundTag;

public record MiningPatternSettings(
        Mode mode,
        int width,
        int height,
        int heightOffset,
        boolean closeFloor,
        int branchSpacing,
        int branchLength,
        int descentStep
) {

    public static final String TAG_MODE = "miningMode";
    public static final String TAG_WIDTH = "miningWidth";
    public static final String TAG_HEIGHT = "miningHeight";
    public static final String TAG_HEIGHT_OFFSET = "miningHeightOffset";
    public static final String TAG_CLOSE_FLOOR = "closeFloor";
    public static final String TAG_BRANCH_SPACING = "branchSpacing";
    public static final String TAG_BRANCH_LENGTH = "branchLength";
    public static final String TAG_DESCENT_STEP = "descentStep";

    public MiningPatternSettings {
        mode = mode == null ? Mode.TUNNEL : mode;
        width = Math.max(1, width);
        height = Math.max(1, height);
        branchSpacing = Math.max(1, branchSpacing);
        branchLength = Math.max(1, branchLength);
        descentStep = Math.max(1, descentStep);
    }

    public static MiningPatternSettings tunnel(int width, int height, int heightOffset, boolean closeFloor, int descentStep) {
        return new MiningPatternSettings(Mode.TUNNEL, width, height, heightOffset, closeFloor, 3, 8, descentStep);
    }

    public static MiningPatternSettings branch(int width, int height, int heightOffset, boolean closeFloor, int branchSpacing, int branchLength) {
        return new MiningPatternSettings(Mode.BRANCH, width, height, heightOffset, closeFloor, branchSpacing, branchLength, 1);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_MODE, mode.getIndex());
        tag.putInt(TAG_WIDTH, width);
        tag.putInt(TAG_HEIGHT, height);
        tag.putInt(TAG_HEIGHT_OFFSET, heightOffset);
        tag.putBoolean(TAG_CLOSE_FLOOR, closeFloor);
        tag.putInt(TAG_BRANCH_SPACING, branchSpacing);
        tag.putInt(TAG_BRANCH_LENGTH, branchLength);
        tag.putInt(TAG_DESCENT_STEP, descentStep);
        return tag;
    }

    public void writeToRoot(CompoundTag root) {
        CompoundTag tag = this.toTag();
        for (String key : tag.getAllKeys()) {
            root.put(key, tag.get(key));
        }
    }

    public static MiningPatternSettings fromTag(CompoundTag tag) {
        return new MiningPatternSettings(
                Mode.fromIndex(tag.contains(TAG_MODE) ? tag.getInt(TAG_MODE) : Mode.TUNNEL.getIndex()),
                tag.contains(TAG_WIDTH) ? tag.getInt(TAG_WIDTH) : 3,
                tag.contains(TAG_HEIGHT) ? tag.getInt(TAG_HEIGHT) : 3,
                tag.contains(TAG_HEIGHT_OFFSET) ? tag.getInt(TAG_HEIGHT_OFFSET) : 1,
                tag.contains(TAG_CLOSE_FLOOR) ? tag.getBoolean(TAG_CLOSE_FLOOR) : true,
                tag.contains(TAG_BRANCH_SPACING) ? tag.getInt(TAG_BRANCH_SPACING) : 3,
                tag.contains(TAG_BRANCH_LENGTH) ? tag.getInt(TAG_BRANCH_LENGTH) : 8,
                tag.contains(TAG_DESCENT_STEP) ? tag.getInt(TAG_DESCENT_STEP) : 1
        );
    }

    public static MiningPatternSettings fromRoot(CompoundTag root) {
        CompoundTag tag = new CompoundTag();
        if (root.contains(TAG_MODE)) tag.putInt(TAG_MODE, root.getInt(TAG_MODE));
        if (root.contains(TAG_WIDTH)) tag.putInt(TAG_WIDTH, root.getInt(TAG_WIDTH));
        if (root.contains(TAG_HEIGHT)) tag.putInt(TAG_HEIGHT, root.getInt(TAG_HEIGHT));
        if (root.contains(TAG_HEIGHT_OFFSET)) tag.putInt(TAG_HEIGHT_OFFSET, root.getInt(TAG_HEIGHT_OFFSET));
        if (root.contains(TAG_CLOSE_FLOOR)) tag.putBoolean(TAG_CLOSE_FLOOR, root.getBoolean(TAG_CLOSE_FLOOR));
        if (root.contains(TAG_BRANCH_SPACING)) tag.putInt(TAG_BRANCH_SPACING, root.getInt(TAG_BRANCH_SPACING));
        if (root.contains(TAG_BRANCH_LENGTH)) tag.putInt(TAG_BRANCH_LENGTH, root.getInt(TAG_BRANCH_LENGTH));
        if (root.contains(TAG_DESCENT_STEP)) tag.putInt(TAG_DESCENT_STEP, root.getInt(TAG_DESCENT_STEP));
        return fromTag(tag);
    }

    public enum Mode {
        CUSTOM(0),
        MINE(1),
        TUNNEL(2),
        BRANCH(3);

        private final int index;

        Mode(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static Mode fromIndex(int index) {
            for (Mode value : values()) {
                if (value.index == index) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid mining mode index: " + index);
        }
    }
}
