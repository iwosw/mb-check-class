package com.talhanation.recruits.network;

import com.talhanation.recruits.testsupport.MessageCodecAssertions;
import com.talhanation.recruits.testsupport.RecruitsFixtures;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageShieldsCodecTest {

    @Test
    void preservesPayloadAcrossBufferRoundTrip() throws ReflectiveOperationException {
        MessageShields message = RecruitsFixtures.sampleShieldsMessage();
        MessageShields restored = MessageCodecAssertions.assertShieldsRoundTrip(message);

        assertEquals(RecruitsFixtures.ATTACK_PLAYER_UUID, readField(restored, "player"));
        assertEquals(RecruitsFixtures.ATTACK_GROUP_UUID, readField(restored, "group"));
        assertTrue((Boolean) readField(restored, "should"));
    }

    @Test
    void exposesDispatchHelperForSharedAuthorityFlow() throws ReflectiveOperationException {
        Method method = MessageShields.class.getDeclaredMethod("dispatchToServer", net.minecraft.server.level.ServerPlayer.class, java.util.UUID.class, java.util.UUID.class, boolean.class);

        assertNotNull(method);
    }

    private static Object readField(MessageShields message, String fieldName) throws ReflectiveOperationException {
        Field field = MessageShields.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(message);
    }
}
