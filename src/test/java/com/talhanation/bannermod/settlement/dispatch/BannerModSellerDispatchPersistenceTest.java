package com.talhanation.bannermod.settlement.dispatch;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the persistence contract for {@link BannerModSellerDispatchRuntime} — the seller-phase
 * subsystem of SETTLEMENT-004 ("Reload does not lose active meaningful settlement work
 * state"). The existing {@link BannerModSellerDispatchRuntimeTest} covers in-memory phase
 * semantics; this suite focuses on the NBT roundtrip surface ({@code toTag} / {@code fromTag}
 * plus {@link SellerPhaseRecord#toTag} / {@link SellerPhaseRecord#fromTag}) so a regression in
 * either side fails a fast unit test rather than only surfacing as a save-corruption bug.
 *
 * <p>Tests run against the runtime directly rather than through {@code BannerModSellerDispatchSavedData}
 * because the {@code SavedData} layer is a thin one — it forwards to {@code runtime.toTag} /
 * {@code BannerModSellerDispatchRuntime.fromTag} and adds vanilla level-data plumbing that
 * needs a {@code ServerLevel}. Locking the runtime contract is the high-leverage
 * unit-testable seam.</p>
 */
class BannerModSellerDispatchPersistenceTest {

    private static final UUID SELLER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SELLER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SELLER_C = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID MARKET_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID MARKET_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void emptyRuntimeRoundTripsToEmptyRuntime() {
        BannerModSellerDispatchRuntime original = new BannerModSellerDispatchRuntime();

        BannerModSellerDispatchRuntime decoded = BannerModSellerDispatchRuntime.fromTag(original.toTag());

        assertTrue(decoded.activeDispatches().isEmpty(),
                "empty runtime must roundtrip to empty runtime — no spurious entries from NBT decoding");
    }

    @Test
    void multiSellerMixedPhaseRoundTripPreservesEveryField() {
        BannerModSellerDispatchRuntime original = new BannerModSellerDispatchRuntime();
        original.beginDispatch(SELLER_A, MARKET_A, 1_000L);
        original.beginDispatch(SELLER_B, MARKET_B, 2_000L);
        // Walk SELLER_A through into AT_STALL so we exercise non-default phase + non-zero tick.
        for (int i = 0; i < BannerModSellerDispatchRuntime.MOVE_TO_STALL_MAX_TICKS; i++) {
            original.tickPhase(SELLER_A, 1_000L + i + 1);
        }
        // Force SELLER_B into a terminal phase to cover that side of the enum too.
        original.advance(SELLER_B, SellerPhase.MARKET_CLOSED, 5_000L);

        List<SellerPhaseRecord> before = original.activeDispatches();

        BannerModSellerDispatchRuntime decoded = BannerModSellerDispatchRuntime.fromTag(original.toTag());

        assertEquals(before, decoded.activeDispatches(),
                "decoded runtime must equal the original record-for-record after NBT roundtrip");
    }

    @Test
    void roundTripPreservesInsertionOrder() {
        // LinkedHashMap-backed iteration order is the deterministic contract that
        // BannerModSellerDispatchAdvisor relies on. The roundtrip must preserve it — a
        // HashMap on the decode side would silently break that contract.
        BannerModSellerDispatchRuntime original = new BannerModSellerDispatchRuntime();
        original.beginDispatch(SELLER_C, MARKET_A, 0L);
        original.beginDispatch(SELLER_A, MARKET_B, 0L);
        original.beginDispatch(SELLER_B, MARKET_A, 0L);

        BannerModSellerDispatchRuntime decoded = BannerModSellerDispatchRuntime.fromTag(original.toTag());

        List<UUID> orderBefore = original.activeDispatches().stream().map(SellerPhaseRecord::sellerResidentUuid).toList();
        List<UUID> orderAfter = decoded.activeDispatches().stream().map(SellerPhaseRecord::sellerResidentUuid).toList();
        assertEquals(orderBefore, orderAfter,
                "insertion order must survive the NBT roundtrip — advisor iteration is order-sensitive");
    }

    @Test
    void roundTripPreservesPhaseTickCountAndPhaseStartGameTime() {
        // Both fields advance independently of phase identity; a roundtrip that loses either
        // would silently restart the budget timer on reload.
        BannerModSellerDispatchRuntime original = new BannerModSellerDispatchRuntime();
        original.beginDispatch(SELLER_A, MARKET_A, 4_242L);
        for (int i = 0; i < 5; i++) {
            original.tickPhase(SELLER_A, 4_242L + i + 1);
        }
        SellerPhaseRecord beforeRecord = original.phase(SELLER_A).orElseThrow();

        BannerModSellerDispatchRuntime decoded = BannerModSellerDispatchRuntime.fromTag(original.toTag());

        SellerPhaseRecord afterRecord = decoded.phase(SELLER_A).orElseThrow();
        assertEquals(beforeRecord.phaseStartGameTime(), afterRecord.phaseStartGameTime(),
                "phaseStartGameTime must survive NBT roundtrip");
        assertEquals(beforeRecord.phaseTickCount(), afterRecord.phaseTickCount(),
                "phaseTickCount must survive NBT roundtrip");
        assertTrue(afterRecord.phaseTickCount() > 0,
                "test would be vacuous if tickCount were still 0 after the loop above");
    }

    @Test
    void everyPhaseEnumValueRoundTripsExactly() {
        // Defends against accidental enum churn — a renamed or removed value would silently
        // fall back to READY via SellerPhaseRecord.phaseFromTagName, masking the breakage in
        // production save data. We assert exact identity so the fallback never hides a typo.
        for (SellerPhase phase : SellerPhase.values()) {
            SellerPhaseRecord original = new SellerPhaseRecord(SELLER_A, MARKET_A, phase, 100L, 7);
            SellerPhaseRecord decoded = SellerPhaseRecord.fromTag(original.toTag());
            assertEquals(phase, decoded.phase(),
                    "SellerPhase." + phase.name() + " must roundtrip to itself, not fall back to READY");
            assertEquals(original, decoded,
                    "full record equality must hold for SellerPhase." + phase.name());
        }
    }

    @Test
    void unknownPhaseNameFallsBackToReady() {
        // Forward-compat: a future build that adds a new phase must not crash the loader on
        // older JVMs. The contract is "fall back to READY" rather than "throw" — locked here
        // explicitly so the fallback is intentional, not an accident waiting to be removed.
        CompoundTag tag = new CompoundTag();
        tag.putUUID("SellerResidentUuid", SELLER_A);
        tag.putUUID("MarketRecordUuid", MARKET_A);
        tag.putString("Phase", "PHASE_FROM_THE_FUTURE");
        tag.putLong("PhaseStartGameTime", 0L);
        tag.putInt("PhaseTickCount", 0);

        SellerPhaseRecord decoded = SellerPhaseRecord.fromTag(tag);

        assertEquals(SellerPhase.READY, decoded.phase(),
                "unknown phase name must fall back to READY, not crash the loader");
        assertEquals(SELLER_A, decoded.sellerResidentUuid(),
                "fallback must still preserve the rest of the record");
    }

    @Test
    void tickPhaseContinuesDeterministicallyAfterRoundTrip() {
        // The full reload-and-continue invariant: a seller mid-MOVE_TO_STALL on save must be
        // mid-MOVE_TO_STALL on load and still trip into AT_STALL at exactly the same total
        // tick count. A regression that reset phaseTickCount on load would let this test
        // catch it before a save-corruption report arrives from a player.
        BannerModSellerDispatchRuntime original = new BannerModSellerDispatchRuntime();
        original.beginDispatch(SELLER_A, MARKET_A, 0L);
        int ticksBeforeSave = BannerModSellerDispatchRuntime.MOVE_TO_STALL_MAX_TICKS - 5;
        for (int i = 0; i < ticksBeforeSave; i++) {
            original.tickPhase(SELLER_A, i + 1L);
        }

        BannerModSellerDispatchRuntime decoded = BannerModSellerDispatchRuntime.fromTag(original.toTag());

        // 4 more ticks should keep us in MOVING_TO_STALL — total tickCount = MAX - 1.
        for (int i = 0; i < 4; i++) {
            decoded.tickPhase(SELLER_A, ticksBeforeSave + i + 1L);
        }
        assertEquals(SellerPhase.MOVING_TO_STALL, decoded.phase(SELLER_A).orElseThrow().phase(),
                "decoded runtime must still be on MOVING_TO_STALL with one tick left in the budget");

        // The next tick crosses the budget boundary and promotes to AT_STALL.
        decoded.tickPhase(SELLER_A, ticksBeforeSave + 5L);
        assertEquals(SellerPhase.AT_STALL, decoded.phase(SELLER_A).orElseThrow().phase(),
                "tick budget must continue counting from the loaded phaseTickCount, not restart");
    }

    @Test
    void roundTripFromTagDoesNotShareReferencesWithSource() {
        // Defensive: a regression where fromTag returned the same record instances would
        // tie the loaded runtime's mutability to the on-disk decode buffer. SellerPhaseRecord
        // is immutable so this is unlikely to surface, but the test is cheap insurance.
        BannerModSellerDispatchRuntime original = new BannerModSellerDispatchRuntime();
        original.beginDispatch(SELLER_A, MARKET_A, 0L);

        BannerModSellerDispatchRuntime decoded = BannerModSellerDispatchRuntime.fromTag(original.toTag());

        // Mutate the decoded runtime; the original must be unaffected.
        decoded.advance(SELLER_A, SellerPhase.RETURNED, 100L);

        assertNotEquals(decoded.activeDispatches(), original.activeDispatches(),
                "mutating the decoded runtime must not bleed into the source runtime");
    }
}
