package com.talhanation.workers.network;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkAreaRotationTest {

    @Test
    void rotatesNorthClockwiseToEast() {
        assertEquals(Direction.EAST, WorkAreaRotation.rotate(Direction.NORTH, true));
    }

    @Test
    void rotatesNorthCounterClockwiseToWest() {
        assertEquals(Direction.WEST, WorkAreaRotation.rotate(Direction.NORTH, false));
    }

    @Test
    void keepsVerticalDirectionsUnchanged() {
        assertEquals(Direction.UP, WorkAreaRotation.rotate(Direction.UP, true));
        assertEquals(Direction.DOWN, WorkAreaRotation.rotate(Direction.DOWN, false));
    }
}
