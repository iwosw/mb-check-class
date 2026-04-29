package com.talhanation.bannermod.compat;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmallShipsSeaTradeCompatTest {

    private static final ResourceLocation COG = ResourceLocation.fromNamespaceAndPath("smallships", "cog");
    private static final ResourceLocation GALLEY = ResourceLocation.fromNamespaceAndPath("smallships", "galley");
    private static final ResourceLocation MINECRAFT_BOAT = ResourceLocation.fromNamespaceAndPath("minecraft", "boat");

    @Test
    void absentSmallShipsExposesNoCarrierCandidates() {
        List<SmallShipsSeaTradeCompat.CarrierCandidate> candidates = SmallShipsSeaTradeCompat.candidateCarrierTypes(
                false,
                List.of(COG, GALLEY)
        );

        assertTrue(candidates.isEmpty());
    }

    @Test
    void loadedSmallShipsExposesOnlyRegisteredSupportedCarrierCandidates() {
        List<SmallShipsSeaTradeCompat.CarrierCandidate> candidates = SmallShipsSeaTradeCompat.candidateCarrierTypes(
                true,
                List.of(COG, MINECRAFT_BOAT)
        );

        assertEquals(List.of(new SmallShipsSeaTradeCompat.CarrierCandidate(COG)), candidates);
    }

    @Test
    void loadedSmallShipsWithRegisteredSupportedTypeReportsBindableCandidate() {
        boolean bindable = SmallShipsSeaTradeCompat.hasBindableCarrierCandidate(
                true,
                List.of(COG, MINECRAFT_BOAT),
                className -> {
                    if ("com.talhanation.smallships.world.entity.ship.Ship".equals(className)) {
                        return FakeSmallShip.class;
                    }
                    throw new ClassNotFoundException(className);
                }
        );

        assertTrue(bindable);
    }

    @Test
    void bindsSupportedSmallShipsCarrierReflectively() {
        UUID carrierId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID ownerId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        var bound = SmallShipsSeaTradeCompat.bindCarrier(
                new SupportedSmallShip(),
                COG,
                carrierId,
                ownerId,
                true,
                className -> {
                    if ("com.talhanation.smallships.world.entity.ship.Ship".equals(className)) {
                        return SupportedSmallShip.class;
                    }
                    throw new ClassNotFoundException(className);
                }
        );

        assertTrue(bound.isPresent());
        assertEquals(new SmallShipsSeaTradeCompat.BoundCarrier(
                COG,
                carrierId,
                ownerId,
                new SmallShipsSeaTradeCompat.CarrierSupport(true, java.util.Optional.empty())
        ), bound.get());
    }

    @Test
    void reportsSupportedCarrierCapabilitiesReflectively() {
        SmallShipsSeaTradeCompat.CarrierSupport support = SmallShipsSeaTradeCompat.inspectCarrierSupport(
                new SupportedSmallShip(),
                COG,
                true,
                className -> SupportedSmallShip.class
        );

        assertTrue(support.supported());
        assertTrue(support.unsupportedReason().isEmpty());
    }

    @Test
    void reportsMissingCargoCapabilityReason() {
        SmallShipsSeaTradeCompat.CarrierSupport support = SmallShipsSeaTradeCompat.inspectCarrierSupport(
                new MissingCargoSmallShip(),
                COG,
                true,
                className -> MissingCargoSmallShip.class
        );

        assertFalse(support.supported());
        assertEquals("chat.bannermod.sea_trade.carrier.unsupported.missing_cargo", support.unsupportedReason().orElseThrow());
    }

    @Test
    void reportsMissingPositionStateCapabilityReason() {
        SmallShipsSeaTradeCompat.CarrierSupport support = SmallShipsSeaTradeCompat.inspectCarrierSupport(
                new MissingPositionSmallShip(),
                COG,
                true,
                className -> MissingPositionSmallShip.class
        );

        assertFalse(support.supported());
        assertEquals("chat.bannermod.sea_trade.carrier.unsupported.missing_position", support.unsupportedReason().orElseThrow());
    }

    @Test
    void reportsMissingNavigationCapabilityReason() {
        SmallShipsSeaTradeCompat.CarrierSupport support = SmallShipsSeaTradeCompat.inspectCarrierSupport(
                new MissingNavigationSmallShip(),
                COG,
                true,
                className -> MissingNavigationSmallShip.class
        );

        assertFalse(support.supported());
        assertEquals("chat.bannermod.sea_trade.carrier.unsupported.missing_navigation", support.unsupportedReason().orElseThrow());
    }

    @Test
    void rejectsUnsupportedOrAbsentCarrierBindings() {
        UUID carrierId = UUID.fromString("00000000-0000-0000-0000-000000000003");
        UUID ownerId = UUID.fromString("00000000-0000-0000-0000-000000000004");

        assertTrue(SmallShipsSeaTradeCompat.bindCarrier(new FakeSmallShip(), COG, carrierId, ownerId, false,
                className -> FakeSmallShip.class).isEmpty());
        assertTrue(SmallShipsSeaTradeCompat.bindCarrier(new FakeSmallShip(), MINECRAFT_BOAT, carrierId, ownerId, true,
                className -> FakeSmallShip.class).isEmpty());
        assertTrue(SmallShipsSeaTradeCompat.bindCarrier(new Object(), COG, carrierId, ownerId, true,
                className -> FakeSmallShip.class).isEmpty());
    }

    private static class SupportedSmallShip {
        public Object getCargoContainer() {
            return new Object();
        }

        public double getX() {
            return 1.0D;
        }

        public double getY() {
            return 2.0D;
        }

        public double getZ() {
            return 3.0D;
        }

        public boolean isAlive() {
            return true;
        }

        public Object getNavigation() {
            return new Object();
        }
    }

    private static final class FakeSmallShip extends SupportedSmallShip {
    }

    private static final class MissingCargoSmallShip extends SupportedSmallShip {
        @Override
        public Object getCargoContainer() {
            return null;
        }
    }

    private static final class MissingPositionSmallShip {
        public Object getCargoContainer() {
            return new Object();
        }

        public Object getNavigation() {
            return new Object();
        }
    }

    private static final class MissingNavigationSmallShip extends SupportedSmallShip {
        @Override
        public Object getNavigation() {
            return null;
        }
    }
}
