package com.talhanation.bannermod.network.messages.war;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagePlaceSiegeStandardHereRoundTripTest {

    private static final UUID WAR_ID = UUID.fromString("00000000-0000-0000-0000-0000000000d1");
    private static final UUID SIDE_ID = UUID.fromString("00000000-0000-0000-0000-0000000000d2");

    private static FriendlyByteBuf buffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    private static byte[] toArray(FriendlyByteBuf buf) {
        byte[] arr = new byte[buf.readableBytes()];
        buf.readBytes(arr);
        return arr;
    }

    @Test
    void placeSiegePacketPayloadRoundTripPreservesEveryField() {
        for (int radius : new int[]{0, 16, 64, 512}) {
            MessagePlaceSiegeStandardHere sent = new MessagePlaceSiegeStandardHere(WAR_ID, SIDE_ID, radius);
            FriendlyByteBuf buf = buffer();
            sent.toBytes(buf);

            MessagePlaceSiegeStandardHere decoded = new MessagePlaceSiegeStandardHere().fromBytes(buf);
            FriendlyByteBuf out = buffer();
            decoded.toBytes(out);

            FriendlyByteBuf reference = buffer();
            sent.toBytes(reference);
            assertEquals(reference.readableBytes(), out.readableBytes(), "byte-length differs for radius=" + radius);
            assertTrue(Arrays.equals(toArray(reference), toArray(out)), "bytes differ for radius=" + radius);
        }
    }
}
