package com.talhanation.bannermod.ai.military.villager;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NOBLESTREAM-001 parity test.
 *
 * <p>The production hot path in {@link VillagerBecomeNobleGoal#stop()} previously did:
 * <pre>
 *     List&lt;LivingEntity&gt; list = world.getEntitiesOfClass(...).stream().toList();
 *     boolean noblePresent = list.stream().anyMatch(e -&gt; e instanceof VillagerNobleEntity);
 *     int villagers = (int) list.stream().filter(e -&gt; e instanceof Villager).count();
 *     if (!noblePresent &amp;&amp; villagers &gt;= 7) promote();
 * </pre>
 * It now does a single-pass for-loop fold and delegates the final accept/reject to
 * {@link VillagerBecomeNobleGoal#shouldPromote(boolean, int)}.
 *
 * <p>Because instantiating real {@code Villager} / {@code VillagerNobleEntity}
 * requires a {@code Level}, the parity is asserted on isomorphic enum lists:
 * one path mimics the legacy three-stream chain, the other mimics the new
 * single-pass fold; both feed the same final predicate. They must agree on
 * every fixed scenario.</p>
 */
class VillagerBecomeNobleGoalParityTest {

    /** Stand-in for the real entity classes used in the production loop. */
    private enum Kind { NOBLE, VILLAGER, OTHER }

    /** Mirrors the legacy: stream + anyMatch + stream + filter/count. */
    private static boolean legacyDecide(List<Kind> nearby) {
        boolean noblePresent = nearby.stream().anyMatch(k -> k == Kind.NOBLE);
        int villagers = (int) nearby.stream().filter(k -> k == Kind.VILLAGER).count();
        return VillagerBecomeNobleGoal.shouldPromote(noblePresent, villagers);
    }

    /** Mirrors the new: single-pass for-loop + shouldPromote. */
    private static boolean refactoredDecide(List<Kind> nearby) {
        boolean noblePresent = false;
        int villagers = 0;
        for (int i = 0, n = nearby.size(); i < n; i++) {
            Kind k = nearby.get(i);
            if (k == Kind.NOBLE) {
                noblePresent = true;
                break;
            }
            if (k == Kind.VILLAGER) {
                villagers++;
            }
        }
        return VillagerBecomeNobleGoal.shouldPromote(noblePresent, villagers);
    }

    private static List<Kind> repeat(Kind kind, int count) {
        List<Kind> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) out.add(kind);
        return out;
    }

    private static List<Kind> concat(List<Kind> a, List<Kind> b) {
        List<Kind> out = new ArrayList<>(a.size() + b.size());
        out.addAll(a);
        out.addAll(b);
        return out;
    }

    @Test
    void emptyScanRejects() {
        List<Kind> scenario = List.of();
        assertFalse(refactoredDecide(scenario));
        assertEquals(legacyDecide(scenario), refactoredDecide(scenario));
    }

    @Test
    void belowThresholdRejects() {
        // 6 villagers < 7 → reject
        List<Kind> scenario = repeat(Kind.VILLAGER, 6);
        assertFalse(refactoredDecide(scenario));
        assertEquals(legacyDecide(scenario), refactoredDecide(scenario));
    }

    @Test
    void exactThresholdAccepts() {
        // exactly 7 villagers, no noble → accept
        List<Kind> scenario = repeat(Kind.VILLAGER, 7);
        assertTrue(refactoredDecide(scenario));
        assertEquals(legacyDecide(scenario), refactoredDecide(scenario));
    }

    @Test
    void aboveThresholdAccepts() {
        List<Kind> scenario = repeat(Kind.VILLAGER, 12);
        assertTrue(refactoredDecide(scenario));
        assertEquals(legacyDecide(scenario), refactoredDecide(scenario));
    }

    @Test
    void noblePresentAlwaysRejects() {
        // many villagers but a noble exists → reject (short-circuit)
        List<Kind> scenario = concat(repeat(Kind.VILLAGER, 20), List.of(Kind.NOBLE));
        assertFalse(refactoredDecide(scenario));
        assertEquals(legacyDecide(scenario), refactoredDecide(scenario));
    }

    @Test
    void noblePresentRejectsEvenAtThreshold() {
        List<Kind> scenario = concat(List.of(Kind.NOBLE), repeat(Kind.VILLAGER, 7));
        assertFalse(refactoredDecide(scenario));
        assertEquals(legacyDecide(scenario), refactoredDecide(scenario));
    }

    @Test
    void otherEntitiesIgnored() {
        // 7 villagers + lots of unrelated entities → still accept
        List<Kind> scenario = concat(repeat(Kind.OTHER, 30), repeat(Kind.VILLAGER, 7));
        assertTrue(refactoredDecide(scenario));
        assertEquals(legacyDecide(scenario), refactoredDecide(scenario));
    }

    @Test
    void onlyOthersRejects() {
        List<Kind> scenario = repeat(Kind.OTHER, 50);
        assertFalse(refactoredDecide(scenario));
        assertEquals(legacyDecide(scenario), refactoredDecide(scenario));
    }

    @Test
    void mixedScenariosAgreeAcrossThreshold() {
        // sweep villager count 0..15 with and without a noble; both
        // implementations must agree on every step.
        for (int v = 0; v <= 15; v++) {
            List<Kind> withoutNoble = repeat(Kind.VILLAGER, v);
            List<Kind> withNoble = concat(List.of(Kind.NOBLE), repeat(Kind.VILLAGER, v));

            assertEquals(legacyDecide(withoutNoble), refactoredDecide(withoutNoble),
                    "no-noble parity at v=" + v);
            assertEquals(legacyDecide(withNoble), refactoredDecide(withNoble),
                    "with-noble parity at v=" + v);
        }
    }

    @Test
    void shouldPromoteThresholdContract() {
        // direct contract on the extracted predicate
        assertFalse(VillagerBecomeNobleGoal.shouldPromote(false, 0));
        assertFalse(VillagerBecomeNobleGoal.shouldPromote(false,
                VillagerBecomeNobleGoal.MIN_VILLAGERS_FOR_PROMOTION - 1));
        assertTrue(VillagerBecomeNobleGoal.shouldPromote(false,
                VillagerBecomeNobleGoal.MIN_VILLAGERS_FOR_PROMOTION));
        assertTrue(VillagerBecomeNobleGoal.shouldPromote(false,
                VillagerBecomeNobleGoal.MIN_VILLAGERS_FOR_PROMOTION + 50));
        assertFalse(VillagerBecomeNobleGoal.shouldPromote(true,
                VillagerBecomeNobleGoal.MIN_VILLAGERS_FOR_PROMOTION + 50));
    }
}
