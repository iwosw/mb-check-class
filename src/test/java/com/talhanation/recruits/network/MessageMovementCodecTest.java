package com.talhanation.recruits.network;

import com.talhanation.recruits.testsupport.MessageCodecAssertions;
import com.talhanation.recruits.testsupport.RecruitsFixtures;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageMovementCodecTest {

    @Test
    void preservesPayloadAcrossBufferRoundTrip() throws ReflectiveOperationException {
        MessageMovement message = RecruitsFixtures.sampleMovementMessage();
        MessageMovement restored = MessageCodecAssertions.assertMovementRoundTrip(message);

        assertEquals(RecruitsFixtures.MOVEMENT_PLAYER_UUID, readField(restored, "player_uuid"));
        assertEquals(3, readField(restored, "state"));
        assertEquals(RecruitsFixtures.MOVEMENT_GROUP_UUID, readField(restored, "group"));
        assertEquals(2, readField(restored, "formation"));
        assertTrue((Boolean) readField(restored, "tight"));
    }

    private static Object readField(MessageMovement message, String fieldName) throws ReflectiveOperationException {
        Field field = MessageMovement.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(message);
    }
}
