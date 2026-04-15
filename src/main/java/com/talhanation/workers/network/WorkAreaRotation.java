package com.talhanation.workers.network;

import net.minecraft.core.Direction;

public class WorkAreaRotation {

    private WorkAreaRotation() {
    }

    public static Direction rotate(Direction direction, boolean clockwise) {
        if (direction == null || direction.getAxis().isVertical()) {
            return direction;
        }

        return clockwise ? direction.getClockWise() : direction.getCounterClockWise();
    }
}
