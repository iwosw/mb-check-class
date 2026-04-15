package com.talhanation.workers.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageRecoverWorkerControlTest {

    @Test
    void roundTripsWorkerSelectionUuids() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        MessageRecoverWorkerControl message = new MessageRecoverWorkerControl(List.of(first, second));

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        message.toBytes(buffer);

        MessageRecoverWorkerControl decoded = new MessageRecoverWorkerControl().fromBytes(buffer);

        assertEquals(List.of(first, second), decoded.workerUuids);
    }
}
