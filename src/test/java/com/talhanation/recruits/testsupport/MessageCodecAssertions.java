package com.talhanation.recruits.testsupport;

import com.talhanation.recruits.network.MessageMovement;
import com.talhanation.recruits.network.MessageAttack;
import com.talhanation.recruits.network.MessageShields;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class MessageCodecAssertions {

    private MessageCodecAssertions() {
    }

    public static MessageMovement assertMovementRoundTrip(MessageMovement original) {
        MessageMovement restored = roundTrip(buffer -> original.toBytes(buffer), buffer -> new MessageMovement().fromBytes(buffer));

        assertEquals(readField(original, "player_uuid"), readField(restored, "player_uuid"));
        assertEquals(readField(original, "state"), readField(restored, "state"));
        assertEquals(readField(original, "group"), readField(restored, "group"));
        assertEquals(readField(original, "formation"), readField(restored, "formation"));
        assertEquals(readField(original, "tight"), readField(restored, "tight"));

        return restored;
    }

    public static MessageAttack assertAttackRoundTrip(MessageAttack original) {
        MessageAttack restored = roundTrip(buffer -> original.toBytes(buffer), buffer -> new MessageAttack().fromBytes(buffer));

        assertEquals(readField(original, "playerUuid"), readField(restored, "playerUuid"));
        assertEquals(readField(original, "group"), readField(restored, "group"));

        return restored;
    }

    public static MessageShields assertShieldsRoundTrip(MessageShields original) {
        MessageShields restored = roundTrip(buffer -> original.toBytes(buffer), buffer -> new MessageShields().fromBytes(buffer));

        assertEquals(readField(original, "player"), readField(restored, "player"));
        assertEquals(readField(original, "group"), readField(restored, "group"));
        assertEquals(readField(original, "should"), readField(restored, "should"));

        return restored;
    }

    public static <T> T roundTrip(BufferWriter writer, Function<FriendlyByteBuf, T> reader) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        writer.write(buffer);
        return reader.apply(buffer);
    }

    private static Object readField(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        }
        catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to read message field: " + fieldName, e);
        }
    }

    @FunctionalInterface
    public interface BufferWriter {
        void write(FriendlyByteBuf buffer);
    }
}
