package com.talhanation.bannermod.network.messages.war;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliticalEntityMutationPacketRoundTripTest {

    private static final UUID ENTITY_ID = UUID.fromString("00000000-0000-0000-0000-0000000000ff");

    private static FriendlyByteBuf buffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    @Test
    void createPacketPayloadRoundTrip() {
        MessageCreatePoliticalEntity sent = new MessageCreatePoliticalEntity("Acadia Republic");
        FriendlyByteBuf buf = buffer();
        sent.toBytes(buf);

        MessageCreatePoliticalEntity decoded = new MessageCreatePoliticalEntity().fromBytes(buf);
        // Reuse encode→decode path to compare wire-visible state, since the message has no public getters.
        FriendlyByteBuf out = buffer();
        decoded.toBytes(out);

        assertEquals(0, buf.readableBytes());
        FriendlyByteBuf reference = buffer();
        sent.toBytes(reference);
        assertEquals(reference.readableBytes(), out.readableBytes());
        assertTrue(java.util.Arrays.equals(toArray(reference), toArray(out)));
    }

    @Test
    void renamePacketPayloadRoundTrip() {
        MessageRenamePoliticalEntity sent = new MessageRenamePoliticalEntity(ENTITY_ID, "New Name");
        FriendlyByteBuf buf = buffer();
        sent.toBytes(buf);

        MessageRenamePoliticalEntity decoded = new MessageRenamePoliticalEntity().fromBytes(buf);
        FriendlyByteBuf out = buffer();
        decoded.toBytes(out);

        FriendlyByteBuf reference = buffer();
        sent.toBytes(reference);
        assertEquals(reference.readableBytes(), out.readableBytes());
        assertTrue(java.util.Arrays.equals(toArray(reference), toArray(out)));
    }

    @Test
    void setCapitalPacketPayloadRoundTrip_PlayerPosVariant() {
        MessageSetPoliticalEntityCapital sent = new MessageSetPoliticalEntityCapital(ENTITY_ID);
        FriendlyByteBuf buf = buffer();
        sent.toBytes(buf);

        MessageSetPoliticalEntityCapital decoded = new MessageSetPoliticalEntityCapital().fromBytes(buf);
        FriendlyByteBuf out = buffer();
        decoded.toBytes(out);

        FriendlyByteBuf reference = buffer();
        sent.toBytes(reference);
        assertEquals(reference.readableBytes(), out.readableBytes());
        assertTrue(java.util.Arrays.equals(toArray(reference), toArray(out)));
    }

    @Test
    void setCapitalPacketPayloadRoundTrip_ExplicitPosVariant() {
        BlockPos pos = new BlockPos(123, 64, -77);
        MessageSetPoliticalEntityCapital sent = new MessageSetPoliticalEntityCapital(ENTITY_ID, false, pos);
        FriendlyByteBuf buf = buffer();
        sent.toBytes(buf);

        MessageSetPoliticalEntityCapital decoded = new MessageSetPoliticalEntityCapital().fromBytes(buf);
        FriendlyByteBuf out = buffer();
        decoded.toBytes(out);

        FriendlyByteBuf reference = buffer();
        sent.toBytes(reference);
        assertEquals(reference.readableBytes(), out.readableBytes());
        assertTrue(java.util.Arrays.equals(toArray(reference), toArray(out)));
    }

    private static byte[] toArray(FriendlyByteBuf buf) {
        byte[] arr = new byte[buf.readableBytes()];
        buf.readBytes(arr);
        return arr;
    }
}
