package com.talhanation.bannermod.network.payload;

import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * REFLLOG-001 acceptance tests.
 *
 * <p>Before this change, {@code BannerModMessage.invokeLegacy} swallowed
 * {@link NoSuchMethodException} on the assumption "this packet is one-way".
 * In practice the {@link BannerModMessage} interface provides {@code default}
 * no-op {@code executeServerSide} / {@code executeClientSide} implementations,
 * so a typoed override (e.g. {@code exectueServerSide}) silently routes through
 * the default no-op &mdash; the packet appears to register and "work" but does
 * nothing.</p>
 *
 * <p>The fix is at registration time: {@link BannerModMessageSides#register}
 * scans the concrete class for declared side handlers and refuses to register
 * if none are found. {@link BannerModMessage#invokeLegacy} additionally turns
 * a "declared this side, but reflection still cannot find the method" mismatch
 * into a loud {@link IllegalStateException}, defending against future refactors
 * that delete the interface defaults.</p>
 */
class BannerModMessageSidesTest {

    @AfterEach
    void clearRegistry() {
        BannerModMessageSides.forget(MisspelledOneWayPacket.class);
        BannerModMessageSides.forget(LegitimateOneWayServerPacket.class);
        BannerModMessageSides.forget(LegitimateOneWayClientPacket.class);
        BannerModMessageSides.forget(LegitimateTwoWayPacket.class);
    }

    @Test
    void misspelledHandlerFailsRegistration() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> BannerModMessageSides.register(MisspelledOneWayPacket.class));
        String msg = ex.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains(MisspelledOneWayPacket.class.getName()),
                "error must name the offending class, was: " + msg);
        assertTrue(msg.contains("executeServerSide") || msg.contains("executeClientSide"),
                "error must mention the expected handler names, was: " + msg);
        assertFalse(BannerModMessageSides.isRegistered(MisspelledOneWayPacket.class),
                "failed registration must not leave the class in the registry");
    }

    @Test
    void serverboundOneWayPacketRegistersQuietly() {
        var sides = BannerModMessageSides.register(LegitimateOneWayServerPacket.class);
        assertEquals(1, sides.size());
        assertTrue(sides.contains(PacketFlow.SERVERBOUND));
        assertFalse(sides.contains(PacketFlow.CLIENTBOUND));
        assertTrue(BannerModMessageSides.declares(LegitimateOneWayServerPacket.class, PacketFlow.SERVERBOUND));
        assertFalse(BannerModMessageSides.declares(LegitimateOneWayServerPacket.class, PacketFlow.CLIENTBOUND));
    }

    @Test
    void clientboundOneWayPacketRegistersQuietly() {
        var sides = BannerModMessageSides.register(LegitimateOneWayClientPacket.class);
        assertEquals(1, sides.size());
        assertTrue(sides.contains(PacketFlow.CLIENTBOUND));
        assertFalse(sides.contains(PacketFlow.SERVERBOUND));
    }

    @Test
    void twoWayPacketRegistersQuietly() {
        var sides = BannerModMessageSides.register(LegitimateTwoWayPacket.class);
        assertEquals(2, sides.size());
        assertTrue(sides.contains(PacketFlow.SERVERBOUND));
        assertTrue(sides.contains(PacketFlow.CLIENTBOUND));
    }

    @Test
    void invokeLegacyOnUndeclaredSideStaysSilent() {
        BannerModMessageSides.register(LegitimateOneWayServerPacket.class);
        // Calling executeClientSide via the legacy path on a server-only packet
        // is the legitimate one-way case and must stay silent. The interface
        // default no-op will handle the call.
        LegitimateOneWayServerPacket packet = new LegitimateOneWayServerPacket();
        packet.invokeLegacy("executeClientSide", BannerModNetworkContext.class, null);
    }

    @Test
    void invokeLegacyKeepsHistoricalSilenceOnNonSideMethods() {
        // toBytes / fromBytes have interface defaults too; calling invokeLegacy
        // with an unrelated name must not throw (NoSuchMethodException is still
        // swallowed for non-side reflection paths). This guards against the
        // REFLLOG-001 change accidentally breaking the legacy serialization
        // adapter.
        BannerModMessageSides.register(LegitimateOneWayServerPacket.class);
        LegitimateOneWayServerPacket packet = new LegitimateOneWayServerPacket();
        packet.invokeLegacy("someMethodThatDoesNotExist", FriendlyByteBuf.class, null);
    }

    // --- Test fixtures ---------------------------------------------------

    /**
     * Bug case: only handler is misspelled. With the BannerModMessage interface
     * defaults in place, the runtime would silently invoke the default no-op
     * for the real {@code executeServerSide} method &mdash; the typo would
     * never run and the packet would appear "registered" but inert. REFLLOG-001
     * refuses to register such a class.
     */
    public static final class MisspelledOneWayPacket implements BannerModMessage<MisspelledOneWayPacket> {
        @Override
        public PacketFlow getExecutingSide() {
            return PacketFlow.SERVERBOUND;
        }

        // typo: should be executeServerSide
        public void exectueServerSide(BannerModNetworkContext context) {
            throw new AssertionError("never invoked");
        }
    }

    /** Server-only packet: declares only executeServerSide. */
    public static final class LegitimateOneWayServerPacket implements BannerModMessage<LegitimateOneWayServerPacket> {
        @Override
        public PacketFlow getExecutingSide() {
            return PacketFlow.SERVERBOUND;
        }

        @Override
        public void executeServerSide(BannerModNetworkContext context) {
            // intentional no-op
        }
    }

    /** Client-only packet: declares only executeClientSide. */
    public static final class LegitimateOneWayClientPacket implements BannerModMessage<LegitimateOneWayClientPacket> {
        @Override
        public PacketFlow getExecutingSide() {
            return PacketFlow.CLIENTBOUND;
        }

        @Override
        public void executeClientSide(BannerModNetworkContext context) {
            // intentional no-op
        }
    }

    /** Two-way packet: declares both side handlers. */
    public static final class LegitimateTwoWayPacket implements BannerModMessage<LegitimateTwoWayPacket> {
        @Override
        public PacketFlow getExecutingSide() {
            return PacketFlow.SERVERBOUND;
        }

        @Override
        public void executeServerSide(BannerModNetworkContext context) {
            // intentional no-op
        }

        @Override
        public void executeClientSide(BannerModNetworkContext context) {
            // intentional no-op
        }

        @Override
        public LegitimateTwoWayPacket fromBytes(FriendlyByteBuf buf) {
            return this;
        }

        @Override
        public void toBytes(FriendlyByteBuf buf) {
        }
    }
}
