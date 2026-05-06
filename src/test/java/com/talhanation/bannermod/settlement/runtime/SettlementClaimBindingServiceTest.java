package com.talhanation.bannermod.settlement.runtime;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettlementClaimBindingServiceTest {

    @Test
    void resolvesRepairBindingOnlyWhenCanonicalTargetIsUnambiguous() {
        UUID currentBinding = UUID.randomUUID();
        UUID duplicateA = UUID.randomUUID();
        UUID duplicateB = UUID.randomUUID();
        UUID canonical = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        assertEquals(
                canonical,
                SettlementClaimBindingService.resolveRepairBinding(
                        currentBinding,
                        List.of(duplicateA, duplicateB),
                        Map.of(duplicateA, canonical, duplicateB, canonical)
                )
        );
        assertEquals(
                canonical,
                SettlementClaimBindingService.resolveRepairBinding(
                        duplicateA,
                        List.of(duplicateA, duplicateB),
                        Map.of(duplicateA, canonical, duplicateB, canonical)
                )
        );
        assertEquals(
                null,
                SettlementClaimBindingService.resolveRepairBinding(
                        currentBinding,
                        List.of(duplicateA, duplicateB),
                        Map.of(duplicateA, duplicateA, duplicateB, other)
                )
        );
    }
}
