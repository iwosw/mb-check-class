package com.talhanation.recruits.network;

import com.talhanation.bannerlord.network.military.RecruitsNetworkRegistrar;
import com.talhanation.recruits.migration.NetworkBootstrapSeams;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitsNetworkRegistrarTest {

    @Test
    void packetRegistrationOrderRemainsStable() {
        RecruitsNetworkRegistrar registrar = new RecruitsNetworkRegistrar();

        List<NetworkBootstrapSeams.MessageRegistration> registrations = registrar.orderedMessageTypes();

        assertEquals(104, registrations.size());
        assertEquals(MessageAggro.class, registrations.get(0).messageClass());
        assertEquals(MessageMovement.class, registrations.get(10).messageClass());
        assertEquals(MessageTransferRoute.class, registrations.get(66).messageClass());
        assertEquals(MessageToClientUpdateClaims.class, registrations.get(82).messageClass());
        assertEquals(MessageFaceCommand.class, registrations.get(101).messageClass());
        assertEquals(MessageOpenGovernorScreen.class, registrations.get(102).messageClass());
        assertEquals(MessageToClientUpdateGovernorScreen.class, registrations.get(103).messageClass());
    }

    @Test
    void packetRegistrationIdsStayContiguous() {
        RecruitsNetworkRegistrar registrar = new RecruitsNetworkRegistrar();

        List<Integer> ids = registrar.orderedMessageTypes().stream()
                .map(NetworkBootstrapSeams.MessageRegistration::id)
                .toList();

        assertEquals(ids.size(), ids.stream().distinct().count());
        for (int i = 0; i < ids.size(); i++) {
            assertEquals(i, ids.get(i));
        }
    }

    @Test
    void packetRegistrationTypesStayUnique() {
        RecruitsNetworkRegistrar registrar = new RecruitsNetworkRegistrar();

        List<? extends Class<?>> messageTypes = registrar.orderedMessageTypes().stream()
                .map(NetworkBootstrapSeams.MessageRegistration::messageClass)
                .toList();
        Set<Class<?>> uniqueTypes = messageTypes.stream().collect(Collectors.toSet());

        assertEquals(messageTypes.size(), uniqueTypes.size());
        assertTrue(uniqueTypes.containsAll(List.of(
                MessageAggro.class,
                MessageMovement.class,
                MessageTransferRoute.class,
                MessageFaceCommand.class
        )));
    }
}
