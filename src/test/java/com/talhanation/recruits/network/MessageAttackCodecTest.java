package com.talhanation.recruits.network;

import com.talhanation.recruits.testsupport.MessageCodecAssertions;
import com.talhanation.recruits.testsupport.RecruitsFixtures;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageAttackCodecTest {

    @Test
    void preservesPayloadAcrossBufferRoundTrip() throws ReflectiveOperationException {
        MessageAttack message = RecruitsFixtures.sampleAttackMessage();
        MessageAttack restored = MessageCodecAssertions.assertAttackRoundTrip(message);

        assertEquals(RecruitsFixtures.ATTACK_PLAYER_UUID, readField(restored, "playerUuid"));
        assertEquals(RecruitsFixtures.ATTACK_GROUP_UUID, readField(restored, "group"));
    }

    @Test
    void exposesDispatchHelperForSharedAuthorityFlow() throws ReflectiveOperationException {
        Method method = MessageAttack.class.getDeclaredMethod("dispatchToServer", net.minecraft.server.level.ServerPlayer.class, java.util.UUID.class, java.util.UUID.class);

        assertNotNull(method);
    }

    private static Object readField(MessageAttack message, String fieldName) throws ReflectiveOperationException {
        Field field = MessageAttack.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(message);
    }
}
