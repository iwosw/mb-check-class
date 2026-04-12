package com.talhanation.workers;

import com.talhanation.workers.entities.workarea.MiningPatternSettings;
import com.talhanation.workers.network.MessageUpdateMiningArea;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageUpdateMiningAreaCodecTest {

    @Test
    void codecRoundTripsEveryMiningField() {
        MessageUpdateMiningArea original = new MessageUpdateMiningArea(
                java.util.UUID.randomUUID(),
                5,
                4,
                -12,
                false,
                MiningPatternSettings.Mode.BRANCH.getIndex(),
                6,
                14,
                2
        );

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        original.toBytes(buffer);

        MessageUpdateMiningArea decoded = new MessageUpdateMiningArea().fromBytes(buffer);

        assertEquals(original.uuid, decoded.uuid);
        assertEquals(5, decoded.xSize);
        assertEquals(4, decoded.ySize);
        assertEquals(-12, decoded.yOffset);
        assertFalse(decoded.closeFloor);
        assertEquals(MiningPatternSettings.Mode.BRANCH.getIndex(), decoded.miningMode);
        assertEquals(6, decoded.branchSpacing);
        assertEquals(14, decoded.branchLength);
        assertEquals(2, decoded.descentStep);
        assertTrue(Arrays.stream(MessageUpdateMiningArea.class.getDeclaredFields()).noneMatch(field -> field.getName().equals("zSize")));
    }

    @Test
    void miningAreaPersistenceHelpersKeepSelectedModeAndBranchGeometry() {
        MiningPatternSettings settings = new MiningPatternSettings(
                MiningPatternSettings.Mode.BRANCH,
                3,
                3,
                -20,
                true,
                5,
                12,
                1
        );
        CompoundTag tag = new CompoundTag();

        settings.writeToRoot(tag);
        MiningPatternSettings restored = MiningPatternSettings.fromRoot(tag);

        assertEquals(settings.mode(), restored.mode());
        assertEquals(settings.branchSpacing(), restored.branchSpacing());
        assertEquals(settings.branchLength(), restored.branchLength());
        assertEquals(settings.heightOffset(), restored.heightOffset());
        assertTrue(restored.closeFloor());
    }
}
