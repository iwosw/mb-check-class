package com.talhanation.bannermod.settlement.household;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Supplements {@link BannerModHomeAssignmentRuntimeTest} for the SETTLEMENT-004 audit. The
 * existing suite covers basic NBT roundtrip + dirty-marking semantics; this suite locks the
 * gaps that matter for save-data robustness:
 *
 * <ul>
 *   <li>empty runtime roundtrips to empty (no spurious entries from NBT decoding)</li>
 *   <li>every {@link HomePreference} value roundtrips to itself, not the NONE fallback</li>
 *   <li>an unknown future preference name falls back to NONE without crashing the loader
 *       — forward-compat contract</li>
 *   <li>multi-resident insertion order survives the roundtrip — relied on by deterministic
 *       advisor / debug iteration</li>
 *   <li>{@code assignedAtGameTime} survives the roundtrip across a non-trivial value</li>
 *   <li>decoded runtime is independent of the source (no shared mutability)</li>
 * </ul>
 *
 * <p>Same shape as {@code BannerModSellerDispatchPersistenceTest} so a future audit pass over
 * a third settlement-state subsystem has a known template to copy from.</p>
 */
class BannerModHomeAssignmentPersistenceTest {

    private static final UUID RESIDENT_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID RESIDENT_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RESIDENT_C = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID HOUSE_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID HOUSE_2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void emptyRuntimeRoundTripsToEmptyRuntime() {
        BannerModHomeAssignmentRuntime original = new BannerModHomeAssignmentRuntime();

        BannerModHomeAssignmentRuntime decoded = BannerModHomeAssignmentRuntime.fromTag(original.toTag());

        assertEquals(0, decoded.totalAssignments(),
                "empty runtime must roundtrip to empty — no spurious entries from NBT decoding");
    }

    @Test
    void everyPreferenceValueRoundTripsExactly() {
        // Defends against accidental enum churn — a renamed or removed value would silently
        // fall back to NONE via HomeAssignment.preferenceFromTagName, masking the breakage in
        // production save data. Asserting exact identity ensures the fallback never hides a
        // typo on the encode side.
        for (HomePreference preference : HomePreference.values()) {
            HomeAssignment original = new HomeAssignment(RESIDENT_A, HOUSE_1, 100L, preference);
            HomeAssignment decoded = HomeAssignment.fromTag(original.toTag());
            assertEquals(preference, decoded.preference(),
                    "HomePreference." + preference.name() + " must roundtrip to itself, not fall back to NONE");
            assertEquals(original, decoded,
                    "full assignment equality must hold for HomePreference." + preference.name());
        }
    }

    @Test
    void unknownPreferenceNameFallsBackToNone() {
        // Forward-compat: a future build that adds a new preference must not crash the
        // loader on older JVMs. The contract is "fall back to NONE" rather than "throw" —
        // locked here explicitly so the fallback is intentional, not an accident waiting to
        // be removed.
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ResidentUuid", RESIDENT_A);
        tag.putUUID("HomeBuildingUuid", HOUSE_1);
        tag.putLong("AssignedAtGameTime", 999L);
        tag.putString("Preference", "PREFERENCE_FROM_THE_FUTURE");

        HomeAssignment decoded = HomeAssignment.fromTag(tag);

        assertEquals(HomePreference.NONE, decoded.preference(),
                "unknown preference name must fall back to NONE, not crash the loader");
        assertEquals(RESIDENT_A, decoded.residentUuid(),
                "fallback must still preserve the rest of the record");
        assertEquals(999L, decoded.assignedAtGameTime(),
                "fallback must still preserve the assignedAtGameTime field");
    }

    @Test
    void multiResidentInsertionOrderSurvivesRoundTrip() {
        // The existing nbtRoundTripRestoresAssignmentsInOrder covers two residents; this
        // case stresses the LinkedHashMap iteration contract with a non-alphabetical
        // insertion (RESIDENT_C first, then A, then B) so a HashMap on the decode side
        // would visibly fail this test rather than coincidentally pass.
        BannerModHomeAssignmentRuntime original = new BannerModHomeAssignmentRuntime();
        original.assign(RESIDENT_C, HOUSE_1, HomePreference.SHARED, 1L);
        original.assign(RESIDENT_A, HOUSE_2, HomePreference.ASSIGNED, 2L);
        original.assign(RESIDENT_B, HOUSE_1, HomePreference.SHARED, 3L);

        BannerModHomeAssignmentRuntime decoded = BannerModHomeAssignmentRuntime.fromTag(original.toTag());

        List<UUID> beforeOrder = original.snapshot().stream().map(HomeAssignment::residentUuid).toList();
        List<UUID> afterOrder = decoded.snapshot().stream().map(HomeAssignment::residentUuid).toList();
        assertEquals(beforeOrder, afterOrder,
                "non-alphabetical insertion order must survive NBT roundtrip");
        assertEquals(List.of(RESIDENT_C, RESIDENT_A, RESIDENT_B), afterOrder,
                "decoded order must match the explicit insertion order, not natural sort");
    }

    @Test
    void assignedAtGameTimeSurvivesNonTrivialValue() {
        // The existing roundtrip test asserts a small game time; this case uses a value
        // beyond Integer.MAX_VALUE so a regression that accidentally narrowed the field to
        // int on the decode side would surface here.
        long farFuture = ((long) Integer.MAX_VALUE) + 1_000_000L;
        BannerModHomeAssignmentRuntime original = new BannerModHomeAssignmentRuntime();
        original.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, farFuture);

        BannerModHomeAssignmentRuntime decoded = BannerModHomeAssignmentRuntime.fromTag(original.toTag());

        assertEquals(farFuture, decoded.homeFor(RESIDENT_A).orElseThrow().assignedAtGameTime(),
                "assignedAtGameTime must remain a long across the roundtrip, not narrow to int");
    }

    @Test
    void decodedRuntimeIsIndependentOfSource() {
        // Defensive: a regression where fromTag returned the same record instances would
        // tie the loaded runtime's mutability to the on-disk decode buffer. HomeAssignment
        // is immutable so this is unlikely to surface, but the test is cheap insurance.
        BannerModHomeAssignmentRuntime original = new BannerModHomeAssignmentRuntime();
        original.assign(RESIDENT_A, HOUSE_1, HomePreference.ASSIGNED, 0L);

        BannerModHomeAssignmentRuntime decoded = BannerModHomeAssignmentRuntime.fromTag(original.toTag());

        decoded.clearAssignment(RESIDENT_A);

        assertEquals(0, decoded.totalAssignments(), "decoded runtime must be mutable in isolation");
        assertEquals(1, original.totalAssignments(),
                "mutating the decoded runtime must not bleed into the source runtime");
        assertTrue(original.homeFor(RESIDENT_A).isPresent(),
                "source runtime's binding must survive a mutation on the decoded copy");
        assertNotEquals(original.snapshot(), decoded.snapshot(),
                "source and decoded snapshots must diverge after the mutation");
    }
}
