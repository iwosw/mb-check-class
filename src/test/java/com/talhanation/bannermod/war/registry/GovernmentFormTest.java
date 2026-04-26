package com.talhanation.bannermod.war.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GovernmentFormTest {

    private static final UUID LEADER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CO_LEADER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OUTSIDER = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void monarchyDefaultsAndKeepsLeaderSoleAuthority() {
        PoliticalEntityRecord record = new PoliticalEntityRecord(
                UUID.randomUUID(), "Acadia", PoliticalEntityStatus.SETTLEMENT,
                LEADER, List.of(CO_LEADER), new BlockPos(0, 64, 0),
                "", "", "", "", 0L, GovernmentForm.MONARCHY
        );

        assertTrue(PoliticalEntityAuthority.canAct(LEADER, false, record));
        assertFalse(PoliticalEntityAuthority.canAct(CO_LEADER, false, record));
        assertFalse(PoliticalEntityAuthority.canAct(OUTSIDER, false, record));
        assertTrue(PoliticalEntityAuthority.canAct(OUTSIDER, true, record));
    }

    @Test
    void republicExtendsAuthorityToCoLeaders() {
        PoliticalEntityRecord record = new PoliticalEntityRecord(
                UUID.randomUUID(), "Acadia", PoliticalEntityStatus.SETTLEMENT,
                LEADER, List.of(CO_LEADER), new BlockPos(0, 64, 0),
                "", "", "", "", 0L, GovernmentForm.REPUBLIC
        );

        assertTrue(PoliticalEntityAuthority.canAct(LEADER, false, record));
        assertTrue(PoliticalEntityAuthority.canAct(CO_LEADER, false, record));
        assertFalse(PoliticalEntityAuthority.canAct(OUTSIDER, false, record));
    }

    @Test
    void backwardsCompatibleConstructorDefaultsToMonarchy() {
        PoliticalEntityRecord record = new PoliticalEntityRecord(
                UUID.randomUUID(), "Acadia", PoliticalEntityStatus.SETTLEMENT,
                LEADER, List.of(CO_LEADER), new BlockPos(0, 64, 0),
                "", "", "", "", 0L
        );
        assertEquals(GovernmentForm.MONARCHY, record.governmentForm());
    }

    @Test
    void tagRoundTripPreservesGovernmentForm() {
        PoliticalEntityRecord republic = new PoliticalEntityRecord(
                UUID.randomUUID(), "Brittany", PoliticalEntityStatus.STATE,
                LEADER, List.of(), new BlockPos(1, 64, 1),
                "", "", "", "", 99L, GovernmentForm.REPUBLIC
        );

        PoliticalEntityRecord restored = PoliticalEntityRecord.fromTag(republic.toTag());

        assertEquals(GovernmentForm.REPUBLIC, restored.governmentForm());
    }

    @Test
    void tagWithoutFormFieldFallsBackToMonarchy() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", UUID.randomUUID());
        tag.putString("Name", "Old Save");
        tag.putString("Status", "SETTLEMENT");
        tag.putUUID("Leader", LEADER);
        tag.putInt("CapitalX", 0);
        tag.putInt("CapitalY", 64);
        tag.putInt("CapitalZ", 0);
        tag.putString("Color", "");
        tag.putString("Charter", "");
        tag.putString("Ideology", "");
        tag.putString("HomeRegion", "");
        tag.putLong("CreatedAtGameTime", 0L);

        PoliticalEntityRecord restored = PoliticalEntityRecord.fromTag(tag);
        assertEquals(GovernmentForm.MONARCHY, restored.governmentForm());
    }

    @Test
    void runtimeUpdateGovernmentFormSwapsValueAndFiresDirty() {
        PoliticalRegistryRuntime runtime = new PoliticalRegistryRuntime();
        AtomicInteger dirty = new AtomicInteger();
        runtime.setDirtyListener(dirty::incrementAndGet);
        PoliticalEntityRecord created = runtime.create("Acadia", LEADER, new BlockPos(0, 64, 0),
                "", "", "", "", 0L).orElseThrow();
        int beforeDirty = dirty.get();

        assertTrue(runtime.updateGovernmentForm(created.id(), GovernmentForm.REPUBLIC));
        assertEquals(GovernmentForm.REPUBLIC, runtime.byId(created.id()).orElseThrow().governmentForm());
        assertEquals(beforeDirty + 1, dirty.get());

        // No-op idempotent update does not fire dirty again.
        assertTrue(runtime.updateGovernmentForm(created.id(), GovernmentForm.REPUBLIC));
        assertEquals(beforeDirty + 1, dirty.get());
    }
}
